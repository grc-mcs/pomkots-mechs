package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
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

import java.util.ArrayList;
import java.util.List;

public class MissilePodEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final int MAX_LIFE_TICKS = 60;
    private int lifeTicks = 0;

    private LivingEntity shooter = null;
    private LivingEntity target = null;
    private float damage = 0;
    private float speed = 0;

    private Mode mode = Mode.DOWN;

    public enum Mode {
        DOWN,
        SPHERE
    }

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

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        this.hasImpulse = true;

        switch (mode) {
            case DOWN -> {
                if (lifeTicks >= 10
                        && lifeTicks % 5 == 0
                ) {
                    this.playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get());
                    for (int row = 0; row < 2; row++) {
                        for (int col = 0; col < 2; col++) {
                            for (int side = -1; side < 2; side += 2) {
                                MissileGenericEnemyEntity be = new MissileGenericEnemyEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), shooter, target, damage, speed);
                                be.setStraight(true);

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
            }

            case SPHERE -> {
                if (Utils.isInRangeOnAxisXZ(this, target, 20)) {
                    this.playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get());
                    this.shootSphericalMissiles(this.level(),
                            this.position(),
                            this.shooter,
                            this.target,
                            this.damage,
                            this.speed,
                            10);
                    this.discard();
                }
            }
        }


        updateRotationBasedOnVelocity();

        if(this.lifeTicks++ >= MAX_LIFE_TICKS) {
            this.discard();
        }
    }

    public void shootSphericalMissiles(
            Level level,
            Vec3 centerPos,
            LivingEntity shooter,
            LivingEntity target,
            Float damage,
            Float speed,
            int count) {

        if (level.isClientSide) {
            return;
        }

        List<Vec3> directions = calculateSphereDirections(count);

        for (Vec3 direction : directions) {
            MissileGenericEnemyEntity missile = new MissileGenericEnemyEntity(PomkotsMechs.MISSILE_GENERIC.get(), level, shooter, target, damage, speed);
            missile.setStraight(true);

            missile.setPos(centerPos);
            Vec3 velocity = direction.normalize().scale(speed);
            missile.setDeltaMovement(velocity);

            level.addFreshEntity(missile);
        }
    }

    private static List<Vec3> calculateSphereDirections(int count) {
        List<Vec3> directions = new ArrayList<>();

        double goldenRatio = (1.0 + Math.sqrt(5.0)) / 2.0;
        double angleIncrement = 2.0 * Math.PI * goldenRatio;

        for (int i = 0; i < count; i++) {
            // 緯度（-1 ~ 1）
            double t = (double) i / (count - 1);
            double inclination = Math.acos(1.0 - 2.0 * t);

            // 経度
            double azimuth = angleIncrement * i;

            // 球面座標→直交座標
            double x = Math.sin(inclination) * Math.cos(azimuth);
            double y = Math.sin(inclination) * Math.sin(azimuth);
            double z = Math.cos(inclination);

            directions.add(new Vec3(x, y, z));
        }

        return directions;
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
        if (!this.level().isClientSide && this.mode == Mode.SPHERE) {
            this.shootSphericalMissiles(this.level(),
                    this.position(),
                    this.shooter,
                    this.target,
                    this.damage,
                    this.speed,
                    10);
        }
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
