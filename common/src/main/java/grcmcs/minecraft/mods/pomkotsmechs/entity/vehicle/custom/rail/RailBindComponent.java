package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.rail;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.List;

public class RailBindComponent {
    private static final double RAIL_FRICTION = 0.02;
    private static final double POWERED_RAIL_ACCELERATION = 4;
    private static final double POWERED_RAIL_DECELERATION = 0.05;
    private static final double MAX_SPEED = 20;
    private static final double MIN_SPEED = 0.01;
    private static final double NATURAL_DECAY = 0.97;
    private static final double SLOPE_ACCELERATION = 0.01;
    private static final double SLOPE_DECELERATION = 0.001;
    private static final double MAX_RAILS_PER_TICK = 40;

    // 坂道進入時の速度補正用
    private static final double SLOPE_ENTRY_THRESHOLD = 0.1;
    private static final double SLOPE_MIN_SPEED = 0.05;

    public void moveRobotOnRail(Pmvc01Entity targetEntity, Vec3 currentVelocity) {
        BlockPos entityBlockPos = targetEntity.blockPosition();
        Vec3 entityPos = targetEntity.position();
        Level level = targetEntity.level();
        BlockPos railBlockPos = findRailBlockBelow(level, entityBlockPos);

        if (railBlockPos == null) {
            targetEntity.unbindFromRail();
            return;
        }

        Vec3 horizontalVelocity = new Vec3(currentVelocity.x, currentVelocity.y, currentVelocity.z);

        if (horizontalVelocity.length() < MIN_SPEED) {
            targetEntity.setDeltaMovement(Vec3.ZERO);
            return;
        }

        RailPathMoveResult result = processRailPathMovement(level, railBlockPos, entityPos, horizontalVelocity);

        targetEntity.setPos(result.finalPosition.x, result.finalPosition.y, result.finalPosition.z);
        targetEntity.setDeltaMovement(result.finalVelocity);
    }

    private RailPathMoveResult processRailPathMovement(Level level, BlockPos startRailPos, Vec3 startPos, Vec3 initialVelocity) {
        List<RailPathSegment> railPath = buildRailPath(level, startRailPos, initialVelocity);

        Vec3 currentPos = startPos;
        Vec3 currentVelocity = initialVelocity;
        double remainingDistance = initialVelocity.length();

        for (RailPathSegment segment : railPath) {
            if (remainingDistance <= 0.001) {
                break;
            }

            RailSegmentResult segmentResult = processRailSegment(level, segment, currentPos, currentVelocity, remainingDistance);

            currentPos = segmentResult.exitPosition;
            currentVelocity = segmentResult.exitVelocity;
            remainingDistance = segmentResult.remainingDistance;

            if (currentVelocity.length() < MIN_SPEED) {
                currentVelocity = Vec3.ZERO;
                break;
            }
        }

        return new RailPathMoveResult(currentPos, currentVelocity);
    }

    private List<RailPathSegment> buildRailPath(Level level, BlockPos startRailPos, Vec3 initialVelocity) {
        List<RailPathSegment> railPath = new ArrayList<>();
        BlockPos currentRailPos = startRailPos;
        Vec3 currentDirection = initialVelocity.normalize();

        int maxRailsToCheck = Math.min((int) Math.ceil(initialVelocity.length() * 2), (int) MAX_RAILS_PER_TICK);

        for (int i = 0; i < maxRailsToCheck; i++) {
            if (currentRailPos == null || !isValidRailBlock(level, currentRailPos)) {
                break;
            }

            BlockState railState = level.getBlockState(currentRailPos);
            RailShape railShape = getRailShape(railState);

            RailPathSegment segment = new RailPathSegment(currentRailPos, railState, railShape);
            railPath.add(segment);

            Vec3 exitDirection = getRailExitDirection(railShape, currentDirection);
            BlockPos nextRailPos = findNextRailBlock(level, currentRailPos, railShape, exitDirection);

            currentRailPos = nextRailPos;
            currentDirection = exitDirection;
        }

        return railPath;
    }

    // 修正版：坂道の方向判定を改善
    private RailSegmentResult processRailSegment(Level level, RailPathSegment segment, Vec3 entryPos, Vec3 entryVelocity, double remainingDistance) {
        BlockPos railPos = segment.position;
        BlockState railState = segment.state;
        RailShape railShape = segment.shape;

        double minX = railPos.getX();
        double maxX = railPos.getX() + 1.0;
        double minZ = railPos.getZ();
        double maxZ = railPos.getZ() + 1.0;

        Vec3 adjustedEntryPos = adjustPositionToRailBounds(entryPos, minX, maxX, minZ, maxZ);
        Vec3 currentVelocity = entryVelocity;

        // 1. 自然減衰
        currentVelocity = currentVelocity.scale(NATURAL_DECAY);

        // 2. レールタイプ効果
        currentVelocity = applyRailTypeEffects(railState, currentVelocity);

        // 3. 坂道の場合、進入時の速度方向を基準に方向を決定
        Vec3 railDirection;
        if (isSlopeRail(railShape)) {
            railDirection = getSlopeRailDirectionFixed(railShape, currentVelocity, adjustedEntryPos, railPos);
        } else {
            railDirection = getRailDirection(railShape, currentVelocity);
        }

        // 4. 速度の大きさを保持してレール方向に制約
        double speed = currentVelocity.length();
        Vec3 constrainedVelocity = railDirection.scale(speed);

        // 5. 坂道効果を適用（方向が確定した後）
        constrainedVelocity = applySlopeEffectsFixed(railShape, constrainedVelocity, railDirection);

        // 6. 速度制限
        if (constrainedVelocity.length() > MAX_SPEED) {
            constrainedVelocity = constrainedVelocity.normalize().scale(MAX_SPEED);
        }

        // 7. 坂道での最小速度チェック（改善版）
        if (isSlopeRail(railShape)) {
            constrainedVelocity = handleSlopeMinimumSpeed(railShape, constrainedVelocity, railDirection);
        } else if (constrainedVelocity.length() < MIN_SPEED) {
            constrainedVelocity = Vec3.ZERO;
        }

        // 8. 移動距離計算
        double segmentDistance = calculateSegmentDistance(adjustedEntryPos, constrainedVelocity, minX, maxX, minZ, maxZ);
        double actualMoveDistance = Math.min(segmentDistance, remainingDistance);

        // 9. 出口位置計算
        Vec3 exitPosition = adjustedEntryPos.add(constrainedVelocity.normalize().scale(actualMoveDistance));

        // 10. 高さ調整
        exitPosition = adjustHeightForRail(exitPosition, railShape, railPos);

        // 11. 境界内制限
        exitPosition = adjustPositionToRailBounds(exitPosition, minX, maxX, minZ, maxZ);

        return new RailSegmentResult(exitPosition, constrainedVelocity, remainingDistance - actualMoveDistance);
    }

    // 修正版：坂道方向の判定を改善
    private Vec3 getSlopeRailDirectionFixed(RailShape railShape, Vec3 currentVelocity, Vec3 currentPos, BlockPos railPos) {
        // 現在位置がレールブロックのどの位置にあるかで判定
        double posX = currentPos.x - railPos.getX(); // 0.0 ~ 1.0
        double posZ = currentPos.z - railPos.getZ(); // 0.0 ~ 1.0

        // 水平方向の速度成分のみで判定
        Vec3 horizontalVelocity = new Vec3(currentVelocity.x, 0, currentVelocity.z);

        switch (railShape) {
            case ASCENDING_EAST:
                // X軸方向の移動：東が上り、西が下り
                if (horizontalVelocity.x > SLOPE_ENTRY_THRESHOLD ||
                        (Math.abs(horizontalVelocity.x) < SLOPE_ENTRY_THRESHOLD && posX < 0.5)) {
                    // 東向き（上り）
                    return new Vec3(1, 1, 0).normalize();
                } else {
                    // 西向き（下り）
                    return new Vec3(-1, -1, 0).normalize();
                }
            case ASCENDING_WEST:
                if (horizontalVelocity.x < -SLOPE_ENTRY_THRESHOLD ||
                        (Math.abs(horizontalVelocity.x) < SLOPE_ENTRY_THRESHOLD && posX > 0.5)) {
                    // 西向き（上り）
                    return new Vec3(-1, 1, 0).normalize();
                } else {
                    // 東向き（下り）
                    return new Vec3(1, -1, 0).normalize();
                }
            case ASCENDING_NORTH:
                if (horizontalVelocity.z < -SLOPE_ENTRY_THRESHOLD ||
                        (Math.abs(horizontalVelocity.z) < SLOPE_ENTRY_THRESHOLD && posZ > 0.5)) {
                    // 北向き（上り）
                    return new Vec3(0, 1, -1).normalize();
                } else {
                    // 南向き（下り）
                    return new Vec3(0, -1, 1).normalize();
                }
            case ASCENDING_SOUTH:
                if (horizontalVelocity.z > SLOPE_ENTRY_THRESHOLD ||
                        (Math.abs(horizontalVelocity.z) < SLOPE_ENTRY_THRESHOLD && posZ < 0.5)) {
                    // 南向き（上り）
                    return new Vec3(0, 1, 1).normalize();
                } else {
                    // 北向き（下り）
                    return new Vec3(0, -1, -1).normalize();
                }
            default:
                return new Vec3(1, 0, 0);
        }
    }

    // 修正版：坂道効果の適用
    private Vec3 applySlopeEffectsFixed(RailShape railShape, Vec3 velocity, Vec3 railDirection) {
        if (!isSlopeRail(railShape)) {
            return velocity;
        }

        // レール方向のY成分で上り下りを判定
        boolean isGoingUp = railDirection.y > 0;

        if (isGoingUp) {
            // 上り：重力で減速するが、完全に止まらないように
            double deceleration = SLOPE_DECELERATION + 0.01;
            double newSpeed = velocity.length() * (1.0 - deceleration);
            // 最小速度を保証
            newSpeed = Math.max(newSpeed, SLOPE_MIN_SPEED);
            return railDirection.scale(newSpeed);
        } else {
            // 下り：重力で加速
            double acceleration = SLOPE_ACCELERATION + 0.01;
            double newSpeed = velocity.length() * (1.0 + acceleration);
            return railDirection.scale(newSpeed);
        }
    }

    // 坂道での最小速度処理
    private Vec3 handleSlopeMinimumSpeed(RailShape railShape, Vec3 velocity, Vec3 railDirection) {
        if (velocity.length() < SLOPE_MIN_SPEED) {
            if (railDirection.y < 0) {
                // 下り坂：重力で最小速度を保持
                return railDirection.scale(SLOPE_MIN_SPEED * 2);
            } else {
                // 上り坂：完全に停止するか、わずかに下がる
                Vec3 downDirection = getOppositeDirection(railShape);
                return downDirection.scale(SLOPE_MIN_SPEED * 0.5);
            }
        }
        return velocity;
    }

    // 坂道の逆方向を取得
    private Vec3 getOppositeDirection(RailShape railShape) {
        switch (railShape) {
            case ASCENDING_EAST:
                return new Vec3(-1, -1, 0).normalize();
            case ASCENDING_WEST:
                return new Vec3(1, -1, 0).normalize();
            case ASCENDING_NORTH:
                return new Vec3(0, -1, 1).normalize();
            case ASCENDING_SOUTH:
                return new Vec3(0, -1, -1).normalize();
            default:
                return new Vec3(-1, 0, 0);
        }
    }

    private boolean isSlopeRail(RailShape railShape) {
        return railShape == RailShape.ASCENDING_EAST ||
                railShape == RailShape.ASCENDING_WEST ||
                railShape == RailShape.ASCENDING_NORTH ||
                railShape == RailShape.ASCENDING_SOUTH;
    }

    // 残りのメソッドは元のコードと同じ
    private double calculateSegmentDistance(Vec3 startPos, Vec3 velocity, double minX, double maxX, double minZ, double maxZ) {
        if (velocity.length() < 0.001) {
            return 0;
        }

        Vec3 direction = velocity.normalize();
        double maxDistance = velocity.length();

        double distanceX = Double.MAX_VALUE;
        double distanceZ = Double.MAX_VALUE;

        if (Math.abs(direction.x) > 0.001) {
            double boundaryX = direction.x > 0 ? maxX : minX;
            distanceX = Math.abs((boundaryX - startPos.x) / direction.x);
        }

        if (Math.abs(direction.z) > 0.001) {
            double boundaryZ = direction.z > 0 ? maxZ : minZ;
            distanceZ = Math.abs((boundaryZ - startPos.z) / direction.z);
        }

        double minDistance = Math.min(distanceX, distanceZ);
        if (minDistance <= 0 || minDistance == Double.MAX_VALUE) {
            return Math.min(1.0, maxDistance);
        }

        return Math.min(minDistance, maxDistance);
    }

    private Vec3 adjustHeightForRail(Vec3 position, RailShape railShape, BlockPos railPos) {
        double railY = railPos.getY() + 0.0625;

        switch (railShape) {
            case ASCENDING_EAST:
                double progressX = position.x - railPos.getX();
                progressX = Math.max(0.0, Math.min(1.0, progressX));
                return new Vec3(position.x, railY + progressX, position.z);
            case ASCENDING_WEST:
                progressX = 1.0 - (position.x - railPos.getX());
                progressX = Math.max(0.0, Math.min(1.0, progressX));
                return new Vec3(position.x, railY + progressX, position.z);
            case ASCENDING_NORTH:
                double progressZ = 1.0 - (position.z - railPos.getZ());
                progressZ = Math.max(0.0, Math.min(1.0, progressZ));
                return new Vec3(position.x, railY + progressZ, position.z);
            case ASCENDING_SOUTH:
                progressZ = position.z - railPos.getZ();
                progressZ = Math.max(0.0, Math.min(1.0, progressZ));
                return new Vec3(position.x, railY + progressZ, position.z);
            default:
                return new Vec3(position.x, railY, position.z);
        }
    }

    private Vec3 adjustPositionToRailBounds(Vec3 pos, double minX, double maxX, double minZ, double maxZ) {
        double clampedX = Math.max(minX, Math.min(maxX, pos.x));
        double clampedZ = Math.max(minZ, Math.min(maxZ, pos.z));
        return new Vec3(clampedX, pos.y, clampedZ);
    }

    private Vec3 applyRailTypeEffects(BlockState railState, Vec3 velocity) {
        if (railState.getBlock() instanceof PoweredRailBlock) {
            boolean powered = railState.getValue(PoweredRailBlock.POWERED);
            if (powered) {
                Vec3 direction = velocity.length() > 0 ? velocity.normalize() : new Vec3(1, 0, 0);
                return velocity.add(direction.scale(POWERED_RAIL_ACCELERATION));
            } else {
                return velocity.scale(1.0 - POWERED_RAIL_DECELERATION);
            }
        } else {
            return velocity.scale(1.0 - RAIL_FRICTION);
        }
    }

    private Vec3 getRailDirection(RailShape railShape, Vec3 currentVelocity) {
        switch (railShape) {
            case NORTH_SOUTH:
                return currentVelocity.z >= 0 ? new Vec3(0, 0, 1) : new Vec3(0, 0, -1);
            case EAST_WEST:
                return currentVelocity.x >= 0 ? new Vec3(1, 0, 0) : new Vec3(-1, 0, 0);
            case ASCENDING_EAST:
                return new Vec3(1, 0, 0);
            case ASCENDING_WEST:
                return new Vec3(-1, 0, 0);
            case ASCENDING_NORTH:
                return new Vec3(0, 0, -1);
            case ASCENDING_SOUTH:
                return new Vec3(0, 0, 1);
            case SOUTH_EAST:
                return chooseCurveDirection(currentVelocity, new Vec3(1, 0, 0), new Vec3(0, 0, 1));
            case SOUTH_WEST:
                return chooseCurveDirection(currentVelocity, new Vec3(-1, 0, 0), new Vec3(0, 0, 1));
            case NORTH_WEST:
                return chooseCurveDirection(currentVelocity, new Vec3(-1, 0, 0), new Vec3(0, 0, -1));
            case NORTH_EAST:
                return chooseCurveDirection(currentVelocity, new Vec3(1, 0, 0), new Vec3(0, 0, -1));
            default:
                return currentVelocity.length() > 0 ? currentVelocity.normalize() : new Vec3(1, 0, 0);
        }
    }

    private Vec3 getRailExitDirection(RailShape railShape, Vec3 entryDirection) {
        switch (railShape) {
            case NORTH_SOUTH:
                return entryDirection.z >= 0 ? new Vec3(0, 0, 1) : new Vec3(0, 0, -1);
            case EAST_WEST:
                return entryDirection.x >= 0 ? new Vec3(1, 0, 0) : new Vec3(-1, 0, 0);
            case ASCENDING_EAST:
                return new Vec3(1, 0, 0);
            case ASCENDING_WEST:
                return new Vec3(-1, 0, 0);
            case ASCENDING_NORTH:
                return new Vec3(0, 0, -1);
            case ASCENDING_SOUTH:
                return new Vec3(0, 0, 1);
            case SOUTH_EAST:
                return chooseCurveDirection(entryDirection, new Vec3(1, 0, 0), new Vec3(0, 0, 1));
            case SOUTH_WEST:
                return chooseCurveDirection(entryDirection, new Vec3(-1, 0, 0), new Vec3(0, 0, 1));
            case NORTH_WEST:
                return chooseCurveDirection(entryDirection, new Vec3(-1, 0, 0), new Vec3(0, 0, -1));
            case NORTH_EAST:
                return chooseCurveDirection(entryDirection, new Vec3(1, 0, 0), new Vec3(0, 0, -1));
            default:
                return entryDirection;
        }
    }

    private Vec3 chooseCurveDirection(Vec3 velocity, Vec3 dir1, Vec3 dir2) {
        if (velocity.length() < MIN_SPEED) {
            return dir1;
        }

        double speed1 = Math.abs(velocity.dot(dir1));
        double speed2 = Math.abs(velocity.dot(dir2));

        if (speed1 > speed2) {
            return dir2.scale(velocity.length());
        } else {
            return dir1.scale(velocity.length());
        }
    }

    private BlockPos findNextRailBlock(Level level, BlockPos currentRail, RailShape railShape, Vec3 direction) {
        List<BlockPos> possiblePositions = getPossibleNextRailPositions(currentRail, railShape, direction);

        return possiblePositions.stream()
                .filter(pos -> isValidRailBlock(level, pos))
                .min((pos1, pos2) -> {
                    Vec3 dir1 = new Vec3(pos1.getX() - currentRail.getX(),
                            pos1.getY() - currentRail.getY(),
                            pos1.getZ() - currentRail.getZ());
                    Vec3 dir2 = new Vec3(pos2.getX() - currentRail.getX(),
                            pos2.getY() - currentRail.getY(),
                            pos2.getZ() - currentRail.getZ());

                    double dot1 = direction.dot(dir1);
                    double dot2 = direction.dot(dir2);

                    return Double.compare(dot2, dot1);
                })
                .orElse(null);
    }

    private List<BlockPos> getPossibleNextRailPositions(BlockPos currentRail, RailShape railShape, Vec3 direction) {
        List<BlockPos> positions = new ArrayList<>();

        switch (railShape) {
            case NORTH_SOUTH:
                positions.add(currentRail.north());
                positions.add(currentRail.south());
                break;
            case EAST_WEST:
                positions.add(currentRail.east());
                positions.add(currentRail.west());
                break;
            case ASCENDING_EAST:
                positions.add(currentRail.east().above());
                positions.add(currentRail.west().below());
                break;
            case ASCENDING_WEST:
                positions.add(currentRail.west().above());
                positions.add(currentRail.east().below());
                break;
            case ASCENDING_NORTH:
                positions.add(currentRail.north().above());
                positions.add(currentRail.south().below());
                break;
            case ASCENDING_SOUTH:
                positions.add(currentRail.south().above());
                positions.add(currentRail.north().below());
                break;
            case SOUTH_EAST:
                positions.add(currentRail.south());
                positions.add(currentRail.east());
                break;
            case SOUTH_WEST:
                positions.add(currentRail.south());
                positions.add(currentRail.west());
                break;
            case NORTH_WEST:
                positions.add(currentRail.north());
                positions.add(currentRail.west());
                break;
            case NORTH_EAST:
                positions.add(currentRail.north());
                positions.add(currentRail.east());
                break;
        }

        return positions;
    }

    public BlockPos findRailBlockBelow(Level level, BlockPos pos) {
        for (int y = pos.getY() + 1; y >= pos.getY() - 2; y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (isValidRailBlock(level, checkPos)) {
                return checkPos;
            }
        }
        return null;
    }

    private boolean isValidRailBlock(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof BaseRailBlock;
    }

    private RailShape getRailShape(BlockState railState) {
        if (railState.getBlock() instanceof BaseRailBlock) {
            return railState.getValue(((BaseRailBlock) railState.getBlock()).getShapeProperty());
        }
        return RailShape.NORTH_SOUTH;
    }

    // データクラス
    private static class RailPathSegment {
        final BlockPos position;
        final BlockState state;
        final RailShape shape;

        RailPathSegment(BlockPos position, BlockState state, RailShape shape) {
            this.position = position;
            this.state = state;
            this.shape = shape;
        }
    }

    private static class RailPathMoveResult {
        final Vec3 finalPosition;
        final Vec3 finalVelocity;

        RailPathMoveResult(Vec3 finalPosition, Vec3 finalVelocity) {
            this.finalPosition = finalPosition;
            this.finalVelocity = finalVelocity;
        }
    }

    private static class RailSegmentResult {
        final Vec3 exitPosition;
        final Vec3 exitVelocity;
        final double remainingDistance;

        RailSegmentResult(Vec3 exitPosition, Vec3 exitVelocity, double remainingDistance) {
            this.exitPosition = exitPosition;
            this.exitVelocity = exitVelocity;
            this.remainingDistance = remainingDistance;
        }
    }
}

