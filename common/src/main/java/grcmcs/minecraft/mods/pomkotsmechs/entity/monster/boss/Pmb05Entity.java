package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.BossAerialDiveGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossWalkGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.ArrayList;
import java.util.List;

public class Pmb05Entity extends BaseBossEntity {

    @Override
    public String getMechName() {
        return "pmb05";
    }

    public Pmb05Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
        this.alwaysLookAtTarget = false;

        actionController.registerAction("leaser_short", new BossActionController.BossAction(30,30, this::laserShort));
        actionController.registerAction("leaser_long", new BossActionController.BossAction(40,55, this::laserLong));

        actionController.registerAction("gatling", new BossActionController.BossAction(40,5, this::gatlingAction));
        actionController.registerAction("missile", new BossActionController.BossAction(40,20, this::missileHorizontalAction));
        actionController.registerAction("stomp", new BossActionController.BossAction(40,40, this::stompAction));
        actionController.registerAction("onground", new BossActionController.BossAction(20,16, this::onGroundAction));

        this.registerActionGoal(new SimpleBossWalkGoal(this, getMechData().speed, 30),
                AI_MODE_ALL,
                7
        );
        this.registerActionGoal(
                new BossAerialDiveGoal(
                        this,           // ボスエンティティ
                        15,           // 必要な高度差
                        3,           // 初動ジャンプ強度
                        1,           // ブースター上昇速度
                        2,           // 空中移動速度
                        3.0,           // 急降下速度
                        100            // 最大追跡時間
                ),
                AI_MODE_ALL,
                20
        );

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("leaser_short"), this, false, 3),
                AI_MODE_ALL,
                10
        );
        this.registerActionGoal(new SimpleBossAttackGoal(actionController.getAction("missile"), this, true, true),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );

        this.registerActionGoal(new SimpleBossAttackGoal(actionController.getAction("gatling"), this, true, 5, true),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("leaser_long"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("stomp"), this, false),
                AI_MODE_ALL,
                0, 20,
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

    private void laserShort(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "laser_short");
            laserLookAtPos = target.position();
            this.setLaserLength(1);

        } else if (action.currentActionTick < 14) {
            laserLookAtPos = target.position();

        } else if (action.currentActionTick == 14) {
            this.setLaserLength(1);

        } else if (action.currentActionTick == 17) {
            this.fireLaser(
                    this.position().add(0, this.getEyeHeight(), 0), // start pos
                    laserLookAtPos, // end pos
                    getMechData().laserDamage, // damage
                    6 // explosion scale
            );
            this.setLaserLength((int)currentLaserLength);

        } else if (action.onEndOfAction()) {
            this.setLaserLength(0);
        }

        this.lookControl.setLookAt(laserLookAtPos);
    }

    private List<Vec3> targetPosHistory = new ArrayList<>();
    private void laserLong(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        targetPosHistory.add(target.position());

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "laser_long");
            targetPosHistory.clear();
            laserLookAtPos = target.position();

        } else if (action.currentActionTick < 14) {
            laserLookAtPos = target.position();

        } else if (action.currentActionTick == 14) {
            this.setLaserLength(1);

        } else if (action.currentActionTick > 17 && action.currentActionTick < 50 && action.currentActionTick % 3 == 0) {
            if (targetPosHistory.size() > 6) {
                laserLookAtPos = targetPosHistory.get(targetPosHistory.size() - 5);
            }
            this.fireLaser(
                    this.position().add(0, this.getEyeHeight(), 0), // start pos
                    laserLookAtPos, // end pos
                    getMechData().laserDamage / 3, // damage
                    2 // explosion scale
            );
            this.setLaserLength((int)currentLaserLength);

        } else if (action.onEndOfAction()) {
            this.setLaserLength(0);
        }

        this.lookControl.setLookAt(laserLookAtPos);
    }

    private Vec3 laserLookAtPos = Vec3.ZERO;
    private Vec3 laserStart;
    private Vec3 laserEnd;
    private Vec3 laserDirection;
    private Vec3 laserHitBlockPos;
    private double currentLaserLength = 0;
    private double LASER_WIDTH = 3;
    private double LASER_MAX_RANGE = 200;

    private void fireLaser(Vec3 startPos, Vec3 targetPos, float damage, int explosionScale) {
        laserStart = startPos;
        laserDirection = targetPos.subtract(laserStart).normalize();

        calculateLaserEnd();

        damageEntitiesInLaserPath(damage);

        if (laserHitBlockPos != Vec3.ZERO) {
            if (explosionScale > 5) {
                ExplosionEntity e = new ExplosionEntity(PomkotsMechs.EXPLOSION.get(), this.level(), explosionScale);
                e.setPos(laserHitBlockPos);
                this.level().addFreshEntity(e);

            } else if (explosionScale > 0) {
                var pos = laserHitBlockPos;
                this.level().explode(this,  pos.x, pos.y, pos.z, explosionScale, false, Level.ExplosionInteraction.BLOCK);

            }
        }
    }

    private void calculateLaserEnd() {
        // レイキャストで障害物を検出
        Vec3 start = laserStart;
        Vec3 end = start.add(laserDirection.scale(LASER_MAX_RANGE));


        // より正確な衝突検出が必要な場合は、Minecraft のレイトレースを使用
        var hitResult = this.level().clip(new ClipContext(
                start, end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                this
        ));

        if (hitResult.getType() != HitResult.Type.MISS) {
            currentLaserLength = start.distanceTo(hitResult.getLocation());
            laserHitBlockPos = hitResult.getLocation();
            laserEnd = hitResult.getLocation();
        } else {
            currentLaserLength = LASER_MAX_RANGE;
            laserHitBlockPos = Vec3.ZERO;
            laserEnd = end;
        }
    }

    private void damageEntitiesInLaserPath(float damage) {
        // レーザーパス上のエンティティを検索
        Vec3 start = laserStart;
        Vec3 end = start.add(laserDirection.scale(currentLaserLength));

        // レーザー周辺のAABBを作成
        AABB searchBox = new AABB(start, end).inflate(LASER_WIDTH);

        List<LivingEntity> entities = this.level().getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != this && this.canAttack(entity)
        );

        for (LivingEntity entity : entities) {
            // エンティティがレーザーライン上にいるかチェック
            if (!this.isSelf(entity) && isEntityInLaserPath(entity)) {
                // ダメージを与える
                entity.hurt(this.damageSources().mobAttack(this), damage);
            }
        }
    }

    private boolean isEntityInLaserPath(LivingEntity entity) {
        Vec3 entityPos = entity.position().add(0, entity.getBbHeight() * 0.5, 0);

        // 点と直線の距離を計算
        Vec3 toEntity = entityPos.subtract(laserStart);
        double projectionLength = toEntity.dot(laserDirection);

        // レーザーの範囲内にいるかチェック
        if (projectionLength < 0 || projectionLength > currentLaserLength) {
            return false;
        }

        Vec3 closestPoint = laserStart.add(laserDirection.scale(projectionLength));
        double distance = entityPos.distanceTo(closestPoint);

        return distance <= LASER_WIDTH + entity.getBbWidth() * 0.5;
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

                var offset = this.position();

                var muzzlPos = new Vec3(2 * side, 6, 10);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                be.setPos(offset.add(muzzlPos));

                if (!Utils.isObstructed(this.level(), be, target)) {
                    float[] angle = Utils.getShootingAngle(be, target, true);

                    be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), getMechData().bulletSpeed, 0F);

                    this.level().addFreshEntity(be);
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
            this.triggerAnim("action_controller", "missilepod");

        } else if (action.currentActionTick == 10) {
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    for (int side = -1; side < 2; side += 2) {
                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                                getMechData().missileDamage, getMechData().missileSpeed);
                        be.setMaxRotationAnglePerTick(2);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotX = side * (col * 1.5F + 7F);
                        float slotY = row + 18F;

                        var muzzlPos = new Vec3(slotX, slotY, 6);
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

    public final AnimationController<Pmb05Entity> trigger = new AnimationController<>(this, "action_controller", state -> PlayState.STOP)
            .triggerableAnim("laser_short", RawAnimation.begin().thenPlay("animation.pmb03.laser1"))
            .triggerableAnim("laser_long", RawAnimation.begin().thenPlay("animation.pmb03.laser2"))
            .triggerableAnim("gatling", RawAnimation.begin().thenPlayXTimes("animation.pmb03.gatling", 5))
            .triggerableAnim("missilepod", RawAnimation.begin().thenPlay("animation.pmb03.missilepod"))
            .triggerableAnim("stomp", RawAnimation.begin().thenPlay("animation.pmb03.stomp"))
            .triggerableAnim("jump", RawAnimation.begin().thenPlay("animation.pmb03.jump"))
            .triggerableAnim("onground", RawAnimation.begin().thenPlay("animation.pmb03.onground"))
            .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb03.boot"))
            .setSoundKeyframeHandler(this::playSounds);

    private final AnimationController<Pmb05Entity> base = new AnimationController<>(this, "basic_move", 1, event -> {
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
            return PlayState.CONTINUE;
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

    private static final EntityDataAccessor<Integer> LASER_LENGTH = SynchedEntityData.defineId(Pmb05Entity.class, EntityDataSerializers.INT);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LASER_LENGTH, 0); // 初期値を設定
    }

    private void setLaserLength(int value) {
        this.entityData.set(LASER_LENGTH, value);
    }

    public int getLaserLength() {
        return this.entityData.get(LASER_LENGTH);
    }
}
