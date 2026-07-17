package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
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

public class MissileGenericEntity extends PomkotsCustomThrowableProjectile implements GeoEntity, GeoAnimatable {
    protected final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected float speed;
    protected float maxRotationAnglePerTick = 3;

    protected LivingEntity target = null;

    protected int switchTick;
    private int remainingTicks = 40;
    private boolean terminalPhase =  false;
    private boolean straight = false;

    private Vec3 prevClientPos = null;

    private float totalRotatedDeg = 0f;        // 累積回転角
    private Vec3 lastDirection = null;          // 前tickの方向
    private float maxTotalRotationDeg = 200f;
    private boolean rotationLimitReached = false;

    public MissileGenericEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        this(entityType, level, null, null);
    }

    public MissileGenericEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, LivingEntity target) {
        this(entityType, level, shooter, target, BattleBalance.MECH_MISSILE_DAMAGE, 1F);
    }

    public MissileGenericEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, LivingEntity target, float damage, float speed) {
        this(entityType, level, shooter, target, damage, speed, 80, 2);
    }

    public MissileGenericEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, LivingEntity target, float damage, float speed, int maxLifeTicks, int stun) {
        super(entityType, level, shooter,
                maxLifeTicks, damage, stun);

        this.setNoGravity(true);
        this.shooter = shooter;
        this.target = target;
        this.damage = damage;
        this.speed = speed;
    }

    public float getScale() {
        return 1.0F;
    }

    @Override
    public void tick() {
        this.setOldPosAndRot();

        if (level().isClientSide) {
            clientTick();
            return;
        }

        if (this.lifeTicks++ >= this.maxLifeTicks) {
            this.createExplosion(this.position());
            this.discard();
            return;
        }

        this.checkInsideBlocks();

        if (!performSubStepMovement()) {
            return;
        }

        if (target == null || !target.isAlive() || straight) {
            moveStraight();
            this.setPos(this.position().add(this.getDeltaMovement()));
            this.hasImpulse = true;

            return;
        }

        if (lifeTicks > 3) {
            homingUpdate();
        }

        this.setPos(this.position().add(this.getDeltaMovement()));

        this.hasImpulse = true;
    }

    @Override
    protected void setPosInSubStep(Vec3 pos) {
    }

    public void setStraight(boolean value) {
        this.straight = value;
    }

    public boolean getStraight() {
        return this.straight;
    }

    private void moveStraight() {
        Vec3 velocity = this.getDeltaMovement();

        if (velocity.lengthSqr() < 1e-4) {
            float yaw = this.getYRot();
            float pitch = this.getXRot();

            Vec3 forward = Vec3.directionFromRotation(pitch, yaw);
            velocity = forward.scale(1.2);
        }

        double maxSpeed = getSpeed();
        if (velocity.length() > maxSpeed) {
            velocity = velocity.normalize().scale(maxSpeed);
        }

        this.setDeltaMovement(velocity);

        updateRotationFromVelocity(velocity);
    }

    private void homingUpdate() {
        Vec3 position = this.position();
        Vec3 velocity = this.getDeltaMovement();

        Vec3 targetPos = target.getBoundingBox().getCenter();
        Vec3 diff = targetPos.subtract(position);

        // 回転制限チェック
        if (rotationLimitReached || isRotationLimitExceeded(velocity)) {
            rotationLimitReached = true;
            moveStraight();
            return;
        }
        // ★ 近距離で誘導終了
//        double distance = diff.length();
//        double terminalDistance = 10;
//        if (distance < terminalDistance) {
//            terminalPhase = true;
//        }
        if (lifeTicks > 60) {
            terminalPhase = true;
        }

        if (!terminalPhase || target instanceof BaseSmallMonsterEntity) {
            float period = Math.max(remainingTicks / 20.0f, 0.05f);

            float homingRate = 2.0F;
            if (target instanceof BaseSmallMonsterEntity) {
                homingRate = 7;
            }

            Vec3 acceleration = diff
                    .subtract(velocity.scale(period))
                    .scale(homingRate / (period * period));

            double maxAcc = getSpeed() * 8.0;

            if (acceleration.length() > maxAcc) {
                acceleration = acceleration.normalize().scale(maxAcc);
            }

            double dt = 1.0 / 20.0;
            velocity = velocity.add(acceleration.scale(dt));
        }

        double maxSpeed = getSpeed();

        if (velocity.length() > maxSpeed) {
            velocity = velocity.normalize().scale(maxSpeed);
        }

        this.setDeltaMovement(velocity);

        updateRotationFromVelocity(velocity);

        remainingTicks--;
    }

    private boolean isRotationLimitExceeded(Vec3 currentVelocity) {
        if (currentVelocity.lengthSqr() < 1e-4) return false;

        Vec3 currentDir = currentVelocity.normalize();

        if (lastDirection != null) {
            double dot = Mth.clamp(lastDirection.dot(currentDir), -1.0, 1.0);
            float deltaDeg = (float) Math.toDegrees(Math.acos(dot));
            totalRotatedDeg += deltaDeg;
        }

        lastDirection = currentDir;

        return totalRotatedDeg >= maxTotalRotationDeg;
    }

    protected void updateRotationFromVelocity(Vec3 vel) {
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
        super.onHitEntity(entityHitResult);
        this.createExplosion(this.position());

        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        this.createExplosion(this.position());
        this.discard();
    }

    public void clientTick() {
        Vec3 currentPos = this.position();

        if (prevClientPos != null) {
            spawnSmokeBetween(prevClientPos, currentPos);
        }

        prevClientPos = currentPos;
    }

    private void spawnSmokeBetween(Vec3 from, Vec3 to) {
        Level level = this.level();

        Vec3 diff = to.subtract(from);
        double length = diff.length();

        if (length < 0.001) return;

        Vec3 dir = diff.normalize();

        // ★ 間隔（ブロック単位）
        double spacing = 0.5;

        int count = (int)(length / spacing);

        for (int i = 0; i <= count; i++) {
            double d = i * spacing;
            Vec3 pos = from.add(dir.scale(d));

            level.addAlwaysVisibleParticle(
                    PomkotsMechs.MISSILE_SMOKE.get(),
                    true,
                    pos.x, pos.y, pos.z,
                    0.0, 0.0, 0.0);
        }
    }

    protected void createExplosion(Vec3 pos) {
        Level world = level();
        if (world.isClientSide) {
            addParticles(4F, this.position());
        } else {
            if (Utils.isBlockDestructionAllowed(shooter)) {
                Utils.explode(this,  pos.x, pos.y, pos.z, BattleBalance.MECH_MISSILE_EXPLOSION, false, Level.ExplosionInteraction.BLOCK, this.level());
            } else {
                Utils.explode(this,  pos.x, pos.y, pos.z, BattleBalance.MECH_MISSILE_EXPLOSION, false, Level.ExplosionInteraction.NONE, this.level());
            }
        }
    }

    protected void addParticles(float scale, Vec3 offset) {
        var level = this.level();

        if (!(scale < 2.0F)) {
            level.addParticle(ParticleTypes.EXPLOSION_EMITTER, offset.x, offset.y, offset.z, 1.0, 0.0, 0.0);
        } else {
            level.addParticle(ParticleTypes.EXPLOSION, offset.x, offset.y, offset.z, 1.0, 0.0, 0.0);
        }

//        Level world = level();
//        for (int i = 0; i < 3; i++) {
//            int rad = Math.abs(i - 3);
//
//            for (int j = 0; j < 3 - rad; j++) {
//                for (int k = 0; k < 3 - rad; k++) {
//                    world.addParticle(
//                            ParticleTypes.EXPLOSION,
//                            offset.x - (k - (1 - rad/2)) * 2,
//                            offset.y - (i - 1) * 2,
//                            offset.z - (j - (1 - rad/2)) * 2,
//                            0,0,0);
//                }
//            }
//        }
    }

    @Override
    public void onClientRemoval() {
        if (this.level().isClientSide) {
            addParticles(1F, this.position());
        }
    }

    public LivingEntity getShooter() {
        return shooter;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rotation", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation.missile.start").thenPlayAndHold("animation.missile.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public float getHitDamage() {
        return this.getDamage();
    }

    protected int getSwitchTick() {
        return switchTick;
    }

    public void setSwitchTick(int val) {
        switchTick = val;
    }

    protected double getSpeed() {
        return this.speed;
    }

    protected int getSeekRange() {
        return 100;
    }

    protected float getMaxRotationAnglePerTick() {
        if (lifeTicks == getSwitchTick() + 1) {
            return 90;
        }

        return maxRotationAnglePerTick;
    }

    public float setMaxRotationAnglePerTick(float angle) {
        return maxRotationAnglePerTick = angle;
    }

    protected float getDamage() {
        return damage;
    }

    protected int getMaxLifeTicks() {
        return maxLifeTicks;
    }

    public void setMaxLifeTick(int maxLifeTick) {
        this.maxLifeTicks = maxLifeTick;
    }

    protected Vec3 getNonTargetVelocity() {
        return this.getDeltaMovement();
    }

    protected Class getTargetClass() {
        return LivingEntity.class;
    }
}
