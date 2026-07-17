package grcmcs.minecraft.mods.pomkotsmechs.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class TitleUtil {

    private TitleUtil() {}

    public static void showArenaMessage(
            Entity entity,
            String title,
            String subtitle
    ) {
        showArenaMessage(
                entity,
                title,
                subtitle,
                10,
                60,
                20

        );
    }

    public static void showArenaMessage(
            Entity entity,
            String title,
            String subtitle,
            int fadeIn,
            int stay,
            int fadeOut
    ) {
        if (entity instanceof ServerPlayer player) {
            showTitle(
                    player,
                    Component.literal(title),
                    Component.literal(subtitle),
                    fadeIn,
                    stay,
                    fadeOut
            );
        }
    }

    public static void showTitle(
            ServerPlayer player,
            Component title,
            Component subtitle,
            int fadeIn,
            int stay,
            int fadeOut
    ) {

        player.connection.send(
                new ClientboundSetTitlesAnimationPacket(
                        fadeIn,
                        stay,
                        fadeOut
                )
        );

        if (title != null) {
            player.connection.send(
                    new ClientboundSetTitleTextPacket(
                            title
                    )
            );
        }

        if (subtitle != null) {
            player.connection.send(
                    new ClientboundSetSubtitleTextPacket(
                            subtitle
                    )
            );
        }
    }

    public static void clear(
            ServerPlayer player
    ) {
        player.connection.send(
                new ClientboundClearTitlesPacket(
                        true
                )
        );
    }
}
