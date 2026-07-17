package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.arena.ArenaGateBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.ElevatorEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchProcessor;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchRuntime;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util.ArenaBossBarUtil;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util.ArenaUtil;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.BGMState;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.ServerBGMTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class BattleProcessor extends AbstractArenaMatchStateProcessor {

    private static final long TIMEOUT = 180_000L;

    @Override
    public void onEnter(ArenaMatchContext context) {
        var challengerMechEntity = context.getChallengerMechEntity();
        var opponentMechEntity = context.getOpponentMechEntity();

        if (!(challengerMechEntity instanceof Pmvc01Entity mc) || !(opponentMechEntity instanceof Pmvc01Entity mo)) {
            return;
        }

        if (context.getChallengerEntity() instanceof ServerPlayer player1) {
            player1.addEffect(
                    new MobEffectInstance(
                            MobEffects.DAMAGE_RESISTANCE,
                            20 * 200,
                            9,
                            false,
                            false,
                            false
                    )
            );
            ServerBGMTracker.forceSetBGMState(player1, BGMState.BATTLE_BOSS);
        }

        if (context.getChallengerEntity() instanceof ServerPlayer player2) {
            player2.addEffect(
                    new MobEffectInstance(
                            MobEffects.DAMAGE_RESISTANCE,
                            20 * 200,
                            9,
                            false,
                            false,
                            false
                    )
            );
            ServerBGMTracker.forceSetBGMState(player2, BGMState.BATTLE_BOSS);
        }

        mc.startArena((LivingEntity) opponentMechEntity);
        mo.startArena((LivingEntity) challengerMechEntity);

        unlockElevator(context.getLevel(), context.match().getGatePosA());
        unlockElevator(context.getLevel(), context.match().getGatePosB());

        ArenaBossBarUtil.createCountdownBar(
                context,
                "Time Left",
                TIMEOUT
        );
    }

    private boolean unlockElevator(Level level, BlockPos gatePos) {
        if (level.getBlockEntity(gatePos) instanceof ArenaGateBlockEntity gateEntity) {
            var elev = gateEntity.getElevator();
            if (elev == null) {
                return false;
            }

            elev.setMode(ElevatorEntity.Mode.IDLE);

            return true;
        }

        return false;
    }

    @Override
    public void tick(ArenaMatchContext context) {

        long elapsed = context.elapsedMillis();

        ArenaBossBarUtil.updateCountdownBar(
                context,
                "Time Left",
                elapsed,
                TIMEOUT
        );

        var result = checkResult(context, elapsed);

        if (result != null) {
            context.runtime().setResult(result);

            ArenaUtil.playSounds(PomkotsMechs.SE_ARENA_END.get(), context);
            ArenaUtil.startScreenFade(context, 10, 10 , 10);
            ArenaMatchProcessor.changeState(
                    context,
                    ArenaMatchState.VICTORY_CEREMONY
            );
        }
    }

    public ArenaMatchRuntime.BattleResult checkResult(ArenaMatchContext context, long elapsed) {
        Entity challengerPilot = context.getChallengerEntity();
        Entity challengerMech = context.getChallengerMechEntity();
        Entity opponentPilot = context.getOpponentEntity();
        Entity opponentMech = context.getOpponentMechEntity();


        boolean challengerDead =
                isDefeated(
                        challengerPilot,
                        challengerMech
                );

        boolean opponentDead =
                isDefeated(
                        opponentPilot,
                        opponentMech
                );

        ArenaMatchRuntime.BattleResult result = null;

        if (challengerDead && opponentDead) {
            result = new ArenaMatchRuntime.BattleResult();

            result.setResultType(
                    ArenaMatchRuntime.BattleResult.ResultType.DRAW
            );
            result.setReason(
                    "Double KO"
            );
        } else if (challengerDead) {
            result = new ArenaMatchRuntime.BattleResult();

            result.setResultType(
                    ArenaMatchRuntime.BattleResult.ResultType.OPPONENT_WIN
            );

            result.setWinnerId(
                    context.match()
                            .getOpponentId()
            );
            result.setLoserId(
                    context.match()
                            .getChallengerId()
            );
            result.setReason(
                    "Defeat"
            );

        } else if (opponentDead) {
            result = new ArenaMatchRuntime.BattleResult();

            result.setResultType(
                    ArenaMatchRuntime.BattleResult.ResultType.CHALLENGER_WIN
            );

            result.setWinnerId(
                    context.match()
                            .getChallengerId()
            );
            result.setLoserId(
                    context.match()
                            .getOpponentId()
            );
            result.setReason(
                    "Defeat"
            );

        } else if (elapsed >= TIMEOUT) {
            result = new ArenaMatchRuntime.BattleResult();

            result.setResultType(
                    ArenaMatchRuntime.BattleResult.ResultType.DRAW
            );

            result.setReason(
                    "Time Over"
            );
        }

        return result;
    }

    public static boolean isDefeated(
            Entity pilot,
            Entity mech
    ) {

        if (pilot == null || !pilot.isAlive()) {
            return true;
        }

        if (!(mech instanceof Pmvc01Entity mechEntity)) {
            return true;
        }

        return mechEntity.isBroken();
    }

    @Override
    public void onExit(ArenaMatchContext context) {
        ArenaBossBarUtil.removeBar(
                context
        );

        if (context.getChallengerEntity() instanceof ServerPlayer player1) {
            ServerBGMTracker.forceUnsetBGMState(player1);
        }
        if (context.getOpponentMechEntity() instanceof ServerPlayer player2) {
            ServerBGMTracker.forceUnsetBGMState(player2);
        }
    }

    @Override
    public void onCancel(ArenaMatchContext context) {
        ArenaBossBarUtil.removeBar(
                context
        );

        if (context.getChallengerEntity() instanceof ServerPlayer player1) {
            ServerBGMTracker.forceUnsetBGMState(player1);
        }
        if (context.getOpponentMechEntity() instanceof ServerPlayer player2) {
            ServerBGMTracker.forceUnsetBGMState(player2);
        }
    }
}
