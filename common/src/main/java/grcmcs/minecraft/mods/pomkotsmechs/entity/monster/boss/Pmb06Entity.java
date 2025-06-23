package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.BossAerialDiveGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.LaunchMobGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.SimpleBossWalkGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import java.util.*;

public class Pmb06Entity extends BaseBossEntity {
    private final List<EntityType<? extends BaseSmallMonsterEntity>> generateTargetMonsterListA = new ArrayList<>();
    private final List<EntityType<? extends BaseSmallMonsterEntity>> generateTargetMonsterListB = new ArrayList<>();
    private final List<EntityType<? extends BaseSmallMonsterEntity>> generateTargetMonsterListC = new ArrayList<>();
    private final List<EntityType<? extends BaseSmallMonsterEntity>> generateTargetMonsterListD = new ArrayList<>();
    private final List<EntityType<? extends BaseSmallMonsterEntity>> generateTargetMonsterListE = new ArrayList<>();
    private final List<EntityType<? extends BaseSmallMonsterEntity>> generateTargetMonsterListF = new ArrayList<>();

    public List<UUID> spawnedMonsters = new ArrayList<>();

    @Override
    public String getMechName() {
        return "pmb06";
    }

    public Pmb06Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
        this.alwaysLookAtTarget = false;

        actionController.registerAction("gen_mobs", new BossActionController.BossAction(200,35, this::generateMobs));

        actionController.registerAction("small_canon", new BossActionController.BossAction(40,10, this::smallCanonAction));
        actionController.registerAction("large_canon", new BossActionController.BossAction(40,20, this::largeCanonAction));
        actionController.registerAction("missile", new BossActionController.BossAction(40,10, this::missileHorizontalAction));

        actionController.registerAction("hadoho", new BossActionController.BossAction(600,70, this::hadohoAction));

        actionController.registerAction("onground", new BossActionController.BossAction(20,16, this::onGroundAction));

        this.registerActionGoal(
                new SimpleBossWalkGoal(this, getMechData().speed, 30),
                AI_MODE_ALL,
                5
        );
        this.registerActionGoal(
                new BossAerialDiveGoal(
                        this,           // ボスエンティティ
                        20,           // 必要な高度差
                        3,           // 初動ジャンプ強度
                        1,           // ブースター上昇速度
                        2,           // 空中移動速度
                        3.0,           // 急降下速度
                        100            // 最大追跡時間
                ),
                AI_MODE_ALL,
                10
        );

        this.registerActionGoal(
                new LaunchMobGoal(actionController.getAction("gen_mobs"), this, false),
                AI_MODE_ALL,
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("small_canon"), this, true, 5, true),
                AI_MODE_ALL,
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("missile"), this, true, true),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("large_canon"), this, true, true),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                10
        );
        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("hadoho"), this, false),
                new int[]{AI_MODE_BATTLE_PHASE_4},
                10
        );

        this.setNoGravity(false);

        generateTargetMonsterListA.add(PomkotsMechs.PMS01.get());
        generateTargetMonsterListA.add(PomkotsMechs.PMS03.get());
        generateTargetMonsterListA.add(PomkotsMechs.PMS04.get());
        generateTargetMonsterListA.add(PomkotsMechs.PMS05.get());

        generateTargetMonsterListB.add(PomkotsMechs.PMS06.get());
        generateTargetMonsterListB.add(PomkotsMechs.PMS07.get());
        generateTargetMonsterListB.add(PomkotsMechs.PMS08.get());

        generateTargetMonsterListC.add(PomkotsMechs.PMS02.get());

        generateTargetMonsterListD.add(PomkotsMechs.PMS09.get());

        generateTargetMonsterListE.add(PomkotsMechs.PMS08.get());

        generateTargetMonsterListF.add(PomkotsMechs.PMS05.get());
        generateTargetMonsterListF.add(PomkotsMechs.PMS03.get());
    }

    public void tick() {
        if (this.isServerSide()) {
            if (this.firstTick) {
                this.registerAdditionalHitBox(new BossHitBoxEntity(PomkotsMechs.HITBOX_PMB03.get(), this.level(), this));
            }

            if (!spawnedMonsters.isEmpty()) {
                spawnedMonsters.removeIf(uuid -> {
                    Entity e = ((ServerLevel) this.level()).getEntity(uuid);
                    return e == null || !e.isAlive();
                });
            }

            if (this.getTarget() != null) {
                this.setTargetEntityID(this.getTarget().getId());
            }

            if (this.isMoving()) {
                AABB bossBoundingBox = this.getBoundingBox().inflate(10, 0, 10);
//                this.handleCollisionWithBlocks(bossBoundingBox);
//                this.handleCollisionWithLivingEntities(bossBoundingBox);
            }
        }

        super.tick();
    }


    protected boolean isMoving() {
        var vel = this.getDeltaMovement();
        return Mth.abs((float)vel.x) > 0 || Mth.abs((float)vel.z) > 0 || Mth.abs((float)vel.y) > 0;
    }

    protected void handleCollisionWithBlocks(AABB bossBoundingBox) {
        // ロボットのAABBを取得
        bossBoundingBox.setMaxY(bossBoundingBox.minY + 10);
        bossBoundingBox.setMinY(bossBoundingBox.minY + 5);
        breakBlocksInAABB(bossBoundingBox);
    }

    protected void breakBlocksInAABB(AABB aabb) {
        // 範囲の座標を計算
        int minX = Mth.floor(aabb.minX);
        int maxX = Mth.floor(aabb.maxX);
        int minZ = Mth.floor(aabb.minZ);
        int maxZ = Mth.floor(aabb.maxZ);
        int minY = Mth.floor(aabb.minY);
        int maxY = Mth.floor(aabb.maxY);

        // 範囲内のブロックをループ処理
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState blockState = level().getBlockState(pos);

                    // ブロックが破壊可能か確認
                    if (!blockState.isAir()) {
                        level().destroyBlock(pos, false, this);
                    }
                }
            }
        }
    }

    protected void handleCollisionWithLivingEntities(AABB bossBoundingBox) {
        // ボスのAABB（現在の位置からの範囲）
        bossBoundingBox = bossBoundingBox.setMaxY(this.getBoundingBox().minY + 3);

        List<LivingEntity> ents = this.level().getEntitiesOfClass(
                LivingEntity.class, // Projectileエンティティのクラス
                bossBoundingBox.inflate(1.0D), // 判定範囲を少し拡大
                entity -> !(entity instanceof LivingEntity) // 削除されていないエンティティのみ
        );


        for (LivingEntity ent : ents) {
            if (bossBoundingBox.intersects(ent.getBoundingBox())) {
                if (this.isSelf(ent)) {
                    continue;
                }

                var kbVel = ent.position().vectorTo(this.position()).normalize();
                DamageSource ds;
                ds = this.damageSources().generic();

                ent.knockback(4, kbVel.x, kbVel.z);
                ent.hurt(ds, 20);
            }
        }
    }

    private void smallCanonAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction() && action.isFirstLoop()) {
            this.triggerAnim("action_controller", "small_canon");

        } else if (action.currentActionTick == 3) {
            for (int side = -1; side < 2; side += 2) {
                BulletGrenadeEntity be = new BulletGrenadeEntity(PomkotsMechs.BULLET_GRENADE.get(), this.level(), this,
                        getMechData().bulletDamage);
                be.setNoGravity(true);
                be.setExplosionScale(1);

                var offset = this.position();

                var muzzlPos = new Vec3(side * 2, 28, 15);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                be.setPos(offset.add(muzzlPos));

                if (!Utils.isObstructed(this.level(), be, target)) {
                    float[] angle = Utils.getShootingAngle(be, target, true);

                    be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(),
                            getMechData().bulletSpeed, 0F);

                    this.level().addFreshEntity(be);
                }
            }
        }
    }

    private void largeCanonAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction() && action.isFirstLoop()) {
            this.triggerAnim("action_controller", "large_canon");

        } else if (action.currentActionTick == 5) {
            BulletGrenadeEntity be = new BulletGrenadeEntity(PomkotsMechs.BULLET_GRENADE.get(), this.level(), this,
                    getMechData().grenadeDamage);
            be.setNoGravity(true);

            var offset = this.position();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）

            var muzzlPos = new Vec3(0, 24, 15);
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
            this.triggerAnim("action_controller", "missile");

        } else if (action.currentActionTick == 10) {
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    for (int side = -1; side < 2; side += 2) {
                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                                getMechData().missileDamage, getMechData().missileSpeed);
                        be.setMaxRotationAnglePerTick(2);

                        var offset = this.position();
                        var muzzlPos = new Vec3((10 + col * 2) * side , 40 + row * 2, 10);
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

    private int spawnedCount = 0;
    private List<EntityType<? extends BaseSmallMonsterEntity>> targetPool = generateTargetMonsterListA;
    private boolean isFly = false;
    private int spawnIndex = 0;

    private void generateMobs(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }

        if (action.onStartOfAction()) {
            if (spawnedMonsters.isEmpty()) {
                switch(spawnIndex % 6) {
                    case 0:
                        spawnedCount = 2;
                        targetPool = generateTargetMonsterListA;
                        isFly = false;
                        this.triggerAnim("action_controller", "mob_launch_front");
                        break;
                    case 1:
                        spawnedCount = 2;
                        targetPool = generateTargetMonsterListB;
                        isFly = false;
                        this.triggerAnim("action_controller", "mob_launch_front");
                        break;
                    case 2:
                        spawnedCount = 2;
                        targetPool = generateTargetMonsterListD;
                        isFly = true;
                        this.triggerAnim("action_controller", "mob_launch_side");
                        break;
                    case 3:
                        spawnedCount = 3;
                        targetPool = generateTargetMonsterListE;
                        isFly = false;
                        this.triggerAnim("action_controller", "mob_launch_side");
                        break;
                    case 4:
                        spawnedCount = 2;
                        targetPool = generateTargetMonsterListF;
                        isFly = false;
                        this.triggerAnim("action_controller", "mob_launch_side");
                        break;
                    case 5:
                        spawnedCount = 2;
                        targetPool = generateTargetMonsterListC;
                        isFly = true;
                        this.triggerAnim("action_controller", "mob_launch_side");
                        break;
                }
                spawnIndex++;
            } else {
                spawnedCount = 0;
            }

        } else if (action.currentActionTick % 5 == 0 && spawnedCount > 0) {
            Random rand = new Random();
            var level = this.level();

            for (int side = -1; side < 2; side += 2) {
                Vec3 muzzlPos;

                if (isFly) {
                    muzzlPos = new Vec3(10 * side, 24, 5);
                } else {
                    muzzlPos = new Vec3(10 * side, 24, 10);
                }

                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());

                BaseSmallMonsterEntity mob = getRandomMobType(rand).create(level);

                if (mob != null) {
                    mob.setPos(muzzlPos.x, muzzlPos.y, muzzlPos.z);
                    level.addFreshEntity(mob);

                    Vec3 dir;
                    if (isFly) {
                        dir = new Vec3(side,0,0).yRot((float) Math.toRadians((-1.0) * this.getYRot())).scale(5 + spawnedCount);
                    } else {
                        mob.setClosed(true);
                        dir = getDirectionVector(this.getYRot(), this.getXRot()).normalize().scale(3 + spawnedCount);
                    }
                    mob.setDeltaMovement(dir.x, 1, dir.z);

                    spawnedMonsters.add(mob.getUUID());

                }
            }

            spawnedCount--;
        }
    }

    public static Vec3 getDirectionVector(float yawDegrees, float pitchDegrees) {
        // ラジアンに変換
        float yaw = (float) Math.toRadians(yawDegrees);
        float pitch = (float) Math.toRadians(pitchDegrees);

        // 向きベクトルを計算（OpenGL方式）
        double x = -Mth.sin(yaw) * Mth.cos(pitch);
        double y = -Mth.sin(pitch);
        double z = Mth.cos(yaw) * Mth.cos(pitch);

        return new Vec3(x, y, z);
    }

    private EntityType<? extends BaseSmallMonsterEntity> getRandomMobType(Random rand) {
        int max = targetPool.size();
        return targetPool.get(Math.abs(rand.nextInt()) % max);
    }

    private List<Vec3> targetPosHistory = new ArrayList<>();
    private void hadohoAction(BossActionController.BossAction action) {
        var target = this.getTarget();
        if (target == null) {
            return;
        }
        this.rotateToTarget(target);
        this.setDeltaMovement(0,0,-0.1);
        this.hasImpulse = true;

        targetPosHistory.add(target.position());

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "hadoho");
            targetPosHistory.clear();
            laserLookAtPos = target.position();

        } else if (action.onEndOfAction()) {
            this.setLaserLength(0);

        } else if (action.currentActionTick < 32) {
            laserLookAtPos = target.position();

        } else if (action.currentActionTick == 32) {
            this.setLaserLength(1);

        } else if (action.currentActionTick > 35 && action.currentActionTick < 74 && action.currentActionTick % 3 == 0) {
            if (targetPosHistory.size() > 7) {
                laserLookAtPos = targetPosHistory.get(targetPosHistory.size() - 6);
            }
            this.fireLaser(
                    this.position().add(0, 3.6F, 0), // start pos
                    laserLookAtPos, // end pos
                    getMechData().laserDamage / 2, // damage
                    2 // explosion scale
            );
            this.setLaserLength((int)currentLaserLength);

        }

        this.lookControl.setLookAt(laserLookAtPos);
    }

    public float getEyeHeight(Pose pose) {
        return 3.6F;
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
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
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

    public final AnimationController<Pmb06Entity> trigger = new AnimationController<>(this, "action_controller", state -> PlayState.STOP)
            .triggerableAnim("hadoho", RawAnimation.begin().thenPlay("animation.pmb03.hadoho_1").thenPlay("animation.pmb03.hadoho_2"))
            .triggerableAnim("small_canon", RawAnimation.begin().thenPlayXTimes("animation.pmb03.small_canon", 5))
            .triggerableAnim("large_canon", RawAnimation.begin().thenPlay("animation.pmb03.large_canon"))
            .triggerableAnim("mob_launch_front", RawAnimation.begin().thenPlay("animation.pmb03.mob_launch_front"))
            .triggerableAnim("mob_launch_side", RawAnimation.begin().thenPlay("animation.pmb03.mob_launch_side"))
            .triggerableAnim("missile", RawAnimation.begin().thenPlay("animation.pmb03.missile"))
            .triggerableAnim("jump", RawAnimation.begin().thenPlay("animation.pmb03.jump"))
            .triggerableAnim("onground", RawAnimation.begin().thenPlay("animation.pmb03.onground"))
            .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb03.boot"))
            .setSoundKeyframeHandler(this::playSounds);

    private final AnimationController<Pmb06Entity> base = new AnimationController<>(this, "basic_move", 1, event -> {
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

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        ListTag list = new ListTag();
        for (UUID uuid : spawnedMonsters) {
            list.add(NbtUtils.createUUID(uuid));
        }
        compound.put(PomkotsMechs.nbtName("SpawnedMonstersPmb06"), list);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        spawnedMonsters.clear();
        ListTag list = (ListTag) compound.get(PomkotsMechs.nbtName("SpawnedMonstersPmb06"));

        if (list != null) {
            for (Tag t : list) {
                spawnedMonsters.add(NbtUtils.loadUUID(t));
            }
        }
    }

    private static final EntityDataAccessor<Integer> LASER_LENGTH = SynchedEntityData.defineId(Pmb06Entity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(Pmb06Entity.class, EntityDataSerializers.INT);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LASER_LENGTH, 0); // 初期値を設定
        this.entityData.define(TARGET, Integer.MIN_VALUE); // 初期値を設定
    }

    private void setTargetEntityID(int id) {
        this.entityData.set(TARGET, id);

    }

    public int getTargetEntityID() {
        return this.entityData.get(TARGET);
    }

    private void setLaserLength(int value) {
        this.entityData.set(LASER_LENGTH, value);
    }

    public int getLaserLength() {
        return this.entityData.get(LASER_LENGTH);
    }
}
