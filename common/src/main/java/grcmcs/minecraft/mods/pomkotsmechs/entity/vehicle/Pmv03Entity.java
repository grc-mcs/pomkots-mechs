package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BulletEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.GrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileVerticalEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletRifleEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;

import java.util.List;

public class Pmv03Entity extends PomkotsVehicleBase {
    @Override
    protected String getMechName() {
        return "pmv01";
    }

    public static float DEFAULT_SCALE = 2.0F;

    public Pmv03Entity(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    protected static final int ACT_HUMMER = 2;
    protected static final int ACT_SCOP = 3;
    protected static final int ACT_R_RIFLE = 4;
    protected static final int ACT_L_RIFLE = 5;
    protected static final int ACT_GRENADE = 6;
    protected static final int ACT_MISSILE = 7;

    @Override
    protected void registerActions() {
        super.registerActions();
        this.actionController.registerAction(ACT_R_RIFLE, new Action(20, 6, 8), ActionController.ActionType.R_ARM_MAIN);
        this.actionController.registerAction(ACT_L_RIFLE, new Action(20, 6, 8), ActionController.ActionType.L_ARM_MAIN);
        this.actionController.registerAction(ACT_MISSILE, new Action(60, 0, 20), ActionController.ActionType.R_SHL_MAIN);
    }

    @Override
    protected void applyPlayerInputWeapons(DriverInput driverInput) {
        if (this.isMainMode()) {
            if (driverInput.isWeaponRightHandPressed()) {
                this.actionController.getAction(ACT_R_RIFLE).startAction();
            } else if (driverInput.isWeaponLeftHandPressed()) {
                this.actionController.getAction(ACT_L_RIFLE).startAction();
            } else if (this.lockTargets.consumeMultiLockComplete()) {
                this.actionController.getAction(ACT_MISSILE).startAction();
            }
        } else {
        }
    }

    @Override
    protected void fireWeapons() {
        Level level = level();

        if (actionController.getAction(ACT_R_RIFLE).isOnFire()) {
            this.fireRifle(level, -1);
        } else if (actionController.getAction(ACT_L_RIFLE).isOnFire()) {
            this.fireRifle(level, 1);
        } else if (actionController.getAction(ACT_MISSILE).isInFire()) {
            if (actionController.getAction(ACT_MISSILE).currentFireTime % 3 == 0) {
                this.fireMissile(level, actionController.getAction(ACT_MISSILE).currentFireTime / 3 - 1);
            }
        }
    }

    public void fireRifle(Level level, int isRight) {
        if (!level.isClientSide()) {
            BulletRifleEntity be = new BulletRifleEntity(PomkotsMechs.BULLET_RIFLE.get(), level, this, 50F);

            var offset = posHistory.getFirst();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(2.8 * isRight, 6.0F, 5F);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            be.setPos(offset.add(muzzlPos));

            float[] angle = this.getShootingAngle(be, true);

            be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), BattleBalance.MECH_RIFLE_SPEED, 0);

            level.addFreshEntity(be);
        } else {
            this.playSoundEffect(PomkotsMechs.SE_RIFLE.get());
        }
    }

    private void fireMissile(Level world, int slot) {
        if (!world.isClientSide()) {
            // 原因不明なんだけど、getPosした時の座標と、レンダリングされてる座標で3tick分ぐらい乖離がある気配がする
            // ので、3tick前の座標をオフセットにする
            // なんかaddVelocity周りが悪さしてる…？
            var offset = posHistory.getFirst();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(-1.5, 11F, 2F);
            var worldMuzzlPos = muzzlPos.add(0, 0, -slot * 0.3).yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            LivingEntity target = null;

            List<Entity> lockTargetsMulti = lockTargets.getLockTargetMulti();

            if (!lockTargetsMulti.isEmpty()) {
                var lockNum = lockTargetsMulti.size();
                int idx = slot;
                if (lockNum <= slot) {
                    idx = slot % lockNum;
                }
                target = (LivingEntity)lockTargetsMulti.get(idx);
            }

            MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), world, this, (LivingEntity) target, 20, BattleBalance.MECH_MISSILE_GENERIC_SPEED);

            be.setPos(offset.add(worldMuzzlPos));

            be.shootFromRotation(be, -10, this.getYRot(), this.getFallFlyingTicks(), 1F, 0F);

            world.addFreshEntity(be);

            if (slot == 5) {
                lockTargets.clearLockTargetsMulti();
            }
        } else {
            if (slot == 0) {
                playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get());
            }

            lockTargets.clearLockTargetsMulti();
        }
    }

    @Override
    protected PlayState controllAnimationWeapons(AnimationState<PomkotsVehicleBase> event) {
        return null;
    }

    @Override
    protected void addExtraAnimationController(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "gatling", 0, event -> {
            if (this.actionController.getAction(ACT_R_RIFLE).isInAction()) {
                if (this.actionController.getAction(ACT_R_RIFLE).isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".w_rifle_right"));
            } else if (this.actionController.getAction(ACT_L_RIFLE).isInAction()) {
                if (this.actionController.getAction(ACT_L_RIFLE).isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".w_rifle_left"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
            }
        }));

        controllers.add(new AnimationController<>(this, "weapons", 0, event -> {
            if (this.actionController.getAction(ACT_MISSILE).isInAction()) {
                if (this.actionController.getAction(ACT_MISSILE).isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".weapon_missile"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
            }
        }));
    }

    @Override
    protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
        if ("se_jump".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_JUMP_EVENT.get());
        } else if ("se_booster".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_BOOSTER_EVENT.get());
        } else if ("se_onground".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_JUMP_EVENT.get());
        }
    }

    protected Vec3 mainCameraPosition = null;

    public Vec3 getMainCameraPosition() {
        if (mainCameraPosition == null) {
            return Vec3.ZERO;
        } else {
            return mainCameraPosition;
        }
    }

    public void setMainCameraPosition(Vec3 pos) {
        this.mainCameraPosition = pos;
    }

    @Override
    public boolean shouldLockMulti(DriverInput driverInput) {
        return driverInput.isWeaponRightShoulderPressed();
    }

    @Override
    public boolean shouldLockWeak(DriverInput driverInput) {
        return driverInput.isWeaponRightHandPressed() || driverInput.isWeaponLeftHandPressed();
    }

    @Override
    public boolean shouldLockStrong(DriverInput driverInput) {
        return driverInput.isLockPressed();
    }


    @Override
    public double getPassengersRidingOffset() {
        return 5F;
    }
}
