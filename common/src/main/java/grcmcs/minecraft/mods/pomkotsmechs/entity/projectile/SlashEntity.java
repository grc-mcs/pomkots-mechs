package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SlashEntity extends ThrowableProjectile implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final int MAX_LIFE_TICKS = 40;
    private int lifeTicks = 0;

    public SlashEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        this.setNoGravity(true);
        super.tick();

        if(this.lifeTicks++ >= MAX_LIFE_TICKS) {
            this.discard();
        } else if (this.lifeTicks == 5) {
            this.generateLava();
        }

        updateRotationBasedOnVelocity();
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

    private void generateLava() {
        if (this.level().isClientSide) {
            RandomSource random = this.level().getRandom();

            // 100個のLavaパーティクルをバラまく
            for (int i = 0; i < 20; i++) {
                // ランダムな速度を生成
                double velocityX = random.nextDouble() * 2.0 - 1;
                double velocityY = random.nextDouble() * 2.0 - 1;
                double velocityZ = random.nextDouble() * 2.0 - 1;

                // パーティクルをクライアント側で発生させる
                this.level().addParticle(ParticleTypes.LAVA,
                        this.getX(), this.getY(), this.getZ(), // 位置
                        velocityX, velocityY, velocityZ // 速度
                );
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        var entity = entityHitResult.getEntity();
        if (entity instanceof LivingEntity) {
            entity.hurt(entity.damageSources().thrown(this, this.getOwner() != null ? this.getOwner() : this), BattleBalance.BOSS_EXPLOADSLASH_DAMAGE);
            entity.invulnerableTime = 20;

            Vec3 velocity = this.getDeltaMovement();

            var kbVel = new Vec3(velocity.x, 0, velocity.z).normalize();
            ((LivingEntity) entity).knockback(2, kbVel.x, kbVel.z);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {

    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rotation", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.slash.new"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

}
