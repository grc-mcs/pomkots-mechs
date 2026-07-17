package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchProcessor;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchRuntime;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util.ArenaUtil;
import grcmcs.minecraft.mods.pomkotsmechs.util.TitleUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class VictoryCeremonyProcessor extends AbstractArenaMatchStateProcessor {
    private static final long TIMEOUT = 6_000L;

    @Override
    public void onEnter(ArenaMatchContext context) {
        if (context.runtime().getResult().getResultType() == ArenaMatchRuntime.BattleResult.ResultType.CHALLENGER_WIN) {
            ArenaUtil.setupCamera(context, context.getOpponentEntity(), context.getChallengerEntity());
        } else if (context.runtime().getResult().getResultType() == ArenaMatchRuntime.BattleResult.ResultType.OPPONENT_WIN){
            ArenaUtil.setupCamera(context, context.getChallengerEntity(), context.getOpponentEntity());
        }
    }

    @Override
    public void tick(ArenaMatchContext context) {
        long elapsed = context.elapsedMillis();

        var challengerEntity = context.getChallengerEntity();
        var opponentEntity = context.getOpponentEntity();

        if (elapsed >= 1000 && context.runtime().runOnce("outro_01")) {
            ArenaUtil.playSounds(PomkotsMechs.SE_CHEERS.get(), context);

            String msg;
            if (isDraw(context)) {
                msg = "Draw";
            } else {
                msg = "Winner";
            }

            TitleUtil.showArenaMessage(
                    challengerEntity,
                    msg,
                    "",
                    20,
                    40,
                    20
            );

            TitleUtil.showArenaMessage(
                    opponentEntity,
                    msg,
                    "",
                    20,
                    40,
                    20
            );
        }

        if (elapsed + 1000 > TIMEOUT && context.runtime().runOnce("outro_02")) {
            ArenaUtil.startScreenFade(context, 20, 30, 20);
        }

        if (elapsed >= TIMEOUT) {
            ArenaMatchProcessor.changeState(
                    context,
                    ArenaMatchState.RETURNING
            );
        }
    }

    private boolean isDraw(ArenaMatchContext context) {
        if (context.runtime().getResult() == null) {
            return true;
        }
        return context.runtime().getResult().getResultType() == ArenaMatchRuntime.BattleResult.ResultType.DRAW;
    }

    @Override
    public void onExit(ArenaMatchContext context) {
        if (context.runtime().getResult().getResultType() == ArenaMatchRuntime.BattleResult.ResultType.CHALLENGER_WIN) {
            ArenaUtil.restoreCamera(context, context.getOpponentEntity());
        } else if (context.runtime().getResult().getResultType() == ArenaMatchRuntime.BattleResult.ResultType.OPPONENT_WIN){
            ArenaUtil.restoreCamera(context, context.getChallengerEntity());
        }
    }

    @Override
    public void onCancel(ArenaMatchContext context) {
        if (context.runtime().getResult().getResultType() == ArenaMatchRuntime.BattleResult.ResultType.CHALLENGER_WIN) {
            ArenaUtil.restoreCamera(context, context.getOpponentEntity());
        } else if (context.runtime().getResult().getResultType() == ArenaMatchRuntime.BattleResult.ResultType.OPPONENT_WIN){
            ArenaUtil.restoreCamera(context, context.getChallengerEntity());
        }
    }
}