package grcmcs.minecraft.mods.pomkotsmechs.cutscene;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class CutsceneService {
    public static final int START_TRANSITION_FADE_TICKS = 10;
    public static final int END_TRANSITION_FADE_TICKS = 6;
    public static final int START_TRANSITION_HOLD_TICKS = 14;
    public static final int END_TRANSITION_HOLD_TICKS = 2;
    public static final int SESSION_TRANSITION_FADE_TICKS = 5;
    public static final int SESSION_TRANSITION_HOLD_TICKS = 4;

    private CutsceneService() { }

    public static boolean play(ServerPlayer viewer, CutsceneDefinition definition, Vec3 target, Entity targetEntity) {
        if (!definition.type().equals("orbit") && !definition.type().equals("linear_sessions")
                && !definition.type().equals("aerial_approach")
                && !definition.type().equals("direct_approach")) {
            throw new IllegalArgumentException("Unsupported cutscene type: " + definition.type());
        }
        CutsceneCameraEntity camera = new CutsceneCameraEntity(PomkotsMechs.CUTSCENE_CAMERA.get(), viewer.serverLevel());
        camera.initialize(viewer, target, targetEntity, definition);
        if (!viewer.serverLevel().addFreshEntity(camera)) return false;
        if (definition.startFade()) {
            sendScreenFade(viewer, START_TRANSITION_FADE_TICKS, START_TRANSITION_HOLD_TICKS);
        }
        return true;
    }

    static void sendEndScreenFade(ServerPlayer viewer) {
        sendScreenFade(viewer, END_TRANSITION_FADE_TICKS, END_TRANSITION_HOLD_TICKS);
    }

    static void sendSessionScreenFade(ServerPlayer viewer) {
        sendScreenFade(viewer, SESSION_TRANSITION_FADE_TICKS, SESSION_TRANSITION_HOLD_TICKS);
    }

    private static void sendScreenFade(ServerPlayer viewer, int fadeTicks, int holdTicks) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeInt(fadeTicks);
        buf.writeInt(holdTicks);
        buf.writeInt(fadeTicks);
        NetworkManager.sendToPlayer(viewer, PomkotsMechs.id(PomkotsMechs.PACKET_GENERAR_SCREEN_FADE), buf);
    }
}
