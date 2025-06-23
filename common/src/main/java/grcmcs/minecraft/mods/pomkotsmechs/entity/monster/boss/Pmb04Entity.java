package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.EarthraiseEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
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

public class Pmb04Entity extends BaseBossEntity {

    @Override
    public String getMechName() {
        return "pmb04";
    }

    public Pmb04Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
        this.alwaysLookAtTarget = false;

        actionController.registerAction("saber_tate", new BossActionController.BossAction(40,16, this::saber_tate));
        actionController.registerAction("saber_upper", new BossActionController.BossAction(40,20, this::saber_upper));
        actionController.registerAction("saber_circle", new BossActionController.BossAction(40,30, this::saber_circle));
        actionController.registerAction("saber_jump", new BossActionController.BossAction(80,60, this::saber_jump));
        actionController.registerAction("gatling", new BossActionController.BossAction(40,5, this::gatlingAction));
        actionController.registerAction("missile", new BossActionController.BossAction(40,20, this::missileHorizontalAction));
        actionController.registerAction("onground", new BossActionController.BossAction(20,15, this::onGroundAction));

        this.registerActionGoal(
                new SimpleBossWalkGoal(this, getMechData().speed, 10),
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
                new SimpleBossAttackGoal(actionController.getAction("saber_tate"), this, false),
                AI_MODE_ALL,
                0,30,
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("saber_circle"), this, false),
                AI_MODE_ALL,
                0,30,
                10
        );
        this.registerActionGoal(
                new BossDashGoal(
                    this,           // ボスエンティティ
                    getMechData().speed * 4,           // ダッシュ速度
                    15.0,          // 最大回転速度（度/ティック）
                    40,           // 開始距離
                    20,           // 停止距離
                    30,            // 最大継続時間（3秒）
                    0.3            // ホーミング強度（0.0-1.0）
                ),
                AI_MODE_ALL,
                20
        );

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("saber_jump"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile"), this, true, true),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );

        this.registerActionGoal(
                new BossDashAttackGoal(
                    this,           // ボスエンティティ
                    getMechData().speed * 6,           // ダッシュ速度
                    15.0,          // 最大回転速度（度/ティック）
                    40,           // 開始距離
                    15,           // 停止距離
                    30,            // 最大継続時間（3秒）
                    0.3            // ホーミング強度（0.0-1.0）
                ),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("gatling"), this, true, 5, true),
                new int[]{AI_MODE_BATTLE_PHASE_4},
                10
        );

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("saber_upper"), this, false),
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

    private void saber_tate(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "saber_tate");

        } else if (action.currentActionTick == 8) {
            this.rangeAttack(
                    new Vec3(10, 18F, 30F),
                    new Vec3(-10, -3F, -5F),
                    getMechData().meleeDamage,
                    2);
        }
    }

    private void saber_circle(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "saber_circle");

        } else if (action.currentActionTick == 8) {
            this.rangeAttack(
                    new Vec3(20, 10F, 20F),
                    new Vec3(-20, -3F, -20F),
                    getMechData().meleeDamage * 2 / 3,
                    2);
        }
    }

    private void saber_upper(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "saber_upper");

        } else if (action.currentActionTick == 8) {
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

    private void saber_jump(BossActionController.BossAction action) {
        float attackRange = 20;
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "saber_jump1");

        } else if (action.currentActionTick > 2 && action.currentActionTick < 10) {
            this.rotateToTarget(target);
            this.hasImpulse = true;

        } else if (action.currentActionTick == 10) {
            // ターゲット位置への弾道計算
            Vec3 startPos = this.position();
            Vec3 targetPos = target.position();

            // 高さの差を考慮
            double deltaX = targetPos.x - startPos.x;
            double deltaY = targetPos.y - startPos.y;
            double deltaZ = targetPos.z - startPos.z;
            double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

            // 重力を考慮した弾道計算
            double gravity = 0.08; // Minecraftの重力値
            double velocityY = Math.sqrt(gravity * Math.max(4.0, deltaY + 4.0));
            double time = Math.sqrt(2 * (deltaY + velocityY * velocityY / (2 * gravity)) / gravity);

            double velocityX = deltaX / time;
            double velocityZ = deltaZ / time;

            // 速度制限（あまりに高速にならないように）
            double maxHorizontalSpeed = 10;
            double horizontalSpeed = Math.sqrt(velocityX * velocityX + velocityZ * velocityZ);
            if (horizontalSpeed > maxHorizontalSpeed) {
                velocityX = velocityX / horizontalSpeed * maxHorizontalSpeed;
                velocityZ = velocityZ / horizontalSpeed * maxHorizontalSpeed;
            }

            // ジャンプ実行
            this.setDeltaMovement(velocityX, velocityY, velocityZ);
            this.hasImpulse = true;

        } else if (action.currentActionTick > 11 && action.currentActionTick < action.maxActionTick - 12) {
            if (this.onGround() || this.distanceToSqr(target) <= attackRange * attackRange || action.onEndOfAction()) {
                this.rangeAttack(
                        new Vec3(20, 10F, 20F),
                        new Vec3(-20, -6F, -20F),
                        getMechData().meleeDamage * 1.5F,
                        2);
                this.triggerAnim("action_controller", "saber_jump2");
                action.currentActionTick = action.maxActionTick - 11;
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
            BulletGrenadeEntity be = new BulletGrenadeEntity(PomkotsMechs.BULLET_GRENADE.get(), this.level(), this,
                    getMechData().bulletDamage);
            be.setNoGravity(true);
            be.setExplosionScale(1);

            var offset = this.position();

            var muzzlPos = new Vec3(7, 14, 10);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            be.setPos(offset.add(muzzlPos));

            if (!Utils.isObstructed(this.level(), be, target)) {
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
            this.triggerAnim("action_controller", "missile");

        } else if (action.currentActionTick == 15) {
            for (int row = 0; row < 4; row++) {
                for (int col = 0; col < 1; col++) {
                    for (int side = -1; side < 2; side += 2) {
                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                                getMechData().missileDamage, getMechData().missileSpeed);
                        be.setMaxRotationAnglePerTick(2);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotZ = (col * 0.8F + 8F);
                        float slotY = row + 8F;

                        var muzzlPos = new Vec3(side * 7, slotY, slotZ);
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

    private final AnimationController<Pmb04Entity> trigger = new AnimationController<>(this, "action_controller", state -> PlayState.STOP)
            .triggerableAnim("saber_tate", RawAnimation.begin().thenPlay("animation.pmb01.attacktate"))
            .triggerableAnim("saber_upper", RawAnimation.begin().thenPlay("animation.pmb01.attackupper"))
            .triggerableAnim("saber_circle", RawAnimation.begin().thenPlay("animation.pmb01.attackcircle"))
            .triggerableAnim("saber_jump1", RawAnimation.begin().thenPlay("animation.pmb01.attackjump1").thenLoop("animation.pmb01.attackjump2"))
            .triggerableAnim("saber_jump2", RawAnimation.begin().thenPlay("animation.pmb01.attackjump3"))
            .triggerableAnim("gatling", RawAnimation.begin().thenPlayXTimes("animation.pmb01.attackgatling", 5))
            .triggerableAnim("missile", RawAnimation.begin().thenPlay("animation.pmb01.attackmissile"))
            .triggerableAnim("jump", RawAnimation.begin().thenPlay("animation.pmb01.jump"))
            .triggerableAnim("onground", RawAnimation.begin().thenPlay("animation.pmb01.onground"))
            .triggerableAnim("hurt", RawAnimation.begin().thenPlay("animation.pmb01.hurt"))
            .triggerableAnim("down", RawAnimation.begin().thenPlay("animation.pmb01.down"))
            .triggerableAnim("break", RawAnimation.begin().thenPlay("animation.pmb01.bodyfrontbreak"))

            .triggerableAnim("dash", RawAnimation.begin().thenPlay("animation.pmb01.dash1").thenLoop("animation.pmb01.dash2"))
            .triggerableAnim("dash_attack", RawAnimation.begin().thenPlay("animation.pmb01.dashAttack1").thenLoop("animation.pmb01.dashAttack2"))

            .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.pmb01.stop"))
            .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb01.boot"))
            .setSoundKeyframeHandler(this::playSounds);

    private final AnimationController<Pmb04Entity> base = new AnimationController<>(this, "basic_move", 1, event -> {
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
