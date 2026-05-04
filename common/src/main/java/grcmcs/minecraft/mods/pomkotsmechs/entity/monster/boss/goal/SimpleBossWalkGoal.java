package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;



public class SimpleBossWalkGoal extends BaseBossGoal {
    private final double moveSpeed;
    private final double desiredDistance;

    private LivingEntity target;

    // モード
    private enum Mode { APPROACH, CIRCLE }
    private Mode mode = Mode.APPROACH;
    private int circleDirection = 1; // 1 or -1

    // タイマー
    private int ticksRemaining = 0;
    private int modeSwitchCooldown = 0;

    // 空中判定
    private int airTicks = 0;
    private static final int MAX_AIR_TICKS = 10;

    // 設定
    private static final int MIN_DURATION = 20;  // 最小実行時間(2秒)
    private static final int MAX_DURATION = 30; // 最大実行時間(5秒)
    private static final int MODE_SWITCH_INTERVAL = 15; // モード切替間隔
    private static final double CIRCLE_RADIUS = 2.0; // 円運動の追加距離

    public SimpleBossWalkGoal(BaseBossEntity mob, double speed, double desiredDistance) {
        super(mob);
        this.moveSpeed = speed;
        this.desiredDistance = desiredDistance;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUseInternal() {
        return mob.getTarget() != null && mob.getTarget().isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        // ゴール継続条件
        return ticksRemaining > 0
                && target != null
                && target.isAlive()
                && airTicks < MAX_AIR_TICKS;
    }

    @Override
    public void start() {
        target = mob.getTarget();
        ticksRemaining = MIN_DURATION + mob.getRandom().nextInt(MAX_DURATION - MIN_DURATION);
        modeSwitchCooldown = MODE_SWITCH_INTERVAL;
        mode = Mode.APPROACH;
        circleDirection = mob.getRandom().nextBoolean() ? 1 : -1;
        airTicks = 0;

        mob.setNoGravity(false);
    }

    @Override
    public void stop() {
        target = null;
        ticksRemaining = 0;

        // 水平移動を停止
        Vec3 vel = mob.getDeltaMovement();
        mob.setDeltaMovement(0, vel.y, 0);
    }

    @Override
    public void tick() {
        ticksRemaining--;

        if (target == null) return;

        // 空中判定
        updateAirTicks();

        // ターゲットを向く
        mob.rotateToTarget(target);

        // モード切り替え
        updateMode();

        // 移動処理
        performMovement();
    }

    /**
     * 空中判定を更新
     */
    private void updateAirTicks() {
        if (mob.onGround() || mob.isInWater() || mob.isInLava()) {
            airTicks = 0;
        } else {
            airTicks++;
        }
    }

    /**
     * モードを更新
     */
    private void updateMode() {
        modeSwitchCooldown--;

        if (modeSwitchCooldown <= 0) {
            double distance = mob.distanceTo(target);

            // 距離に応じてモード切り替え
            if (mode == Mode.APPROACH && distance <= desiredDistance) {
                mode = Mode.CIRCLE;
                circleDirection *= -1; // 方向反転
            } else if (mode == Mode.CIRCLE && distance > desiredDistance + CIRCLE_RADIUS) {
                mode = Mode.APPROACH;
            } else {
                // 同じモードを継続するが方向を変える
                circleDirection *= -1;
            }

            modeSwitchCooldown = MODE_SWITCH_INTERVAL + mob.getRandom().nextInt(20);
        }
    }

    /**
     * 移動処理
     */
    private void performMovement() {
        Vec3 currentPos = mob.position();
        Vec3 toTarget = target.position().subtract(currentPos);

        // 水平方向の移動ベクトルを計算
        Vec3 horizontalDir = calculateHorizontalDirection(toTarget);

        // 移動先のXZ座標
        double nextX = currentPos.x + horizontalDir.x;
        double nextZ = currentPos.z + horizontalDir.z;

        // 移動可能かチェック
        if (!canMoveToPosition(nextX, currentPos.y, nextZ)) {
            // 移動不可 → 重力のみ適用してゴール終了
            applyGravityOnly();
            ticksRemaining = 0;
            return;
        }

        // Y方向の移動を計算
        double yVelocity = calculateVerticalMovement(nextX, currentPos.y, nextZ);

        // 移動ベクトルを設定
        mob.setDeltaMovement(horizontalDir.x, yVelocity, horizontalDir.z);
    }

    /**
     * 水平方向の移動ベクトルを計算
     */
    private Vec3 calculateHorizontalDirection(Vec3 toTarget) {
        // 水平成分のみ（Y=0）
        Vec3 horizontal = new Vec3(toTarget.x, 0, toTarget.z);

        return switch (mode) {
            case APPROACH -> {
                // ターゲットへ直進
                if (horizontal.lengthSqr() < 0.01) {
                    yield Vec3.ZERO;
                }
                yield horizontal.normalize().scale(moveSpeed);
            }

            case CIRCLE -> {
                // ターゲット中心に円運動
                double angleToTarget = Math.atan2(horizontal.z, horizontal.x);
                double circleAngle = angleToTarget + (circleDirection * Math.PI / 2);

                yield new Vec3(
                        Math.cos(circleAngle) * moveSpeed,
                        0,
                        Math.sin(circleAngle) * moveSpeed
                );
            }
        };
    }
    private boolean isStandingInFluid() {
        var startPos = mob.blockPosition().above();
        for (int y = startPos.getY() + 5; y >= startPos.getY() - 5; y--) {
            BlockPos pos = new BlockPos(startPos.getX(), y, startPos.getZ());
            BlockState state = mob.level().getBlockState(pos);

            if (state.isSolid() && !state.isAir()) {
                return false;
            } else if (!state.getFluidState().isEmpty()) {
                return true;
            }
        }

        return false;
    }
    /**
     * 移動先に行けるかチェック
     * @param x 移動先X座標
     * @param y 現在のY座標（探索開始位置）
     * @param z 移動先Z座標
     * @return true = 移動可能, false = 移動不可
     */
    private boolean canMoveToPosition(double x, double y, double z) {
        Level level = mob.level();

        // 既に流体内にいるかチェック
        boolean currentlyInFluid = mob.isInWater() || mob.isInLava();
//        boolean currentlyInFluid = isStandingInFluid();

        // 移動先の足元の地面Y座標を探す
        double groundY = findGroundY(x, y, z);

        if (groundY == Double.MIN_VALUE) {
            // 地面がない = 崖
            return false;
        }

        // 地面ブロックの上（足が着く位置）をチェック
        BlockPos footPos = new BlockPos(Mth.floor(x), Mth.floor(groundY), Mth.floor(z));
        BlockState footState = level.getBlockState(footPos);

        BlockPos footPosB = new BlockPos(Mth.floor(x), Mth.floor(groundY) + 1, Mth.floor(z));
        BlockState footStateB = level.getBlockState(footPosB);

        BlockPos footPosC = new BlockPos(Mth.floor(x), Mth.floor(groundY) - 1, Mth.floor(z));
        BlockState footStateC = level.getBlockState(footPosC);

        // 流体判定
        if (!footState.getFluidState().isEmpty() || !footStateB.getFluidState().isEmpty()|| !footStateC.getFluidState().isEmpty()) {
            // 流体内にいる場合はOK、いない場合はNG
            return currentlyInFluid;
        }

        // 段差が高すぎないかチェック
        double heightDiff = groundY - y;
        double maxStep = mob.maxUpStep() + 1.0;

        if (heightDiff > maxStep) {
            return false;
        }

        // 頭上に十分な空間があるかチェック
        if (!hasHeadroom(x, groundY, z)) {
            return false;
        }

        return true;
    }

    /**
     * 指定XZ座標の地面Y座標を探す
     * @return 地面のY座標、見つからない場合は Double.MIN_VALUE
     */
    private double findGroundY(double x, double startY, double z) {
        Level level = mob.level();

        int searchUp = (int) Math.ceil(mob.maxUpStep()) + 2;
        int searchDown = 30;

        int startYInt = Mth.floor(startY);

        for (int y = startYInt + searchUp; y >= startYInt - searchDown; y--) {
            BlockPos pos = new BlockPos(Mth.floor(x), y, Mth.floor(z));
            BlockState state = level.getBlockState(pos);

            // 流体はスキップ
            if (!state.getFluidState().isEmpty()) {
                continue;
            }

            // 固体ブロック判定
            if (state.isSolid() && !state.isAir()) {
                // コリジョン形状から上面を取得
                double topY = getBlockTopY(level, pos, state);
                return topY;
            }
        }

        return Double.MIN_VALUE;
    }

    /**
     * ブロックの上面Y座標を取得
     */
    private double getBlockTopY(Level level, BlockPos pos, BlockState state) {
        try {
            var shape = state.getCollisionShape(level, pos);
            if (!shape.isEmpty()) {
                return pos.getY() + shape.max(Direction.Axis.Y);
            }
        } catch (Exception ignored) {
        }
        return pos.getY() + 1.0;
    }

    /**
     * 頭上に十分な空間があるかチェック
     */
    private boolean hasHeadroom(double x, double groundY, double z) {
        Level level = mob.level();
        double height = mob.getBbHeight();

        int checkHeight = Mth.ceil(height) + 1;

        for (int y = 0; y < checkHeight; y++) {
            BlockPos pos = new BlockPos(
                    Mth.floor(x),
                    Mth.floor(groundY) + y,
                    Mth.floor(z)
            );

            BlockState state = level.getBlockState(pos);

            // 固体ブロックがあったら頭上が詰まっている
            if (state.isSolid() && !state.isAir()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Y方向の移動量を計算
     */
    private double calculateVerticalMovement(double nextX, double currentY, double nextZ) {
        double groundY = findGroundY(nextX, currentY, nextZ);

        if (groundY == Double.MIN_VALUE) {
            // 地面がない = 落下
            return applyGravity();
        }

        double heightDiff = groundY - currentY;

        if (Math.abs(heightDiff) < 0.1) {
            // ほぼ同じ高さ
            return 0.0;
        } else if (heightDiff > 0) {
            // 段差を上る
            double maxStep = mob.maxUpStep();
            if (heightDiff <= maxStep) {
                return heightDiff; // 一気に上る
            } else {
                return maxStep; // 最大ステップ高さまで
            }
        } else {
            // 下り坂 or 落下
            return applyGravity();
        }
    }

    /**
     * 重力を適用
     */
    private double applyGravity() {
        double currentVY = mob.getDeltaMovement().y;
        double gravity = 0.08;

        if (!mob.onGround()) {
            return Math.max(currentVY - gravity, -3.0); // 最大落下速度
        }

        return 0.0;
    }

    /**
     * 重力のみ適用して水平移動を停止
     */
    private void applyGravityOnly() {
        mob.setDeltaMovement(0, applyGravity(), 0);
    }
}