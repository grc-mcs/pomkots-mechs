package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;


import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class HelicopterHoverMoveGoal extends Goal {
    private final BaseBossEntity mob;
    private final double baseSpeed;
    private final double minHoverHeight;    // 最低浮遊高度
    private final double maxHoverHeight;    // 最高浮遊高度
    private final int patternSwitchInterval; // パターン切替間隔

    private LivingEntity target;
    private int tickCounter = 0;
    private int patternSwitchTimer = 0;
    private double currentHoverHeight;      // 現在の目標浮遊高度

    // 移動パターン
    private enum MovementPattern {
        APPROACH,          // 近寄る
        RAPID_APPROACH,    // 急速に近寄る
        CIRCLE,            // 円運動
        STRAFE,            // 左右ダッシュ
        RETREAT,           // 遠ざかる
        HOVER              // その場で浮遊
    }

    private MovementPattern currentPattern = MovementPattern.HOVER;
    private int circleDirection = 1;  // 円運動の方向（1 or -1）
    private int strafeDirection = 1;  // 左右移動の方向（1 or -1）
    private int strafeTimer = 0;      // 左右移動の継続時間

    public HelicopterHoverMoveGoal(BaseBossEntity mob,
                                   double baseSpeed,
                                   double minHoverHeight,
                                   double maxHoverHeight,
                                   int patternSwitchInterval) {
        this.mob = mob;
        this.baseSpeed = baseSpeed;
        this.minHoverHeight = minHoverHeight;
        this.maxHoverHeight = maxHoverHeight;
        this.patternSwitchInterval = patternSwitchInterval;

        this.setFlags(EnumSet.of(Flag.JUMP));

        // 初期浮遊高度をランダムに設定
        randomizeHoverHeight();
    }

    @Override
    public boolean canUse() {
        target = mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        tickCounter = 0;
        patternSwitchTimer = patternSwitchInterval;
        mob.setNoGravity(true);
        randomizePattern();
    }

    @Override
    public void stop() {
        target = null;
        currentPattern = MovementPattern.HOVER;

        // 停止時は浮遊を維持
        Vec3 vel = mob.getDeltaMovement();
        mob.setDeltaMovement(0, vel.y * 0.5, 0);
    }

    @Override
    public void tick() {
        tickCounter++;

        if (target == null) return;

        if (mob.isStunning()) {
            mob.setDeltaMovement(Vec3.ZERO);
            return;
        }

        if (currentPattern == MovementPattern.APPROACH || currentPattern == MovementPattern.RAPID_APPROACH) {
            Vec3 currentPos = mob.position();
            Vec3 toTarget = target.position().subtract(currentPos);

            Vec3 horizontal = new Vec3(toTarget.x, 0, toTarget.z);
            double distance = horizontal.length();

            if (distance < 20) {
                patternSwitchTimer = 0;
            }
        }

        // ターゲットを見る
        mob.rotateToTarget(target);

        // パターン切り替え
        patternSwitchTimer--;
        if (patternSwitchTimer <= 0) {
            randomizePattern();
            patternSwitchTimer = patternSwitchInterval + mob.getRandom().nextInt(40) - 20;
        }

        // 浮遊高度をランダムに変更（10%の確率）
        if (mob.getRandom().nextFloat() < 0.1F) {
            randomizeHoverHeight();
        }

        // 移動処理
        performMovement();
    }

    /**
     * 移動処理
     */
    private void performMovement() {
        Vec3 currentPos = mob.position();
        Vec3 toTarget = target.position().subtract(currentPos);

        // 水平方向の移動ベクトルを計算
        Vec3 horizontalMove = calculateHorizontalMovement(toTarget);

        // 垂直方向の移動を計算（浮遊高度維持）
        double verticalMove = calculateVerticalMovement(currentPos);

        // 最終的な移動ベクトルを設定
        mob.setDeltaMovement(horizontalMove.x, verticalMove, horizontalMove.z);
    }

    /**
     * 水平方向の移動を計算
     */
    private Vec3 calculateHorizontalMovement(Vec3 toTarget) {
        Vec3 horizontal = new Vec3(toTarget.x, 0, toTarget.z);
        double distance = horizontal.length();

        return switch (currentPattern) {
            case APPROACH -> {
                // 通常速度で接近
                if (distance < 0.1) yield Vec3.ZERO;
                yield horizontal.normalize().scale(baseSpeed);
            }

            case RAPID_APPROACH -> {
                // 高速で接近
                if (distance < 0.1) yield Vec3.ZERO;
                yield horizontal.normalize().scale(baseSpeed * 2.0);
            }

            case CIRCLE -> {
                // 円運動
                double angleToTarget = Math.atan2(horizontal.z, horizontal.x);
                double circleAngle = angleToTarget + (circleDirection * Math.PI / 2);

                yield new Vec3(
                        Math.cos(circleAngle) * baseSpeed * 1.2,
                        0,
                        Math.sin(circleAngle) * baseSpeed * 1.2
                );
            }

            case STRAFE -> {
                // 左右にダッシュ
                strafeTimer--;
                if (strafeTimer <= 0) {
                    // 方向を反転
                    strafeDirection *= -1;
                    strafeTimer = 20 + mob.getRandom().nextInt(20);
                }

                // ターゲット方向に対して垂直に移動
                double angleToTarget = Math.atan2(horizontal.z, horizontal.x);
                double strafeAngle = angleToTarget + (strafeDirection * Math.PI / 2);

                yield new Vec3(
                        Math.cos(strafeAngle) * baseSpeed * 1.5,
                        0,
                        Math.sin(strafeAngle) * baseSpeed * 1.5
                );
            }

            case RETREAT -> {
                // 後退
                if (distance < 0.1) yield Vec3.ZERO;
                yield horizontal.normalize().scale(-baseSpeed * 0.8);
            }

            case HOVER -> {
                // その場で浮遊（微小な移動）
                Vec3 drift = new Vec3(
                        (mob.getRandom().nextDouble() - 0.5) * baseSpeed * 0.2,
                        0,
                        (mob.getRandom().nextDouble() - 0.5) * baseSpeed * 0.2
                );
                yield drift;
            }
        };
    }

    /**
     * 垂直方向の移動を計算（浮遊高度を維持）
     */
    private double calculateVerticalMovement(Vec3 currentPos) {
        // 直下の地面または水面の高さを取得
        double groundY = findGroundOrWaterLevel(currentPos.x, currentPos.y, currentPos.z);

        if (groundY == Double.MIN_VALUE) {
            // 地面が見つからない場合は現在の高度を維持
            return limitVerticalSpeed(0);
        }

        // 目標高度
        double targetY = groundY + currentHoverHeight;

        // 現在高度との差
        double yDiff = targetY - currentPos.y;

        // 補正速度（差が大きいほど速く）
        double verticalSpeed = Mth.clamp(yDiff * 0.2, -0.5, 0.5);

        return limitVerticalSpeed(verticalSpeed);
    }

    /**
     * 垂直速度を制限
     */
    private double limitVerticalSpeed(double speed) {
        return Mth.clamp(speed, -0.8, 0.8);
    }

    /**
     * 直下の地面または水面の高さを取得
     */
    private double findGroundOrWaterLevel(double x, double y, double z) {
        Level level = mob.level();

        int startY = Mth.floor(y);
        int searchRange = 200;

        for (int checkY = startY; checkY >= startY - searchRange; checkY--) {
            BlockPos pos = new BlockPos(Mth.floor(x), checkY, Mth.floor(z));
            BlockState state = level.getBlockState(pos);

            // 水面をチェック
            if (!state.getFluidState().isEmpty()) {
                // 流体の上面を返す
                return checkY + 1.0;
            }

            // 固体ブロックをチェック
            if (state.isSolid() && !state.isAir()) {
                try {
                    var shape = state.getCollisionShape(level, pos);
                    if (!shape.isEmpty()) {
                        return pos.getY() + shape.max(Direction.Axis.Y);
                    }
                } catch (Exception ignored) {
                }
                return pos.getY() + 1.0;
            }
        }

        return Double.MIN_VALUE;
    }

    /**
     * ランダムに移動パターンを変更
     */
    private void randomizePattern() {
        MovementPattern[] patterns = MovementPattern.values();
        MovementPattern newPattern = patterns[mob.getRandom().nextInt(patterns.length)];

        // 前のパターンと同じ場合は再抽選（連続同一を避ける）
        if (newPattern == currentPattern) {
            newPattern = patterns[mob.getRandom().nextInt(patterns.length)];
        }

        currentPattern = newPattern;

        // パターンごとの初期化
        switch (currentPattern) {
            case CIRCLE -> {
                circleDirection = mob.getRandom().nextBoolean() ? 1 : -1;
            }
            case STRAFE -> {
                strafeDirection = mob.getRandom().nextBoolean() ? 1 : -1;
                strafeTimer = 20 + mob.getRandom().nextInt(20);
            }
        }
    }

    /**
     * ランダムに浮遊高度を変更
     */
    private void randomizeHoverHeight() {
        currentHoverHeight = minHoverHeight +
                mob.getRandom().nextDouble() * (maxHoverHeight - minHoverHeight);
    }

    // ==================== デバッグ用 ====================

    public MovementPattern getCurrentPattern() {
        return currentPattern;
    }

    public double getCurrentHoverHeight() {
        return currentHoverHeight;
    }
}
