package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaCameraEntity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchProcessor;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util.ArenaUtil;
import grcmcs.minecraft.mods.pomkotsmechs.util.TitleUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.io.ByteArrayOutputStream;

public class OpeningProcessor extends AbstractArenaMatchStateProcessor {

    private static final long TIMEOUT = 10_000L;

    @Override
    public void onEnter(ArenaMatchContext context) {
        ArenaUtil.setupCameras(context);
    }

    @Override
    public void tick(ArenaMatchContext context) {
        long elapsed = context.elapsedMillis();

        var challengerEntity = context.getChallengerEntity();
        var opponentEntity = context.getOpponentEntity();

        if (challengerEntity == null || opponentEntity == null) {
            ArenaMatchProcessor.cancelMatch(
                    context,
                    "Player disconnected."
            );
        }

        if (elapsed >= 1 && context.runtime().runOnce("intro_01")) {
            ArenaUtil.playSounds(PomkotsMechs.SE_CHEERS.get(), context);
            showFighterProfile(context, challengerEntity, opponentEntity);
        }

        if (elapsed >= 5000 && context.runtime().runOnce("intro_02")) {
            showMechProfile(context, challengerEntity, opponentEntity);
        }

        if (elapsed + 2000 >= TIMEOUT
            && context.runtime().runOnce("intro_03")
        ) {
            ArenaUtil.startScreenFade(context, 20, 20, 15);

            TitleUtil.showArenaMessage(
                    challengerEntity,
                    "Get Ready?",
                    "",
                    20,
                    20,
                    20
            );

            TitleUtil.showArenaMessage(
                    opponentEntity,
                    "Get Ready?",
                    "",
                    20,
                    20,
                    20
            );
        }

        if (elapsed >= TIMEOUT) {
            ArenaUtil.playSounds(PomkotsMechs.SE_ARENA_START.get(), context);
            TitleUtil.showArenaMessage(
                    challengerEntity,
                    "Fight!",
                    "",
                    5,
                    10,
                    5
            );

            TitleUtil.showArenaMessage(
                    opponentEntity,
                    "Fight!",
                    "",
                    5,
                    10,
                    5
            );

            ArenaMatchProcessor.changeState(
                    context,
                    ArenaMatchState.BATTLE
            );
        }
    }

    private void showFighterProfile(ArenaMatchContext context, Entity challengerEntity, Entity opponentEntity) {
        var fighterCh = context.arena().getFighter(context.match().getChallengerId());
        var fighterOp = context.arena().getFighter(context.match().getOpponentId());

        var ranking = ArenaManager.getRanking(context.server(), context.arena().getArenaId());

        int rc = 99;
        int ro = 99;

        int i = 1;
        for (var f: ranking) {
            if (f.getFighterId().equals(fighterCh.getFighterId())) {
                rc = i;
            }
            if (f.getFighterId().equals(fighterOp.getFighterId())) {
                ro = i;
            }
            i++;
        }

        TitleUtil.showArenaMessage(
                challengerEntity,
                "Rank " + ro,
                fighterOp.getDisplayName(),
                20,
                100,
                20
        );

        TitleUtil.showArenaMessage(
                opponentEntity,
                "Rank " + rc,
                fighterCh.getDisplayName(),
                20,
                100,
                20
        );
    }

    private void showMechProfile(ArenaMatchContext context, Entity challengerEntity, Entity opponentEntity) {
        var fighterCh = context.arena().getFighter(context.match().getChallengerId());
        var fighterOp = context.arena().getFighter(context.match().getOpponentId());

        TitleUtil.showArenaMessage(
                challengerEntity,
                fighterOp.getMechData().getMechName(),
                "",
                20,
                80,
                20
        );

        TitleUtil.showArenaMessage(
                opponentEntity,
                fighterCh.getMechData().getMechName(),
                "",
                20,
                80,
                20
        );
    }

    @Override
    public void onExit(ArenaMatchContext context) {
        ArenaUtil.restoreCameras(context);
    }

    @Override
    public void onCancel(ArenaMatchContext context) {
        ArenaUtil.restoreCameras(context);
    }
}
