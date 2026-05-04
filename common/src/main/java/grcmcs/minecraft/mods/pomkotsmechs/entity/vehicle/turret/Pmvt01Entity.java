package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.turret;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;

public class Pmvt01Entity extends PomkotsVehicleBase {

    protected static final int ACT_MAIN_WEAPON = 0;

    public Pmvt01Entity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
    }

    @Override
    protected void registerActions() {
        this.actionController.registerAction(ACT_MAIN_WEAPON, new Action(0, 2, 12), ActionController.ActionType.R_ARM_MAIN);
    }

    @Override
    public void tick() {
        super.tick();

    }

    private int muzzleSide = -1;

    protected void applyPlayerInput(DriverInput driverInput) {
        getUserIntentionForDirectionFromKey(driverInput);

        if (driverInput.isWeaponRightHandPressed()) {
            this.actionController.getAction(ACT_MAIN_WEAPON).startAction();
        }
    }

    @Override
    protected void fireWeapons() {
        var world = this.level();

        if (actionController.getAction(ACT_MAIN_WEAPON).isOnStart() && this.isServerSide()) {
            muzzleSide = -1;
        }
        if (actionController.getAction(ACT_MAIN_WEAPON).isOnFire()) {
            if (this.isServerSide()) {
                BulletGrenadeEntity be = new BulletGrenadeEntity(PomkotsMechs.BULLET_MACHINE_LARGE.get(), world, this, 40);
                be.setNoGravity(true);
                be.setExplosionScale(2);

                var offset = this.position();

                // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                var muzzlPos = new Vec3(0.5 * muzzleSide, 1, 2F);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
                be.setPos(offset.add(muzzlPos));

                float[] angle = this.getShootingAngle(be, false);

                be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), BattleBalance.MECH_BULLET_GRENADE_SPEED, 2F);

                world.addFreshEntity(be);

                muzzleSide *= -1;
            }
        }
    }

    protected float[] getShootingAngle(Entity bullet, boolean useLockTarget) {
        double xRot = 0;
        double yRot = 0;

        var lockTarget = (lockTargets.getLockTargetHard() != null)?lockTargets.getLockTargetHard():lockTargets.getLockTargetSoft();
        if (useLockTarget && lockTarget != null) {
            Vec3 positionA = bullet.position();
            // エンティティBの位置を取得
            Vec3 positionB = lockTarget.getBoundingBox().getCenter();

            // エンティティAからエンティティBへの相対ベクトル
            double deltaX = positionB.x - positionA.x;
            double deltaY = positionB.y - positionA.y;
            double deltaZ = positionB.z - positionA.z;

            // 水平角度 (Yaw) の計算 (XZ平面上の角度)
            yRot = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0; // 90度引いて北基準に

            // 垂直角度 (Pitch) の計算 (Y軸方向の角度)
            double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ); // 水平方向の距離
            xRot = -Math.toDegrees(Math.atan2(deltaY, distanceXZ)); // Y

        } else {
            var driver = this.getDrivingPassenger();
            var lookAngle = driver.getLookAngle();

            xRot = -Math.toDegrees(Math.asin(lookAngle.y)) - 3.5;
            yRot = Math.toDegrees(Math.atan2(lookAngle.z, lookAngle.x)) - 90.0;
        }

        return new float[]{(float)xRot, (float)yRot};
    }

    @Override
    public void travel(Vec3 pos) {
        if (this.isAlive() && this.isVehicle() && this.canWork()) {
            var pilot = this.getDrivingPassenger();

            // ROTATE Vehicle
            this.setYRot(pilot.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(pilot.getXRot() * 0.5F);
            this.setRot(this.getYRot(), this.getXRot());
            this.setYBodyRot(this.getYRot());
            this.setYHeadRot(this.getYRot());

            super.travelBypass(pos);
            this.hasImpulse = true;
        } else {
            super.travel(pos);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<PomkotsVehicleBase>(this, "controller", 0, event -> {
            if (this.actionController.getAction(ACT_MAIN_WEAPON).isInAction()) {
                if (this.actionController.getAction(ACT_MAIN_WEAPON).isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmt01.attack"));
            }

            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pmt01.idle"));
        }).setSoundKeyframeHandler(soundKeyframeEvent -> {
            this.registerAnimationSoundHandlers(soundKeyframeEvent);
        }));
    }

    protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
        if ("small_canon".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_SHOTGUN.get());
        }
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.5F;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
        return false;
    }
    @Override
    protected String getMechName() {
        return "pmvt01";
    }
}
