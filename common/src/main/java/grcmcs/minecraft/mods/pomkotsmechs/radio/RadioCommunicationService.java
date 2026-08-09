package grcmcs.minecraft.mods.pomkotsmechs.radio;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public final class RadioCommunicationService {
    private RadioCommunicationService() {
    }

    public static boolean play(
            Collection<ServerPlayer> recipients,
            ResourceLocation messageId,
            RadioInterruptMode interruptMode
    ) {
        RadioMessage message = RadioMessageRegistry.get(messageId).orElse(null);
        if (message == null) {
            PomkotsMechs.LOGGER.warn("Unknown radio message: {}", messageId);
            return false;
        }

        for (ServerPlayer player : recipients) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeEnum(interruptMode);
            message.write(buf);
            NetworkManager.sendToPlayer(player, PomkotsMechs.id(PomkotsMechs.PACKET_RADIO_START), buf);
        }
        return true;
    }

    public static void stop(Collection<ServerPlayer> recipients) {
        for (ServerPlayer player : recipients) {
            NetworkManager.sendToPlayer(
                    player,
                    PomkotsMechs.id(PomkotsMechs.PACKET_RADIO_STOP),
                    new FriendlyByteBuf(Unpooled.buffer())
            );
        }
    }
}
