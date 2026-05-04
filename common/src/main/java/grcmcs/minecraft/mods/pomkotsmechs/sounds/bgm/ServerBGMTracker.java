package grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.mojang.text2speech.Narrator.LOGGER;

public class ServerBGMTracker {

    private static final Map<UUID, BGMState> LAST = new HashMap<>();
    private static final Map<UUID, BGMState> FORCE = new HashMap<>();
    public static LostCitiesCompatible lostCitiesAccessor = new LostCitiesCompatible();

    public static void tick(ServerPlayer player) {
        if (!PomkotsMechs.CONFIG.survivalModeEnabled) {
            return;
        }

        BGMState oldState = LAST.get(player.getUUID());
        BGMState newState = computeState(player, oldState);

        if (newState != oldState) {
            LAST.put(player.getUUID(), newState);
            sendDataPack2Player(player, newState);
        }
    }

    public static void forceSetBGMState(ServerPlayer player, BGMState state) {
        FORCE.put(player.getUUID(), state);
    }

    public static void forceUnsetBGMState(ServerPlayer player) {
        FORCE.remove(player.getUUID());
    }

    public static void sendDataPack2Player(ServerPlayer player, BGMState newState) {
        try {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeEnum(newState);
            NetworkManager.sendToPlayer(player, PomkotsMechs.id(PomkotsMechs.PACKET_BGM_STATE), buf);
        } catch (Exception e) {
            LOGGER.error("Failed to send bgm event to client", e);
        }
    }

    private static BGMState computeState(ServerPlayer player, BGMState oldState) {
        var forceSound = FORCE.get(player.getUUID());
        if (forceSound != null) {
            return forceSound;
        }

        var lostCitiesSound = lostCitiesAccessor.getBGMState(player, oldState);
        if (lostCitiesSound != null) {
            return lostCitiesSound;
        }

        return BGMState.NONE;
    }

    private static boolean isBossNearby(Player player) {
        return !player.level().getEntitiesOfClass(BaseBossEntity.class,
                player.getBoundingBox().inflate(50)).isEmpty();
    }
}
