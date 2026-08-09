package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.EnumSet;

public class FinFunnelGoal extends SmallMobGoalBase {
    private LivingEntity target;
    private final double baseSpeed;
    private final float longRange;
    private final float midRange;
    private final float closeRange;
    private final int attackCooldown;

    // 現在の戦闘距離
    private CombatRange currentRange;
    private int rangeTimer;
    private int attackTimer;

    // 球状軌道制御（中距離）
    private double sphereRadius;
    private double sphereTheta; // 極角（0-π）
    private double spherePhi;   // 方位角（0-2π）
    private double sphereThetaSpeed;
    private double spherePhiSpeed;
    private Vec3 sphereCenter;
    private int directionChangeTimer;
    private int directionChangeCooldown;

    // 立体機動制御（近距離）
    private Vec3 targetPosition;
    private Vec3 currentVelocity;
    private boolean isHovering;
    private int hoverTimer;
    private int hoverDuration;
    private int closeRangeTimer;
    private static final int CLOSE_RANGE_MAX_TIME = 200; // 10秒

    // 平滑化パラメータ
    private Vec3 targetVelocity;
    private static final double VELOCITY_SMOOTHING = 0.15;
    private static final double POSITION_SMOOTHING = 0.08;
    private static final double TARGET_CLEARANCE = 3.0D;

    private enum CombatRange {
        LONG_RANGE,
        MID_RANGE,
        CLOSE_RANGE
    }

    public FinFunnelGoal(BaseSmallMonsterEntity mob, double baseSpeed, float longRange, float midRange, float closeRange, int attackCooldown) {
        super(mob, (float)baseSpeed);
        this.mob = mob;
        this.baseSpeed = baseSpeed;
        this.longRange = longRange;
        this.midRange = midRange;
        this.closeRange = closeRange;
        this.attackCooldown = attackCooldown;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));

        // 初期化
        this.currentRange = CombatRange.LONG_RANGE;
        this.targetVelocity = Vec3.ZERO;
        this.currentVelocity = Vec3.ZERO;
        this.sphereRadius = midRange * 0.7;
        this.directionChangeCooldown = 60; // 3秒
        this.hoverDuration = 20; // 1秒
    }

    @Override
    public boolean canUse() {
        if (super.canUse()) {
            this.target = mob.getTarget();
            return target != null && target.isAlive();
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() &&
                mob.distanceToSqr(target) < (longRange * longRange * 4);
    }

    @Override
    public void start() {
        determineRange();
        initializeMovementPattern();
    }

    @Override
    public void tick() {
        if (target == null) return;

        mob.rotateToTarget(target);

        if (escapeTargetBounds()) {
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            return;
        }

        rangeTimer++;
        attackTimer++;
        directionChangeTimer++;

        // 距離の再評価
        if (rangeTimer % 20 == 0) { // 1秒ごと
            updateRange();
        }

        // 移動とアクション
        switch (currentRange) {
            case LONG_RANGE:
                handleLongRangeMovement();
                break;
            case MID_RANGE:
                handleMidRangeMovement();
                break;
            case CLOSE_RANGE:
                handleCloseRangeMovement();
                break;
        }

        // 攻撃処理
        handleAttacking();

        // 常にターゲットを向く
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    private void determineRange() {
        double distance = mob.distanceTo(target);

        if (distance > longRange) {
            currentRange = CombatRange.LONG_RANGE;
        } else if (distance > midRange) {
            currentRange = CombatRange.MID_RANGE;
        } else {
            currentRange = CombatRange.CLOSE_RANGE;
        }

        initializeMovementPattern();
    }

    private void updateRange() {
        double distance = mob.distanceTo(target);
        CombatRange newRange = currentRange;

        switch (currentRange) {
            case LONG_RANGE:
                if (distance <= midRange) {
                    newRange = CombatRange.MID_RANGE;
                }
                break;
            case MID_RANGE:
                // 中距離から近距離への移行（ランダム）
                if (distance <= closeRange || (mob.getRandom().nextInt(100) < 5)) { // 5%の確率
                    newRange = CombatRange.CLOSE_RANGE;
                } else if (distance > longRange) {
                    newRange = CombatRange.LONG_RANGE;
                }
                break;
            case CLOSE_RANGE:
                closeRangeTimer++;
                // 近距離時間制限または距離が離れた場合
                if (closeRangeTimer >= CLOSE_RANGE_MAX_TIME || distance > midRange * 1.5) {
                    newRange = CombatRange.MID_RANGE;
                    closeRangeTimer = 0;
                }
                break;
        }

        if (newRange != currentRange) {
            currentRange = newRange;
            initializeMovementPattern();
        }
    }

    private void initializeMovementPattern() {
        RandomSource random = mob.getRandom();

        switch (currentRange) {
            case LONG_RANGE:
                // 接近準備
                break;
            case MID_RANGE:
                // 球状軌道の初期化
                sphereCenter = target.position();
                sphereRadius = midRange * (0.6 + random.nextDouble() * 0.3); // 0.6-0.9倍

                Vec3 toMob = mob.position().subtract(sphereCenter).normalize();
                sphereTheta = Math.acos(toMob.y); // 現在の極角
                spherePhi = Math.atan2(toMob.z, toMob.x); // 現在の方位角

                sphereThetaSpeed = (random.nextDouble() - 0.5) * 0.03;
                spherePhiSpeed = (random.nextDouble() * 0.04 + 0.02) * (random.nextBoolean() ? 1 : -1);

                directionChangeTimer = 0;
                break;
            case CLOSE_RANGE:
                // 近距離機動の初期化
                generateRandomClosePosition();
                isHovering = false;
                hoverTimer = 0;
                closeRangeTimer = 0;
                break;
        }
    }

    private void handleLongRangeMovement() {
        // 中距離まで直線的に接近
        Vec3 mobPos = mob.position();
        Vec3 targetPos = target.position();
        Vec3 direction = targetPos.subtract(mobPos).normalize();

        double desiredDistance = midRange * 0.8;
        Vec3 desiredPos = targetPos.subtract(direction.scale(desiredDistance));

        Vec3 movement = desiredPos.subtract(mobPos);
        if (movement.length() > 0.1) {
            targetVelocity = movement.normalize().scale(baseSpeed * 1.2);
            applySmoothedMovement();
        }
    }

    private void handleMidRangeMovement() {
        // 球状軌道の更新
        sphereCenter = sphereCenter.lerp(target.position(), POSITION_SMOOTHING);

        // 方向変更
        if (directionChangeTimer >= directionChangeCooldown) {
            if (mob.getRandom().nextInt(100) < 25) { // 25%の確率で方向変更
                sphereThetaSpeed = (mob.getRandom().nextDouble() - 0.5) * 0.04;
                spherePhiSpeed = (mob.getRandom().nextDouble() * 0.05 + 0.02) *
                        (mob.getRandom().nextBoolean() ? 1 : -1);
                directionChangeTimer = 0;
                directionChangeCooldown = 40 + mob.getRandom().nextInt(80); // 2-6秒
            }
        }

        // 球面座標の更新
        sphereTheta += sphereThetaSpeed;
        spherePhi += spherePhiSpeed;

        // 範囲制限
        sphereTheta = Mth.clamp(sphereTheta, 0.3, Math.PI - 0.3); // 上下限制限

        // 球面座標→直交座標変換
        double x = sphereCenter.x + sphereRadius * Math.sin(sphereTheta) * Math.cos(spherePhi);
        double y = sphereCenter.y + sphereRadius * Math.cos(sphereTheta);
        double z = sphereCenter.z + sphereRadius * Math.sin(sphereTheta) * Math.sin(spherePhi);

        Vec3 targetPos = new Vec3(x, y, z);
        Vec3 movement = targetPos.subtract(mob.position());

        if (movement.length() > 0.1) {
            targetVelocity = movement.normalize().scale(baseSpeed * 0.8);
            applySmoothedMovement();
        }
    }

    private void handleCloseRangeMovement() {
        if (isHovering) {
            // ホバリング状態
            hoverTimer++;
            if (hoverTimer >= hoverDuration) {
                isHovering = false;
                hoverTimer = 0;
                generateRandomClosePosition();
            }

            // 微小な揺れ
            Vec3 wobble = new Vec3(
                    (mob.getRandom().nextDouble() - 0.5) * 0.1,
                    (mob.getRandom().nextDouble() - 0.5) * 0.1,
                    (mob.getRandom().nextDouble() - 0.5) * 0.1
            );
            targetVelocity = currentVelocity.scale(0.1).add(wobble);
            applySmoothedMovement();
        } else {
            // 移動状態
            Vec3 mobPos = mob.position();
            Vec3 movement = targetPosition.subtract(mobPos);

            if (movement.length() < 0.8) {
                // 目標位置に到達、ホバリング開始
                isHovering = true;
                hoverTimer = 0;
                hoverDuration = 15 + mob.getRandom().nextInt(20); // 0.75-1.75秒
                applyCloseRangeMovement(Vec3.ZERO);
            } else {
                // 目標位置への移動
                targetVelocity = movement.normalize().scale(baseSpeed * 1.5);
                applySmoothedMovement();
            }
        }
//        applyCloseRangeMovement(targetVelocity);
//        currentVelocity = targetVelocity;
//        applySmoothedMovement();
    }

    private void applyCloseRangeMovement(Vec3 movement) {
        // 近距離では慣性を無視したキビキビした動き
        if (mob.isNoGravity()) {
            mob.setDeltaMovement(movement);
        } else {
            // 重力の影響を受ける場合
            Vec3 currentMovement = mob.getDeltaMovement();
            mob.setDeltaMovement(movement.x, currentMovement.y, movement.z);
        }
    }

    private void generateRandomClosePosition() {
        RandomSource random = mob.getRandom();
        Vec3 targetPos = target.position();

        // ターゲット周辺のランダム位置（立体的）
        double minimumClearance = target.getBbWidth() * 0.5D
                + mob.getBbWidth() * 0.5D + TARGET_CLEARANCE;
        double distance = Math.max(
                closeRange * (0.3 + random.nextDouble() * 0.4),
                minimumClearance);

        Vec3 currentOffset = mob.position().subtract(targetPos);
        double currentAngle = currentOffset.horizontalDistanceSqr() > 0.01D
                ? Math.atan2(currentOffset.z, currentOffset.x)
                : random.nextDouble() * Math.PI * 2.0D;
        double theta = currentAngle + (random.nextDouble() - 0.5D) * Math.PI;
        double phi = random.nextDouble() * Math.PI; // 0-π

        double x = targetPos.x + distance * Math.sin(phi) * Math.cos(theta);
        double y = targetPos.y + distance * Math.cos(phi);
        double z = targetPos.z + distance * Math.sin(phi) * Math.sin(theta);

        // 高度制限
        y = Math.max(targetPos.y - 2, Math.min(targetPos.y + 4, y));

        targetPosition = new Vec3(x, y, z);
    }

    private boolean escapeTargetBounds() {
        double padding = mob.getBbWidth() * 0.5D + 1.0D;
        AABB exclusion = target.getBoundingBox().inflate(padding);
        if (!mob.getBoundingBox().intersects(exclusion)) {
            return false;
        }

        Vec3 center = exclusion.getCenter();
        Vec3 away = new Vec3(mob.getX() - center.x, 0.0D, mob.getZ() - center.z);
        if (away.horizontalDistanceSqr() < 0.0001D) {
            double angle = mob.getRandom().nextDouble() * Math.PI * 2.0D;
            away = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
        } else {
            away = away.normalize();
        }

        Vec3 escapeVelocity = away.scale(baseSpeed * 1.8D).add(0.0D, 0.12D, 0.0D);
        currentVelocity = escapeVelocity;
        targetVelocity = escapeVelocity;
        targetPosition = mob.position().add(away.scale(TARGET_CLEARANCE + padding));
        isHovering = false;
        hoverTimer = 0;
        mob.setDeltaMovement(escapeVelocity);
        mob.hurtMarked = true;
        return true;
    }

    private void applySmoothedMovement() {
        // 速度の平滑化
        currentVelocity = currentVelocity.lerp(targetVelocity, VELOCITY_SMOOTHING);

        // 飛行エンティティの場合のY軸制御
        if (mob.isNoGravity()) {
            mob.setDeltaMovement(currentVelocity);
        } else {
            // 重力の影響を受ける場合
            Vec3 currentMovement = mob.getDeltaMovement();
            mob.setDeltaMovement(currentVelocity.x, currentMovement.y, currentVelocity.z);
        }
    }

    private void handleAttacking() {
        if (attackTimer < attackCooldown) return;

        double distance = mob.distanceTo(target);
        boolean shouldAttack = false;

        switch (currentRange) {
            case LONG_RANGE:
                // 遠距離では攻撃しない
                break;
            case MID_RANGE:
                // 時々遠距離攻撃（20%の確率）
                shouldAttack = mob.getRandom().nextInt(100) < 20;
                break;
            case CLOSE_RANGE:
                // ホバリング時のみ攻撃
                shouldAttack = isHovering && hoverTimer > 5;
                break;
        }

        if (shouldAttack) {
            performAttack();
            attackTimer = 0;
        }
    }

    private void performAttack() {
        // 攻撃処理の実装
        // 例: 発射体の生成、即座攻撃など

        // 攻撃タイプを距離に応じて変更
        switch (currentRange) {
            case MID_RANGE:
                performRangedAttack();
                break;
            case CLOSE_RANGE:
                performMeleeAttack();
                break;
        }
    }

    private void performRangedAttack() {
        mob.tryAttack();
    }

    private void performMeleeAttack() {
        mob.tryAttack();
    }

    @Override
    public void stop() {
        target = null;
        currentVelocity = Vec3.ZERO;
        targetVelocity = Vec3.ZERO;
        isHovering = false;
        closeRangeTimer = 0;
    }
}
