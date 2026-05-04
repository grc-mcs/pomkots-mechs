package grcmcs.minecraft.mods.pomkotsmechs.survival;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class SurvivalInitActions {
    // サーバ起動時（ワールド生成直後を含む）
    public static void onServerStarted(MinecraftServer server) {
        if (PomkotsMechs.CONFIG.survivalModeEnabled) {
            Scoreboard board = server.getScoreboard();
            Objective obj = board.getObjective(PomkotsMechs.SCOREBOARD_NAME_FOR_PROGRESS);
            if (obj == null) {
                PomkotsMechs.LOGGER.info("Creating Score Board Objective for Progress Management: " + PomkotsMechs.SCOREBOARD_NAME_FOR_PROGRESS);
                board.addObjective(
                        PomkotsMechs.SCOREBOARD_NAME_FOR_PROGRESS,
                        ObjectiveCriteria.DUMMY,
                        net.minecraft.network.chat.Component.literal("Main Progress"),
                        ObjectiveCriteria.RenderType.INTEGER
                );
            }
        }
    }

    // プレイヤー初Join時（サーバサイド）
    public static void onPlayerJoin(ServerPlayer player) {
        if (PomkotsMechs.CONFIG.survivalModeEnabled) {
            MinecraftServer server = player.getServer();
            if (server == null) return;

            Scoreboard board = server.getScoreboard();
            Objective obj = board.getObjective(PomkotsMechs.SCOREBOARD_NAME_FOR_PROGRESS);
            if (obj == null) {
                PomkotsMechs.LOGGER.info("No Score Board Objective for Progress Management:" + PomkotsMechs.SCOREBOARD_NAME_FOR_PROGRESS);
                return;
            }

            if (!board.hasPlayerScore(player.getScoreboardName(), obj)) {
                board.getOrCreatePlayerScore(player.getScoreboardName(), obj).setScore(0);
                PomkotsMechs.LOGGER.info("Initialized score for " + player.getName().getString());

            } else if (board.getOrCreatePlayerScore(player.getScoreboardName(), obj).getScore() == 0) {
                sendStartOpening(player);

            }
        }
    }

    public static void sendStartOpening(ServerPlayer player) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.EMPTY_BUFFER);
        NetworkManager.sendToPlayer(player, PomkotsMechs.id(PomkotsMechs.PACKET_START_OPENING), buf);
    }
//
//    // クライアント側 Join 完了後（視覚演出）
//    public static void onClientJoin(LocalPlayer player) {
//        if (PomkotsMechs.CONFIG.survivalModeEnabled) {
//            if (isFirstTime(player)) {
//                PomkotsMechs.LOGGER.info("Client world render started. Play intro.");
//                Minecraft.getInstance().setScreen(new IntroNarrationScreen());
//            }
//        }
//    }
//
//    private static boolean isFirstTime(LocalPlayer player) {
//        Minecraft mc = Minecraft.getInstance();
//        if (mc.level == null) {
//            return true;
//        }
//
//        Scoreboard board = mc.level.getScoreboard();
//        Objective obj = board.getObjective(PomkotsMechs.SCOREBOARD_NAME_FOR_PROGRESS);
//        if (obj == null) {
//            System.out.println("2");
//            return true;
//        }
//
//        System.out.println("3");
//        return !board.hasPlayerScore(player.getScoreboardName(), obj);
//    }
}
