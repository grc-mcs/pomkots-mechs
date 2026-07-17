package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util;

import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;

public final class ArenaBossBarUtil {

    private ArenaBossBarUtil() {}

    public static void createCountdownBar(
            ArenaMatchContext context,
            String title,
            long durationMs
    ) {

        removeBar(context);

        ServerBossEvent bossBar =
                new ServerBossEvent(
                        Component.literal(title),
                        BossEvent.BossBarColor.BLUE,
                        BossEvent.BossBarOverlay.PROGRESS
                );

        bossBar.setProgress(1F);

        var challenger = context.getChallengerEntity();
        if (challenger instanceof ServerPlayer player) {
            bossBar.addPlayer(player);
        }

        var opponent = context.getOpponentEntity();
        if (opponent instanceof ServerPlayer player) {
            bossBar.addPlayer(player);
        }

        context.runtime()
                .setBossBar(
                        bossBar
                );
    }

    public static void updateCountdownBar(
            ArenaMatchContext context,
            String title,
            long elapsedMs,
            long durationMs
    ) {

        ServerBossEvent bossBar =
                context.runtime()
                        .getBossBar();

        if (bossBar == null) {
            return;
        }

        long remainMs =
                Math.max(
                        0,
                        durationMs - elapsedMs
                );

        long remainSec =
                remainMs / 1000;

        bossBar.setName(
                Component.literal(
                        title + " : "
                                + remainSec
                                + "s"
                )
        );

        float progress =
                Math.max(
                        0F,
                        Math.min(
                                1F,
                                1F - (
                                        elapsedMs
                                                / (float) durationMs
                                )
                        )
                );

        bossBar.setProgress(
                progress
        );
    }

    public static void removeBar(
            ArenaMatchContext context
    ) {

        ServerBossEvent bossBar =
                context.runtime()
                        .getBossBar();

        if (bossBar == null) {
            return;
        }

        bossBar.removeAllPlayers();

        context.runtime()
                .setBossBar(
                        null
                );
    }
}
