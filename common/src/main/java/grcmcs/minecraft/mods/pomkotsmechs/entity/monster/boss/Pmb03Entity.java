package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.BossAerialDiveGoal2;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossWalkGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.*;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.List;

public class Pmb03Entity extends BaseBossEntity {
    @Override
    public String getMechName() {
        return "pmb03";
    }

    public Pmb03Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
        this.alwaysLookAtTarget = false;

        actionController.registerAction("grenade", new BossActionController.BossAction(80,20, this::grenadeAction));
        actionController.registerAction("gatling", new BossActionController.BossAction(40,5, this::gatlingAction));

        actionController.registerAction("missilepod", new BossActionController.BossAction(80,20, this::missilePodAction));

        actionController.registerAction("missile", new BossActionController.BossAction(40,21, this::missileHorizontalAction));
        actionController.registerAction("missilev", new BossActionController.BossAction(40,21, this::missileVerticalAction));

        actionController.registerAction("stomp", new BossActionController.BossAction(40,40, this::stompAction));

        actionController.registerAction("onground", new BossActionController.BossAction(20,16, this::onGroundAction));

        this.registerActionGoal(
                new SimpleBossWalkGoal(this, getMechData().speed, 30),
                AI_MODE_ALL,
                15
        );

        this.registerActionGoal(
                new BossAerialDiveGoal2(
                    this,   // ボスエンティティ
                    10,           // 必要な高度差（5ブロック以上低い時に発動）
                    3,           // riseSpeed
                    3,           // flySpeed
                    3,           // diveSpeed
                    20,           // targetHeight
                    100            // 最大追跡時間（10秒）
                ),
                AI_MODE_ALL,
                15
        );

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("gatling"), this, true, 5, true),
                AI_MODE_ALL,
                30
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile"), this, true, true),
                AI_MODE_ALL,
                20
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missilev"), this, true, true),
                AI_MODE_ALL,
                20
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("stomp"), this, false),
                AI_MODE_ALL,
                0,20,
                40
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("grenade"), this, true, true),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                15
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missilepod"), this, true, true),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                15
        );

        this.setNoGravity(false);
    }

    public void tick() {
        if (this.firstTick && this.isServerSide()) {
            this.registerAdditionalHitBox(new BossHitBoxEntity(PomkotsMechs.HITBOX_PMB03.get(), this.level(), this));
        }

        super.tick();
    }

    private void stompAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "stomp");

        } else if (action.currentActionTick == 22) {
            var pilePos1 = new Vec3(20, 3.0F, 20F).add(this.position());
            var pilePos2 = new Vec3(-20, -3F, -20F).add(this.position());

            var world = this.level();
            var kbVel = new Vec3(0, 0, -1F).yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            for (var ent : world.getEntities(null, new AABB(pilePos1, pilePos2))) {
                if (this.isSelf(ent)) {
                    continue;
                }

                if (ent instanceof LivingEntity le) {
                    le.knockback(2, kbVel.x, kbVel.z);

                    DamageSource ds = this.damageSources().generic();
                    float damage = getMechData().meleeDamage;

                    le.hurt(ds, damage);
                }
            }
        }
    }

    private void grenadeAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "grenade");

        } else if (action.currentActionTick == 5) {
            for (int side = -1; side < 2; side += 2) {
                BulletGrenadeLargeEntity be = new BulletGrenadeLargeEntity(PomkotsMechs.BULLET_GRENADE_LARGE.get(), this.level(), this,
                        getMechData().grenadeDamage);
                be.setNoGravity(true);
                be.setExplosionScale((int)getMechData().grenadeExplosionScale);

                var offset = this.position();

                var muzzlPos = new Vec3(5.5 * side, 11, 10);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                be.setPos(offset.add(muzzlPos));

                float[] angle = Utils.getShootingAngle(be, target, true);

                be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(),
                        getMechData().grenadeSpeed, 0F);

                this.level().addFreshEntity(be);
            }
        }
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

                var muzzlPos = new Vec3(5.5 * side, 14, 10);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                be.setPos(offset.add(muzzlPos));

                float[] angle = Utils.getShootingAngle(be, target, true);

                be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), getMechData().bulletSpeed, 0F);

                this.level().addFreshEntity(be);
            }
        }
    }

    private void missileHorizontalAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missilepod");

        } else if (action.currentActionTick == 5) {
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 1; col++) {
                    for (int side = -1; side < 2; side += 2) {
                        MissileGenericEnemyEntity be = new MissileGenericEnemyEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                                getMechData().missileDamage, this.getMechData().missileSpeed * 2);
                        be.setMaxRotationAnglePerTick(2);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotZ = (col * 0.8F + 8F);
                        float slotY = row + 10F;

                        var muzzlPos = new Vec3(side * 7, slotY, slotZ);
                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                        be.setPos(offset.add(muzzlPos));

                        float[] angle = Utils.getShootingAngle(be, target, true);

                        be.shootFromRotation(be, angle[0], angle[1] - side * 30, this.getFallFlyingTicks(),
                                this.getMechData().missileSpeed, 0F);

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
            this.triggerAnim("action_controller", "missilepod");

        } else if (action.currentActionTick == 5) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 1; col++) {
                    for (int side = -1; side < 2; side += 2) {
                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                                getMechData().missileDamage, getMechData().missileSpeed * 2);
                        be.setMaxRotationAnglePerTick(7);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotZ = (col * 0.8F + 8F);
                        float slotY = row + 10F;

                        var muzzlPos = new Vec3(side * 7, slotY, slotZ);
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
            this.triggerAnim("action_controller", "missilepod");

        } else if (action.currentActionTick == 5) {
            for (int side = -1; side < 2; side += 2) {
                MissilePodEntity be = new MissilePodEntity(PomkotsMechs.MISSILE_POD.get(), this.level(), this, target,
                        getMechData().missileDamage, this.getMechData().missileSpeed);
                be.setNoGravity(true);

                var offset = this.position();

                var muzzlPos = new Vec3(4 * side, 17, 4);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                be.setPos(offset.add(muzzlPos));

                float[] angle = Utils.getShootingAngle(be, target, true);

                be.shootFromRotation(be, -15, angle[1], this.getFallFlyingTicks(),
                        this.getMechData().missileSpeed, 0F);

                this.level().addFreshEntity(be);
            }
        }
    }

    private void onGroundAction(BossActionController.BossAction action) {
        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "onground");

            // 着地時のエフェクト処理
            Level level = this.level();
            BlockPos landingPos = this.blockPosition();

            // 周囲のエンティティにダメージを与える（例）
            AABB damageArea = new AABB(landingPos).inflate(10.0);
            List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, damageArea);

            for (LivingEntity entity : nearbyEntities) {
                if (!this.isSelf(entity)) {
                    entity.hurt(this.damageSources().generic(), 30);

                    Vec3 knockback = entity.position().subtract(this.position()).normalize().scale(1.5);
                    entity.knockback(2, knockback.x, knockback.z);
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

    private final AnimationController<Pmb03Entity> trigger = new AnimationController<>(this, "action_controller", state -> PlayState.STOP)
            .triggerableAnim("grenade", RawAnimation.begin().thenPlay("animation.pmb03.grenade"))
            .triggerableAnim("gatling", RawAnimation.begin().thenPlayXTimes("animation.pmb03.gatling", 5))
            .triggerableAnim("missilepod", RawAnimation.begin().thenPlay("animation.pmb03.missilepod"))
            .triggerableAnim("stomp", RawAnimation.begin().thenPlay("animation.pmb03.stomp"))
            .triggerableAnim("jump", RawAnimation.begin().thenPlay("animation.pmb03.jump"))
            .triggerableAnim("onground", RawAnimation.begin().thenPlay("animation.pmb03.onground"))
            .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb03.boot"))
            .triggerableAnim("on_stun", RawAnimation.begin().thenPlay("animation.pmb03.hurt").thenPlayAndHold("animation.pmb03.down"))
            .triggerableAnim("off_stun", RawAnimation.begin().thenPlay("animation.pmb03.up"))

            .setSoundKeyframeHandler(this::playSounds);

    private final AnimationController<Pmb03Entity> base = new AnimationController<>(this, "basic_move", 0, event -> {
        if (!trigger.isPlayingTriggeredAnimation()) {
            if (this.getAiMode() == AI_MODE_INACTIVE) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.inactive"));
            } else if (event.isMoving()) {
                if (!this.isNoGravity()) {
                    var dir = getDominantMoveDirection();
                    if (dir == MoveDirection.LEFT) {
                        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.walk_right"));
                    } else if (dir == MoveDirection.RIGHT) {
                        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.walk_left"));
                    } else {
                        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.walk"));
                    }
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.fly"));
                }
            } else {
                if (!this.isNoGravity()) {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.idle"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb03.fly"));
                }

            }
        } else {
            event.getController().forceAnimationReset();
            return PlayState.STOP;
        }
    }).setSoundKeyframeHandler(this::playSounds);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(base);
        controllers.add(trigger);
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
