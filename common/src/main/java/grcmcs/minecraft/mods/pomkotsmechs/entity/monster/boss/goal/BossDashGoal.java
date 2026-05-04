package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class BossDashGoal extends BaseBossGoal {
    protected final double dashSpeed;
    protected final double maxRotationSpeed; // 1ティックあたりの最大回転角度（度）
    protected final double startDistance;
    protected final double stopDistance; // この距離以下でストップ
    protected final int maxDuration; // 最大継続時間（ティック）
    protected final double homingStrength; // ホーミングの強さ（0.0-1.0）

    protected LivingEntity target;
    protected Vec3 dashDirection;
    protected int dashTimer;
    protected float initialYaw;
    protected boolean hasStarted;

    public BossDashGoal(BaseBossEntity mob, double dashSpeed, double maxRotationSpeed,
                        double startDistance, double stopDistance, int maxDuration, double homingStrength) {
        super(mob);
        this.dashSpeed = dashSpeed;
        this.maxRotationSpeed = maxRotationSpeed;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.maxDuration = maxDuration;
        this.homingStrength = Math.max(0.0, Math.min(1.0, homingStrength));
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUseInternal() {
        return !hasStarted;
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null || !target.isAlive()) {
            return false;
        }

        // 距離チェック
        if (mob.distanceTo(target) <= stopDistance) {
            int s = dashTimer % 2;
            if (s == 0) {
                mob.getActionController().getAction("saber_tate").tryAction();
            } else {
                mob.getActionController().getAction("saber_circle").tryAction();
            }

            return false;
        }

        // 時間制限チェック
        if (dashTimer >= maxDuration) {
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        target = mob.getTarget();
        hasStarted = true;
        dashTimer = 0;
        initialYaw = mob.getYRot();

        // 初期ダッシュ方向を設定（ターゲットへの方向）
        Vec3 toTarget = target.position().subtract(mob.position()).normalize();
        dashDirection = new Vec3(toTarget.x, 0, toTarget.z).normalize();

//        // ダッシュ開始時のエフェクト
//        performDashStartEffects();

        mob.triggerAnim("action_controller", "dash");
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) {
            return;
        }

        dashTimer++;

        // ホーミング処理
        Vec3 updatedDirection = calculateHomingDirection();

        // 回転制限を適用
        Vec3 limitedDirection = applyRotationLimit(updatedDirection);

        // 地面に接地した移動ベクトルを計算
        Vec3 groundMovement = calculateGroundMovement(limitedDirection);

        // 移動を適用
        mob.setDeltaMovement(groundMovement);

        // 向きを更新
        updateRotation(limitedDirection);

    }

    protected Vec3 calculateHomingDirection() {
        Vec3 currentDirection = dashDirection;

        if (homingStrength > 0) {
            // ターゲットへの方向を計算（地面レベルで）
            Vec3 toTarget = target.position().subtract(mob.position());
            Vec3 targetDirection = new Vec3(toTarget.x, 0, toTarget.z).normalize();

            // 現在の方向とターゲット方向を補間
            Vec3 newDirection = currentDirection.scale(1.0 - homingStrength)
                    .add(targetDirection.scale(homingStrength))
                    .normalize();

            dashDirection = newDirection;
        }

        return dashDirection;
    }

    protected Vec3 applyRotationLimit(Vec3 desiredDirection) {
        // 現在の向きから目標方向への角度差を計算
        float currentYaw = mob.getYRot();
        float desiredYaw = (float)(Mth.atan2(desiredDirection.x, desiredDirection.z) * (-180 / Math.PI));

        // 角度差を計算
        float angleDiff = Mth.wrapDegrees(desiredYaw - currentYaw);

        // 最大回転速度で制限
        float limitedAngleDiff = Mth.clamp(angleDiff, -(float)maxRotationSpeed, (float)maxRotationSpeed);
        float limitedYaw = currentYaw + limitedAngleDiff;

        // 制限された角度から方向ベクトルを再計算
        float radians = limitedYaw * Mth.DEG_TO_RAD;
        return new Vec3(-Math.sin(radians), 0, Math.cos(radians)).normalize();
    }

    protected Vec3 calculateGroundMovement(Vec3 horizontalDirection) {
        Vec3 currentPos = mob.position();
        Vec3 targetHorizontalPos = currentPos.add(horizontalDirection.scale(dashSpeed));

        // 地面の高さを見つける
        double groundY = findGroundLevel(targetHorizontalPos.x, targetHorizontalPos.z);

        // 移動が安全かチェック
//        if (!canMoveToPosition(targetHorizontalPos.x, groundY, targetHorizontalPos.z)) {
//            // 移動不可の場合は停止
//            return new Vec3(0, applyGravity(), 0);
//        }

        // Y軸の移動を計算
        double yMovement = calculateYMovement(currentPos.y, groundY);

        return new Vec3(horizontalDirection.x * dashSpeed, yMovement, horizontalDirection.z * dashSpeed);
    }

    protected double findGroundLevel(double x, double z) {
        Level level = mob.level();
        int startY = (int) mob.getY();
        int searchRange = 30;

        // 下方向に探索
        for (int y = startY; y >= startY - searchRange; y--) {
            BlockPos pos = new BlockPos((int)x, y, (int)z);
            BlockState state = level.getBlockState(pos);

            if (!state.isAir() && state.isSolidRender(level, pos)) {
                return y + 1.0;
            }
        }

        // 上方向にも探索
        for (int y = startY + 1; y <= startY + searchRange; y++) {
            BlockPos pos = new BlockPos((int)x, y - 1, (int)z);
            BlockState state = level.getBlockState(pos);

            if (!state.isAir() && state.isSolidRender(level, pos)) {
                return y;
            }
        }

        return mob.getY() - 20;
    }

    private boolean canMoveToPosition(double x, double y, double z) {
        Level level = mob.level();
        AABB entityBB = mob.getBoundingBox();
        double width = entityBB.getXsize();
        double height = entityBB.getYsize();

        AABB targetBB = new AABB(
                x - width/2, y, z - width/2,
                x + width/2, y + height, z + width/2
        );

        return level.noCollision(mob, targetBB);
    }

    protected double calculateYMovement(double currentY, double groundY) {
        double yDiff = groundY - currentY;
        double maxStepHeight = mob.maxUpStep();

        if (yDiff > 0 && yDiff <= maxStepHeight) {
            // StepHeight内の段差は瞬間移動
            return yDiff;
        } else if (yDiff > maxStepHeight) {
            // 大きな段差は少しずつ上る（ダッシュの勢いを維持）
            return Math.min(0.5, yDiff * 0.3);
        } else if (yDiff < -0.1) {
            // 落下処理
            return applyGravity();
        } else {
            // 地面レベル付近なら微調整
            return yDiff * 0.2;
        }
    }

    protected double applyGravity() {
        double currentVerticalVelocity = mob.getDeltaMovement().y;
        double gravity = 0.08;

        if (!mob.onGround()) {
            return Math.max(currentVerticalVelocity - gravity, -1.5); // ダッシュ中は落下速度を少し制限
        }

        return 0.0;
    }

    protected void updateRotation(Vec3 direction) {
        float desiredYaw = (float)(Mth.atan2(direction.x, direction.z) * (-180 / Math.PI));
        mob.setYRot(desiredYaw);
        mob.yBodyRot = desiredYaw;
        mob.yHeadRot = desiredYaw;
    }

    private void performDashStartEffects() {
        Level level = mob.level();

        // パーティクルエフェクト
        if (level instanceof ServerLevel serverLevel) {
            // 開始時の爆発エフェクト
            Vec3 pos = mob.position();
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    pos.x, pos.y + 0.5, pos.z, 8, 0.5, 0.3, 0.5, 0.1);

            // 地面の土埃
            serverLevel.sendParticles(ParticleTypes.POOF,
                    pos.x, pos.y, pos.z, 5, 0.3, 0.1, 0.3, 0.05);
        }

        // サウンドエフェクト
        // level.playSound(null, mob.blockPosition(), SoundEvents.RAVAGER_ROAR,
        //     SoundSource.HOSTILE, 1.0f, 1.2f);
    }

    private void performDashEffects() {
        Level level = mob.level();
        Vec3 pos = mob.position();

        if (level instanceof ServerLevel serverLevel) {
            // 移動軌跡のパーティクル
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                    pos.x, pos.y + mob.getBbHeight() * 0.5, pos.z, 1, 0.2, 0.2, 0.2, 0.0);

            // 地面との摩擦エフェクト
            if (mob.onGround()) {
                serverLevel.sendParticles(ParticleTypes.SMOKE,
                        pos.x, pos.y + 0.1, pos.z, 2, 0.3, 0.1, 0.3, 0.02);
            }
        }
    }

    @Override
    public void stop() {
        hasStarted = false;
        dashTimer = 0;
        dashDirection = null;

        // 停止時のエフェクト
//        performDashStopEffects();

        // 移動の慣性を残す
        Vec3 currentMovement = mob.getDeltaMovement();
        mob.setDeltaMovement(currentMovement.multiply(0.3, 1.0, 0.3));

        mob.triggerAnim("action_controller", "stop");
    }

    private void performDashStopEffects() {
        Level level = mob.level();
        Vec3 pos = mob.position();

        if (level instanceof ServerLevel serverLevel) {
            // 停止時の衝撃エフェクト
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    pos.x, pos.y + 0.5, pos.z, 3, 0.5, 0.3, 0.5, 0.0);
        }
    }

    // デバッグ用のゲッター
    public int getDashTimer() {
        return dashTimer;
    }

    public Vec3 getDashDirection() {
        return dashDirection;
    }

    public boolean isActive() {
        return hasStarted && canContinueToUse();
    }
}