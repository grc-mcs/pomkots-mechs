package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SimpleBossWalkGoal extends BaseBossGoal {
    private final double speed;
    private final double desiredDistance;
    private LivingEntity target;

    private enum Mode { APPROACH, CIRCLE }
    private Mode mode = Mode.APPROACH;
    private int switchTimer = 0;
    private int circleDirection = 1; // 1 or -1

    private int ticksUntilNextSwitch = 0;

    public SimpleBossWalkGoal(BaseBossEntity mob, double speed, double desiredDistance) {
        super(mob);
        this.speed = speed;
        this.desiredDistance = desiredDistance;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUseInternal() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.ticksUntilNextSwitch > 0;
    }

    @Override
    public void start() {
        switchTimer = 20;
        circleDirection = mob.getRandom().nextBoolean() ? 1 : -1;
        ticksUntilNextSwitch = mob.getRandom().nextInt(20) + 20;
        mode = Mode.APPROACH;
        mob.setNoGravity(false);
        target = mob.getTarget();
    }


    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void tick() {
        this.ticksUntilNextSwitch--;

        if (target == null || !target.isAlive()) return;

        mob.rotateToTarget(target);

        // 向きの更新
        Vec3 toTarget = target.position().subtract(mob.position());

        double distance = mob.distanceTo(target);

        switchTimer--;
        if (switchTimer <= 0) {
            // モード切り替え
            if (mode == Mode.APPROACH && distance <= desiredDistance) {
                mode = Mode.CIRCLE;
                circleDirection *= -1;

                switchTimer = 20 + mob.getRandom().nextInt(10);
            } else if (mode == Mode.CIRCLE && distance > desiredDistance + 2) {
                mode = Mode.APPROACH;

                switchTimer = 40 + mob.getRandom().nextInt(10);
            } else {
                switchTimer = 20 + mob.getRandom().nextInt(10);
                circleDirection *= -1;
            }

        }

        Vec3 moveVec = switch (mode) {
            case APPROACH -> {
                if (distance <= desiredDistance) {
                    mode = Mode.CIRCLE;
                    circleDirection *= -1;

                    switchTimer = 20 + mob.getRandom().nextInt(10);
                }
                Vec3 dir = toTarget.normalize().scale(speed);
                yield calculateSafeMovement(dir);
            }
            case CIRCLE -> {
                double angle = Math.atan2(toTarget.z, toTarget.x) + (circleDirection * Math.PI / 2);
                Vec3 dir = new Vec3(Math.cos(angle), 0, Math.sin(angle)).normalize().scale(speed);
                yield calculateSafeMovement(dir);
            }
        };

        mob.setDeltaMovement(moveVec);
        mob.setSpeed((float) speed);
    }

    private float rotLerp(float current, float target, float maxChange) {
        float delta = Mth.wrapDegrees(target - current);
        if (delta > maxChange) delta = maxChange;
        if (delta < -maxChange) delta = -maxChange;
        return current + delta;
    }

    private Vec3 trySafeMove(Vec3 dir) {
        // 1ブロック先の位置
        BlockPos next = new BlockPos((int)(mob.getX() + dir.x), (int)(mob.getY() - 1), (int)(mob.getZ() + dir.z));
        BlockState below = mob.level().getBlockState(next);

        if (below.getFluidState().isSource()) {
            // 穴や水 → 移動しない
            return Vec3.ZERO;
        }
//
//        // 上にブロックが詰まってるときも防止
//        BlockState head = mob.level().getBlockState(next.above());
//        if (!head.getMaterial().isReplaceable()) {
//            return Vec3.ZERO;
//        }

        return dir;
    }

    private Vec3 calculateSafeMovement(Vec3 horizontalDir) {
        // 現在位置
        Vec3 currentPos = mob.position();

        // 移動先の水平位置
        Vec3 targetHorizontalPos = currentPos.add(horizontalDir.x, horizontalDir.y, horizontalDir.z);

        // 地面の高さを見つける
        double groundY = findGroundLevel(targetHorizontalPos.x, targetHorizontalPos.y, targetHorizontalPos.z);

        // 移動が可能かチェック
//        if (!canMoveToPosition(targetHorizontalPos.x, groundY, targetHorizontalPos.z)) {
//            return new Vec3(0, applyGravity(), 0); // 移動不可の場合は重力のみ適用
//        }

        // Y軸の移動を計算
        double yMovement = calculateYMovement(currentPos.y, groundY);

        return new Vec3(horizontalDir.x, yMovement, horizontalDir.z);
    }

    private double findGroundLevel(double x, double startY, double z) {
        Level level = mob.level();

        int searchRange = 10; // 上下10ブロックの範囲で探索

        // 下方向に探索
        for (int y = (int)startY; y >= startY - searchRange; y--) {
            BlockPos pos = new BlockPos((int)x, y, (int)z);
            BlockState state = level.getBlockState(pos);

            if (!state.isAir() && state.isSolidRender(level, pos)) {
                // 固体ブロックが見つかった場合、その上の面を地面とする
                return y + 1.0;
            }
        }

        // 上方向にも探索（念のため）
//        for (int y = startY + 1; y <= startY + searchRange; y++) {
//            BlockPos pos = new BlockPos((int)x, y - 1, (int)z);
//            BlockState state = level.getBlockState(pos);
//
//            if (!state.isAir() && state.isSolidRender(level, pos)) {
//                return y;
//            }
//        }

        // 地面が見つからない場合は現在のY座標を返す
        return mob.getY();
    }

    private boolean canMoveToPosition(double x, double y, double z) {
        Level level = mob.level();

        // エンティティのバウンディングボックスを考慮
        AABB entityBB = mob.getBoundingBox();
        double width = entityBB.getXsize();
        double height = entityBB.getYsize();

        // 移動先でのバウンディングボックスを計算
        AABB targetBB = new AABB(
                x - width/2, y, z - width/2,
                x + width/2, y + height, z + width/2
        );

        // 衝突チェック
        return level.noCollision(mob, targetBB);
    }

    private double calculateYMovement(double currentY, double groundY) {
        double yDiff = groundY - currentY;

        // StepHeight相当の処理（ボスの大きさに応じて調整）
        double maxStepHeight = 10;//mob.maxUpStep(); // 通常は0.6だが、ボスなら大きくすることも可能

        if (yDiff > 0 && yDiff <= maxStepHeight) {
            // 小さな段差は一気に上る
            return yDiff;
        } else if (yDiff < -0.1) {
            // 下向きの移動（落下）- 重力を適用
            return applyGravity();
        } {
            return 0;
        }
//        else if (yDiff > maxStepHeight) {
//            // 大きな段差は少しずつ上る
//            return Math.min(0.3, yDiff);
//        } else if (yDiff < -0.1) {
//            // 下向きの移動（落下）- 重力を適用
//            return applyGravity();
//        } else {
//            // 地面レベル付近なら微調整
//            return yDiff * 0.1;
//        }
    }

    private double applyGravity() {
        // 重力の適用（バニラと同様の処理）
        double currentVerticalVelocity = mob.getDeltaMovement().y;
        double gravity = 0.08; // バニラの重力値

        if (!mob.onGround()) {
            return Math.max(currentVerticalVelocity - gravity, -2.0); // 最大落下速度制限
        }

        return 0.0;
    }
}
