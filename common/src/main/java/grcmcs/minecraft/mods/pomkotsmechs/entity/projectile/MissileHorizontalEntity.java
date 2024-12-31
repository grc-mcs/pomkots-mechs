package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public class MissileHorizontalEntity extends MissileBaseEntity {
    public MissileHorizontalEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    public MissileHorizontalEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, LivingEntity target) {
        this(entityType, world);
        this.shooter = shooter;
        this.target = target;
    }

    @Override
    protected LivingEntity findTarget() {
        final int range = getSeekRange();
        List<Entity> candidates = this.level().getEntitiesOfClass(
                getTargetClass(),
                new AABB(this.getX() - range ,this.getY() - range ,this.getZ() - range ,
                        this.getX() + range ,this.getY() + range,this.getZ() + range),
                new MissileTargetPredicate()
        );

        return (LivingEntity) getBestMatch(candidates, this);
    }

    public Entity getBestMatch(List<Entity> entities, Entity currentEntity) {
        Entity bestMatch = null; // 最適なエンティティ
        double bestScore = Double.MAX_VALUE; // 最適スコアの初期値 (小さいほど良い)

        for (Entity entity : entities) {
            if (entity == shooter || entity.getVehicle() == shooter || entity == currentEntity) continue;

            // 高さの差を計算
            double heightDifference = Math.abs(entity.getY() - currentEntity.getY());
            // 距離を計算
            double distance = entity.distanceTo(currentEntity);

            // スコア計算: 高さの差を優先しつつ、次に距離を考慮
            double score = heightDifference * 100 + distance;

            // 現在の最良スコアと比較
            if (score < bestScore) {
                bestScore = score;
                bestMatch = entity;
            }
        }

        return bestMatch;
    }

    private class MissileTargetPredicate<LivingEntity> implements Predicate<LivingEntity> {
        @Override
        public boolean test(LivingEntity entity) {
            return true;
        }
    }

    // ホーミング処理
    protected void updateHomingMovement() {
        Vec3 currentPosition = this.position();
        Vec3 targetPosition = target.position().add(0, target.getBbHeight() / 2, 0);

        // 現在の進行方向とターゲット方向を計算
        Vec3 currentVelocity = this.getDeltaMovement().normalize();
        Vec3 directionToTarget = targetPosition.subtract(currentPosition).normalize();

        // 角度差を計算（ラジアン）
        double angleBetween = Math.acos(currentVelocity.dot(directionToTarget));

        // 最大回転角度（ラジアンに変換）
        double maxRotationRadians = Math.toRadians(getMaxRotationAnglePerTick());

        // ターゲット方向に向かうための回転角を制限
        Vec3 newVelocity;
        if (angleBetween > maxRotationRadians) {
            // 最大回転角度まで回転する
            newVelocity = rotateTowards(currentVelocity, directionToTarget, maxRotationRadians);
        } else {
            // 角度が許容範囲内であれば、ターゲットに向けて直接進む
            newVelocity = directionToTarget;
        }

        // 新しい速度ベクトルに基づいて進行方向を更新
        this.setDeltaMovement(newVelocity.scale(this.getSpeed()));
    }

    protected int getSwitchTick() {
        return 7;
    }

    protected double getSpeed() {
        return 2;
    }

    protected int getSeekRange() {
        return 40;
    }

    protected float getMaxRotationAnglePerTick() {
        return 10;
    }

    protected float getDamage() {
        return 60;
    }

    protected int getMaxLifeTicks() {
        return 120;
    }

    protected Vec3 getNonTargetVelocity() {
        return this.getDeltaMovement();
    }

    protected Class getTargetClass() {
        return LivingEntity.class;
    }
}
