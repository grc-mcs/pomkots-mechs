package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BulletMachineEntity extends PomkotsCustomDecayedThrowableProjectile implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public BulletMachineEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        this(entityType, level, null, 20, BattleBalance.MECH_MACHINEGUN_LARGE_DAMAGE, 2);
    }

    public BulletMachineEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, float damage) {
        this(entityType, level, shooter, 20, damage, 1);
    }

    public BulletMachineEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, int maxLifeTicks, float damage, int stun) {
        super(entityType, level, shooter,
                maxLifeTicks, damage,stun);

        this.setNoGravity(true);
        this.noCulling = true;
        this.noPhysics = true;
        this.shooter = shooter;
        this.damage = damage;
    }

    @Override
    public void tick() {
        super.tick();
        this.hasImpulse = true;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
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

    @Override
    public RangeCategory calcRangeCategory(int distance) {
        return calcRangeCategoryStatic(distance);
    }

    @Override
    protected float getDamageModifier(RangeCategory range) {
        return switch (range) {
            case OPTIMAL -> 1.0F;
            case EFFECTIVE -> 0.8F;
            case MAXIMUM -> 0.5F;
            case OUT -> 0.1F;
            default -> 1F;
        };
    }

    public static RangeCategory calcRangeCategoryStatic(int distance) {
        if (distance < 50) {
            return RangeCategory.OPTIMAL;
        } else if (distance < 80) {
            return RangeCategory.EFFECTIVE;
        } else if (distance < 200){
            return RangeCategory.MAXIMUM;
        } else {
            return RangeCategory.OUT;
        }
    }
}
