package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.BossAerialDiveGoal2;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossWalkGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.EarthbreakEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.EarthraiseEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.SlashEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.*;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
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
        actionController.registerAction("aerial_attack", new BossActionController.BossAction(200,100, this::aerial_punch));
        actionController.registerAction("gatling", new BossActionController.BossAction(40,5, this::gatlingAction));
        actionController.registerAction("grenade", new BossActionController.BossAction(40,20, this::grenadeAction));
        actionController.registerAction("missile", new BossActionController.BossAction(40,21, this::missileHorizontalAction));
        actionController.registerAction("missilev", new BossActionController.BossAction(40,21, this::missileVerticalAction));

        actionController.registerAction("large_missile", new BossActionController.BossAction(600,20, this::missileLargeAction));
        actionController.registerAction("onground", new BossActionController.BossAction(20,15, this::onGroundAction));
        actionController.registerAction("break", new BossActionController.BossAction(20,95, this::breakAction));

        this.registerActionGoal(
                new SimpleBossWalkGoal(this, getMechData().speed, 30),
                AI_MODE_ALL,
                10
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
                20
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missilev"), this, true, true),
                AI_MODE_ALL,
                20
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
                new SimpleBossAttackGoal(actionController.getAction("aerial_attack"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                50,300,
                20
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
            this.setNoGravity(false);
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

    private boolean punched = false;
    private void aerial_punch(BossActionController.BossAction action) {
        float punchRange = 35;
        var target = this.getTarget();
        if (target == null) {
            this.setNoGravity(false);
            return;
        }

        // === フェーズ1: 飛び立ち ===
        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "attack_arial_punch_01");
            punched = false;
        } else if (action.currentActionTick == 10) {
            // 飛び立ちジャンプ
            Vec3 toTarget = target.position().subtract(this.position()).normalize();
            this.setDeltaMovement(
                    toTarget.x * 1.5,
                    4.0,        // 上方向へ強く
                    toTarget.z * 1.5
            );
            this.hasImpulse = true;
            this.setNoGravity(true); // 空中制御のため重力オフ

            // === フェーズ2: 空中ホーミング ===
        } else if (action.currentActionTick > 10 && action.currentActionTick < action.maxActionTick - 20) {
            Vec3 myPos     = this.position();
            Vec3 targetPos = target.getBoundingBox().getCenter();
            Vec3 toTarget  = targetPos.subtract(myPos);
            double dist    = toTarget.length();

            // ホーミング：現在速度とターゲット方向をブレンド
            Vec3 current    = this.getDeltaMovement();
            Vec3 desired    = toTarget.normalize().scale(5); // 目標速度
            Vec3 newVelocity = current.scale(0.85).add(desired.scale(0.15)); // 徐々に向きを変える

            // 速度制限
            double maxSpeed = 5;
            if (newVelocity.length() > maxSpeed) {
                newVelocity = newVelocity.normalize().scale(maxSpeed);
            }
            if (newVelocity.y >= 2) {
                newVelocity = new Vec3(newVelocity.x, 2, newVelocity.z);
            }

            this.setDeltaMovement(newVelocity);
            this.rotateToTarget(target);

            // === フェーズ3: 一定距離でパンチ発動 ===
            if (dist <= punchRange) {
                punched = true;
                this.triggerAnim("action_controller", "attack_arial_punch_02");
                action.currentActionTick = action.maxActionTick - 19; // 着地フェーズへ
            }

            // === フェーズ4: 着地 ===
        } else if (action.currentActionTick >= action.maxActionTick - 20) {
            if (!punched && onGround()) {
                action.currentActionTick = action.maxActionTick;
                this.setNoGravity(false);
            }
            if (punched && action.currentActionTick - (action.maxActionTick - 20) == 7 ) {
                firePunch(target);
            }
            if (action.onEndOfAction()) {
                this.setNoGravity(false);
            }
        } else {
            this.setNoGravity(false);
        }
    }

    private void firePunch(LivingEntity target) {
        if (this.level().isClientSide()) return;

        // パンチの当たり判定（ボスの前方向に大きめのAABB）
        Vec3 forward = new Vec3(0, 0, 30)
                .yRot((float) Math.toRadians(-1.0 * this.getYRot()));
        Vec3 punchCenter = this.position().add(forward);

        AABB punchBox = new AABB(
                punchCenter.x - 15, punchCenter.y - 15, punchCenter.z - 15,
                punchCenter.x + 15, punchCenter.y + 15, punchCenter.z + 15
        );

        for (Entity ent : this.level().getEntities(this, punchBox)) {
            if (ent instanceof LivingEntity le && !isSelf(le)) {
                le.hurt(this.damageSources().mobAttack(this), 50);
                // 吹き飛ばし
                Vec3 kb = le.position().subtract(this.position()).normalize();
                le.knockback(3.0, -kb.x, -kb.z);
            }
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
            be.setStunPoint(2);

            var offset = this.position();

            var muzzlPos = new Vec3(-8, 8.0F, 10);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            be.setPos(offset.add(muzzlPos));

            float[] angle = Utils.getShootingAngle2(be, target, true, false, getMechData().bulletSpeed, 0);

            be.shootFromRotation(be, angle[0], angle[1], 0, getMechData().bulletSpeed, 0F);

            this.level().addFreshEntity(be);
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
            BulletGrenadeLargeEntity be = new BulletGrenadeLargeEntity(PomkotsMechs.BULLET_GRENADE_LARGE.get(), this.level(), this,
                    getMechData().grenadeDamage);
            be.setNoGravity(true);
            be.setExplosionScale((int)getMechData().grenadeExplosionScale);

            var offset = this.position();

            var muzzlPos = new Vec3(8, 8.0F, 10);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            be.setPos(offset.add(muzzlPos));

            float[] angle = Utils.getShootingAngle2(be, target, true, false, getMechData().grenadeSpeed, 0);

            be.shootFromRotation(be, angle[0], angle[1], 0,
                    getMechData().grenadeSpeed, 0F);

            this.level().addFreshEntity(be);
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
                        MissileGenericEnemyEntity be = new MissileGenericEnemyEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                                getMechData().missileDamage, getMechData().missileSpeed * 2);
                        be.setMaxRotationAnglePerTick(2);
                        be.setSwitchTick(12);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotZ = (col * 0.8F + 8F);
                        float slotY = row + 15;

                        var muzzlPos = new Vec3(side * 10, slotY, 0);
                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                        be.setPos(offset.add(muzzlPos));

                        float[] angle = Utils.getShootingAngle(be, target, true);

                        be.shootFromRotation(be, angle[0], angle[1] - side * 80, this.getFallFlyingTicks(),
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
            this.triggerAnim("action_controller", "attackmissile");

        } else if (action.currentActionTick == 15) {
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 1; col++) {
                    for (int side = -1; side < 2; side += 2) {
                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                                getMechData().missileDamage, getMechData().missileSpeed * 2);
                        be.setMaxRotationAnglePerTick(7);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotZ = - row * 0.8F;

                        var muzzlPos = new Vec3(side * 10, 16, slotZ);
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

    @Override
    protected void onStun() {
        this.triggerAnim("action_controller", "on_stun");
    }

    @Override
    protected void offStun() {
        this.triggerAnim("action_controller", "off_stun");
    }

    private final AnimationController<Pmb01mk2Entity> trigger = new AnimationController<>(this, "action_controller", state -> PlayState.STOP)
            .triggerableAnim("attackpunch", RawAnimation.begin().thenPlay("animation.pmb01.attackpunch"))
            .triggerableAnim("attackupper", RawAnimation.begin().thenPlay("animation.pmb01.attackupper"))
            .triggerableAnim("attackjump", RawAnimation.begin().thenPlay("animation.pmb01.attackjump"))
            .triggerableAnim("attackgatling", RawAnimation.begin().thenPlayXTimes("animation.pmb01.attackgatling", 5))
            .triggerableAnim("attackgrenade", RawAnimation.begin().thenPlay("animation.pmb01.attackgrenade"))
            .triggerableAnim("attackmissile", RawAnimation.begin().thenPlay("animation.pmb01.attackmissile"))
            .triggerableAnim("attacklargemissile", RawAnimation.begin().thenPlay("animation.pmb01.attacklargemissile"))
            .triggerableAnim("attack_arial_punch_01", RawAnimation.begin().thenPlay("animation.pmb01.attack_arial_punch_01"))
            .triggerableAnim("attack_arial_punch_fly", RawAnimation.begin().thenLoop("animation.pmb01.fly"))
            .triggerableAnim("attack_arial_punch_02", RawAnimation.begin().thenPlay("animation.pmb01.attack_arial_punch_02"))

            .triggerableAnim("jump", RawAnimation.begin().thenPlay("animation.pmb01.jump"))
            .triggerableAnim("onground", RawAnimation.begin().thenPlay("animation.pmb01.onground"))
            .triggerableAnim("hurt", RawAnimation.begin().thenPlay("animation.pmb01.hurt"))
            .triggerableAnim("down", RawAnimation.begin().thenPlay("animation.pmb01.down"))
            .triggerableAnim("break", RawAnimation.begin().thenPlay("animation.pmb01.bodyfrontbreak"))

            .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.pmb01.stop"))
            .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb01.boot"))

            .triggerableAnim("on_stun", RawAnimation.begin().thenPlay("animation.pmb01.hurt").thenPlayAndHold("animation.pmb01.down"))
            .triggerableAnim("off_stun", RawAnimation.begin().thenPlay("animation.pmb01.up"))

            .triggerableAnim("reset", RawAnimation.begin().thenPlay("animation.pmb01.idle"))

            .setSoundKeyframeHandler(this::playSounds);

    private final AnimationController<Pmb01mk2Entity> base = new AnimationController<>(this, "basic_move", 0, event -> {
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
