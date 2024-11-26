package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Predicate;

public abstract class MissileBaseEntity extends ThrowableProjectile implements GeoEntity, GeoAnimatable {
    protected final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    protected int lifeTicks = 0;

    protected LivingEntity shooter = null;
    protected LivingEntity target = null;

    protected double firstYaw = -999;

    public MissileBaseEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        this(entityType, world, null, null);
    }

    public MissileBaseEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, LivingEntity target) {
        super(entityType, world);
        this.setNoGravity(true);
        this.shooter = shooter;
        this.target = target;
    }

    protected abstract int getSwitchTick();

    protected abstract double getSpeed();

    protected abstract int getSeekRange();

    protected abstract float getMaxRotationAnglePerTick();

    protected abstract float getDamage();

    protected abstract int getMaxLifeTicks();

    protected abstract Vec3 getNonTargetVelocity();

    protected abstract Class getTargetClass();

    public float getScale() {
        return 1.0F;
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            generateSmokeParticles();
        }

        super.tick();

        if (this.firstYaw == -999) {
            this.firstYaw = (-1.0) * this.getYRot();
        }

        if(this.lifeTicks++ >= getMaxLifeTicks()) {
            this.createExplosion(this.position());
            this.discard();
        }

        if (!this.level().isClientSide) {
            if (lifeTicks <= this.getSwitchTick()) {
                // 指定ティック以内はそのままの角度で進む
                this.setDeltaMovement(this.getDeltaMovement());
            }
            else {
                // 指定ティック経過後はホーミング処理に移行する
                if (target == null) {
                    target = this.findTarget();
                }

                if (target == null || target.isDeadOrDying()) {
                    var vel = this.getNonTargetVelocity();
                    this.setDeltaMovement(vel);
                } else {
                    // ホーミング処理
                    updateHomingMovement();
                }
            }
        }

        updateRotationBasedOnVelocity();
    }

    protected LivingEntity findTarget() {
        final int range = getSeekRange();
        List<Entity> candidates = this.level().getEntitiesOfClass(
                getTargetClass(),
                new AABB(this.getX() - range ,this.getY() - range ,this.getZ() - range ,
                        this.getX() + range ,this.getY() + range,this.getZ() + range),
                new MissileTargetPredicate()
        );

        for (var candidate: candidates) {
            var res = level().clip(new ClipContext( this.position(),  candidate.position(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this));
            if (res.getType().equals(HitResult.Type.MISS)) {
                return (LivingEntity) candidate;
            }
        }

        return null;
    }

    private class MissileTargetPredicate<Entity> implements Predicate<Entity> {
        @Override
        public boolean test(Entity entity) {
            return true;
        }
    }

    // ホーミング処理
    protected void updateHomingMovement() {
        Vec3 currentPosition = this.position();
        Vec3 targetPosition = target.position();

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

    // 2つのベクトルの間の角度に基づいて回転させる
    protected Vec3 rotateTowards(Vec3 currentDirection, Vec3 targetDirection, double maxRotationRadians) {
        // 球面線形補間（Slerp）を使用して、currentDirectionからtargetDirectionへのベクトルを補間
        return currentDirection.lerp(targetDirection, maxRotationRadians / Math.acos(currentDirection.dot(targetDirection)));
    }

    protected void updateRotationBasedOnVelocity() {
        Vec3 velocity = this.getDeltaMovement();

        if (!velocity.equals(Vec3.ZERO)) {
            double yaw = Math.toDegrees(Math.atan2(velocity.x, velocity.z));
            this.setYRot((float) yaw);

            double horizontalDistance = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
            double pitch = Math.toDegrees(Math.atan2(velocity.y, horizontalDistance));
            this.setXRot((float) pitch);

            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
    }

    protected void generateSmokeParticles() {
        Vec3 velocity = this.getDeltaMovement();
        Vec3 directionBackwards = velocity.normalize().scale(-0.2);

        for (int i = 0; i < 10; i++) {
            Vec3 particlePos = this.position().add(directionBackwards.scale(i));
            this.level().addAlwaysVisibleParticle(PomkotsMechs.MISSILE_SMOKE.get(), true, particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        var entity = entityHitResult.getEntity();
        if (entity instanceof LivingEntity) {
            if (entity.equals(shooter)) {
                return;
            }
            entity.hurt(entity.damageSources().thrown(this, this.getOwner() != null ? this.getOwner() : this), getDamage());
            entity.invulnerableTime = 0;
        }
        this.createExplosion(this.position());

        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        this.createExplosion(this.position());
        this.discard();
    }

    protected void createExplosion(Vec3 pos) {
        Level world = level();
        if (!world.isClientSide) {
            world.explode(this,  pos.x, pos.y, pos.z, BattleBalance.MECH_MISSILE_EXPLOSION, false, Level.ExplosionInteraction.NONE);
        } else {
            addParticles(this.position());
        }
    }

    @Override
    public void onClientRemoval() {
        if (this.level().isClientSide) {
            addParticles(this.position());
        }
    }

    private void addParticles(Vec3 offset) {
        Level world = level();
        for (int i = 0; i < 3; i++) {
            int rad = Math.abs(i - 3);

            for (int j = 0; j < 3 - rad; j++) {
                for (int k = 0; k < 3 - rad; k++) {
                    world.addParticle(
                            ParticleTypes.EXPLOSION,
                            offset.x - (k - (1 - rad/2)) * 2,
                            offset.y - (i - 1) * 2,
                            offset.z - (j - (1 - rad/2)) * 2,
                            0,0,0);
                }
            }
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rotation", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.missile.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

}
