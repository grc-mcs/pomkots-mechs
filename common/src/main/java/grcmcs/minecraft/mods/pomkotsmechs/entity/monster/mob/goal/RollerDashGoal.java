package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

import java.util.EnumSet;

public class RollerDashGoal extends SmallMobGoalBase {
    private final BaseSmallMonsterEntity mob;
    private LivingEntity target;
    private final double speed;
    private final float attackRange;
    private final int attackCooldown;

    private final int dashRange;
    private final int closeRange;

    private final int dashDuration;
    private final int circleDuration;

    // 移動パターン制御
    private MovementPattern currentPattern;
    private int patternTimer;
    private int patternDuration;
    private int attackTimer;

    // 円運動用変数
    private double circleRadius;
    private double circleAngle;
    private double circleSpeed;
    private Vec3 circleCenter;

    // 突進用変数
    private Vec3 dashTarget;
    private boolean isDashing;
    private int dashCooldown;

    private enum MovementPattern {
        DASH_TO_TARGET,
        CIRCLE_MOVEMENT,
        PATTERN_SWITCH_DELAY
    }

    public RollerDashGoal(BaseSmallMonsterEntity mob, double speed, float attackRange, int attackCooldown, int dashRange, int closeRange, int dashDuration, int circleDuration) {
        super(mob, (float)speed);
        this.mob = mob;
        this.speed = speed;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;
        this.dashRange = dashRange;
        this.closeRange = closeRange;
        this.dashDuration = dashDuration;
        this.circleDuration = circleDuration;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));

        // 初期値設定
        this.currentPattern = MovementPattern.DASH_TO_TARGET;
        this.patternTimer = 0;
        this.patternDuration = 60; // 3秒（20tick * 3）
        this.attackTimer = 0;
        this.dashCooldown = 0;
        this.circleRadius = 3.0;
        this.circleSpeed = 0.1;
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
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        switchPattern();
    }

    @Override
    public void tick() {
        if (target == null) return;

        mob.rotateToTarget(target);

        patternTimer++;
        attackTimer++;
        if (dashCooldown > 0) dashCooldown--;

        // パターン切り替え判定
        if (patternTimer >= patternDuration) {
            switchPattern();
        }

        // 移動処理
        switch (currentPattern) {
            case DASH_TO_TARGET:
                handleDashMovement();
                break;
            case CIRCLE_MOVEMENT:
                handleCircleMovement();
                break;
            case PATTERN_SWITCH_DELAY:
                // 待機時間、少し減速
                mob.setDeltaMovement(mob.getDeltaMovement().multiply(0.8, 1.0, 0.8));
                break;
        }

        // 攻撃処理
        handleAttack();

        // ターゲットを見る
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        mob.hasImpulse = true;
    }

    private void switchPattern() {
        RandomSource random = mob.getRandom();

        // 前のパターンと同じにならないよう制御
        MovementPattern newPattern;
        do {
            newPattern = random.nextBoolean() ?
                    MovementPattern.DASH_TO_TARGET : MovementPattern.CIRCLE_MOVEMENT;
        } while (newPattern == currentPattern && random.nextFloat() < 0.7f);

        currentPattern = newPattern;
        patternTimer = 0;

        switch (currentPattern) {
            case DASH_TO_TARGET:
                patternDuration = dashDuration + random.nextInt(dashDuration); // 2-4秒
                setupDash();
                break;
            case CIRCLE_MOVEMENT:
                patternDuration = circleDuration + random.nextInt(circleDuration); // 3-6秒
                setupCircleMovement();
                break;
        }
    }

    private void setupDash() {
        if (target == null || dashCooldown > 0) return;

        // ターゲットの少し手前を狙う
        Vec3 targetPos = target.position();
        Vec3 mobPos = mob.position();
        Vec3 direction = targetPos.subtract(mobPos).normalize();

        // ターゲットに近すぎる場合は少し離れた位置を狙う
        double distance = Math.max(2.0, mobPos.distanceTo(targetPos) - 1.5);
        dashTarget = mobPos.add(direction.scale(distance));

        isDashing = true;
        dashCooldown = 20; // 1秒のクールダウン
    }

    private void handleDashMovement() {
        if (target == null) return;

        Vec3 mobPos = mob.position();
        Vec3 targetPos = target.position();

        if (isDashing && dashTarget != null) {
            // 突進移動
            Vec3 direction = dashTarget.subtract(mobPos);
            if (direction.length() < dashRange) {
                isDashing = false;
                dashTarget = null;
                return;
            }

            direction = direction.normalize();
            Vec3 movement = direction.scale(speed * 1.5); // 突進時は1.5倍速

            // StepHeightを考慮した移動
            Vec3 newMovement = adjustMovementForTerrain(movement);
            mob.setDeltaMovement(newMovement);

        } else {
            Vec3 dist = targetPos.subtract(mobPos);
            if (dist.length() < closeRange) {
                switchPattern();
                return;
            }

            // 通常の接近移動
            Vec3 direction = targetPos.subtract(mobPos).normalize();
            Vec3 movement = direction.scale(speed);
            Vec3 newMovement = adjustMovementForTerrain(movement);
            mob.setDeltaMovement(newMovement);
        }
    }

    private void setupCircleMovement() {
        if (target == null) return;

        circleCenter = target.position();
        circleRadius = 40 + mob.getRandom().nextDouble() * 20; // 3-5ブロック
        circleSpeed = 0.08 + mob.getRandom().nextDouble() * 0.04; // 速度にランダム性

        // 現在位置から角度を計算
        Vec3 mobPos = mob.position();
        Vec3 offset = mobPos.subtract(circleCenter);
        circleAngle = Math.atan2(offset.z, offset.x);
    }

    private void handleCircleMovement() {
        if (target == null) return;

        // ターゲットが動いた場合、円の中心を更新
        circleCenter = circleCenter.lerp(target.position(), 0.1);

        // 円運動の計算
        circleAngle += circleSpeed;
        double targetX = circleCenter.x + Math.cos(circleAngle) * circleRadius;
        double targetZ = circleCenter.z + Math.sin(circleAngle) * circleRadius;

        Vec3 mobPos = mob.position();
        Vec3 targetCirclePos = new Vec3(targetX, mobPos.y, targetZ);

        Vec3 direction = targetCirclePos.subtract(mobPos);
        if (direction.length() > 0.1) {
            direction = direction.normalize();
            Vec3 movement = direction.scale(speed);
            Vec3 newMovement = adjustMovementForTerrain(movement);
            mob.setDeltaMovement(newMovement);
        }
    }

    private Vec3 adjustMovementForTerrain(Vec3 movement) {
        Vec3 mobPos = mob.position();
        Level level = mob.level();

        // 新しい位置を計算
        Vec3 newPos = mobPos.add(movement);
        BlockPos newBlockPos = new BlockPos((int)newPos.x, (int)newPos.y, (int)newPos.z);

        // 地面の高さをチェック
        BlockPos groundPos = findGroundLevel(newBlockPos, level);
        if (groundPos != null) {
            double groundY = groundPos.getY() + 1.0;
            double stepHeight = mob.maxUpStep();

            // StepHeightを考慮
            if (groundY - mobPos.y <= stepHeight && groundY - mobPos.y >= -stepHeight) {
//                return new Vec3(movement.x, (groundY - mobPos.y) * 0.3, movement.z);
                return new Vec3(movement.x, Math.min(0.5, (groundY - mobPos.y) * 0.3), movement.z);
            } else if (groundY > mobPos.y + stepHeight) {
                // 高すぎる場合はジャンプ
                return new Vec3(movement.x * 0.5, 0.4, movement.z * 0.5);
            }
        }

        return movement;
    }

    private BlockPos findGroundLevel(BlockPos pos, Level level) {
        // 下方向に地面を探す
        for (int i = 0; i < 10; i++) {
            BlockPos checkPos = pos.below(i);
            BlockState state = level.getBlockState(checkPos);
            if (!state.isAir() && state.isSolidRender(level, checkPos)) {
                return checkPos;
            }
        }

        // 上方向も確認
        for (int i = 1; i < 5; i++) {
            BlockPos checkPos = pos.above(i);
            BlockState state = level.getBlockState(checkPos.below());
            if (!state.isAir() && state.isSolidRender(level, checkPos.below())) {
                return checkPos.below();
            }
        }

        return null;
    }

    private void handleAttack() {
        if (target == null || attackTimer < attackCooldown) return;

        double distance = mob.distanceTo(target);
        if (distance <= attackRange) {
            // 攻撃実行
            performAttack();
            attackTimer = 0;
        }
    }

    private void performAttack() {
        mob.tryAttack();
    }

    @Override
    public void stop() {
        target = null;
        isDashing = false;
        dashTarget = null;
        currentPattern = MovementPattern.DASH_TO_TARGET;
        patternTimer = 0;
    }
}
