package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileBaseEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ProjectileUtil;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
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

public class MissilePodEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final int MAX_LIFE_TICKS = 60;
    private int lifeTicks = 0;

    private LivingEntity shooter = null;
    private LivingEntity target = null;
    private float damage = 0;
    private float speed = 0;

    public MissilePodEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        this(entityType, world, null, null, BattleBalance.MECH_MISSILE_DAMAGE, BattleBalance.MECH_MISSILE_GENERIC_SPEED);
    }

    public MissilePodEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, LivingEntity target, float damage, float speed) {
        super(entityType, shooter, world);
        this.shooter = shooter;
        this.target = target;
        this.damage = damage;
        this.speed = speed;
    }

    @Override
    public void tick() {
        super.tick();
        this.hasImpulse = true;

        if (!this.level().isClientSide && (lifeTicks == MAX_LIFE_TICKS - 40 || (lifeTicks % 5 == 0 && Utils.isInRangeOnAxisXZ(this, target, 40)))) {
            Utils.playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get(),this);
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    for (int side = -1; side < 2; side += 2) {
                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), shooter, target, damage, speed);
                        be.setMaxRotationAnglePerTick(2);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotZ = (col * 0.8F + 1.5F);
                        float slotY = row + 0.8F;

                        var muzzlPos = new Vec3(side * 3, slotY, slotZ);
                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                        be.setPos(offset.add(muzzlPos));
                        be.shootFromRotation(be, 70, this.getYRot() -10 + 20 * side, 0, speed, 0F);

                        this.level().addFreshEntity(be);
                    }
                }
            }
        }

        updateRotationBasedOnVelocity();

        if(this.lifeTicks++ >= MAX_LIFE_TICKS) {
            this.discard();
        }
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

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
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
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.missilepod.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
