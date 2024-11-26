package grcmcs.minecraft.mods.pomkotsmechs.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.Pmb01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01Entity;
import io.netty.buffer.Unpooled;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class UserInteractionManager {
    public static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    public enum Keys {
        // キーバインドに登録したくないやつは仮にDELETEキーにマップしとく（もっとかっこいい方法考える）
        FORWARD            ((short)0b0000000000000001, GLFW.GLFW_KEY_DELETE, "none"),
        BACK               ((short)0b0000000000000010, GLFW.GLFW_KEY_DELETE, "none"),
        LEFT               ((short)0b0000000000000100, GLFW.GLFW_KEY_DELETE, "none"),
        RIGHT              ((short)0b0000000000001000, GLFW.GLFW_KEY_DELETE, "none"),
        EVASION            ((short)0b0000000000010000, GLFW.GLFW_KEY_DELETE, "none"),
        JUMP               ((short)0b0000000000100000, InputConstants.KEY_B, "jump"),
        WEAPON_ARM_R       ((short)0b0000000001000000, GLFW.GLFW_KEY_DELETE, "none"),
        WEAPON_ARM_L       ((short)0b0000000010000000, GLFW.GLFW_KEY_DELETE, "none"),
        WEAPON_SHOULDER_R  ((short)0b0000000100000000, InputConstants.KEY_P, "weapon_shld_r"),
        WEAPON_SHOULDER_L  ((short)0b0000001000000000, InputConstants.KEY_O, "weapon_shld_l"),
        LOCK               ((short)0b0000010000000000, InputConstants.KEY_U, "lock"),
        MODE               ((short)0b0000100000000000, InputConstants.KEY_Y, "mode");

        private final short keyID;
        private final int id;
        private final String confName;

        Keys(final short keyID, final int id, final String confName) {
            this.keyID = keyID;
            this.id = id;
            this.confName = confName;
        }

        public short getKeyID() {
            return this.keyID;
        }
        public int getId() {
            return this.id;
        }
        public String getConfName() {
            return this.confName;
        }
    }

    private final TargetLocker targetLocker = TargetLocker.getInstance();
    private short prevDriverInput = 0;

    public UserInteractionManager() {
    }

    public void registerClient() {
        // 左クリックでの攻撃抑制
        PlayerEvent.ATTACK_ENTITY.register((player, world, hand, entity, hitResult) -> {
            if (isRidingPomkotsMechs(player)) {
                return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        Map<Keys, KeyMapping> kbMap = new HashMap<>();

        for (Keys key: Keys.values()) {
            if (key.getId() != GLFW.GLFW_KEY_DELETE) {
                KeyMapping kb = new KeyMapping(
                    key.getConfName(),
                    InputConstants.Type.KEYSYM,
                    key.getId(),
                    "Pomkots-Bots");
                kbMap.put(key, kb);
                KeyMappingRegistry.register(kb);
            }
        }

        // クライアントのTickの最終段に、キー押下を検出してクラサバ両方にメッセージを送る処理を追加
        ClientTickEvent.CLIENT_POST.register(client -> {
            short keyPressStatus = 0;
            if (client.player != null) {
                if (isRidingPomkotsMechs(client.player)) {
                    for (Map.Entry<Keys, KeyMapping> entry : kbMap.entrySet()) {
                        if (entry.getKey() == Keys.LOCK || entry.getKey() == Keys.MODE) {
                            if (entry.getValue().consumeClick()) {
                                keyPressStatus |= entry.getKey().keyID;
                            }
                        } else {
                            if (entry.getValue().isDown()) {
                                keyPressStatus |= entry.getKey().keyID;
                            }
                        }
                    }

                    if (client.player.input.up) {
                        keyPressStatus |= Keys.FORWARD.getKeyID();
                    }
                    if (client.player.input.down) {
                        keyPressStatus |= Keys.BACK.getKeyID();
                    }
                    if (client.player.input.right) {
                        keyPressStatus |= Keys.RIGHT.getKeyID();
                    }
                    if (client.player.input.left) {
                        keyPressStatus |= Keys.LEFT.getKeyID();
                    }
                    if (client.options.keyAttack.isDown()) {
                        keyPressStatus |= Keys.WEAPON_ARM_R.getKeyID();
                    }
                    if (client.options.keyUse.isDown()) {
                        keyPressStatus |= Keys.WEAPON_ARM_L.getKeyID();
                    }
                    if (client.options.keyJump.isDown()) {
                        keyPressStatus |= Keys.JUMP.getKeyID();
                    }
                    if (client.options.keySprint.isDown()) {
                        keyPressStatus |= Keys.EVASION.getKeyID();
                    }

                    if (this.prevDriverInput != keyPressStatus) {
                        sendDriverInput2Server(keyPressStatus);
                        this.prevDriverInput = keyPressStatus;
                    }

                    if (client.player.getVehicle() instanceof Pmv01Entity bot) {
                        var driverInput = new DriverInput(keyPressStatus);
                        targetLocker.tick(driverInput, bot);
                    }
                } else {
                    targetLocker.clearLockTargets();
                }
            }
        });
    }

    protected boolean isRidingPomkotsMechs(Player player) {
        return player.getVehicle() instanceof Pmv01Entity || player.getVehicle() instanceof Pmb01Entity;
    }

    private void sendDriverInput2Server(short keyPressStatus) {

        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeShort(keyPressStatus);
        NetworkManager.sendToServer(PomkotsMechs.id(PomkotsMechs.PACKET_DRIVER_INPUT), buf);

    }
}
