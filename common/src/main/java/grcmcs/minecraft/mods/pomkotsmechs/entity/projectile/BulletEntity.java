package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

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

public class BulletEntity extends ThrowableProjectile implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final float DAMAGE = BattleBalance.MECH_GATLING_DAMAGE;
    private static final int MAX_LIFE_TICKS = 30;
    private int lifeTicks = 0;
    private LivingEntity shooter = null;

    public BulletEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    public BulletEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
        this(entityType, world);
        this.shooter = shooter;
    }

    @Override
    public void tick() {
        this.setNoGravity(true);

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

        float damage = DAMAGE;

        if (lifeTicks < 10) {
            damage += (10 - (float)lifeTicks) * DAMAGE / 20;
        }

        entity.hurt(entity.damageSources().thrown(this, this.getOwner() != null ? this.getOwner() : this), damage);
        entity.invulnerableTime = 0;

        this.discard();
    }


    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        this.discard();
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rotation", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.bullet.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

}
