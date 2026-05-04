package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class BossAerialDiveGoal2 extends BaseBossGoal {

    // 設定パラメータ
    private final double heightDifferenceThreshold; // 高低差の閾値
    private final double riseSpeed;                 // 上昇速度
    private final double flySpeed;                  // 飛行速度
    private final double diveSpeed;                 // 降下速度
    private final double targetHeight;              // 目標飛行高度
    private final int maxDuration;                  // 最大実行時間

    // 状態管理
    private enum Phase {
        CHARGE,      // 上昇
        RISE,      // 上昇
        FLY,       // 飛行
        DIVE,      // 降下
        FINISHED   // 完了
    }

    private Phase currentPhase = Phase.FINISHED;
    private Vec3 destination;     // 目的地
    private int tickCounter;
    private LivingEntity target;

    // 定数
    private static final double DIVE_START_DISTANCE = 5.0;  // この距離で降下開始
    private static final int LAND_SEARCH_RANGE = 50;        // 陸地探索範囲

    public BossAerialDiveGoal2(BaseBossEntity mob,
                               double heightDifferenceThreshold,
                               double riseSpeed,
                               double flySpeed,
                               double diveSpeed,
                               double targetHeight,
                               int maxDuration) {
        super(mob);
        this.heightDifferenceThreshold = heightDifferenceThreshold;
        this.riseSpeed = riseSpeed;
        this.flySpeed = flySpeed;
        this.diveSpeed = diveSpeed;
        this.targetHeight = targetHeight;
        this.maxDuration = maxDuration;

        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUseInternal() {
        if (currentPhase != Phase.FINISHED) {
            return false;
        }

        target = mob.getTarget();

        // ターゲットがいない場合はスキップ
        if (target == null || !target.isAlive()) {
            return false;
        }

        // 条件1: 高低差が大きい場合
        if (checkHeightDifference()) {
            destination = calculateTargetDestination();
            return true;
        }

        // 条件2: 水中にいる場合
        if (isStandingInFluid()) {
            destination = findNearestLand();
            return destination != null;
        }

        return false;
    }

    private boolean isStandingInFluid() {
        // 足元のブロック状態を取得
        BlockState feetState = mob.level().getBlockState(mob.blockPosition().above());
        // 流体があればtrue
        return !feetState.getFluidState().isEmpty();
    }

    @Override
    public boolean canContinueToUse() {
        return currentPhase != Phase.FINISHED
                && tickCounter < maxDuration
                && destination != null;
    }

    @Override
    public void start() {
        currentPhase = Phase.CHARGE;
        tickCounter = 0;
        mob.setNoGravity(true);
    }

    @Override
    public void stop() {
        currentPhase = Phase.FINISHED;
        tickCounter = 0;
        destination = null;
        target = null;

        mob.setNoGravity(false);

        // 着地処理（あれば）
        if (mob.onGround()) {
            mob.getActionController().getAction("onground").tryAction();
        }
    }

    @Override
    public void tick() {
        tickCounter++;

        // タイムアウトチェック
        if (tickCounter >= maxDuration) {
            currentPhase = Phase.FINISHED;
            return;
        }

        // フェーズごとの処理
        switch (currentPhase) {
            case CHARGE -> handleChargePhase();
            case RISE -> handleRisePhase();
            case FLY -> handleFlyPhase();
            case DIVE -> handleDivePhase();
        }

        // 向きを更新
        updateRotation();
    }

    /**
     * 上昇フェーズ
     */
    private void handleChargePhase() {
        if (tickCounter == 1) {
            mob.triggerAnim("action_controller", "jump");

        } else if (tickCounter >= 10) {
            currentPhase = Phase.RISE;
        }
    }


    /**
     * 上昇フェーズ
     */
    private void handleRisePhase() {
        double currentY = mob.getY();
        double goalY = destination.y + targetHeight;

        if (currentY >= goalY) {
            // 目標高度に達したら飛行フェーズへ
            currentPhase = Phase.FLY;
            return;
        }

        // 上昇（水平移動も少し）
        Vec3 toDestination = destination.subtract(mob.position());
        Vec3 horizontalDir = new Vec3(toDestination.x, 0, toDestination.z).normalize();

        Vec3 movement = horizontalDir.scale(flySpeed * 0.3).add(0, riseSpeed, 0);
        mob.setDeltaMovement(movement);
    }

    /**
     * 飛行フェーズ
     */
    private void handleFlyPhase() {
        Vec3 currentPos = mob.position();
        Vec3 toDestination = destination.subtract(currentPos);

        // 水平距離を計算
        double horizontalDistance = Math.sqrt(
                toDestination.x * toDestination.x +
                        toDestination.z * toDestination.z
        );

        // 目的地に近づいたら降下フェーズへ
        if (horizontalDistance < DIVE_START_DISTANCE) {
            currentPhase = Phase.DIVE;
            mob.setNoGravity(false);
            return;
        }

        // 飛行移動
        Vec3 horizontalDir = new Vec3(toDestination.x, 0, toDestination.z).normalize();

        // 高度維持（目標高度との差を補正）
        double goalY = destination.y + targetHeight;
        double yAdjustment = (goalY - currentPos.y) * 0.2;

        Vec3 movement = horizontalDir.scale(flySpeed).add(0, yAdjustment, 0);
        mob.setDeltaMovement(movement);
    }

    /**
     * 降下フェーズ
     */
    private void handleDivePhase() {
        // 重力で落下（NoGravityは既にfalse）
        Vec3 currentVel = mob.getDeltaMovement();

        // 少しだけ目的地方向に誘導
        Vec3 toDestination = destination.subtract(mob.position());
        Vec3 horizontalDir = new Vec3(toDestination.x, 0, toDestination.z).normalize();

        Vec3 movement = new Vec3(
                horizontalDir.x * flySpeed * 0.3,
                Math.max(currentVel.y - 0.1, -diveSpeed), // 重力加速
                horizontalDir.z * flySpeed * 0.3
        );

        mob.setDeltaMovement(movement);

        // 着地判定
        if (mob.onGround() || isNearGround()) {
            currentPhase = Phase.FINISHED;
        }
    }

    /**
     * 向きを更新
     */
    private void updateRotation() {
        if (destination == null) return;

        Vec3 toDestination = destination.subtract(mob.position());

        // Yaw（水平方向）
        float targetYaw = (float) (Mth.atan2(toDestination.z, toDestination.x) * (180 / Math.PI)) - 90;
        mob.setYRot(lerpRotation(mob.getYRot(), targetYaw, 5.0F));
        mob.yBodyRot = mob.getYRot();

        // Pitch（上下方向）- 飛行中のみ
        if (currentPhase == Phase.FLY || currentPhase == Phase.RISE) {
            double horizontalDist = Math.sqrt(
                    toDestination.x * toDestination.x +
                            toDestination.z * toDestination.z
            );
            float targetPitch = (float) (Math.atan2(-toDestination.y, horizontalDist) * (180 / Math.PI));
            mob.setXRot(lerpRotation(mob.getXRot(), targetPitch, 3.0F));
        }
    }

    /**
     * 回転を滑らかに補間
     */
    private float lerpRotation(float current, float target, float maxDelta) {
        float delta = Mth.wrapDegrees(target - current);
        delta = Mth.clamp(delta, -maxDelta, maxDelta);
        return current + delta;
    }

    /**
     * 地面に近いかチェック
     */
    private boolean isNearGround() {
        BlockPos belowPos = mob.blockPosition().below(2);
        return mob.level().getBlockState(belowPos).isSolid();
    }

    // ==================== 条件判定 ====================

    /**
     * 高低差条件をチェック
     */
    private boolean checkHeightDifference() {
        if (target == null) return false;

        double heightDiff = Math.abs(target.getY() - mob.getY());

        if (heightDiff < heightDifferenceThreshold) {
            return false;
        }

        // ターゲットが地面にいるかチェック
        Entity vehicle = target.getVehicle();
        if (vehicle != null) {
            return vehicle.onGround() && !vehicle.isInWater();
        } else {
            return target.onGround() && !target.isInWater();
        }
    }

    /**
     * ターゲット位置への目的地を計算
     */
    private Vec3 calculateTargetDestination() {
        if (target == null) return null;

        Vec3 targetPos = target.position();

        // 地面の高さを探す
        double groundY = findGroundY(targetPos.x, targetPos.y, targetPos.z);

        if (groundY != Double.MIN_VALUE) {
            return new Vec3(targetPos.x, groundY, targetPos.z);
        }

        // 地面が見つからない場合はターゲットの位置をそのまま使う
        return targetPos;
    }

    /**
     * 最寄りの陸地を探す
     */
    private Vec3 findNearestLand() {
        Level level = mob.level();
        Vec3 currentPos = mob.position();

        BlockPos startPos = mob.blockPosition();

        // 螺旋状に探索
        for (int radius = 1; radius <= LAND_SEARCH_RANGE; radius += 5) {
            for (int angle = 0; angle < 360; angle += 30) {
                double rad = Math.toRadians(angle);
                int x = (int) (currentPos.x + Math.cos(rad) * radius);
                int z = (int) (currentPos.z + Math.sin(rad) * radius);

                // この位置の地面を探す
                BlockPos pos = findSurfaceBlockPos(x, currentPos.y, z);

                if (pos == null) {
                    continue;
                }

                BlockState state = level.getBlockState(pos);

                // 流体でない固体ブロックなら陸地
                if (state.isSolid() && state.getFluidState().isEmpty()) {
                    return new Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                }
            }
        }

        return null; // 陸地が見つからない
    }

    /**
     * 指定座標の地面Y座標を探す
     */
    private double findGroundY(double x, double startY, double z) {
        Level level = mob.level();

        int searchUp = 10;
        int searchDown = 50;
        int startYInt = Mth.floor(startY);

        for (int y = startYInt + searchUp; y >= startYInt - searchDown; y--) {
            BlockPos pos = new BlockPos(Mth.floor(x), y, Mth.floor(z));
            BlockState state = level.getBlockState(pos);

            if (state.isSolid() && !state.isAir()) {
                // コリジョン形状から上面を取得
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

    private BlockPos findSurfaceBlockPos(double x, double startY, double z) {
        Level level = mob.level();

        int searchUp = 20;
        int searchDown = 20;
        int startYInt = Mth.floor(startY);

        for (int y = startYInt + searchUp; y >= startYInt - searchDown; y--) {
            BlockPos pos = new BlockPos(Mth.floor(x), y, Mth.floor(z));
            BlockState state = level.getBlockState(pos);

            if (!state.getFluidState().isEmpty()) {
                return null;
            }

            if (state.isSolid() && !state.isAir()) {
                return pos;
            }
        }

        return null;
    }

    // ==================== デバッグ用 ====================

    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public Vec3 getDestination() {
        return destination;
    }

    public int getTickCounter() {
        return tickCounter;
    }
}