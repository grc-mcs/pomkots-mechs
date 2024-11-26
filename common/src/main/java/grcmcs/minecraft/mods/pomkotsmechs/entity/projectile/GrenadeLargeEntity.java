package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
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

public class GrenadeLargeEntity extends ThrowableProjectile implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final float DAMAGE = BattleBalance.BOSS_GRENADE_DAMAGE;
    private static final int MAX_LIFE_TICKS = 80;
    private int lifeTicks = 0;
    private float explosionScale = 0;
    private LivingEntity shooter = null;

    public GrenadeLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        this(entityType, world, null);
    }

    public GrenadeLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
        this(entityType, world, shooter, 10);
    }

    public GrenadeLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, float exprosionScale) {
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
        this.discard();
    }

    private void createExplosion(Vec3 pos) {
        Level world = level();
        if (!world.isClientSide) {
            if (ProjectileUtil.isDestructionAllowed(this)) {
                world.explode(this,  pos.x, pos.y, pos.z, explosionScale, false, Level.ExplosionInteraction.BLOCK);
            } else {
                world.explode(this,  pos.x, pos.y, pos.z, explosionScale, false, Level.ExplosionInteraction.NONE);
            }

            ExplosionEntity e = new ExplosionEntity(PomkotsMechs.EXPLOSION.get(), world);
            e.setPos(this.position());
            world.addFreshEntity(e);
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
