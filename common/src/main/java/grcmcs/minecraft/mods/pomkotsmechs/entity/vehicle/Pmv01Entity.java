package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BulletEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.GrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileVerticalEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
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

public class Pmv01Entity extends PomkotsVehicleBase {
    @Override
    protected String getMechName() {
        return "pmv01";
    }

    public Pmv01Entity(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    protected static final int ACT_HUMMER = 2;
    protected static final int ACT_SCOP = 3;
    protected static final int ACT_GATRING = 4;
    protected static final int ACT_PILE = 5;
    protected static final int ACT_GRENADE = 6;
    protected static final int ACT_MISSILE = 7;

    @Override
    protected void registerActions() {
        super.registerActions();
        this.actionController.registerAction(ACT_HUMMER, new Action(20, 12, 8), ActionController.ActionType.R_ARM_SUB);
        this.actionController.registerAction(ACT_SCOP, new Action(20, 12, 8), ActionController.ActionType.L_ARM_SUB);
        this.actionController.registerAction(ACT_GATRING, new Action(0, 7, 2), ActionController.ActionType.R_ARM_MAIN);
        this.actionController.registerAction(ACT_PILE, new Action(20, 10, 10), ActionController.ActionType.L_ARM_MAIN);
        this.actionController.registerAction(ACT_GRENADE, new Action(60, 4, 10), ActionController.ActionType.L_SHL_MAIN);
        this.actionController.registerAction(ACT_MISSILE, new Action(60, 0, 20), ActionController.ActionType.R_SHL_MAIN);
    }

    @Override
    protected void applyPlayerInputWeapons(DriverInput driverInput) {
        if (this.isMainMode()) {
            if (driverInput.isWeaponRightHandPressed()) {
                this.actionController.getAction(ACT_GATRING).startAction();
            } else if (driverInput.isWeaponLeftHandPressed()) {
                if (!this.actionController.getAction(ACT_PILE).isInAction()) {
                    this.startPileBunker();
                }
                this.actionController.getAction(ACT_PILE).startAction();
            } else if (driverInput.isWeaponLeftShoulderPressed()) {
                this.actionController.getAction(ACT_GRENADE).startAction();
            } else if (this.lockTargets.consumeMultiLockComplete()) {
                this.actionController.getAction(ACT_MISSILE).startAction();
            }
        } else {
            if (driverInput.isWeaponRightHandPressed()) {
                this.actionController.getAction(ACT_HUMMER).startAction();
            } else if (driverInput.isWeaponLeftHandPressed()) {
                this.actionController.getAction(ACT_SCOP).startAction();
            }
        }
    }

    private void startPileBunker() {
        if (isServerSide()) {
            Vec3 vel;

            if (this.lockTargets.getLockTargetHard() != null) {
                vel = this.position().vectorTo(this.lockTargets.getLockTargetHard().position()).normalize();
            } else if (this.lockTargets.getLockTargetSoft() != null) {
                vel = this.position().vectorTo(this.lockTargets.getLockTargetSoft().position()).normalize();
            } else {
                vel = new Vec3(0, 0, 1);
                vel = vel.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            }

            var distance = 0.475 * 3;
            if (!this.onGround()) {
                distance = 0.475;
            }

            vel = vel.scale(distance);
            this.push(vel.x, vel.y, vel.z);
        }
    }

    @Override
    protected void fireWeapons() {
        Level level = level();

        if (actionController.getAction(ACT_HUMMER).isOnFire()) {
            this.fireHummer(level);
        } else if (actionController.getAction(ACT_SCOP).isOnFire()) {
            this.fireScop(level);
        } else if (actionController.getAction(ACT_GATRING).isOnFire()) {
            this.fireGatring(level);
        } else if (actionController.getAction(ACT_PILE).isOnFire()) {
            this.firePilebunker(level);
        } else if (actionController.getAction(ACT_GRENADE).isOnFire()) {
            this.fireGrenade(level);
        } else if (actionController.getAction(ACT_MISSILE).isInFire()) {
            if (actionController.getAction(ACT_MISSILE).currentFireTime % 3 == 0) {
                this.fireMissile(level, actionController.getAction(ACT_MISSILE).currentFireTime / 3 - 1);
            }
        }
    }

    private void fireHummer(Level world) {
        if (!world.isClientSide()) {
            BlockPos curBP = this.blockPosition();
            Vec3 vec = new Vec3(0, 0, 5);
            vec = vec.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            BlockPos tgtOffset = new BlockPos(curBP.getX() + (int) vec.x, curBP.getY() + (int) vec.y, +curBP.getZ() + (int) vec.z);

            for (int i = 0; i < 4; i++) {
                for (var v : Utils.circlePosRad9) {
                    world.destroyBlock(new BlockPos(tgtOffset.getX() + v.getX(), tgtOffset.getY() + v.getY() + i, tgtOffset.getZ() + v.getZ()), false);
                }
            }

            for (var v : Utils.circlePosRad7) {
                world.destroyBlock(new BlockPos(tgtOffset.getX() + v.getX(), tgtOffset.getY() + v.getY() + 4, tgtOffset.getZ() + v.getZ()), false);
            }

            for (var v : Utils.circlePosRad5) {
                world.destroyBlock(new BlockPos(tgtOffset.getX() + v.getX(), tgtOffset.getY() + v.getY() + 5, tgtOffset.getZ() + v.getZ()), false);
            }
        }
    }

    private void fireScop(Level world) {
        if (!world.isClientSide()) {
            BlockPos curBP = this.blockPosition();
            Vec3 vec = new Vec3(0, 0, 8);
            vec = vec.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            BlockPos tgtOffset = new BlockPos(curBP.getX() + (int) vec.x, curBP.getY() + (int) vec.y, +curBP.getZ() + (int) vec.z);

            for (var v : Utils.circlePosRad9) {
                world.destroyBlock(new BlockPos(tgtOffset.getX() + v.getX(), tgtOffset.getY() - 0, tgtOffset.getZ() + v.getZ()), false);
            }

            for (var v : Utils.circlePosRad9) {
                world.destroyBlock(new BlockPos(tgtOffset.getX() + v.getX(), tgtOffset.getY() - 1, tgtOffset.getZ() + v.getZ()), false);
            }

            for (var v : Utils.circlePosRad7) {
                world.destroyBlock(new BlockPos(tgtOffset.getX() + v.getX(), tgtOffset.getY() - 2, tgtOffset.getZ() + v.getZ()), false);
            }

            for (var v : Utils.circlePosRad5) {
                world.destroyBlock(new BlockPos(tgtOffset.getX() + v.getX(), tgtOffset.getY() - 3, tgtOffset.getZ() + v.getZ()), false);
            }
        }
    }

    private void fireGatring(Level world) {
        if (!world.isClientSide()) {
            BulletEntity be = new BulletEntity(PomkotsMechs.BULLET.get(), world, this);

            // 原因不明なんだけど、getPosした時の座標と、レンダリングされてる座標で3tick分ぐらい乖離がある気配がする
            // ので、3tick前の座標をオフセットにする
            // なんかaddVelocity周りが悪さしてる…？
            var offset = posHistory.getFirst();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(-1.65, 2.0F, 3.5F);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            be.setPos(offset.add(muzzlPos));

            float[] angle = getShootingAngle(be, true);

            be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), 0.9F, 2F);

            world.addFreshEntity(be);
        } else {
            if (tickCount % 7 == 0) {
                playSoundEffect(PomkotsMechs.SE_GATLING_EVENT.get());
            }
        }
    }

    private void firePilebunker(Level world) {
        Entity driver = this.getDrivingPassenger();
        var pilePos1 = new Vec3(6.5, 3.0F, 18F).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
        var pilePos2 = new Vec3(-2, -3F, -2F).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());

        var kbVel = new Vec3(0, 0, -1F).yRot((float) Math.toRadians((-1.0) * this.getYRot()));
        for (var ent : world.getEntities(null, new AABB(pilePos1, pilePos2))) {
            if (isSelf(ent)) {
                continue;
            }

            if (ent instanceof LivingEntity le) {
                if (!world.isClientSide()) {
                    le.knockback(2, kbVel.x, kbVel.z);

                    DamageSource ds;
                    if (driver instanceof Player p) {
                        ds = this.damageSources().playerAttack(p);
                    } else {
                        ds = this.damageSources().generic();
                    }
                    le.hurt(ds, BattleBalance.MECH_PILE_DAMAGE);
                } else {
                    addHitParticles(le);
                }
            }
        }

        if (this.level().isClientSide) {
            playSoundEffect(PomkotsMechs.SE_PILEBUNKER_EVENT.get());
        }
    }

    private void addHitParticles(Entity target) {
        var offset = new Vec3(target.position().x, target.getBoundingBox().getCenter().y, target.position().z);

        for (int i = 0; i < 40; i++) {
            // ランダムな速度を生成
            double velocityX = random.nextDouble() * 4.3 - 1;
            double velocityY = random.nextDouble() * 4.3 - 1;
            double velocityZ = random.nextDouble() * 4.3 - 1;

            // パーティクルをクライアント側で発生させる
            this.level().addAlwaysVisibleParticle(PomkotsMechs.SPARK.get(),
                    true,
                    offset.x(), offset.y(), offset.z(), // 位置
                    velocityX, velocityY, velocityZ // 速度
            );
        }
    }

    private void fireMissile(Level world, int slot) {
        if (!world.isClientSide()) {
            // 原因不明なんだけど、getPosした時の座標と、レンダリングされてる座標で3tick分ぐらい乖離がある気配がする
            // ので、3tick前の座標をオフセットにする
            // なんかaddVelocity周りが悪さしてる…？
            var offset = posHistory.getFirst();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(-1.0, 3.5F, 0.8F);
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

            MissileVerticalEntity be = new MissileVerticalEntity(PomkotsMechs.MISSILE_VERTICAL.get(), world, this, target);

            be.setPos(offset.add(worldMuzzlPos));

            be.shootFromRotation(be, -89, this.getYRot(), this.getFallFlyingTicks(), 1F, 0F);

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

    private void fireGrenade(Level world) {
        if (!world.isClientSide()) {
            GrenadeEntity be = new GrenadeEntity(PomkotsMechs.GRENADE.get(), world, this, BattleBalance.MECH_GRENADE_EXPLOSION);

            // 原因不明なんだけど、getPosした時の座標と、レンダリングされてる座標で3tick分ぐらい乖離がある気配がする
            // ので、3tick前の座標をオフセットにする
            // なんかaddVelocity周りが悪さしてる…？
            var offset = posHistory.getFirst();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(1.65, 3.0F, 3.5F);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            be.setPos(offset.add(muzzlPos));

            float[] angle = getShootingAngle(be, false);
            be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), 0.9F, 0F);

            world.addFreshEntity(be);

            var knockBackvel = new Vec3(0, 0, -3F).yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            this.addDeltaMovement(knockBackvel);
        } else {
            playSoundEffect(PomkotsMechs.SE_GRENADE_EVENT.get());
        }
    }

    @Override
    protected PlayState controllAnimationWeapons(AnimationState<PomkotsVehicleBase> event) {
        if (this.actionController.getAction(ACT_PILE).isInAction()) {
            if (this.actionController.getAction(ACT_PILE).isOnStart()) {
                event.getController().forceAnimationReset();
            }
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".pilebunker"));

        } else if (this.actionController.getAction(ACT_GRENADE).isInAction()) {
            if (this.actionController.getAction(ACT_GRENADE).isOnStart()) {
                event.getController().forceAnimationReset();
            }
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".grenade"));

        } else if (this.actionController.getAction(ACT_HUMMER).isInAction()) {
            if (this.actionController.getAction(ACT_HUMMER).isOnStart()) {
                event.getController().forceAnimationReset();
            }
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".hummer"));

        } else if (this.actionController.getAction(ACT_SCOP).isInAction()) {
            if (this.actionController.getAction(ACT_SCOP).isOnStart()) {
                event.getController().forceAnimationReset();
            }
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".scop"));
        }

        return null;
    }

    @Override
    protected void addExtraAnimationController(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "gatling", 0, event -> {
            if (this.actionController.getAction(ACT_GATRING).isInAction()) {
                if (this.actionController.getAction(ACT_GATRING).isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                if (!this.actionController.getAction(ACT_GATRING).isInFire()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".gatringgun1"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".gatringgun2"));
                }
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
            }
        }));

        controllers.add(new AnimationController<>(this, "weapons", 0, event -> {
            if (this.actionController.getAction(ACT_GRENADE).isInAction()) {
                if (this.actionController.getAction(ACT_GRENADE).isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".weapon_grenade"));
            } else if (this.actionController.getAction(ACT_MISSILE).isInAction()) {
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

    @Override
    public boolean shouldLockMulti(DriverInput driverInput) {
        return driverInput.isWeaponRightShoulderPressed();
    }

    @Override
    public boolean shouldLockWeak(DriverInput driverInput) {
        return driverInput.isWeaponRightHandPressed();
    }

    @Override
    public boolean shouldLockStrong(DriverInput driverInput) {
        return driverInput.isLockPressed();
    }
}
