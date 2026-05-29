package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.PomkotsControllable;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidObjectiveEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonsterPercistant;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.BaseBossGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.GoalDice;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.HateTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal.NearestEntityTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.RaidTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BluePrintItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
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

public abstract class BaseBossEntity extends GenericPomkotsMonsterPercistant implements GeoEntity, GeoAnimatable, PomkotsControllable {
    public static final float DEFAULT_SCALE = 1f;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public boolean isCarrying = false;

    protected final BossActionController actionController;
    protected final List<BossHitBoxEntity> hitBoxes = new ArrayList<>();
    public final GoalDice goalDice;

    protected HateTargetGoal hateTargetGoal = null;

    protected float gravity = 0.98F;

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
//        this.noCulling = true;
        this.goalDice = new GoalDice(this);

        this.registerTargetSelectorGoals();
    }

    protected void registerTargetSelectorGoals() {
//        this.targetSelector.addGoal(1, new RaidTargetGoal<RaidObjectiveEntity>(
//                this,
//                RaidObjectiveEntity.class, // 拠点中心を表すエンティティ
//                this.getAttribute(Attributes.FOLLOW_RANGE).getBaseValue(),                 // 探索範囲
//                200,                  // プレイヤーを追うtick数
//                this::isInRaid // レイド中判定
//        ));

        this.hateTargetGoal = new HateTargetGoal(this, this.getAttributeValue(Attributes.FOLLOW_RANGE), 30);
        this.targetSelector.addGoal(2, hateTargetGoal);
    }

    protected void registerAdditionalHitBox(BossHitBoxEntity box) {
        this.hitBoxes.add(box);
    }

    @Override
    public void tick() {
//        if (this.getAiMode() == AI_MODE_INACTIVE) {
//            this.noCulling = false;
//        } else {
//            this.noCulling = true;
//        }

        if (this.isServerSide()) {
            if (this.isCarrying && this.onGround()) {
                this.isCarrying = false;
            }

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

            this.updateStunPoint();
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

            if (!this.onGround() && !this.isNoGravity()) {
                this.push(0, -0.18 * 0.9800000190734863D, 0);
            }
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
        this.bootTicks = 98;
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

            if (!this.isStunning()) {
                amount *= getMechData().baseDamageModifier;
            } else {
                amount *= (getMechData().baseDamageModifier);
            }

            ParticleUtil.addParticles(source,this);

            if (this.isServerSide() && hateTargetGoal != null) {
                var cause = getCause(source);
//                if (cause != null && !(cause instanceof Player pl && pl.isCreative())) {
                if (cause != null) {
                    this.hateTargetGoal.addDamageHate(cause, amount);
                }
            }
            return super.hurt(source, amount);
        }
    }

    protected void actuallyHurt(DamageSource damageSource, float f) {
        float beforeRatio = getHealth() / getMaxHealth();

        super.actuallyHurt(damageSource, f);

        if (level().isClientSide || this.isStunning()) {
            return;
        }

        float afterRatio = getHealth() / getMaxHealth();

        int beforeStage = (int)(beforeRatio * 5F);
        int afterStage = (int)(afterRatio * 5F);

        if (afterStage != 4 && afterStage < beforeStage) {
            this.goalSelector.getRunningGoals().forEach(WrappedGoal::stop);
            this.onSmallDown();
            burstArmorDrops(1F, true);
        }
    }

    private void burstArmorDrops(
            float ratio, boolean dropBlueprint
    ) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        LootTable lootTable =
                serverLevel.getServer()
                        .getLootData()
                        .getLootTable(getLootTable());

        LootParams params = new LootParams.Builder(serverLevel)
                .withParameter(
                        LootContextParams.THIS_ENTITY,
                        this
                )
                .withParameter(
                        LootContextParams.ORIGIN,
                        position()
                )
                .withParameter(
                        LootContextParams.DAMAGE_SOURCE,
                        damageSources().generic()
                )
                .create(LootContextParamSets.ENTITY);

        List<ItemStack> drops =
                lootTable.getRandomItems(params);

        RandomSource random = this.random;

        for (ItemStack stack : drops) {
//            if (random.nextFloat() > ratio) {
//                continue;
//            }

            if (stack.getItem() instanceof BluePrintItem) {
                if (!dropBlueprint || random.nextInt() % 8 < 7) {
                    continue;
                }
            }

            Vec3 pos = new Vec3(
                    getX(),
                    getY() + getBbHeight() * 0.7,
                    getZ()
            );

            stack.setCount(1);

            ItemEntity item = new ItemEntity(
                    level(),
                    pos.x,
                    pos.y,
                    pos.z,
                    stack.copy()
            );

            item.setDeltaMovement(
                    random.nextGaussian() * 0.5,
                    random.nextDouble() * 0.5 + 0.2,
                    random.nextGaussian() * 0.5
            );

            item.setPickUpDelay(40);

            level().addFreshEntity(item);
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
        this.bossInfo.setVisible(false);
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
        this.addDeltaMovement(new Vec3(0,0,0.001).yRot((float) Math.toRadians((-1.0) * this.getYRot())));
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
        compound.putBoolean(PomkotsMechs.nbtName("IsCarrying"), isCarrying);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains(PomkotsMechs.nbtName("BossActivated"))) {
            isActivated = compound.getBoolean(PomkotsMechs.nbtName("BossActivated"));
        } else {
            isActivated = true;
        }

        if (compound.contains(PomkotsMechs.nbtName("IsCarrying"))) {
            isCarrying = compound.getBoolean(PomkotsMechs.nbtName("IsCarrying"));
        } else {
            isCarrying = false;
        }
    }

    public void setActivated(boolean value) {
        this.isActivated = value;
        if (!value) {
            bootTicks = -1;
        }
    }

    // クラサバ同期系 ===============================================================================================

    private static final EntityDataAccessor<Boolean> MODE = SynchedEntityData.defineId(BaseBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> AI_MODE = SynchedEntityData.defineId(BaseBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> STUN_POINT = SynchedEntityData.defineId(BaseBossEntity.class, EntityDataSerializers.INT);


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MODE, true);
        this.entityData.define(AI_MODE, AI_MODE_BATTLE_PHASE_1);
        this.entityData.define(STUN_POINT, 0);
    }

    private void setMainMode(boolean value) {
        this.entityData.set(MODE, value);
    }

    public boolean isMainMode() {
        return this.entityData.get(MODE);
    }

    protected int aiMode = AI_MODE_BATTLE_PHASE_1;

    public static final int AI_MODE_CARRYING = -3;
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

        if (isCarrying) {
            nextMode = AI_MODE_CARRYING;
        } else if (isActivated) {
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

        if (isServerSide()) {
            var currentMode = this.getAiMode();

            if (currentMode == AI_MODE_INACTIVE && nextMode != AI_MODE_INACTIVE) {
                // フェードイン 20 ticks, 表示 60 ticks, フェードアウト 20 ticks
                ClientboundSetTitlesAnimationPacket timesPacket =
                        new ClientboundSetTitlesAnimationPacket(20, 60, 20);

                // メインタイトル
                ClientboundSetTitleTextPacket titlePacket =
                        new ClientboundSetTitleTextPacket(this.getDisplayName());

                // サブタイトル
                ClientboundSetSubtitleTextPacket subtitlePacket =
                        new ClientboundSetSubtitleTextPacket(Component.literal("Activated!!"));

                // 全プレイヤーに送信
                for (ServerPlayer player : bossInfo.getPlayers()) {
                    player.connection.send(timesPacket);
                    player.connection.send(titlePacket);
                    player.connection.send(subtitlePacket);
                }

                bossInfo.setPlayBossMusic(true);
            }
        }

        setAiMode(nextMode);
    }

    public void setAiMode(int value) {
        this.entityData.set(AI_MODE, value);
    }

    public int getAiMode() {
        return this.entityData.get(AI_MODE);
    }

    public static final int STUN_STUN_START = 200;
    public static final int STUN_MAX = STUN_STUN_START + 100;

    public void addStunPoint(int point) {
        int current = getStunPoint();

        if (current < STUN_STUN_START) {
            current += point;
            if (current >= STUN_STUN_START) {
                current = STUN_MAX;

                this.getAttribute(Attributes.ARMOR).setBaseValue(0);

                this.goalSelector.getRunningGoals().forEach(WrappedGoal::stop);
                this.actionController.reset();
                this.onStun();
            }
        }

        setStunPoint(current);
    }

    public void updateStunPoint() {
        var stunPoint = getStunPoint();
        if (stunPoint > 0 && this.tickCount % 5 == 0) {
            if (stunPoint < STUN_STUN_START) {
                stunPoint -= 1;
            } else {
                stunPoint -= 5;
            }

            if (stunPoint == STUN_STUN_START + 10) {
                this.getAttribute(Attributes.ARMOR).setBaseValue(mechData.armor);
                this.offStun();
            } else if (stunPoint == STUN_STUN_START) {
                stunPoint = 0;
            }

            this.setStunPoint(stunPoint);
        }
    }

    protected void onStun() {

    }

    protected void offStun() {

    }

    protected void onSmallDown() {

    }

    public void setStunPoint(int value) {
        this.entityData.set(STUN_POINT, value);
    }

    public int getStunPoint() {
        return this.entityData.get(STUN_POINT);
    }

    public boolean isStunning() {
        return getStunPoint() >= STUN_STUN_START;
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
            this.playSoundEffect(PomkotsMechs.SE_SABER.get());
        } else if ("missile".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get());
        } else if ("canon".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_GRENADE_EVENT.get());
        } else if ("dash".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_BOOSTER_EVENT.get());
        } else if ("walk_large".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_WALK_LARGE.get());
        } else if ("mech".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_MACHINE.get());
        } else if ("jump".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_WALK_LARGE.get());
        } else if ("onground".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_WALK_LARGE.get());
        } else if ("gatling".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_HIT_EVENT.get());
        } else if ("stomp".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_BOSSDOWN_EVENT.get());
        } else if ("chainsaw".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_CHAINSAW.get());
        } else if ("impact1".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_IMPACT_1.get());
        } else if ("impact2".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_IMPACT_2.get());
        } else if ("laser1".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_BEAM1.get());
        } else if ("laser2".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_BEAM2.get());
        } else if ("grenade".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_GRENADE_EVENT.get());
        } else if ("gashon".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_STEP.get());
        } else if ("roller".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_ROLLER1.get(), 0.2F);
        } else if ("charge".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_CHARGE.get());
        } else if ("se_heri".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_HERI.get());
        }
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(20, 0.0, 20);
    }
}
