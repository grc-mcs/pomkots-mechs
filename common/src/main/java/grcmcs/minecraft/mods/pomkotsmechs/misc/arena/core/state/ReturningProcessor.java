package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import grcmcs.minecraft.mods.pomkotsmechs.block.arena.ArenaGateBlock;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchProcessor;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaFighterData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class ReturningProcessor extends AbstractArenaMatchStateProcessor {

    private static final long TIMEOUT = 1_000L;

    @Override
    public void onEnter(ArenaMatchContext context) {

        var challengerInfo = context.arena().getFighter(context.match().getChallengerId());
        var opponentInfo = context.arena().getFighter(context.match().getOpponentId());

        returnFighter(challengerInfo, context.getChallengerEntity(), context.getChallengerMechEntity(), context.match().getGatePosA(), context.getLevel());
        returnFighter(opponentInfo, context.getOpponentEntity(), context.getOpponentMechEntity(), context.match().getGatePosB(), context.getLevel());

    }

    private void returnFighter(ArenaFighterData info, Entity pilotEntity, Entity mechEntity, BlockPos pos, ServerLevel level) {
        if (info.getType() == ArenaFighterData.FighterType.NPC) {
            // NPCの場合は消去

            if (pilotEntity instanceof MechPilotEntity) {
                pilotEntity.discard();
            }
            if (mechEntity instanceof Pmvc01Entity) {
                mechEntity.discard();
            }

        } else {
            // 帰還時の向き
            var yaw = getYaw(level, pos);

            //　プレイヤーの場合はゲートまでテレポ
            pos = pos.above(2);

            if (pilotEntity instanceof Player player) {
                player.removeEffect(
                        MobEffects.DAMAGE_RESISTANCE
                );
            }

            if (mechEntity instanceof Pmvc01Entity mech && mechEntity.isAlive()) {
                if (mech.getDrivingPassenger() != null && mech.getDrivingPassenger().equals(pilotEntity)) {
                    setYaw(mech, yaw);
                    setYaw(pilotEntity, yaw);

                    mech.moveTo(pos.getX(), pos.getY(), pos.getZ(), yaw, 0F);
                    return;
                } else {
                    mech.teleportTo(pos.getX(), pos.getY(), pos.getZ());
                    setYaw(mech, yaw);
                }
            }

            if (pilotEntity != null && pilotEntity.isAlive()) {
                pilotEntity.teleportTo(pos.getX(), pos.getY(), pos.getZ());
                setYaw(pilotEntity, yaw);
            }
        }
    }

    private void setYaw(Entity ent, float yaw) {
        ent.setYRot(yaw);
        ent.setYBodyRot(yaw);
        ent.setYHeadRot(yaw);
    }

    @Override
    public void tick(ArenaMatchContext context) {
        long elapsed = context.elapsedMillis();

        if (elapsed >= TIMEOUT) {
            ArenaMatchProcessor.changeState(
                    context,
                    ArenaMatchState.FINISHED
            );
        }
    }

    private float getYaw(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        Direction facing = state.getValue(ArenaGateBlock.FACING);
        Direction exitDirection = facing.getOpposite();

        return directionToYaw(exitDirection);
    }

    public float directionToYaw(
            Direction direction
    ) {
        return switch (direction) {
            case SOUTH -> 0F;
            case WEST  -> 90F;
            case NORTH -> 180F;
            case EAST  -> -90F;
            default    -> 0F;
        };
    }

    @Override
    public void onExit(ArenaMatchContext context) {

    }

    @Override
    public void onCancel(ArenaMatchContext context) {

    }
}
