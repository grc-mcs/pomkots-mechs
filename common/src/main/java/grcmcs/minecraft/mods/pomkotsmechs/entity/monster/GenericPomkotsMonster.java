package grcmcs.minecraft.mods.pomkotsmechs.entity.monster;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPack;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.items.CoreStonePMB01Item;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class GenericPomkotsMonster extends Monster {
    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0F)
                .add(Attributes.FOLLOW_RANGE, 200);
    }

    // 搭乗してから操作開始するまでの間のティック
    protected short rideCoolTick = 0;

    protected boolean alwaysLookAtTarget = true;

    protected boolean isInRaid = false;

    protected PomkotsDataPack.EnemyData mechData;

    private float soundEffectVolume = -1;

    public PomkotsDataPack.EnemyData getMechData() {
        return mechData;
    }

    abstract public String getMechName();

    protected GenericPomkotsMonster(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);

        mechData = loadMechData();

        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(mechData.knockBackResistance);
        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(mechData.followRange);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(mechData.health);
        this.setHealth(mechData.health);
        this.getAttribute(Attributes.ARMOR).setBaseValue(mechData.armor);
        this.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(mechData.armorToughness);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        if (mechData == null) {
            mechData = loadMechData();
            this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(mechData.followRange);
        }
    }

    protected PomkotsDataPack.EnemyData loadMechData() {
        return PomkotsDataPackManager.getInstance().getDataPack().getEnemyData(getMechName());
    }

    protected int attackCooltime = 0;

    public boolean tryAttack() {
        if (this.attackCooltime == 0) {
            doAttack();
            this.attackCooltime = getMaxAttackCooltime();

            return true;
        } else {
            return false;
        }
    }

    public boolean isInAttackCooltime() {
        return attackCooltime > 0;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        var passes = this.getPassengers();

        if (passes.isEmpty()) {
            return null;
        } else {
            for (var ent: passes) {
                if (ent instanceof LivingEntity le) {
                    return le;
                }
            }
            return null;
        }
    }

    @Override
    public void tick() {
        this.soundEffectVolume = -1;
        if (!level().isClientSide && !attributeInitialized) {
            initializeHealthAttribute();
            attributeInitialized = true;
        }

        if (!(this.getControllingPassenger() instanceof Player player)) {
            this.setNoAi(false);
        } else {
            if (rideCoolTick > 0) {
                rideCoolTick--;

            } else {
                if (player.swinging) {
                    this.tryAttack();
                }
            }
        }

        super.tick();

        if (this.attackCooltime > 0) {
            this.attackCooltime--;
        }

        LivingEntity target = this.getTarget();

        if (!level().isClientSide && target != null && alwaysLookAtTarget) {
            this.getLookControl().setLookAt(target, 30F, 30F);
        }

        if (isServerSide() && isInRaid()) {
            handleObstacleBlocking();
        }
    }

    @Override
    public void travel(Vec3 pos) {
        if (this.isAlive() && this.isVehicle()) {
            LivingEntity pilot = this.getControllingPassenger();

            if (pilot != null) {
                // ROTATE Vehicle
                this.setYRot(pilot.getYRot());
                this.yRotO = this.getYRot();
                this.setXRot(pilot.getXRot() * 0.5F);
                this.setRot(this.getYRot(), this.getXRot());
                this.setYBodyRot(this.getYRot());
                this.setYHeadRot(this.getYRot());
                float f = pilot.xxa * 0.5F;
                float f1 = pilot.zza * 0.5F;

                super.travel(new Vec3(f, pos.y, f1));
            }
        } else {
            super.travel(pos);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && player.getMainHandItem().getItem() instanceof CoreStonePMB01Item) {
            player.setYRot(this.getYRot());
            player.setXRot(this.getXRot());
            player.startRiding(this);
            this.setNoAi(true);
        }
        this.rideCoolTick = 3;
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public void rotateToTarget(LivingEntity target) {
        if (this.level().isClientSide || target == null) {
            return; // サーバー側のみで処理する
        }

        Vec3 tgtPos = target.position();
        Vec3 slfPos = this.position();
        Vec3 v = slfPos.vectorTo(tgtPos);

        this.setYRot((float) Math.toDegrees(Mth.atan2(v.z, v.x)) - 90);
        this.setYBodyRot(this.getYRot());
        this.setYHeadRot(this.getYRot());
        this.yRotO = this.getYRot();
        this.yBodyRotO = this.getYRot();
        this.yHeadRotO = this.getYRot();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return true;
    }

    protected boolean isServerSide() {
        return !isClientSide();
    }

    protected boolean isClientSide() {
        return this.level().isClientSide();
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime == 10) {
            this.remove(RemovalReason.KILLED);
            this.generateDestroyedExplosionEffect();
        }
    }

    protected void generateDestroyedExplosionEffect() {
        if (isServerSide()) {
            var level = this.level();
            ExplosionEntity e = new ExplosionEntity(PomkotsMechs.EXPLOSION.get(), level);
            e.setPos(this.position());
            level.addFreshEntity(e);
        }
    }

    @Override
    public boolean hurt(DamageSource ds, float dmg) {
        ParticleUtil.addParticles(ds,this);
        return super.hurt(ds, dmg);
    }

    @Override
    public void playHurtSound(DamageSource ds) {
        if (this.isClientSide()) {
            this.playSoundEffect(PomkotsMechs.SE_HIT_EVENT.get());
        }
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    protected abstract void doAttack();

    protected abstract int getMaxAttackCooltime();

    public static boolean canSpawnCommon(EntityType<? extends GenericPomkotsMonster> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return true;
        //        return Monster.checkMonsterSpawnRules(type, world, reason, pos, random)
        //                && world.getBlockState(pos.below()).isSolidRender(world, pos.below())
        //                && world.getRawBrightness(pos, 0) < 8;
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType spawnType) {
        // Monster の暗さチェックをスキップして常にスポーン許可
        return true;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level,
                                        DifficultyInstance difficulty,
                                        MobSpawnType spawnType,
                                        SpawnGroupData spawnData,
                                        CompoundTag tag) {
        return super.finalizeSpawn(level, difficulty, spawnType, spawnData, tag);
    }

    private boolean attributeInitialized = false;
    protected float healthModifier = 1.0F;
    protected float attackModifier = 1.0F;

    public void setModifiers(float healthModifier, float attackModifier) {
        this.healthModifier = healthModifier;
        this.attackModifier = attackModifier;
    }

    protected void initializeHealthAttribute() {
        AttributeInstance healthAttribute = getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null || healthModifier == 1.0F) return;

        double newHealthValue = healthAttribute.getBaseValue() * healthModifier;

        healthAttribute.setBaseValue(newHealthValue);

        if (getHealth() > newHealthValue) {
            setHealth((float) newHealthValue);
        }
    }

    private static final EntityDataAccessor<Boolean> IS_IN_EVENT = SynchedEntityData.defineId(GenericPomkotsMonster.class, EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_IN_EVENT, false);
    }

    public void setInEvent(boolean value) {
        this.entityData.set(IS_IN_EVENT, value);
    }

    public boolean isInEvent() {
        return this.entityData.get(IS_IN_EVENT);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        this.healthModifier = compound.getFloat(PomkotsMechs.nbtName("MonsterHealthModifier"));
        this.attackModifier = compound.getFloat(PomkotsMechs.nbtName("MonsterAttackModifier"));

        if (this.healthModifier == 0) {
            this.healthModifier = 1;
        }
        if (this.attackModifier == 0) {
            this.attackModifier = 1;
        }

        this.isInRaid = compound.getBoolean(PomkotsMechs.nbtName("IsInRaid"));

        if (compound.contains(PomkotsMechs.nbtName("IsInEvent"))) {
            this.setInEvent(compound.getBoolean(PomkotsMechs.nbtName("IsInEvent")));
        }

        this.attributeInitialized = compound.getBoolean(PomkotsMechs.nbtName("AttributeInitialized"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putFloat(PomkotsMechs.nbtName("MonsterHealthModifier"), this.healthModifier);
        compound.putFloat(PomkotsMechs.nbtName("MonsterAttackModifier"), this.attackModifier);
        compound.putBoolean(PomkotsMechs.nbtName("IsInRaid"), this.isInRaid);
        compound.putBoolean(PomkotsMechs.nbtName("IsInEvent"), this.isInEvent());
        compound.putBoolean(PomkotsMechs.nbtName("AttributeInitialized"), this.attributeInitialized);
    }

    public boolean isInRaid() {
        return this.isInRaid;
    }

    public void setInRaid(boolean flag) {
        this.isInRaid = flag;
    }

    public float getModifiedDamage(float base) {
        return base * attackModifier;
    }

    protected void addHitParticles(Entity target) {
        var offset = new Vec3(target.position().x, target.getBoundingBox().getCenter().y, target.position().z);

        for (int i = 0; i < 40; i++) {
            // ランダムな速度を生成
            double velocityX = random.nextDouble() * 4.3 - 1;
            double velocityY = random.nextDouble() * 4.3 - 1;
            double velocityZ = random.nextDouble() * 4.3 - 1;

            // パーティクルをクライアント側で発生させる
            this.level().addAlwaysVisibleParticle(PomkotsMechs.SPARK.get(),
                    true,
                    offset.x(), offset.y(), offset.z(), // 位置
                    velocityX, velocityY, velocityZ // 速度
            );
        }
    }

    @Override
    public boolean isAffectedByFluids() {
        return false;
    }

    @Override
    public int decreaseAirSupply(int air) {
        return 0;
    }

    // ブロック破壊関連
    private int breakCooldown = 0;
    private boolean breakingMode = false;

    private void handleObstacleBlocking() {
        if (breakCooldown > 0) {
            breakCooldown--;
            return;
        }

        Vec3 forward = this.getLookAngle().normalize();
        double reach = 2.5; // 前方2.5ブロックまでチェック
        double width = this.getBbWidth() * 0.8;
        double height = this.getBbHeight();

        // --- 前方AABBを作成 ---
        AABB forwardBox = this.getBoundingBox().inflate(0.1)
                .expandTowards(forward.scale(reach))
                .inflate(width * 0.2, height * 0.1, width * 0.2);

        // --- ブロック占有率を調査 ---
        double filledCount = 0;
        double totalCount = 0;
        double maxStep = this.maxUpStep(); // 通常は0.6～1.0程度
        double highestSolid = Double.NEGATIVE_INFINITY;

        for (BlockPos pos : BlockPos.betweenClosed(
                BlockPos.containing(forwardBox.minX, forwardBox.minY, forwardBox.minZ),
                BlockPos.containing(forwardBox.maxX, forwardBox.maxY, forwardBox.maxZ))) {

            BlockState state = level().getBlockState(pos);
            if (state.isAir()) continue;

            VoxelShape shape = state.getCollisionShape(level(), pos);
            if (shape.isEmpty()) continue;

            totalCount++;
            filledCount++;

            // --- もっとも高い衝突面を記録 ---
            AABB blockBox = shape.bounds();
            double topY = pos.getY() + blockBox.maxY;
            if (topY > highestSolid) {
                highestSolid = topY;
            }
        }

        double fillRatio = (totalCount == 0) ? 0.0 : (filledCount / totalCount);

        // --- STEPUPでは超えられない高さか判定 ---
        double feetY = this.getY();
        double stepHeight = highestSolid - feetY;

        boolean tooHigh = stepHeight > maxStep + 0.2;
        boolean tooDense = fillRatio > 0.70; // 前方空間の35%以上がブロック

        if (tooHigh && tooDense) {
            if (!breakingMode) {
                breakingMode = true;
                triggerExplosion(forwardBox);
                breakCooldown = 100; // 約5秒クールダウン
            }
        } else {
            breakingMode = false;
        }
    }

    // --- 爆発処理 ---
    private void triggerExplosion(AABB area) {
        Vec3 center = area.getCenter();
        float strength = (float)(this.getBbWidth() + this.getBbHeight()) * 0.9F;
        Utils.explode(this, center.x, center.y, center.z, strength, Level.ExplosionInteraction.BLOCK, this.level());
    }

    protected void playSoundEffect(SoundEvent event) {
        this.playSoundEffect(event, 1.0F);
    }

    protected void playSoundEffect(SoundEvent event, float volume) {
        this.playSoundEffect(event, volume, 100);
    }

    protected void playSoundEffect(SoundEvent event, float volume, double maxDistance) {
        if (this.soundEffectVolume < 0) {
            this.soundEffectVolume = computeVolume(maxDistance);
        }

        if (this.soundEffectVolume > 0) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), event, SoundSource.PLAYERS, volume * this.soundEffectVolume, 1.0F, false);
        }
    }

    private float computeVolume(double maxDistance) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return 0f;

        double distance = player.distanceTo(this);
        if (distance > maxDistance) return 0f;

        float volume = 1.0f - (float)(distance / maxDistance);

        return Mth.clamp(volume, 0f, 1f);
    }
}
