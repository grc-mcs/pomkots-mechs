package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ProjectileUtil;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BulletGrenadeEntity extends PomkotsCustomThrowableProjectile implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int explosionScale = 10;

    public BulletGrenadeEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        this(entityType, level, null, 80, BattleBalance.MECH_BULLET_GRENADE_DAMAGE, 15);
    }

    public BulletGrenadeEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, float damage) {
        this(entityType, level, shooter, 80, damage, 20);
    }

    public BulletGrenadeEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, int maxLifeTicks, float damage, int stun) {
        super(entityType, level, shooter,
                maxLifeTicks, damage,stun);
    }

    public void setExplosionScale(int scale) {
        this.explosionScale = scale;
    }

    @Override
    public void tick() {
        super.tick();
        this.hasImpulse = true;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        this.createExplosion(entityHitResult.getLocation());
        entityHitResult.getEntity().invulnerableTime = 0;

        super.onHitEntity(entityHitResult);
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        this.createExplosion(blockHitResult.getLocation());
        super.onHitBlock(blockHitResult);
    }

    private void createExplosion(Vec3 pos) {
        Level world = level();

        if (Utils.isBlockDestructionAllowed(shooter)) {
            Utils.explode(this,  pos.x, pos.y, pos.z, explosionScale, false, Level.ExplosionInteraction.BLOCK, this.level());
        } else {
            Utils.explode(this,  pos.x, pos.y, pos.z, explosionScale, false, Level.ExplosionInteraction.NONE, this.level());
        }

        //        if (!world.isClientSide) {
//            if (Utils.isBlockDestructionAllowed(shooter)) {
//                Utils.explode(this,  pos.x, pos.y, pos.z, explosionScale, false, Level.ExplosionInteraction.BLOCK, this.level());
//            } else {
//                Utils.explode(this,  pos.x, pos.y, pos.z, explosionScale, false, Level.ExplosionInteraction.NONE, this.level());
//            }
//        } else {
//            addParticles(this.position());
//        }
    }

    @Override
    public void onClientRemoval() {
        if (this.level().isClientSide) {
            var pos = this.position();
            if (!(this.explosionScale < 2.0F)) {
                this.level().addParticle(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 1.0, 0.0, 0.0);
            } else {
                this.level().addParticle(ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 1.0, 0.0, 0.0);
            }
        }
    }

    private void addParticles(Vec3 offset) {
        Level world = level();
        for (int i = 0; i < 5; i++) {
            int rad = Math.abs(i - 3);

            for (int j = 0; j < 5 - rad; j++) {
                for (int k = 0; k < 5 - rad; k++) {
                    world.addParticle(
                            ParticleTypes.EXPLOSION,
                            offset.x - (k - (3 - rad/2)) * 2,
                            offset.y - (i - 3) * 2,
                            offset.z - (j - (3 - rad/2)) * 2,
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
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.grenade.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
