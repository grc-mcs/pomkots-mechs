package grcmcs.minecraft.mods.pomkotsmechs.mission.support;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public final class MissionPresentationService {
    private MissionPresentationService() { }

    public static void show(Collection<ServerPlayer> players, String style, Component title,
                            Component message, int durationTicks) {
        for (ServerPlayer player : players) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeUtf(style);
            buf.writeComponent(title);
            buf.writeComponent(message);
            buf.writeVarInt(Math.max(1, durationTicks));
            NetworkManager.sendToPlayer(player, PomkotsMechs.id(PomkotsMechs.PACKET_MISSION_PRESENTATION), buf);
        }
    }

    public static void objective(ServerPlayer player, Component mission, Component stage,
                                 Component objective, Component progress, int remainingTicks) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(true);
        buf.writeComponent(mission);
        buf.writeComponent(stage);
        buf.writeComponent(objective);
        buf.writeComponent(progress);
        buf.writeVarInt(remainingTicks + 1);
        NetworkManager.sendToPlayer(player, PomkotsMechs.id(PomkotsMechs.PACKET_MISSION_OBJECTIVE), buf);
    }

    public static void clearObjective(ServerPlayer player) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeBoolean(false);
        NetworkManager.sendToPlayer(player, PomkotsMechs.id(PomkotsMechs.PACKET_MISSION_OBJECTIVE), buf);
    }
}
