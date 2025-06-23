package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.PomkotsControllable;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.BaseBossGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.GoalDice;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.HateTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseBossEntity extends GenericPomkotsMonster implements GeoEntity, GeoAnimatable, PomkotsControllable {
    public static final float DEFAULT_SCALE = 1f;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    protected final BossActionController actionController;
    protected final List<BossHitBoxEntity> hitBoxes = new ArrayList<>();
    public final GoalDice goalDice;

    protected HateTargetGoal hateTargetGoal = null;

    public BossActionController getActionController() {
        return actionController;
    }

    public BaseBossEntity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);

        this.actionController = new BossActionController();
        this.setMaxUpStep(getMechData().maxStepUp);
        this.setSpeed(getMechData().speed);
        this.setPersistenceRequired();
        this.setNoGravity(false);
        this.setYRot(0F);
        this.noCulling = true;
        this.goalDice = new GoalDice(this);

        this.registerTargetSelectorGoals();
    }

    protected void registerTargetSelectorGoals() {
        this.hateTargetGoal = new HateTargetGoal(this, this.getAttributeValue(Attributes.FOLLOW_RANGE), 30);
        this.targetSelector.addGoal(1, hateTargetGoal);
    }

    protected void registerAdditionalHitBox(BossHitBoxEntity box) {
        this.hitBoxes.add(box);
    }

    @Override
    public void tick() {
        if (this.isServerSide()) {
            this.updateAiMode();

            var offset = this.position();

            if (this.firstTick) {
                for (var hitBox: hitBoxes) {
                    hitBox.setPos(hitBox.getRelativeParentPos().add(offset));
                    this.level().addFreshEntity(hitBox);
                }
            } else {
                for (var hitBox: hitBoxes) {
                    hitBox.setPos(hitBox.getRelativeParentPos().add(offset));
                }
            }
        }

        super.tick();
        this.actionController.tick();

        if (this.isAlive() && this.isVehicle()) {
            if (rideCoolTick == 0) {
                applyPlayerControll();
            }
        }

        if (this.isServerSide()) {
            goalDice.tick();
        }
    }

    protected void registerActionGoal(BaseBossGoal goal, int[] availableMode, int weight) {
        this.goalSelector.addGoal(1, goal);
        this.goalDice.registerGoal(goal, availableMode, -1, -1, weight);
    }

    protected void registerActionGoal(BaseBossGoal goal, int[] availableMode, float availableDistanceMin, float availableDistanceMax, int weight) {
        this.goalSelector.addGoal(1, goal);
        this.goalDice.registerGoal(goal, availableMode, availableDistanceMin, availableDistanceMax, weight);
    }

    public abstract void registerControllers(AnimatableManager.ControllerRegistrar controllers);
    protected abstract void applyPlayerControll();

    protected void rangeAttack(Vec3 aabb1, Vec3 aabb2, float damage, int knockBack) {
        var pilePos1 = aabb1.yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
        var pilePos2 = aabb2.yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());

        var world = this.level();
        var kbVel = new Vec3(0, 0, -1F).yRot((float) Math.toRadians((-1.0) * this.getYRot()));
        for (var ent : world.getEntities(null, new AABB(pilePos1, pilePos2))) {
            if (this.isSelf(ent)) {
                continue;
            }

            if (ent instanceof LivingEntity le) {
                le.knockback(knockBack, kbVel.x, kbVel.z);

                DamageSource ds = this.damageSources().generic();

                le.hurt(ds, damage);
            }
        }
    }

    private int waitTicks = 60; // 初期状態で60tick待機
    private int bootTicks = -1;

    @Override
    public void aiStep() {
        if (isServerSide()) {
            if (waitTicks > 0) {
                waitTicks--;
                this.setDeltaMovement(Vec3.ZERO);
                this.setNoAi(true); // AI無効
                return;
            }

            if (this.getAiMode() == AI_MODE_INACTIVE) {
                if (bootTicks > 0) {
                    bootTicks--;
                    this.setDeltaMovement(Vec3.ZERO);
                    this.setNoAi(true); // AI無効
                    return;
                } else if (bootTicks == 0) {
                    this.isActivated = true;
                    this.setAiMode(AI_MODE_BATTLE_PHASE_1);
                }
            } else if (this.isNoAi()) {
                this.setNoAi(false);
            }
        }

        super.aiStep();
    }

    public void boot() {
        this.bootTicks = 100;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (Utils.isSystemicDamage(source)) {
            return super.hurt(source, amount);
        } else {
            return false;
        }
    }

    public boolean hurtFromAdditionalHitBox(DamageSource source, float amount) {
        if (getAiMode() == AI_MODE_INACTIVE) {
            return false;

        } else {
            if (source.is(DamageTypes.EXPLOSION)) {
                amount *= getMechData().explosionDamageModifier;
            }
            amount *= getMechData().baseDamageModifier;

            ParticleUtil.addParticles(source,this);

            if (this.isServerSide() && hateTargetGoal != null) {
                var cause = getCause(source);
                if (cause != null && !(cause instanceof Player pl && pl.isCreative())) {
                    this.hateTargetGoal.addDamageHate(cause, amount);
                }
            }

            return super.hurt(source, amount);
        }
    }

    private LivingEntity getCause(DamageSource source) {
        var sourceEntity = source.getEntity();

        LivingEntity res;

        if (sourceEntity instanceof Projectile p) {
            res = Utils.getProjectileOwner(p);

        } else if (sourceEntity instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof PomkotsVehicleBase pv) {
                res = pv.getDrivingPassenger();
            } else {
                res = livingEntity;
            }
        } else {
            res = null;
        }

        return res;
    }

    // カスタムヘイト操作用のメソッド
    public void addHateToEntity(LivingEntity entity, float hate) {
        if (hateTargetGoal != null) {
            hateTargetGoal.addCustomHate(entity, hate);
        }
    }

    public float getEntityHate(LivingEntity entity) {
        if (hateTargetGoal != null) {
            return hateTargetGoal.getHate(entity);
        }
        return 0;
    }

    public void clearAllHate() {
        if (hateTargetGoal != null) {
            hateTargetGoal.clearHate();
        }
    }

    public void clearEntityHate(LivingEntity entity) {
        if (hateTargetGoal != null) {
            hateTargetGoal.clearHate(entity);
        }
    }

    private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.RED, ServerBossEvent.BossBarOverlay.PROGRESS);

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
    }

    public void choiMove() {
        this.setDeltaMovement(new Vec3(0,0,0.01).yRot((float) Math.toRadians((-1.0) * this.getYRot())));
    }

    public void rotateToTarget(LivingEntity target) {
        if (target == null) {
            return;
        }

        Vec3 tgtPos = target.position();
        Vec3 slfPos = this.position();
        Vec3 v = slfPos.vectorTo(tgtPos);

        this.rotateDegree((float) Math.toDegrees(Mth.atan2(v.z, v.x)) - 90);
    }

    public void rotateDegree(float degree) {
        this.setYRot(degree);
        this.setYBodyRot(this.getYRot());
        this.setYHeadRot(this.getYRot());
        this.yRotO = this.getYRot();
        this.yBodyRotO = this.getYRot();
        this.yHeadRotO = this.getYRot();
    }

    @Override
    public boolean tryAttack() {
        // Goalで実装するので使わない
        return false;
    }

    @Override
    public void doAttack() {
        // Goalで実装するので使わない
    }

    @Override
    public int getMaxAttackCooltime() {
        // Goalで実装するので使わない
        return 100;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 19F;
    }

    private Vec3 clientSeatPos = Vec3.ZERO;

    public void setClientSeatPos(Vec3 seatPos) {
        this.clientSeatPos = seatPos;
    }

    public Vec3 getClientSeatPos() {
        return this.clientSeatPos;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    // プレイヤーが搭乗しているときの処理

    // 操作しているドライバーのキー入力（サーバと他クライアントにも同期する）
    private DriverInput driverInput = null;

    @Override
    public void setDriverInput(DriverInput di) {
        this.driverInput = new DriverInput(di.getStatus(), this.driverInput);

        if (isServerSide() && di.isModeChangePressed()) {
            this.setMainMode(!this.isMainMode());
        }
    }

    // Compound Tag系 ===============================================================================================

    private boolean isActivated = true;

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean(PomkotsMechs.nbtName("BossActivated"), isActivated);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains(PomkotsMechs.nbtName("BossActivated"))) {
            isActivated = compound.getBoolean(PomkotsMechs.nbtName("BossActivated"));

        } else {
            isActivated = true;
        }
    }

    // クラサバ同期系 ===============================================================================================

    private static final EntityDataAccessor<Boolean> MODE = SynchedEntityData.defineId(BaseBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> AI_MODE = SynchedEntityData.defineId(BaseBossEntity.class, EntityDataSerializers.INT);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MODE, true);
        this.entityData.define(AI_MODE, AI_MODE_BATTLE_PHASE_1);
    }

    private void setMainMode(boolean value) {
        this.entityData.set(MODE, value);
    }

    public boolean isMainMode() {
        return this.entityData.get(MODE);
    }

    protected int aiMode = AI_MODE_BATTLE_PHASE_1;

    public static final int AI_MODE_INACTIVE = -2;
    public static final int AI_MODE_PATROL = -1;
    public static final int AI_MODE_BATTLE_PHASE_1 = 1;
    public static final int AI_MODE_BATTLE_PHASE_2 = 2;
    public static final int AI_MODE_BATTLE_PHASE_3 = 3;
    public static final int AI_MODE_BATTLE_PHASE_4 = 4;

    public static final int[] AI_MODE_ALL = {
            AI_MODE_PATROL,
            AI_MODE_BATTLE_PHASE_1,
            AI_MODE_BATTLE_PHASE_2,
            AI_MODE_BATTLE_PHASE_3,
            AI_MODE_BATTLE_PHASE_4,
    };

    protected void updateAiMode() {
        int nextMode;

        if (isActivated) {
            if (aiMode > 0) {
                // aiModeが0より大きい＝戦闘モードの時はHPによって状態を返す
                float healthPercentage = this.getHealth() / this.getMaxHealth();

                if (healthPercentage > 0.8) {
                    nextMode = AI_MODE_BATTLE_PHASE_1;
                } else if (healthPercentage > 0.6) {
                    nextMode = AI_MODE_BATTLE_PHASE_2;
                } else if (healthPercentage > 0.2) {
                    nextMode = AI_MODE_BATTLE_PHASE_3;
                } else {
                    nextMode = AI_MODE_BATTLE_PHASE_4;
                }
            } else {
                nextMode = aiMode;
            }
        } else {
            nextMode = AI_MODE_INACTIVE;
        }

        setAiMode(nextMode);
    }

    private void setAiMode(int value) {
        this.entityData.set(AI_MODE, value);
    }

    public int getAiMode() {
        return this.entityData.get(AI_MODE);
    }

    // 移動判定系 ===============================================================================================

    public enum MoveDirection {
        FORWARD, BACKWARD, RIGHT, LEFT, NONE
    }

    public MoveDirection getDominantMoveDirection() {
        Vec3 velocity = this.getDeltaMovement();
        if (velocity.lengthSqr() < 0.001) {
            return MoveDirection.NONE;
        }

        float yawDeg = this.getYRot(); // 視線の向き（度）
        double yawRad = Math.toRadians(yawDeg);

        // 前方向（視線方向）
        Vec3 forward = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));

        // 右方向（視線方向 + 90°）
        Vec3 right = new Vec3(Math.cos(yawRad), 0, Math.sin(yawRad));

        // 各方向への速度成分（内積）
        double forwardDot = velocity.dot(forward);
        double rightDot = velocity.dot(right);

        double absForward = Math.abs(forwardDot);
        double absRight = Math.abs(rightDot);

        if (absForward >= absRight) {
            return forwardDot > 0 ? MoveDirection.FORWARD : MoveDirection.BACKWARD;
        } else {
            return rightDot > 0 ? MoveDirection.RIGHT : MoveDirection.LEFT;
        }
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
        return false;
    }

    public boolean isSelf(Entity ent) {
        boolean res = false;
        for (var hitBox: hitBoxes) {
            res |= hitBox.equals(ent);
        }

        return res || this.equals(ent);
    }

    @Override
    protected void checkInsideBlocks() {
    }

    protected void playSounds(SoundKeyframeEvent event) {
        if ("saber".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_SABER.get(), this);
        } else if ("missile".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get(), this);
        } else if ("canon".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_GRENADE_EVENT.get(), this);
        } else if ("dash".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_BOOSTER_EVENT.get(), this);
        } else if ("walk_large".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_WALK_LARGE.get(), this);
        } else if ("mech".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_MACHINE.get(), this);
        } else if ("jump".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_WALK_LARGE.get(), this);
        } else if ("onground".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_WALK_LARGE.get(), this);
        } else if ("gatling".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_HIT_EVENT.get(), this);
        } else if ("stomp".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_BOSSDOWN_EVENT.get(), this);
        } else if ("chainsaw".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_CHAINSAW.get(), this);
        } else if ("impact1".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_IMPACT_1.get(), this);
        } else if ("impact2".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_IMPACT_2.get(), this);
        } else if ("laser1".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_BEAM1.get(), this);
        } else if ("laser2".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_BEAM2.get(), this);
        } else if ("grenade".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_GRENADE_EVENT.get(), this);
        } else if ("gashon".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_STEP.get(), this);
        } else if ("roller".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_ROLLER1.get(), this, 0.2F);
        } else if ("charge".equals(event.getKeyframeData().getSound())) {
            Utils.playSoundEffect(PomkotsMechs.SE_CHARGE.get(), this);
        }
    }
}
