package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEnemyEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;

import java.util.List;

public class Pmb99Entity extends BaseBossEntity {
    private static final Vec3 OFFSET_HUMMER_1 = new Vec3(0,0,15);
    private static final Vec3 OFFSET_HUMMER_2 = new Vec3(0,0,0);

    @Override
    public String getMechName() {
        return "pmb99";
    }

    public Vec3 seatPos = Vec3.ZERO;
    public float[] seatRots = new float[2];
    private int inAirTick = 0;

    public Pmb99Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
        this.alwaysLookAtTarget = false;

        actionController.registerAction("evasion", new BossActionController.BossAction(20,20, this::evasion));
        actionController.registerAction("jump", new BossActionController.BossAction(20,6, this::jump));
        actionController.registerAction("gatling", new BossActionController.BossAction(1,10, this::gatlingAction));
        actionController.registerAction("missile", new BossActionController.BossAction(40,20, this::missileHorizontalAction));
        actionController.registerAction("throw", new BossActionController.BossAction(40,30, this::throwAction));
        actionController.registerAction("onground", new BossActionController.BossAction(20,15, this::onGroundAction));
        actionController.registerAction("startriding", new BossActionController.BossAction(20,25, this::onStartRiding));
        actionController.registerAction("drill", new BossActionController.BossAction(20,200, this::drillAction));
        actionController.registerAction("punch", new BossActionController.BossAction(20,20, this::punch));

        this.registerActionGoal(
                new SimpleBossWalkGoal(this, getMechData().speed, 10),
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
                new SimpleBossAttackGoal(actionController.getAction("missile"), this, true, true),
                new int[]{AI_MODE_BATTLE_PHASE_2, AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                20
        );

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("throw"), this, true, true),
                AI_MODE_ALL,
                20
        );

        this.registerActionGoal(
                new BossDashAttackDrillGoal(
                    this,           // ボスエンティティ
                    getMechData().speed * 6,           // ダッシュ速度
                    15.0,          // 最大回転速度（度/ティック）
                    40,           // 開始距離
                    15,           // 停止距離
                    30,            // 最大継続時間（3秒）
                    0.3            // ホーミング強度（0.0-1.0）
                ),
                new int[]{AI_MODE_BATTLE_PHASE_3, AI_MODE_BATTLE_PHASE_4},
                20
        );

        this.registerActionGoal(
                new SimpleBossAttackGoal(actionController.getAction("gatling"), this, true, 5, true),
                AI_MODE_ALL,
                20
        );

        this.setNoGravity(false);
    }

    protected void registerTargetSelectorGoals() {

    }

    private int ridingTicks = 0;

    public void tick() {
        if (this.firstTick && this.isServerSide()) {
            this.registerAdditionalHitBox(new BossHitBoxEntity(PomkotsMechs.HITBOX_PMB03.get(), this.level(), this));
        }

        if (this.onGround()) {
            if (this.isServerSide() && inAirTick > 10) {
                this.actionController.getAction("onground").startAction();
            }
            inAirTick = 0;
        } else {
            inAirTick++;
        }

        if (this.isServerSide()) {
            if (this.isVehicle()) {
                ridingTicks++;

                if (ridingTicks == 1) {
                    this.triggerAnim("action_controller", "gattai");
                } else if (ridingTicks == 15) {
                    this.actionController.getAction("startriding").startAction();
                }
            } else {
                ridingTicks = 0;
                setRiding(false);
            }
        }

        super.tick();

        if (this.rock != null) {
            var throwActionTick = actionController.getAction("throw").currentActionTick;
            if (throwActionTick > 10 && throwActionTick <= 22 && this.rock.isAlive()) {
                this.rock.moveTo(getRockPosition(throwActionTick));
            } else {
                this.rock = null;
            }
        }
    }

    @Override
    protected void applyPlayerControll() {
        if (this.isClientSide()) {
            return;
        }
        var driver = this.getControllingPassenger();
        if (driver instanceof Pmvc01Entity pmvc) {
            var input = pmvc.getDriverInput();

            if (input == null || driver == null) return;

            if (input.isReloadReleased()) {
                if (!this.getActionController().getAction("drill").isInAction()) {
                    this.getActionController().getAction("drill").startAction();
                } else {
                    this.getActionController().getAction("drill").stopAction();
                    this.triggerAnim("action_controller", "boot2");
                }
            }

            if (!this.getActionController().getAction("drill").isInAction()) {
                if (input.isWeaponRightHandPressed() && !this.getActionController().getAction("gatling").isInAction()) {
                    this.getActionController().getAction("gatling").startAction();
                }

                if (input.isWeaponLeftHandPressed() && !this.getActionController().getAction("punch").isInAction()) {
                    this.getActionController().getAction("punch").startAction();
                }

                if (input.isWeaponRightShoulderPressed() && !this.getActionController().getAction("throw").isInAction()) {
                    this.getActionController().getAction("throw").startAction();
                }

                if (input.isWeaponLeftShoulderReleased() && !this.getActionController().getAction("missile").isInAction()) {
                    this.getActionController().getAction("missile").startAction();
                }

                if (this.onGround()) {
                    if (input.isEvasionPressed()) {
                        var act = this.getActionController().getAction("evasion");
                        if (!act.isInAction() && !act.isInCooltime()) {
                            act.startAction();
                        }
                    } else if (input.isJumpPressed()) {
                        var act = this.getActionController().getAction("jump");
                        if (!act.isInAction() && !act.isInCooltime()) {
                            act.startAction();
                        }
                    }
                }
            }

            if (pmvc.getDrivingPassenger() == null) {
                return;
            }

            if (this.getActionController().isInActionAll()) {
                return;
            }

            applyPlayerInputInAirActions(this.getDriverInput());

            var pilot = (ServerPlayer)pmvc.getDrivingPassenger();

            float forward = pilot.zza * 0.5F;
            float strafe = -pilot.xxa * 0.5F;

            // 押されてないなら停止
            if (forward == 0 && strafe == 0) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.8, 1, 0.8));
                return;
            }

            Vec3 forwardVec = ((ServerPlayer)(pmvc.getDrivingPassenger())).getCamera().getForward();
            Vec3 rightVec   = forwardVec.cross(new Vec3(0, 1, 0)).normalize();

            // WASD合成
            Vec3 moveDir =
                    forwardVec.scale(forward)
                            .add(rightVec.scale(strafe));

            if (moveDir.lengthSqr() > 0.0001) {
                moveDir = moveDir.normalize();

                // 移動
                Vec3 velocity = moveDir.scale(1);

                this.setDeltaMovement(
                        velocity.x,
                        this.getDeltaMovement().y,
                        velocity.z
                );

                // 向きを移動方向へ
                float targetYaw =
                        (float)(Math.atan2(velocity.z, velocity.x) * (180f/Math.PI)) - 90f;

                this.setYRot(rotlerp(this.getYRot(), targetYaw, 15f));
                this.setYBodyRot(this.getYRot());
                this.setYHeadRot(this.getYRot());
            }
        }
    }

    public static float rotlerp(float current, float target, float maxStep) {

        float delta = Mth.wrapDegrees(target - current);

        if (delta > maxStep) delta = maxStep;
        if (delta < -maxStep) delta = -maxStep;

        return target;
    }

    protected void applyPlayerInputInAirActions(DriverInput driverInput) {
        if (driverInput == null) {
            return;
        }

        if (!this.onGround()) {
            if (driverInput.isJumpPressed()) {
                if (!tryVerticalBoost()) {
                    this.push(0, -0.18 * 0.9800000190734863D, 0);
                }
            } else {
                this.push(0, -0.18 * 0.9800000190734863D, 0);
            }
        } else {
            this.setNoGravity(false);
        }
    }

    protected boolean tryVerticalBoost() {
        if (this.getDeltaMovement().y() < getVerticalBoostMaxSpeed()) {
            this.push(0, getVerticalBoostAcceleration(), 0);

            return true;
        }
        return false;
    }

    protected float getVerticalBoostMaxSpeed() {
        return 2;
    }

    protected float getVerticalBoostAcceleration() {
        return 0.2F;
    }

    @Override
    public void addStunPoint(int point) {
        int current = getStunPoint();

        if (current < STUN_STUN_START) {
            current += point/4;
            if (current >= STUN_STUN_START) {
                current = STUN_MAX;
                this.onStun();

                this.getAttribute(Attributes.ARMOR).setBaseValue(0);

                this.goalSelector.getRunningGoals().forEach(WrappedGoal::stop);
            }
        }

        setStunPoint(current);
    }

    private int[] userIntentionForDirectionFromKey = new int[] {0, 0};

    private void evasion(BossActionController.BossAction action) {
        if (action.onStartOfAction()) {
            userIntentionForDirectionFromKey = getUserIntentionForDirectionFromKey();

            int forwardIntention = userIntentionForDirectionFromKey[0];
            int sidewayIntention = userIntentionForDirectionFromKey[1];

            String animationName;
            if (sidewayIntention > 0) {
                animationName = "evasion_left";
            } else if (sidewayIntention < 0) {
                animationName = "evasion_right";
            } else if (forwardIntention < 0) {
                animationName = "evasion_back";
            } else {
                animationName = "evasion_front";
            }

            this.triggerAnim("action_controller", animationName);

        } else if (action.currentActionTick == 4) {
            startEvasion(10, userIntentionForDirectionFromKey);
        }
    }

    protected int[] getUserIntentionForDirectionFromKey() {
        int forwardIntention = 0;
        int sidewayIntention = 0;

        var driverInput = this.getDriverInput();

        if (driverInput != null) {
            if (driverInput.isForwardPressed()) {
                forwardIntention = 1;
            } else if (driverInput.isBackPressed()) {
                forwardIntention = -1;
            } else {
                forwardIntention = 0;
            }

            if (driverInput.isRightPressed()) {
                sidewayIntention = -1;
            } else if (driverInput.isLeftPressed()) {
                sidewayIntention = 1;
            } else {
                sidewayIntention = 0;
            }
        }

        return new int[] {forwardIntention, sidewayIntention};
    }

    private DriverInput getDriverInput() {
        var driver = this.getControllingPassenger();
        if (driver instanceof Pmvc01Entity pmvc) {
            var input = pmvc.getDriverInput();

            if (input == null || driver == null) {
                return null;
            } else {
                return input;
            }
        }

        return null;
    }

    private Pmvc01Entity getDriverMech() {
        var driver = this.getControllingPassenger();
        if (driver instanceof Pmvc01Entity pmvc) {
            return pmvc;
        }

        return null;
    }

    protected void startEvasion(float speed, int[] userIntension) {
        if (isServerSide()) {
            int forwardIntention = userIntension[0];
            int sidewayIntention = userIntension[1];

            Vec3 vel;
            if (forwardIntention == 0 && sidewayIntention == 0) {
                vel = new Vec3(0, 0, 1);
            } else {
                vel = new Vec3(sidewayIntention, 0, forwardIntention).normalize();
            }

            vel = vel.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            vel = vel.scale(speed);

            this.push(vel.x, vel.y, vel.z);
        }
    }

    private void jump(BossActionController.BossAction action) {
        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "jump_normal");

        } else if (action.currentActionTick == 4) {
            this.push(0, 5, 0);
        }
    }

    private void punch(BossActionController.BossAction action) {
        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "punch");

        } else if (action.currentActionTick == 8) {
            var impactPoint = Utils.computeAoeCenter(this, OFFSET_HUMMER_1);
            Utils.doCircleAoeDamageAndKnockback(
                    this.level(),
                    this,
                    impactPoint,
                    20,
                    getMechData().meleeDamage,
                    10,
                    2
            );
        }
    }

    private void drillAction(BossActionController.BossAction action) {
        if (action.onStartOfAction() && action.isFirstLoop()) {
            this.triggerAnim("action_controller", "drillManual");

        } else if (action.currentActionTick > 42) {
            var driver = this.getControllingPassenger();

            if (driver instanceof Pmvc01Entity pmvc01 && pmvc01.getDrivingPassenger() instanceof Player p) {
                Vec3 look = p.getLookAngle(); // カメラの向きベクトル（Pitch/Yaw反映済）
                Vec3 velocity = look.normalize().scale(4);

                // --- サーバ側でも同期させる（tickでsetDeltaMovementを適用）
                setDeltaMovement(velocity);

                // --- 向きをカメラ方向に合わせる
                setYRot(p.getYRot());
                setXRot(p.getXRot());

                this.yRotO = this.getYRot();
                this.xRotO = this.getXRot();
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.getYRot();

                this.hasImpulse = true;


                if (action.currentActionTick % 5 == 0) {
                    this.dealDashDamage();
                }
                if (action.currentActionTick % 2 == 0){
                    this.breakBlocks();
                }
            }

            if (action.onEndOfAction()) {
                this.triggerAnim("action_controller", "boot2");
            }
        }
    }

    protected void breakBlocks() {
        if (Utils.isBlockDestructionAllowed(this)) {
            Utils.destroyBlockSphere(
                    11,
                    new Vec3(0, 11, 11),
                    this.blockPosition(),
                    this.getYRot(),
                    this.level(),
                    true
            );
        }
    }

    protected void dealDashDamage() {
        Level level = this.level();
        AABB damageArea = this.getBoundingBox().inflate(20);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, damageArea);

        for (LivingEntity entity : nearbyEntities) {
            if (!this.isSelf(entity) && this.distanceTo(entity) <= 20) {
                // ダメージ処理
                entity.hurt(this.damageSources().mobAttack(this), 100);

                // ノックバック
                Vec3 knockback = entity.getDeltaMovement().normalize().scale(3);
                entity.knockback(10, -knockback.x, -knockback.y);
            }
        }
    }

    public boolean isSelf(Entity ent) {
        return super.isSelf(ent) || ent.equals(this.getControllingPassenger());
    }

    public float[] getShootingAngle(Entity bullet, Entity target, boolean useDeviation, boolean aimFoot) {
        Vec3 targetPos = null;

        if (target == null && this.getControllingPassenger() instanceof Pmvc01Entity pmvc01 && pmvc01.getDrivingPassenger() instanceof Player p) {
            var hit = p.pick(200,0,true);
            if (hit.getType() == HitResult.Type.MISS) {
                return new float[]{this.getXRot(), this.getYRot()};
            }
            targetPos = hit.getLocation();
        } else {
            targetPos = Utils.getTargetPos(target, useDeviation, aimFoot);
        }

        if (aimFoot) {
            targetPos = new Vec3(targetPos.x, targetPos.y - 3, targetPos.z);
        }

        var bulletPos = bullet.position();

        Vec3 bulletDir = targetPos.subtract(bulletPos).normalize();

        float yaw = (float) (Math.atan2(-bulletDir.x, bulletDir.z) * (180.0 / Math.PI));
        float pitch = (float) (Math.asin(-bulletDir.y) * (180.0 / Math.PI));

        return new float[]{pitch, yaw};
    }

    private void gatlingAction(BossActionController.BossAction action) {
        var target = this.getTarget();

        if (action.onStartOfAction() && action.isFirstLoop()) {
            this.triggerAnim("action_controller", "gatling");

        } else if (action.currentActionTick == 3) {
            NeedleEntity be = new NeedleEntity(PomkotsMechs.NEEDLE.get(), this.level(), this,
                    getMechData().bulletDamage);
            be.setNoGravity(true);
            be.setExplosionScale(this.getMechData().grenadeExplosionScale);
            be.setStunPoint(10);

            var offset = this.position();

            var muzzlPos = new Vec3(-7, 14, 10);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            be.setPos(offset.add(muzzlPos));

            float[] angle;

            if (target == null && this.getControllingPassenger() instanceof Pmvc01Entity pmvc01) {
                angle = pmvc01.getShootingAngle(be, true, true);
            } else {
                angle = Utils.getShootingAngle(be, target, true, true);
            }

            be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), getMechData().bulletSpeed, 0F);

            this.level().addFreshEntity(be);
        }
    }

    private RockLargeEntity rock = null;

    private void throwAction(BossActionController.BossAction action) {
        var target = this.getTarget();

        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "throw");

        } else if (action.currentActionTick == 10) {
            RockLargeEntity be = new RockLargeEntity(PomkotsMechs.ROCK_LARGE.get(), this.level(), this,
                    getMechData().grenadeDamage);
            be.setNoGravity(true);
            be.setExplosionScale(this.getMechData().grenadeExplosionScale);

            be.setPos(getRockPosition(action.currentActionTick));

            this.level().addFreshEntity(be);

            rock = be;

        } else if (action.currentActionTick == 22) {
            if (rock != null) {
                float[] angle;
                if (target == null && this.getControllingPassenger() instanceof Pmvc01Entity pmvc01) {
                    angle = pmvc01.getShootingAngle(rock, true, true);
                } else {
                    angle = Utils.getShootingAngle(rock, target, true, true);
                }

                rock.shootFromRotation(rock, angle[0], angle[1], this.getFallFlyingTicks(), getMechData().bulletSpeed, 0F);

                this.rock = null;
            }
        }
    }

    private Vec3 getRockPosition(int actionTick) {
        int maxTick = 4;
        actionTick = Mth.clamp(actionTick - 10, 0, maxTick);
        float r = 5.46F;
        float base = actionTick * r / maxTick;

        var offset = this.position();
        var muzzlPos = new Vec3(0, base * base, -10 + 3 * (10 - actionTick));
        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
        return offset.add(muzzlPos);
    }

    private void missileHorizontalAction(BossActionController.BossAction action) {
        if (action.onStartOfAction()) {
            this.triggerAnim("action_controller", "missile");

        } else if (action.currentActionTick > 15 && action.currentActionTick < 20) {
            int row = action.currentActionTick - 16;
            var coreMech = getDriverMech();

            if (coreMech != null) {
                var targets = coreMech.getLockTargets().getLockTargetMulti(Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER);

                if (!targets.isEmpty()) {
                    int targetIdx = row % targets.size();
                    var target = (LivingEntity)targets.get(targetIdx);

                    for (int side = -1; side < 2; side += 2) {
                        MissileGenericEnemyEntity be = new MissileGenericEnemyEntity(PomkotsMechs.MISSILE_GENERIC.get(), this.level(), this, target,
                                getMechData().missileDamage, getMechData().missileSpeed * 2);
                        be.setMaxRotationAnglePerTick(2);
                        be.setSwitchTick(12);
                        be.setStunPoint(5);

                        var offset = this.position();

                        // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                        float slotZ = (1 * 0.8F + 8F);
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

        if (action.onEndOfAction()) {
            var coreMech = getDriverMech();
            if (coreMech != null) {
                var targets = coreMech.getLockTargets().getLockTargetMulti(Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER);
                targets.clear();
            }
        }
    }

    public void scatterMines(
            Level level,
            LivingEntity owner,
            LivingEntity target
    ) {
        if (level.isClientSide) return;

        BlockPos center = target.blockPosition();

        int range = 60;
        int step = 40;

        for (int dx = -range; dx <= range; dx += step) {
            for (int dz = -range; dz <= range; dz += step) {
                int x = center.getX() + dx;
                int z = center.getZ() + dz;

                // 地面Y取得（地雷向け）
                int y = level.getHeight(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        x,
                        z
                );

                Vec3 targetPos = new Vec3(
                        x + 0.5,
                        y + 0.1,
                        z + 0.5
                );

                shootMineTo(level, owner, targetPos);
            }
        }
    }

    public void shootMineTo(
            Level level,
            LivingEntity shooter,
            Vec3 targetPos
    ) {
        MineEntity mine = new MineEntity(PomkotsMechs.MINE.get(), level, shooter);

        // 発射位置（少し上）
        Vec3 start = shooter.position().add(0, 12, 0);
        mine.setPos(start);
        mine.setNoGravity(false);
        mine.setExplosionScale(this.getMechData().grenadeExplosionScale);

        // 到達までのtick数（距離に応じて）
        double distance = start.distanceTo(targetPos);
        int travelTicks = Mth.clamp((int)(distance / 1.5), 10, 20);

        Vec3 velocity = computeBallisticVelocity(
                start,
                targetPos,
                travelTicks
        );

        mine.setDeltaMovement(velocity);
        level.addFreshEntity(mine);
    }

    private static Vec3 computeBallisticVelocity(
            Vec3 start,
            Vec3 target,
            int ticks
    ) {
        double dx = target.x - start.x;
        double dy = target.y - start.y;
        double dz = target.z - start.z;

        double vx = dx / ticks;
        double vz = dz / ticks;

        // 重力補正（Minecraft用）
        double gravity = 0.05;
        double vy = dy / ticks + gravity * ticks / 2.0;

        return new Vec3(vx, vy, vz);
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

    public boolean shouldLockMulti(DriverInput driverInput) {
        if (driverInput == null) {
            return false;
        } else {
            return driverInput.isWeaponLeftShoulderPressed();
        }
    }

    private void onStartRiding(BossActionController.BossAction action) {
        if (action.onStartOfAction() && action.isFirstLoop()) {
            this.triggerAnim("action_controller", "boot2");

        } else if (action.onEndOfAction()) {
            setRiding(true);
        }
    }

    private static final EntityDataAccessor<Boolean> RIDING = SynchedEntityData.defineId(Pmb99Entity.class, EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(RIDING, false);
    }

    private void setRiding(boolean value) {
        this.entityData.set(RIDING, value);
    }

    public boolean getRiding() {
        return this.entityData.get(RIDING);
    }

    @Override
    protected void onStun() {
        this.triggerAnim("action_controller", "on_stun");
    }

    @Override
    protected void offStun() {
        this.triggerAnim("action_controller", "off_stun");
    }

    private final AnimationController<Pmb99Entity> trigger = new AnimationController<>(this, "action_controller", state -> PlayState.STOP)
            .triggerableAnim("saber_tate", RawAnimation.begin().thenPlay("animation.pmb01.attacktate"))
            .triggerableAnim("saber_upper", RawAnimation.begin().thenPlay("animation.pmb01.attackupper"))
            .triggerableAnim("saber_circle", RawAnimation.begin().thenPlay("animation.pmb01.attackcircle"))
            .triggerableAnim("saber_jump1", RawAnimation.begin().thenPlay("animation.pmb01.attackjump1").thenLoop("animation.pmb01.attackjump2"))
            .triggerableAnim("saber_jump2", RawAnimation.begin().thenPlay("animation.pmb01.attackjump3"))
            .triggerableAnim("gatling", RawAnimation.begin().thenPlay("animation.pmb01.attackgatling"))
            .triggerableAnim("missile", RawAnimation.begin().thenPlay("animation.pmb01.attackmissile"))
            .triggerableAnim("throw", RawAnimation.begin().thenPlay("animation.pmb01.attackthrow2"))
            .triggerableAnim("jump", RawAnimation.begin().thenPlay("animation.pmb01.jump"))
            .triggerableAnim("onground", RawAnimation.begin().thenPlay("animation.pmb01.onground"))
            .triggerableAnim("hurt", RawAnimation.begin().thenPlay("animation.pmb01.hurt"))
            .triggerableAnim("down", RawAnimation.begin().thenPlay("animation.pmb01.down"))
            .triggerableAnim("break", RawAnimation.begin().thenPlay("animation.pmb01.bodyfrontbreak"))

            .triggerableAnim("dash", RawAnimation.begin().thenPlay("animation.pmb01.dash1").thenLoop("animation.pmb01.dash2"))
            .triggerableAnim("dash_attack", RawAnimation.begin().thenPlay("animation.pmb01.attackdrill1").thenLoop("animation.pmb01.attackdrill2"))
            .triggerableAnim("drillManual", RawAnimation.begin().thenPlay("animation.pmb01.attackdrill1").thenLoop("animation.pmb01.attackdrill2"))

            .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.pmb01.stop"))
            .triggerableAnim("boot", RawAnimation.begin().thenPlay("animation.pmb01.boot"))
            .triggerableAnim("boot2", RawAnimation.begin().thenPlay("animation.pmb01.boot2"))
            .triggerableAnim("gattai", RawAnimation.begin().thenPlay("animation.pmb01.gattai"))

            .triggerableAnim("on_stun", RawAnimation.begin().thenPlay("animation.pmb01.hurt").thenPlayAndHold("animation.pmb01.down"))
            .triggerableAnim("off_stun", RawAnimation.begin().thenPlay("animation.pmb01.up"))

            .triggerableAnim("evasion_front", RawAnimation.begin().thenPlay("animation.pmb01.evasion_front"))
            .triggerableAnim("evasion_back", RawAnimation.begin().thenPlay("animation.pmb01.evasion_back"))
            .triggerableAnim("evasion_left", RawAnimation.begin().thenPlay("animation.pmb01.evasion_left"))
            .triggerableAnim("evasion_right", RawAnimation.begin().thenPlay("animation.pmb01.evasion_right"))
            .triggerableAnim("jump_normal", RawAnimation.begin().thenPlay("animation.pmb01.jump_normal"))
            .triggerableAnim("punch", RawAnimation.begin().thenPlay("animation.pmb01.attack_punch"))

            .setSoundKeyframeHandler(this::playSounds);

    private final AnimationController<Pmb99Entity> base = new AnimationController<>(this, "basic_move", 1, event -> {
        if (!trigger.isPlayingTriggeredAnimation()) {
            if (this.getAiMode() == AI_MODE_INACTIVE) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.inactive"));
            } else if (event.isMoving()) {
                if (!isInAir()) {
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
                if (!isInAir()) {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.idle"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.fly"));
                }

            }
        } else {
            if (this.isInAir()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.fly"));
            }
            event.getController().forceAnimationReset();
            return PlayState.STOP;
        }
    }).setSoundKeyframeHandler(this::playSounds);

    private boolean isInAir() {
        return this.inAirTick > 5;
    }

    @Override
    protected void playSounds(SoundKeyframeEvent event) {
        if ("gatling".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_GRENADE_EVENT.get());
        } else if ("impact3".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_IMPACT_2.get());

            var impactPoint = Utils.computeAoeCenter(this, OFFSET_HUMMER_1);
            Utils.spawnAoeDustParticles(this.level(), impactPoint, 20, 100);

        } else if ("impact4".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_IMPACT_1.get());

            var impactPoint = Utils.computeAoeCenter(this, OFFSET_HUMMER_2);
            Utils.spawnAoeDustParticles(this.level(), impactPoint, 20, 100);

        } else if ("throw".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_THROW.get());

        } else if ("landing".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_IMPACT_1.get());
            Utils.spawnAoeDustParticles2(this.level(), this.position().add(0, 2, 0), 20, 100);

        } else {
            super.playSounds(event);
        }
    }

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
    protected void tickDeath() {
        super.tickDeath();
        if (this.rock != null) {
            this.rock.setNoGravity(false);
            this.rock = null;
        }
    }

    @Override
    public double getPassengersRidingOffset() {
        return 10F;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        if (this.getControllingPassenger() instanceof Pmvc01Entity pmvc01) {
            return pmvc01.interact(player, interactionHand);
        }

        return InteractionResult.PASS;
    }
}
