package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb01Entity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
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

public class GrenadeEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final float DAMAGE = BattleBalance.MECH_GRENADE_DAMAGE;
    private static final int MAX_LIFE_TICKS = 80;
    private int lifeTicks = 0;
    private float explosionScale = 0;
    private LivingEntity shooter = null;

    public GrenadeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        this(entityType, world, null);
    }

    public GrenadeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
        this(entityType, world, shooter, 10);
    }

    public GrenadeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, float exprosionScale) {
        super(entityType, world);
        this.shooter = shooter;
        this.explosionScale = exprosionScale;
    }


    @Override
    public void tick() {
        // 弾速が早すぎると、ティック間にすり抜けちゃうのでレイキャスティングで補完
        var hitResult = ProjectileUtil.raycastBoundingCheck(this);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
        }

        super.tick();

        var vel = this.getDeltaMovement();
        this.setPos(this.getX() + vel.x(), this.getY() + vel.y(), this.getZ() + vel.z());

        if(this.lifeTicks++ >= MAX_LIFE_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        var entity = entityHitResult.getEntity();
        if (entity.equals(shooter)) {
            return;
        }

        entity.hurt(entity.damageSources().thrown(this, this.getOwner() != null ? this.getOwner() : this), DAMAGE);
        entity.invulnerableTime = 0;

        this.createExplosion(entityHitResult.getLocation());
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        this.createExplosion(blockHitResult.getLocation());
//        this.createExplosionKujira(blockHitResult.getLocation());
        this.discard();
    }


    private void createExplosionKujira(Vec3 pos) {
        Level world = level();

        if (!world.isClientSide) {
            var level = this.level();
            KujiraEntity e = new KujiraEntity(PomkotsMechs.KUJIRA.get(), level);
            e.setPos(this.position());
            level.addFreshEntity(e);
        }
    }

    private void createExplosion(Vec3 pos) {
        Level world = level();
        if (!world.isClientSide) {
            if (shooter instanceof Pmb01Entity && ProjectileUtil.isDestructionAllowed(this)) {
                world.explode(this,  pos.x, pos.y, pos.z, explosionScale, false, Level.ExplosionInteraction.BLOCK);

            } else {
                world.explode(this,  pos.x, pos.y, pos.z, explosionScale, false, Level.ExplosionInteraction.NONE);
            }
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
