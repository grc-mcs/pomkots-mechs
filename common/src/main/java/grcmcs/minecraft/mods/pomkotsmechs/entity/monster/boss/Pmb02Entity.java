package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.OrbitalFlyGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissilePodEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class Pmb02Entity extends BaseBossEntity {
    @Override
    public String getMechName() {
        return "pmb02";
    }

    public Pmb02Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);

        this.setNoGravity(true);

        actionController.registerAction("missile_straight", new BossActionController.BossAction(60,20, this::missileStraightAction));
        actionController.registerAction("missile_horizontal", new BossActionController.BossAction(60,20, this::missileHorizontalAction));
        actionController.registerAction("missile_vertical", new BossActionController.BossAction(60,20, this::missileVerticalAction));
        actionController.registerAction("missile_full", new BossActionController.BossAction(1000,20, this::missileFullAction));

        actionController.registerAction("missile_pod", new BossActionController.BossAction(200,20, this::missilePodAction));
        actionController.registerAction("canon", new BossActionController.BossAction(60,20, this::canonAction));
        actionController.registerAction("saber_horizontal", new BossActionController.BossAction(60,105, this::saberHorizontal));

        actionController.registerAction("onsmalldown", new BossActionController.BossAction(20,15, this::smallDown));

        this.registerActionGoal(
                new OrbitalFlyGoal(this, getMechData().speed,  20, 100, 10, 30),
                AI_MODE_ALL,
                10
        );

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_straight"), this, true, true),
                AI_MODE_ALL,
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_horizontal"), this, false),
                AI_MODE_ALL,
                10
        );
        this.registerActionGoal(new SimpleBossAttackGoal(actionController.getAction("saber_horizontal"), this, false),
                AI_MODE_ALL,
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_pod"), this, true, true),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("canon"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_vertical"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_full"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_4},
                10
        );

    }

    public void tick() {
        if (this.firstTick && this.isServerSide()) {
            this.registerAdditionalHitBox(new BossHitBoxEntity(PomkotsMechs.HITBOX_PMB02.get(), this.level(), this));
        }
        this.setNoGravity(true);
        super.tick();
    }

    private void saberHorizontal(BossActionController.BossAction action) {
        final float speed = 6;
        final float maxTurnRadiansPerTick = 0.15F;
        final float attackDistanceSq = 1900;
        final int startChargeTick = 15;
        final int maxHomingTicks = 60;

        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "dash");

        } else if (action.currentActionTick > startChargeTick && action.currentActionTick <= maxHomingTicks + startChargeTick) {
            Vec3 mobPos = this.position();
            Vec3 targetPos = target.position();
            Vec3 direction = targetPos.subtract(mobPos).normalize();

            // 現在の向き
            Vec3 forward = this.getLookAngle().normalize();

            // 補間して新しい向きを計算
            Vec3 newForward = forward.lerp(direction, maxTurnRadiansPerTick).normalize();

            // 新しい速度ベクトル
            Vec3 movement = newForward.scale(speed);

            this.setDeltaMovement(movement);
            this.rotateDegree((float)(Math.toDegrees(Math.atan2(newForward.z, newForward.x)) - 90.0));

            double distSq = this.distanceToSqr(target);

            if (distSq <= attackDistanceSq || action.currentActionTick == maxHomingTicks + startChargeTick) {
                if (distSq <= attackDistanceSq) {
                    action.currentActionTick = maxHomingTicks + startChargeTick;
                }
                this.triggerAnim("action_controller", "saber_horizontal");
            }
        } else if (action.currentActionTick == maxHomingTicks + startChargeTick + 10) {
            var pilePos1 = new Vec3(14, 4F, 14).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
            var pilePos2 = new Vec3(-14, -4F, -4F).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
            var kbVel = new Vec3(0, 0, -1F).yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            for (var ent : this.level().getEntities(null, new AABB(pilePos1, pilePos2))) {
                if (!isSelf(ent) && ent instanceof LivingEntity le) {
                    le.knockback(2, kbVel.x, kbVel.z);
                    le.hurt(this.damageSources().generic(), getMechData().meleeDamage);
                }
            }
        }
    }

    private void canonAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "canon");

        } else if (action.currentActionTick == 7) {
            for (int side = -1; side < 2; side += 2) {
                BulletGrenadeEntity be = new BulletGrenadeEntity(PomkotsMechs.BULLET_GRENADE.get(), this.level(), this,
                        getMechData().bulletDamage);
                be.setNoGravity(true);
                be.setExplosionScale(3);

                var offset = this.position();

                var muzzlPos = new Vec3(2 * side, 4, 7);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                be.setPos(offset.add(muzzlPos));

                float[] angle = Utils.getShootingAngle(be, target, true);

                be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(),
                        getMechData().bulletSpeed, 0F);

                this.level().addFreshEntity(be);
            }
        }
    }

    private void missilePodAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");
        } else if (action.currentActionTick == 2) {
            for (int side = -1; side < 2; side += 2) {
                MissilePodEntity be = new MissilePodEntity(PomkotsMechs.MISSILE_POD.get(), this.level(), this, target,
                        getMechData().missileDamage, getMechData().missileSpeed * missileModifier);

                var offset = this.position();

                var muzzlPos = new Vec3(4 * side, 3.5, 7);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                be.setPos(offset.add(muzzlPos));

                float[] angle = Utils.getShootingAngle(be, target, true);

                be.shootFromRotation(be, -15, angle[1], this.getFallFlyingTicks(),
                        getMechData().missileSpeed, 0F);

                this.level().addFreshEntity(be);
            }
        }
    }

    private float missileModifier = 1.5F;

    private void missileStraightAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");

        } else if (action.currentActionTick == 2) {
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    for (int side = -1; side < 2; side += 2) {
                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                                getMechData().missileDamage, getMechData().missileSpeed * missileModifier);
                        be.setMaxRotationAnglePerTick(2);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotX = (col * 0.8F + 1.5F) * side;
                        float slotY = row + 0.5F;

                        var muzzlPos = new Vec3(slotX, slotY, 3);
                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                        be.setPos(offset.add(muzzlPos));

                        float[] angle = Utils.getShootingAngle(be, target, true);

                        be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(),
                                getMechData().missileSpeed, 0F);

                        this.level().addFreshEntity(be);
                    }
                }
            }
        }
    }

    private void missileHorizontalAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");

        } else if (action.currentActionTick == 2) {
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    for (int side = -1; side < 2; side += 2) {
                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                                getMechData().missileDamage, getMechData().missileSpeed * missileModifier);
                        be.setMaxRotationAnglePerTick(2);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotZ = (col * 0.8F + 1.5F);
                        float slotY = row + 0.8F;

                        var muzzlPos = new Vec3(side * 7, slotY, slotZ);
                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                        be.setPos(offset.add(muzzlPos));

                        float[] angle = Utils.getShootingAngle(be, target, true);

                        be.shootFromRotation(be, angle[0], angle[1] - side * 30, this.getFallFlyingTicks(),
                                getMechData().missileSpeed, 0F);

                        this.level().addFreshEntity(be);
                    }
                }
            }
        }
    }

    private void missileVerticalAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");

        } else if (action.currentActionTick == 2) {
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    for (int side = -1; side < 2; side += 2) {
                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                                getMechData().missileDamage, getMechData().missileSpeed * missileModifier);
                        be.setMaxRotationAnglePerTick(7);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotX = (col * 0.8F + 1.5F) * side;
                        float slotZ = - row - 0.8F;

                        var muzzlPos = new Vec3(slotX, 8, slotZ);
                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                        be.setPos(offset.add(muzzlPos));

                        float[] angle = Utils.getShootingAngle(be, target, true);

                        be.shootFromRotation(be, -80, angle[1], this.getFallFlyingTicks(),
                                getMechData().missileSpeed, 0F);

                        this.level().addFreshEntity(be);
                    }
                }
            }
        }
    }

    private void missileFullAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");

        } else if (action.currentActionTick == 2) {
            int mNum = 9;

            for (int i = 0; i < mNum; i++) {
                for (int j = 0; j < mNum; j++) {
                    MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                            getMechData().missileDamage, getMechData().missileSpeed * missileModifier);
                    be.setMaxRotationAnglePerTick(6);

                    be.setPos(this.position());
                    be.shootFromRotation(be, -i * (90/mNum), -90 + (j * (180/mNum)), this.getFallFlyingTicks(), 1.5F, 0F);
                    this.level().addFreshEntity(be);

                }
            }
        }
    }

    @Override
    protected void onStun() {
        this.triggerAnim("action_controller", "on_stun");
    }

    @Override
    protected void offStun() {
        this.triggerAnim("action_controller", "off_stun");
    }

    @Override
    protected void onSmallDown() {
        this.actionController.reset();
        this.actionController.getAction("onsmalldown").startAction();
        this.triggerAnim("action_controller", "on_small_down");
    }
    
    private void smallDown(BossActionController.BossAction action) {

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0, event -> {
            if (this.getAiMode() == AI_MODE_INACTIVE) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb02.inactive"));

            } else if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb02.idle"));

            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb02.idle"));

            }
        }));

        controllers.add(new AnimationController<>(this, "action_controller", state -> PlayState.STOP)
                .triggerableAnim("dash", RawAnimation.begin().thenPlay("animation.pmb02.dash1").thenLoop("animation.pmb02.dash2"))
                .triggerableAnim("saber_horizontal", RawAnimation.begin().thenPlay("animation.pmb02.attack_arm_horizontal"))
                .triggerableAnim("canon", RawAnimation.begin().thenPlay("animation.pmb02.canon"))
                .triggerableAnim("missile", RawAnimation.begin().thenPlay("animation.pmb02.missile"))
                .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb02.boot"))

                .triggerableAnim("on_stun", RawAnimation.begin().thenPlay("animation.pmb02.hurt").thenPlayAndHold("animation.pmb02.down"))
                .triggerableAnim("off_stun", RawAnimation.begin().thenPlay("animation.pmb02.up"))

                .triggerableAnim("on_small_down", RawAnimation.begin().thenPlay("animation.pmb02.hurt"))

                .setSoundKeyframeHandler(this::playSounds)
        );
    }

    @Override
    public void boot() {
        super.boot();
        this.triggerAnim("action_controller", "boot");
    }

    @Override
    protected void applyPlayerControll() {

    }
}
