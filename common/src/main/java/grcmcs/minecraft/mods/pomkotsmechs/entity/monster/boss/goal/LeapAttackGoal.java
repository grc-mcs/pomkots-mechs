package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

public class LeapAttackGoal extends Goal {
    protected final BaseBossEntity mob;
    private LivingEntity target;

    // 攻撃の段階
    private enum AttackPhase {
        IDLE,
        CHARGING,
        LEAPING,
        LANDED
    }

    private AttackPhase currentPhase = AttackPhase.IDLE;

    // 設定可能なパラメータ
    private final int chargeDuration; // 溜め時間（ティック）
    private final double leapRange; // ジャンプ攻撃の射程
    private final double attackDamage; // 攻撃ダメージ
    private final float attackRange; // 攻撃判定の範囲

    // 内部状態
    private int chargeTicks = 0;
    private Vec3 targetPosition;
    private boolean hasLanded = false;
    private int cooldownTicks = 0;
    private final int cooldownDuration = 100; // クールダウン時間

    public LeapAttackGoal(BaseBossEntity mob, int chargeDuration, double leapRange, double attackDamage, float attackRange) {
        this.mob = mob;
        this.chargeDuration = chargeDuration;
        this.leapRange = leapRange;
        this.attackDamage = attackDamage;
        this.attackRange = attackRange;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        // クールダウン中は使用不可
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return false;
        }

        // ターゲットが存在し、適切な距離にいるかチェック
        this.target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }

        double distance = mob.distanceToSqr(target);
        return distance <= leapRange * leapRange && distance > attackRange * attackRange;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() && currentPhase != AttackPhase.IDLE;
    }

    @Override
    public void start() {
        currentPhase = AttackPhase.CHARGING;
        chargeTicks = 0;
        hasLanded = false;

        // ターゲットの現在位置を記録（予測位置も考慮可能）
        targetPosition = target.position();

        // 溜めモーション開始（アニメーション呼び出し）
        onChargeStart();
    }

    @Override
    public void tick() {
        switch (currentPhase) {
            case CHARGING:
                handleCharging();
                break;
            case LEAPING:
                handleLeaping();
                break;
            case LANDED:
                handleLanded();
                break;
        }
    }

    private void handleCharging() {
        chargeTicks++;

        // ターゲットを見続ける
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 溜め完了
        if (chargeTicks >= chargeDuration) {
            executeLeap();
            currentPhase = AttackPhase.LEAPING;
        }

        // 溜め中のエフェクト
        onChargeTick(chargeTicks);
    }

    private void executeLeap() {
        // ターゲット位置への弾道計算
        Vec3 startPos = mob.position();
        Vec3 targetPos = targetPosition;

        // 高さの差を考慮
        double deltaX = targetPos.x - startPos.x;
        double deltaY = targetPos.y - startPos.y;
        double deltaZ = targetPos.z - startPos.z;
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // 重力を考慮した弾道計算
        double gravity = 0.08; // Minecraftの重力値
        double velocityY = Math.sqrt(2 * gravity * Math.max(4.0, deltaY + 4.0));
        double time = Math.sqrt(2 * (deltaY + velocityY * velocityY / (2 * gravity)) / gravity);

        double velocityX = deltaX / time;
        double velocityZ = deltaZ / time;

        // 速度制限（あまりに高速にならないように）
        double maxHorizontalSpeed = 3.0;
        double horizontalSpeed = Math.sqrt(velocityX * velocityX + velocityZ * velocityZ);
        if (horizontalSpeed > maxHorizontalSpeed) {
            velocityX = velocityX / horizontalSpeed * maxHorizontalSpeed;
            velocityZ = velocityZ / horizontalSpeed * maxHorizontalSpeed;
        }

        // ジャンプ実行
        mob.setDeltaMovement(velocityX, velocityY, velocityZ);
        mob.hasImpulse = true;

        // ジャンプエフェクト
        onLeapStart();
    }

    private void handleLeaping() {
        // 着地チェック
        if (mob.onGround() && !hasLanded) {
            hasLanded = true;
            currentPhase = AttackPhase.LANDED;
            onLanded();
            performAttack();
            return;
        }

        // ターゲットとの接触チェック
        if (mob.distanceToSqr(target) <= attackRange * attackRange) {
            hasLanded = true;
            currentPhase = AttackPhase.LANDED;
            performAttack();
            return;
        }

        // 空中でのエフェクト
        onLeapTick();
    }

    private void handleLanded() {
        // 攻撃後の処理
        stop();
    }

    private void performAttack() {
        // 範囲内のエンティティに攻撃
        for (LivingEntity entity : mob.level().getEntitiesOfClass(
                LivingEntity.class,
                mob.getBoundingBox().inflate(attackRange),
                e -> e != mob && mob.canAttack(e))) {

            // ダメージを与える
            entity.hurt(mob.damageSources().mobAttack(mob), (float)attackDamage);

            // ノックバック
            double knockbackStrength = 1.5;
            Vec3 direction = entity.position().subtract(mob.position()).normalize();
            entity.setDeltaMovement(entity.getDeltaMovement().add(
                    direction.x * knockbackStrength,
                    0.5,
                    direction.z * knockbackStrength
            ));
        }

        // 攻撃エフェクト
        onAttackHit();
    }

    @Override
    public void stop() {
        currentPhase = AttackPhase.IDLE;
        chargeTicks = 0;
        hasLanded = false;
        cooldownTicks = cooldownDuration;
        target = null;
        targetPosition = null;
    }

    // カスタマイズ可能なイベントメソッド
    protected void onChargeStart() {
        // 溜め開始時の処理（音、パーティクル、アニメーション等）
    }

    protected void onChargeTick(int chargeTicks) {
        // 溜め中の処理（パーティクルエフェクト等）
    }

    protected void onLeapStart() {
        // ジャンプ開始時の処理（音、エフェクト等）
    }

    protected void onLeapTick() {
        // ジャンプ中の処理（軌跡エフェクト等）
    }

    protected void onLanded() {
        // 着地時の処理（音、パーティクル等）
    }

    protected void onAttackHit() {
        // 攻撃ヒット時の処理（音、エフェクト等）
    }

    // ゲッター
    public AttackPhase getCurrentPhase() {
        return currentPhase;
    }

    public int getChargeTicks() {
        return chargeTicks;
    }

    public int getMaxChargeTicks() {
        return chargeDuration;
    }
}

/*
使用例：

public class RobotEntity extends Mob {
    // エンティティの初期化時に
    @Override
    protected void registerGoals() {
        // 溜め時間20ティック、射程15ブロック、ダメージ8、攻撃範囲3ブロック
        this.goalSelector.addGoal(2, new LeapAttackGoal(this, 20, 15.0, 8.0, 3.0f));
        // 他のGoalも追加...
    }
}

// エフェクト付きのカスタム版
public class CustomLeapAttackGoal extends LeapAttackGoal {
    public CustomLeapAttackGoal(Mob mob) {
        super(mob, 30, 20.0, 10.0, 4.0f);
    }

    @Override
    protected void onChargeStart() {
        // 溜め開始音
        mob.level().playSound(null, mob.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.0f, 0.5f);
    }

    @Override
    protected void onChargeTick(int chargeTicks) {
        // 溜め中のパーティクル
        if (chargeTicks % 5 == 0) {
            ((ServerLevel)mob.level()).sendParticles(
                ParticleTypes.FLAME,
                mob.getX(), mob.getY() + 1, mob.getZ(),
                5, 0.5, 0.5, 0.5, 0.1
            );
        }
    }

    @Override
    protected void onLeapStart() {
        // ジャンプ音
        mob.level().playSound(null, mob.blockPosition(), SoundEvents.ENDER_DRAGON_FLAP, SoundSource.HOSTILE, 1.0f, 1.2f);
    }

    @Override
    protected void onAttackHit() {
        // 爆発エフェクト
        mob.level().explode(mob, mob.getX(), mob.getY(), mob.getZ(), 2.0f, Level.ExplosionInteraction.NONE);
    }
}
*/
