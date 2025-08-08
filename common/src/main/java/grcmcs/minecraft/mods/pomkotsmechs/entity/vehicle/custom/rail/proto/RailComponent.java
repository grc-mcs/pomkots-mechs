package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.rail.proto;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class RailComponent {
    private boolean railBound = false;
    private LivingEntity ent;
    private BlockPos currentRailPos = null;
    private BlockPos previousRailPos = null;

    public RailComponent(LivingEntity ent) {
        this.ent = ent;
    }

    public void tick() {
        if (!ent.level().isClientSide) {
            Level level = ent.level();

            if (!railBound) {
                findInitialRail(level, ent.blockPosition()).ifPresent(pos -> {
                    railBound = true;
                    currentRailPos = pos;
                    previousRailPos = null;
                });
                return;
            }

            if (currentRailPos == null || !isRail(level, currentRailPos)) {
                unbindRail();
                return;
            }

            Vec3 velocity = ent.getDeltaMovement();
            double remainingDistance = velocity.length();
            Vec3 position = ent.position();
            Vec3 accumulatedMovement = Vec3.ZERO;

            while (remainingDistance > 0.001 && currentRailPos != null) {
                List<BlockPos> neighbors = getConnectedRails(level, currentRailPos);
                BlockPos bestNext = null;
                double bestDot = -Double.MAX_VALUE;

                Vec3 moveDir = velocity.normalize();

                for (BlockPos neighbor : neighbors) {
                    if (neighbor.equals(previousRailPos)) continue;
                    if (!isRail(level, neighbor)) continue;
                    Vec3 dir = Vec3.atCenterOf(neighbor).subtract(Vec3.atCenterOf(currentRailPos)).normalize();
                    double dot = moveDir.dot(dir);
                    if (dot > bestDot) {
                        bestDot = dot;
                        bestNext = neighbor;
                    }
                }

                if (bestNext == null) break;

                Vec3 nextCenter = Vec3.atCenterOf(bestNext);
                Vec3 currentCenter = Vec3.atCenterOf(currentRailPos);
                Vec3 segmentVec = nextCenter.subtract(currentCenter);
                double segmentLength = segmentVec.length();

                if (segmentLength <= remainingDistance) {
                    accumulatedMovement = accumulatedMovement.add(segmentVec);
                    remainingDistance -= segmentLength;
                    previousRailPos = currentRailPos;
                    currentRailPos = bestNext;
                } else {
                    Vec3 step = segmentVec.normalize().scale(remainingDistance);
                    accumulatedMovement = accumulatedMovement.add(step);
                    remainingDistance = 0;
                }
            }

            if (accumulatedMovement.lengthSqr() > 0) {
                ent.setDeltaMovement(accumulatedMovement);
//                ent.moveTo(position.add(accumulatedMovement));

                if (passedPoweredRail(level, Arrays.asList(currentRailPos, previousRailPos))) {
                    Vec3 boosted = ent.getDeltaMovement().scale(1.2);
                    ent.setDeltaMovement(boosted);
                }
            } else {
                unbindRail();
            }
        }
    }

    public void unbindRail() {
        railBound = false;
        currentRailPos = null;
        previousRailPos = null;
    }

    public boolean isRailBound() {
        return railBound;
    }

    public boolean isRail(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof BaseRailBlock;
    }

    public Optional<BlockPos> findInitialRail(Level level, BlockPos pos) {
        if (isRail(level, pos)) return Optional.of(pos);

        BlockPos below = pos.below();

        if (isRail(level, below)) return Optional.of(below);
        return Optional.empty();
    }

    public RailShape getRailShape(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BaseRailBlock) {
            return state.getValue(((BaseRailBlock) state.getBlock()).getShapeProperty());
        }
        return RailShape.NORTH_SOUTH;
    }

    public List<BlockPos> getConnectedRails(Level level, BlockPos pos) {
        RailShape shape = getRailShape(level, pos);
        List<BlockPos> neighbors = new ArrayList<>();

        switch (shape) {
            case NORTH_SOUTH:
                neighbors.add(pos.north());
                neighbors.add(pos.south());
                break;
            case EAST_WEST:
                neighbors.add(pos.east());
                neighbors.add(pos.west());
                break;
            case ASCENDING_EAST:
                neighbors.add(pos.east().above());
                neighbors.add(pos.west());
                break;
            case ASCENDING_WEST:
                neighbors.add(pos.west().above());
                neighbors.add(pos.east());
                break;
            case ASCENDING_NORTH:
                neighbors.add(pos.north().above());
                neighbors.add(pos.south());
                break;
            case ASCENDING_SOUTH:
                neighbors.add(pos.south().above());
                neighbors.add(pos.north());
                break;
            case NORTH_EAST:
                neighbors.add(pos.north());
                neighbors.add(pos.east());
                break;
            case NORTH_WEST:
                neighbors.add(pos.north());
                neighbors.add(pos.west());
                break;
            case SOUTH_EAST:
                neighbors.add(pos.south());
                neighbors.add(pos.east());
                break;
            case SOUTH_WEST:
                neighbors.add(pos.south());
                neighbors.add(pos.west());
                break;
        }

        return neighbors;
    }

    public boolean passedPoweredRail(Level level, List<BlockPos> path) {
        for (BlockPos pos : path) {
            if (pos == null) continue;
            if (level.getBlockState(pos).is(Blocks.POWERED_RAIL) &&
                    level.getBlockState(pos).getValue(PoweredRailBlock.POWERED)) {
                return true;
            }
        }
        return false;
    }
}
