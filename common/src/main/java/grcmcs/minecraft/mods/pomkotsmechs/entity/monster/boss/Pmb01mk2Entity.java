package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.BossAerialDiveGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossWalkGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.EarthbreakEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.EarthraiseEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileEnemyLargeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.SlashEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericLargeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
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

public class Pmb01mk2Entity extends BaseBossEntity {
    public static final float DEFAULT_SCALE = 2f;

    @Override
    public String getMechName() {
        return "pmb01mk2";
    }

    public Pmb01mk2Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
        this.alwaysLookAtTarget = false;

        actionController.registerAction("punch1", new BossActionController.BossAction(40,20, this::punchAction1));
        actionController.registerAction("punch2", new BossActionController.BossAction(40,20, this::punchAction2));
        actionController.registerAction("upper", new BossActionController.BossAction(40,20, this::upperAction));
        actionController.registerAction("jump_attack", new BossActionController.BossAction(200,25, this::jumpAttackAction));
        actionController.registerAction("gatling", new BossActionController.BossAction(40,5, this::gatlingAction));
        actionController.registerAction("grenade", new BossActionController.BossAction(40,20, this::grenadeAction));
        actionController.registerAction("missile", new BossActionController.BossAction(80,20, this::missileHorizontalAction));
        actionController.registerAction("large_missile", new BossActionController.BossAction(600,20, this::missileLargeAction));
        actionController.registerAction("onground", new BossActionController.BossAction(20,15, this::onGroundAction));
        actionController.registerAction("break", new BossActionController.BossAction(20,95, this::breakAction));

        this.registerActionGoal(
                new SimpleBossWalkGoal(this, getMechData().speed, 30),
                AI_MODE_ALL,
                10
        );
        this.registerActionGoal(
                new BossAerialDiveGoal(
                        this,           // ボスエンティティ
                        15,           // 必要な高度差（5ブロック以上低い時に発動）
                        3,           // 初動ジャンプ強度
                        1,           // ブースター上昇速度
                        3,           // 空中移動速度
                        3.0,           // 急降下速度
                        60            // 最大追跡時間（10秒）
                ),
                AI_MODE_ALL,
                10
        );

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("gatling"), this, true, 5, true),
                AI_MODE_ALL,
                20
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile"), this, true, true),
                AI_MODE_ALL,
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("punch1"), this, false),
                AI_MODE_ALL,
                0,30,
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("upper"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                0,50,
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("grenade"), this, true, true),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                15
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("punch2"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                0,50,
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("jump_attack"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                0,50,
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("large_missile"), this, true, true),
                new int[]{AI_MODE_BATTLE_PHASE_4},
                10
        );

        this.setNoGravity(false);
    }

    public void tick() {
        if (this.firstTick && this.isServerSide()) {
            this.registerAdditionalHitBox(new BossHitBoxEntity(PomkotsMechs.HITBOX_PMB03.get(), this.level(), this));
        }

        super.tick();
    }

    private void punchAction1(BossActionController.BossAction action) {
        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "attackpunch");

        } else if (action.currentActionTick == 15) {
            this.rangeAttack(
                    new Vec3(20, 10F, 20F),
                    new Vec3(-20, -3F, -20F),
                    getMechData().meleeDamage,
                    2);
        }
    }

    private void punchAction2(BossActionController.BossAction action) {
        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "attackpunch");

        } else if (action.currentActionTick == 15) {
            var offset = this.position();

            for (int i = 0; i < 12; i++) {
                SlashEntity be = new SlashEntity(PomkotsMechs.EXPLOADSLASH.get(), this.level());

                be.setPos(offset);
                be.shootFromRotation(be, 0, -180 + i * 30, this.getFallFlyingTicks(), 1.5F, 0F);
                this.level().addFreshEntity(be);
            }
        }
    }

    private void upperAction(BossActionController.BossAction action) {
        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "attackupper");

        } else if (action.currentActionTick == 12) {
            var offset = this.position();

            var muzzlPos = new Vec3(0, 0, 10);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            var vec = muzzlPos.normalize().multiply(6,6,6);
            muzzlPos = offset.add(muzzlPos);

            EarthraiseEntity be = new EarthraiseEntity(PomkotsMechs.EARTHRAISE.get(), this.level(), vec, this, 8);
            be.setPos(muzzlPos);
            be.setYRot((float)(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0));

            this.level().addFreshEntity(be);
        }
    }

    private void jumpAttackAction(BossActionController.BossAction action) {
        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "attackjump");

        } else if (action.currentActionTick == 20) {
            var offset = this.position();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(0, 0, -3);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            muzzlPos = offset.add(muzzlPos);

            EarthbreakEntity be = new EarthbreakEntity(PomkotsMechs.EARTHBREAK.get(), this.level(), this);
            be.setPos(muzzlPos);
            be.shootFromRotation(be, 0, 0, this.getFallFlyingTicks(), 0F, 0F);
            this.level().addFreshEntity(be);
        }
    }

    private void gatlingAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction() && action.isFirstLoop()) {
            this.triggerAnim("action_controller", "attackgatling");

        } else if (action.currentActionTick == 3) {
            BulletGrenadeEntity be = new BulletGrenadeEntity(PomkotsMechs.BULLET_GRENADE.get(), this.level(), this,
                    getMechData().bulletDamage);
            be.setNoGravity(true);
            be.setExplosionScale(1);

            var offset = this.position();

            var muzzlPos = new Vec3(-8, 8.0F, 10);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            be.setPos(offset.add(muzzlPos));

            if (!Utils.isObstructed(this.level(), be, target)) {
                float[] angle = Utils.getShootingAngle(be, target, true);

                be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), getMechData().bulletSpeed, 0F);

                this.level().addFreshEntity(be);
            }
        }
    }

    private void grenadeAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "attackgrenade");

        } else if (action.currentActionTick == 12) {
            BulletGrenadeEntity be = new BulletGrenadeEntity(PomkotsMechs.BULLET_GRENADE.get(), this.level(), this,
                    getMechData().grenadeDamage);
            be.setNoGravity(true);
            be.setExplosionScale((int)getMechData().grenadeExplosionScale);

            var offset = this.position();

            var muzzlPos = new Vec3(8, 8.0F, 10);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            be.setPos(offset.add(muzzlPos));

            if (!Utils.isObstructed(this.level(), be, target)) {
                float[] angle = Utils.getShootingAngle(be, target, true);

                be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(),
                        getMechData().grenadeSpeed, 0F);

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
            this.triggerAnim("action_controller", "attackmissile");

        } else if (action.currentActionTick == 15) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 1; col++) {
                    for (int side = -1; side < 2; side += 2) {
                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                                getMechData().missileDamage, getMechData().missileSpeed);
                        be.setMaxRotationAnglePerTick(2);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotZ = (col * 0.8F + 8F);
                        float slotY = row + 15;

                        var muzzlPos = new Vec3(side * 10, slotY, 0);
                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                        be.setPos(offset.add(muzzlPos));

                        if (!Utils.isObstructed(this.level(), be, target)) {
                            float[] angle = Utils.getShootingAngle(be, target, true);

                            be.shootFromRotation(be, angle[0], angle[1] - side * 30, this.getFallFlyingTicks(),
                                    getMechData().missileSpeed, 0F);

                            this.level().addFreshEntity(be);
                        }
                    }
                }
            }
        }
    }

    private void missileLargeAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "attacklargemissile");

        } else if (action.currentActionTick == 15) {
            var offset = this.position();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(0, 16F, 1);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            muzzlPos = offset.add(muzzlPos);

            MissileGenericLargeEntity be = new MissileGenericLargeEntity(PomkotsMechs.MISSILE_GENERIC_LARGE.get(), this.level(), this, target,
                    getMechData().missileDamage * 2 , getMechData().missileSpeed);

            be.setPos(muzzlPos);
            be.shootFromRotation(be, -89, this.getYRot(), this.getFallFlyingTicks(), 1.5F, 0F);
            this.level().addFreshEntity(be);
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
                    // ダメージ処理
                    // entity.hurt(DamageSource.mobAttack(mob), 10.0f);

                    // ノックバック効果
                    Vec3 knockback = entity.position().subtract(this.position()).normalize().scale(1.5);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(knockback.x, 0.5, knockback.z));
                }
            }
        }
    }

    private void breakAction(BossActionController.BossAction action) {
        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "bodyfrontbreak");
        }
    }

    private final AnimationController<Pmb01mk2Entity> trigger = new AnimationController<>(this, "action_controller", state -> PlayState.STOP)
            .triggerableAnim("attackpunch", RawAnimation.begin().thenPlay("animation.pmb01.attackpunch"))
            .triggerableAnim("attackupper", RawAnimation.begin().thenPlay("animation.pmb01.attackupper"))
            .triggerableAnim("attackjump", RawAnimation.begin().thenPlay("animation.pmb01.attackjump"))
            .triggerableAnim("attackgatling", RawAnimation.begin().thenPlayXTimes("animation.pmb01.attackgatling", 5))
            .triggerableAnim("attackgrenade", RawAnimation.begin().thenPlay("animation.pmb01.attackgrenade"))
            .triggerableAnim("attackmissile", RawAnimation.begin().thenPlay("animation.pmb01.attackmissile"))
            .triggerableAnim("attacklargemissile", RawAnimation.begin().thenPlay("animation.pmb01.attacklargemissile"))

            .triggerableAnim("jump", RawAnimation.begin().thenPlay("animation.pmb01.jump"))
            .triggerableAnim("onground", RawAnimation.begin().thenPlay("animation.pmb01.onground"))
            .triggerableAnim("hurt", RawAnimation.begin().thenPlay("animation.pmb01.hurt"))
            .triggerableAnim("down", RawAnimation.begin().thenPlay("animation.pmb01.down"))
            .triggerableAnim("break", RawAnimation.begin().thenPlay("animation.pmb01.bodyfrontbreak"))

            .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.pmb01.stop"))
            .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb01.boot"))
            .setSoundKeyframeHandler(this::playSounds);

    private final AnimationController<Pmb01mk2Entity> base = new AnimationController<>(this, "basic_move", 1, event -> {
        if (!trigger.isPlayingTriggeredAnimation()) {
            if (this.getAiMode() == AI_MODE_INACTIVE) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.inactive"));
            } else if (event.isMoving()) {
                if (!this.isNoGravity()) {
                    var dir = getDominantMoveDirection();
                    if (dir == MoveDirection.LEFT) {
                        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.walk_right"));
                    } else if (dir == MoveDirection.RIGHT) {
                        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.walk_left"));
                    } else {
                        return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.walk"));
                    }
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.fly"));
                }
            } else {
                if (!this.isNoGravity()) {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.idle"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.fly"));
                }

            }
        } else {
            event.getController().forceAnimationReset();
            return PlayState.STOP;
        }
    }).setSoundKeyframeHandler(this::playSounds);

    @Override
    public void boot() {
        super.boot();
        this.triggerAnim("action_controller", "boot");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(base);
        controllers.add(trigger);
    }

    @Override
    protected void applyPlayerControll() {

    }
}
