package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.HelicopterHoverMoveGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.OrbitalFlyGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissilePodEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class Pmb08Entity extends BaseBossEntity {
    @Override
    public String getMechName() {
        return "pmb08";
    }

    public Pmb08Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);

        this.setNoGravity(true);

        actionController.registerAction("missile_straight", new BossActionController.BossAction(60,20, this::missileStraightAction));
        actionController.registerAction("missile_horizontal", new BossActionController.BossAction(60,20, this::missileHorizontalAction));
        actionController.registerAction("missile_vertical", new BossActionController.BossAction(60,20, this::missileVerticalAction));

        actionController.registerAction("missile_pod", new BossActionController.BossAction(200,20, this::missilePodAction));
        actionController.registerAction("missile_pod2", new BossActionController.BossAction(200,20, this::missilePodAction2));

        actionController.registerAction("gatling", new BossActionController.BossAction(40,5, this::gatlingAction));

        actionController.registerAction("onsmalldown", new BossActionController.BossAction(20,15, this::smallDown));

        this.goalSelector.addGoal(1, new HelicopterHoverMoveGoal(
                this,
                2,
                15,
                30,
                20
        ));

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("gatling"), this, true, 5, true),
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
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_vertical"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_pod2"), this, true, true),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile_pod"), this, true, true),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );

    }

    public void tick() {
        if (this.firstTick && this.isServerSide()) {
            this.registerAdditionalHitBox(new BossHitBoxEntity(PomkotsMechs.HITBOX_PMB08.get(), this.level(), this));
        }
        this.setNoGravity(true);
        super.tick();
    }

    private void gatlingAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction() && action.isFirstLoop()) {
            this.triggerAnim("action_controller", "gatling");

        } else if (action.currentActionTick == 3) {
            for (int side = -1; side < 2; side += 2) {
                BulletGrenadeEntity be = new BulletGrenadeEntity(PomkotsMechs.BULLET_GRENADE.get(), this.level(), this,
                        getMechData().bulletDamage);
                be.setNoGravity(true);
                be.setExplosionScale(1);
                be.setStunPoint(2);

                var offset = this.position();

                var muzzlPos = new Vec3(8 * side, 10, 3);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                be.setPos(offset.add(muzzlPos));

                float[] angle = Utils.getShootingAngle(be, target, true);

                be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), getMechData().bulletSpeed, 0F);

                this.level().addFreshEntity(be);
            }
        }
    }

    private float missileModifier = 2F;

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
                        float slotX = (col * 0.8F + 15F) * side;
                        float slotY = row + 3F;

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
                        float slotX = (col * 0.8F + 15F) * side;
                        float slotY = row + 3F;

                        var muzzlPos = new Vec3(slotX, slotY, 3);
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
                        float slotX = (col * 0.8F + 15F) * side;
                        float slotY = row + 3F;

                        var muzzlPos = new Vec3(slotX, slotY, 3);
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

                var muzzlPos = new Vec3(15 * side, 3.5, 7);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                be.setPos(offset.add(muzzlPos));

                float[] angle = Utils.getShootingAngle(be, target, true);

                be.shootFromRotation(be, -15, angle[1], this.getFallFlyingTicks(),
                        getMechData().missileSpeed, 0F);

                this.level().addFreshEntity(be);
            }
        }
    }

    private void missilePodAction2(BossActionController.BossAction action) {
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
                be.setMode(MissilePodEntity.Mode.SPHERE);

                var offset = this.position();

                var muzzlPos = new Vec3(15 * side, 3.5, 7);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                be.setPos(offset.add(muzzlPos));

                float[] angle = Utils.getShootingAngle(be, target, true);

                be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(),
                        getMechData().missileSpeed * missileModifier, 0F);

                this.level().addFreshEntity(be);
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
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.inactive"));

            } else if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.idle"));

            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.idle"));

            }
        }));


        controllers.add(new AnimationController<>(this, "rotor", 1, event -> {
            if (this.getAiMode() == AI_MODE_INACTIVE) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.idle"));

            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.rotor"));

            }
        }).setSoundKeyframeHandler(this::playSounds)
        );

        controllers.add(new AnimationController<>(this, "action_controller", state -> PlayState.STOP)
                .triggerableAnim("dash", RawAnimation.begin().thenPlay("animation.pmb03.boost1").thenLoop("animation.pmb03.boost2"))
                .triggerableAnim("gatling", RawAnimation.begin().thenPlayXTimes("animation.pmb03.gatling", 5))
                .triggerableAnim("missile", RawAnimation.begin().thenPlay("animation.pmb03.missilepod"))
                .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb03.boot"))

                .triggerableAnim("on_stun", RawAnimation.begin().thenPlay("animation.pmb03.hurt").thenPlayAndHold("animation.pmb03.down"))
                .triggerableAnim("off_stun", RawAnimation.begin().thenPlay("animation.pmb03.up"))

                .triggerableAnim("on_small_down", RawAnimation.begin().thenPlay("animation.pmb03.hurt"))

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
