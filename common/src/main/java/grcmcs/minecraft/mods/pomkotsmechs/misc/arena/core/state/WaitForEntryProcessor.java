package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.arena.ArenaGateBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.ElevatorEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchProcessor;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util.ArenaBossBarUtil;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util.ArenaUtil;
import grcmcs.minecraft.mods.pomkotsmechs.util.TitleUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class WaitForEntryProcessor
        implements IArenaMatchStateProcessor {

    private static final long TIMEOUT =
            180_000L;

    @Override
    public void onEnter(
            ArenaMatchContext context
    ) {
        if (!ArenaUtil.initPilotEntities(context)) {
            return;
        }

        var challengerEntity = context.getChallengerEntity();
        var opponentEntity = context.getOpponentEntity();

        if (challengerEntity == null || opponentEntity == null) {
            return;
        }

        if (!setupElevator(context.getLevel(), context.match().getGatePosA(), challengerEntity)) {
            return;
        }

        if (!setupElevator(context.getLevel(), context.match().getGatePosB(), opponentEntity)) {
            return;
        }

        if (challengerEntity instanceof ServerPlayer player) {
            TitleUtil.showArenaMessage(
                    player,
                    "Move To Gate",
                    ""
            );

        }

        if (opponentEntity instanceof ServerPlayer player) {
            TitleUtil.showArenaMessage(
                    player,
                    "New Challenger",
                    "Check Arena by data pad"
            );

            for (var dataPad: PomkotsDatapadItem.getPlayersDataPads(player)) {
                PomkotsDatapadItem.addArenaRequest(dataPad, challengerEntity.getUUID(), context.match().getMatchId(), context.arena().getArenaId());
            }
        }

        ArenaUtil.playSounds(PomkotsMechs.SE_COIN.get(), context);

        ArenaBossBarUtil.createCountdownBar(
                context,
                "Time Left",
                TIMEOUT
        );
    }

    private boolean setupElevator(ServerLevel level, BlockPos pos, Entity target) {
        if (level.getBlockEntity(pos) instanceof ArenaGateBlockEntity gateEntity) {
            var elevator = gateEntity.refreshElevator();

            if (elevator == null) {
                return false;
            }

            elevator.setMode(ElevatorEntity.Mode.WAITING);
            elevator.setPassengerUUID(target.getUUID());

            return true;
        }
        return false;
    }

    @Override
    public void tick(
            ArenaMatchContext context
    ) {
        var challenger = context.getChallengerEntity();
        var opponent = context.getOpponentEntity();

        if (challenger == null || opponent == null) {
            ArenaMatchProcessor.cancelMatch(
                    context,
                    "Player disconnected."
            );

            return;
        }

        long elapsed = context.elapsedMillis();

        ArenaBossBarUtil.updateCountdownBar(
                context,
                "Time Left",
                elapsed,
                TIMEOUT
        );

        if (elapsed >= TIMEOUT) {
            ArenaMatchProcessor.cancelMatch(
                    context,
                    "Entry timeout."
            );

            return;
        }

        if (isMechEntried(context.getLevel(), context.match().getGatePosA()) && challenger.getVehicle() instanceof Pmvc01Entity mech) {

            if (context.runtime().runOnce("WFE_01")) {
                ArenaUtil.supplyMechConsumables(mech);
                context.match().setChallengerMechId(mech.getUUID());
                ArenaManager.update(context.server(), context.arena().getArenaId());
            }
        } else {
            return;
        }

        if (isMechEntried(context.getLevel(), context.match().getGatePosB()) && opponent.getVehicle() instanceof Pmvc01Entity mech2) {
            if (context.runtime().runOnce("WFE_02")) {
                ArenaUtil.supplyMechConsumables(mech2);
                context.match().setOpponentMechId(mech2.getUUID());

                if (context.getOpponentEntity() instanceof ServerPlayer player) {
                    for (var dataPad: PomkotsDatapadItem.getPlayersDataPads(player)) {
                        PomkotsDatapadItem.removeArenaRequest(dataPad, context.match().getMatchId());
                    }
                }

                ArenaManager.update(context.server(), context.arena().getArenaId());
            }
        } else {
            return;
        }

        ArenaMatchProcessor.changeState(
                context,
                ArenaMatchState.TRANSPORT_TO_ARENA
        );
    }

    private boolean isMechEntried(Level level, BlockPos gatePos) {
        if (level.getBlockEntity(gatePos) instanceof ArenaGateBlockEntity gateEntity) {
            var elev = gateEntity.getElevator();
            if (elev == null) {
                return false;
            }

            return !elev.getPassengers().isEmpty();
        }

        return false;
    }

    @Override
    public void onExit(
            ArenaMatchContext context
    ) {
        ArenaBossBarUtil.removeBar(
                context
        );
    }

    @Override
    public void onCancel(
            ArenaMatchContext context
    ) {
        ArenaBossBarUtil.removeBar(
                context
        );
    }

}
