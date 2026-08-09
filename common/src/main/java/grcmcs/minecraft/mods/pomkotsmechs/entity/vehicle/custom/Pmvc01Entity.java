package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.MechWorkbenchBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechWorkbenchMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadMenu;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.client.misc.ClientHudShake;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.client.sound.PomkotsSoundManager;
import grcmcs.minecraft.mods.pomkotsmechs.client.sound.SoundConfig;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.BlockPlacementPreviewEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.ElevatorEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb99Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai.MechAutoController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.rail.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.RepairKitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.pilot.PilotRoleItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItemStackHelper;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.SkillEffectApplicator;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechFeatureType;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatus;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.CachedBoneFinder;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.*;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.AmagiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShoutouItem;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionManager;
import grcmcs.minecraft.mods.pomkotsmechs.misc.scan.ScanUtils;
import grcmcs.minecraft.mods.pomkotsmechs.save.PomkotsMechsSaveData;
import grcmcs.minecraft.mods.pomkotsmechs.util.ServerElectricSparkEffect;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;

import java.util.*;
import java.util.function.Supplier;

public class Pmvc01Entity extends PomkotsVehicleBase implements HasCustomInventoryScreen, Container, MenuProvider {
    private static final int WATER_WAKE_PARTICLES_PER_FOOT = 8;
    private static final double PARTICLE_FULL_DISTANCE_SQR = 100.0D * 100.0D;
    private static final double PARTICLE_HALF_DISTANCE_SQR = 150.0D * 150.0D;
    private static final double PARTICLE_QUARTER_DISTANCE_SQR = 200.0D * 200.0D;
    private static final double GROUND_PARTICLE_DENSITY = 0.7D;
    private static final double WINGMAN_COMMAND_RANGE = 150.0D;
    public static final float DEFAULT_SCALE = 1f;
    public static final int CONTAINER_SIZE = 82;
    public static final int CONTAINER_GENERAL_ITEM_START_INDEX = 27;
    public static final int CONTAINER_ADDITIONAL_CIRCUIT_START_INDEX = 63;

    public static final double BASE_KNOCKBACK_RESISTANCE = 0.8D;

    @Override
    protected String getMechName() {
        return "pmv01";
    }

    private NonNullList<ItemStack> itemStacks;

    private int fuel = 0;
    private int inAirTicks = 0;
    private float prevHealth = 0;
    private int damageSoundCooldown = 0;
    private MechAutoController mechAutoController = null;
    private MechStatus modifiedMechStatus = new MechStatus();

    private BlockPlacementPreviewEntity blockPreviewEntity = null;

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.KNOCKBACK_RESISTANCE, BASE_KNOCKBACK_RESISTANCE)
                .add(Attributes.MAX_HEALTH, BattleBalance.MECH_HEALTH)
                .add(Attributes.ARMOR, 18);
    }

    public Pmvc01Entity(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);

        this.noCulling = false;
        this.itemStacks = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    }

    private final CachedBoneFinder boneFinder = new CachedBoneFinder();

    public CachedBoneFinder getBoneFinder() {
        return boneFinder;
    }

    public void setupBoneFinder(BakedGeoModel model) {
        boneFinder.setModel(model);
    }

    public MechStatus getModifiedMechStatus() {
        return this.modifiedMechStatus;
    }

    /******************************************************************************************
     * エンティティ基本関係の処理
     ******************************************************************************************/


    @Override
    public void tick() {
        if (MissionManager.discardOrphanedMissionEntity(this)) {
            return;
        }

        if (this.firstTick) {
            firstTickEvent();
        }

        if (this.getVehicle() instanceof ElevatorEntity elv) {

        }

        // オートパイロット関連
        if (isServerSide()) {
            var driver = this.getDrivingPassenger();
            if (driver instanceof Mob mob && mechAutoController == null) {
                mechAutoController = Utils.createMechAutoController(driver, this);
                mob.setNoAi(true);
            }
            if (mechAutoController != null) {
                if (driver == null || driver instanceof Player) {
                    mechAutoController = null;
                    lockTargets.clearLockTargets();
                } else {
                    mechAutoController.tick();
                }
            }
        }

        if (this.isClientSide()) {
            if (damageSoundCooldown > 0) {
                damageSoundCooldown--;
            }

            float current = this.getHealth();

            clientDamageThisTick = prevHealth - current;
            if (clientDamageThisTick > 0.0f && consumeWasHurtClient()) {
                onClientDamage(clientDamageThisTick);
            }

            prevHealth = current;
        }

        disableMovementModesWhenBroken();

        // @JOKE
        if (!isBroken() && this.actionController.getAction(ACT_GATTAI).isInAction()) {
            handleGattaiMode();
            return;
        }

        if (onGroundPrev) {
            inAirTicks = 0;
        } else {
            inAirTicks++;
        }

        handleMechActivationAction();

        if (areWeaponsChanged()) {
            registerWeapons();
        }

        if (this.isServerSide()) {
            if (this.isAlive() && this.isVehicle() && tickCount % 20 == 0) {
                syncFuels();
            }

            if (isHovering() && this.isServerSide()) {
                this.setNoGravity(true);
            }
        }

        this.tickAmmos();
        this.resetAllActionsWhenNotActive();
        super.tick();
        applyHoveringHorizontalDrag();

        if (this.isServerSide()) {
            if (this.isBoundToRail()) {
                Vec3 currentVelocity = this.getDeltaMovement();
                railBindComponent.moveRobotOnRail(this, currentVelocity);
            }

            if (this.isBuildMode()) {
                if (this.getDrivingPassenger() instanceof Player player) {
                    updateBuildModePreview(player);
                } else {
                    setBuildMode(false);
                }
            }

            if (this.havingEntity().isPresent()) {
                var tgt = ((ServerLevel)level()).getEntity(this.havingEntity().get());
                if (tgt != null && tgt.isAlive()) {
                    tgt.setDeltaMovement(Vec3.ZERO);
                    var pos = this.position();
                    tgt.moveTo(pos.x, pos.y + 7.5, pos.z);
                    var rot = this.getYRot();
                    tgt.setYRot(rot);
                    tgt.setYBodyRot(rot);
                    tgt.setYHeadRot(rot);
                    tgt.yRotO = rot;
                } else {
                    this.setHavingEntity(Optional.empty());
                }
            }
        } else {
            if (this.onGround()) {
                if (this.actionController.getAction(ACT_EVASION).isInAction()) {
                    spawnMovingParticles(6, 3, true);
                } else if (this.actionController.isBoost() || isSuperBoost()) {
                    spawnMovingParticles(20, 0.8F,true);
                }
            }
            spawnWaterWakeParticles();
        }

        handleElectricStun();
    }

    public void startArena(LivingEntity target) {
        if (getDrivingPassenger() instanceof Mob && mechAutoController != null) {
            mechAutoController.startArenaBattle(target);
        }
    }

    private float clientDamageThisTick = 0;

    public float getClientDamageThisTick () {
        return clientDamageThisTick;
    }

    private void onClientDamage(float damage) {
        if (damage > 50f) {
            this.playDamageSound(PomkotsMechs.SE_HIT_HEAVY_EVENT.get());
            ParticleUtil.addSparkParticles(this.getBoundingBox().getCenter(), this.level(), 15, PomkotsMechs.SPARK.get());
        } else if (damage > 15f) {
            this.playDamageSound(PomkotsMechs.SE_HIT_MIDDLE_EVENT.get());
            ParticleUtil.addSparkParticles(this.getBoundingBox().getCenter(), this.level(), 10, PomkotsMechs.SPARK.get());
        } else {
            this.playDamageSound(PomkotsMechs.SE_HIT_EVENT.get());
            ParticleUtil.addSparkParticles(this.getBoundingBox().getCenter(), this.level(), 5, PomkotsMechs.SPARK.get());
        }

        if (this.getDrivingPassenger() instanceof LocalPlayer) {
            ClientHudShake.addShake(
                    Math.min(damage * 0.5f, 10f)
            );
        }
    }

    private void playDamageSound(SoundEvent se) {
        if (damageSoundCooldown <= 0) {
            this.playSoundEffect(se);
            damageSoundCooldown = 10;
        }
    }

    @Override
    protected void playHurtSound(DamageSource damageSource) {
        //NOP
    }

    private int electricStunTick = 0;
    
    public void onElectricStun() {
        if (isServerSide() && electricStunTick == 0) {
            electricStunTick = 40;
            
            double height = getBbHeight() / 2;
            ServerElectricSparkEffect.spawnOverTime(
                (ServerLevel)this.level(),
                this.position().add(0, height, 0),
                height, // 球の半径
                40,   // 期間中に生成する総数
                2.0D  // 発生時間（秒）
            );
        }
    }

    private void handleElectricStun() {
        if (isServerSide() && electricStunTick > 0) {
            this.setDeltaMovement(Vec3.ZERO);
            electricStunTick--;
        }
    }

    private void handleGattaiMode() {
        this.setNoGravity(true);

        super.tick();

        var act = this.actionController.getAction(ACT_GATTAI);
        if (this.isServerSide()) {
            if (act.currentFireTime < 15) {
                this.setDeltaMovement(0, 0, 0);
            } else  {
                this.setDeltaMovement(0, -2, 0);

                if (this.onGround()) {
                    act.reset();
                    this.setNoGravity(false);
                }

                var ents = level().getEntitiesOfClass(Pmb99Entity.class, this.getBoundingBox());
                if (!ents.isEmpty()) {
                    var tgt = ents.get(0);
                    this.startRiding(tgt);
                    act.reset();
                }
            }
        } else {
            if (act.currentFireTime < 15) {
            } else  {
                if (this.onGround()) {
                    act.reset();
                    this.setNoGravity(false);
                }

                var ents = level().getEntitiesOfClass(Pmb99Entity.class, this.getBoundingBox());
                if (!ents.isEmpty() || this.getVehicle() instanceof Pmb99Entity) {
                    this.playSoundEffect(PomkotsMechs.SE_GASHAN.get());

                    var p1 = new Vec3(-1, 7, -1).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
                    var p2 = new Vec3(1, 7, -1).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
                    var p3 = new Vec3(-1, 7, 1).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
                    var p4 = new Vec3(1, 7, 1).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());

                    ParticleUtil.addSparkParticles(p1, this.level());
                    ParticleUtil.addSparkParticles(p2, this.level());
                    ParticleUtil.addSparkParticles(p3, this.level());
                    ParticleUtil.addSparkParticles(p4, this.level());

                    act.reset();
                }
            }
        }
    }

    private void spawnMovingParticles(int amount, float height, boolean smoke) {
        int densityDivisor = getClientParticleDensityDivisor();
        if (densityDivisor == 0) {
            return;
        }
        int reducedAmount = Math.max(1, (int)Math.round(amount * GROUND_PARTICLE_DENSITY));
        int reducedHeavyAmount = Math.max(
                1,
                (int)Math.round(Math.min(amount, 3) * GROUND_PARTICLE_DENSITY));
        int particleAmount = Math.max(1, (reducedAmount + densityDivisor - 1) / densityDivisor);
        int heavyAmount = Math.max(1, (reducedHeavyAmount + densityDivisor - 1) / densityDivisor);
        var offset = this.position();

        var pos1 = new Vec3(1, 0F, 0);
        pos1 = pos1.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
        pos1 = offset.add(pos1);

        var pos2 = new Vec3(-1, 0F, 0);
        pos2 = pos2.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
        pos2 = offset.add(pos2);

        for (int i = 0; i < particleAmount; i++) {
            double dx = (random.nextDouble() - 0.5);
            double dz = (random.nextDouble() - 0.5);
            double vy = random.nextDouble() * height + 1;

            // if (i < heavyAmount) {
            //     this.level().addParticle(
            //             PomkotsMechs.MECH_DUST_HEAVY.get(),
            //             pos1.x + dx,
            //             pos1.y,
            //             pos1.z + dz,
            //             0, vy, 0
            //     );

            //     this.level().addParticle(
            //             PomkotsMechs.MECH_DUST_HEAVY.get(),
            //             pos2.x + dx,
            //             pos2.y,
            //             pos2.z + dz,
            //             0, vy, 0
            //     );
            // }

            if (smoke) {
                double sy = random.nextDouble() * 0.2 + 0.1;
                this.level().addParticle(PomkotsMechs.MECH_DUST.get(),
                        pos1.x() + dz, pos1.y(), pos1.z() + dx, // 位置
                        0, sy, 0 // 速度
                );
                this.level().addParticle(PomkotsMechs.MECH_DUST.get(),
                        pos2.x()  + dz, pos2.y(), pos2.z() + dx, // 位置
                        0, sy, 0 // 速度
                );
            }
        }
    }

    private void spawnWaterWakeParticles() {
        int densityDivisor = getClientParticleDensityDivisor();
        if (densityDivisor == 0) {
            return;
        }
        if (!isHovering()
                || getDeltaMovement().horizontalDistanceSqr() < 0.01D) {
            return;
        }

        double waterSurfaceY = findWaterSurfaceBelow(5);
        if (Double.isNaN(waterSurfaceY)
                || getY() - waterSurfaceY > hoverHeight + 1.0D) {
            return;
        }

        Vec3 movement = getDeltaMovement();
        Vec3 wakeVelocity = movement.horizontalDistanceSqr() > 0.0001D
                ? new Vec3(movement.x, 0.0D, movement.z).normalize().scale(-0.20D)
                : Vec3.ZERO;

        int particlesPerFoot = Math.max(
                1,
                (WATER_WAKE_PARTICLES_PER_FOOT + densityDivisor - 1) / densityDivisor);
        spawnWaterWakeAt(new Vec3(1.0D, 0.0D, -1.5D), waterSurfaceY, wakeVelocity, particlesPerFoot);
        spawnWaterWakeAt(new Vec3(-1.0D, 0.0D, -1.5D), waterSurfaceY, wakeVelocity, particlesPerFoot);
    }

    private void spawnWaterWakeAt(
            Vec3 localOffset,
            double waterSurfaceY,
            Vec3 velocity,
            int particleCount) {
        Vec3 position = localOffset
                .yRot((float) Math.toRadians(-getYRot()))
                .add(getX(), waterSurfaceY + 0.05D, getZ());
        for (int i = 0; i < particleCount; i++) {
            level().addAlwaysVisibleParticle(
                    PomkotsMechs.WATER_WAKE.get(),
                    true,
                    position.x + (random.nextDouble() - 0.5D) * 0.35D,
                    position.y,
                    position.z + (random.nextDouble() - 0.5D) * 0.35D,
                    velocity.x + (random.nextDouble() - 0.5D) * 0.70D,
                    0.20D + random.nextDouble() * 0.28D,
                    velocity.z + (random.nextDouble() - 0.5D) * 0.70D
            );
        }
    }

    private int getClientParticleDensityDivisor() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return 0;
        }
        if (getDrivingPassenger() == minecraft.player) {
            return 1;
        }

        double distanceSqr = minecraft.gameRenderer.getMainCamera()
                .getPosition()
                .distanceToSqr(position());
        if (distanceSqr <= PARTICLE_FULL_DISTANCE_SQR) {
            return 1;
        }
        if (distanceSqr <= PARTICLE_HALF_DISTANCE_SQR) {
            return 2;
        }
        if (distanceSqr <= PARTICLE_QUARTER_DISTANCE_SQR) {
            return 4;
        }
        return 0;
    }

    private double findWaterSurfaceBelow(int maxDepth) {
        for (int offsetY = 0; offsetY <= maxDepth; offsetY++) {
            BlockPos pos = BlockPos.containing(getX(), getY() - offsetY, getZ());
            BlockState state = level().getBlockState(pos);
            if (state.getFluidState().is(FluidTags.WATER)) {
                return pos.getY() + state.getFluidState().getHeight(level(), pos);
            }
            if (!state.isAir()) {
                return Double.NaN;
            }
        }
        return Double.NaN;
    }

    private void resetAllActionsWhenNotActive() {
        if (this.isBroken() || !this.canWork(false) || !this.isVehicle()) {
            for (Action a: actionController.getAllActions()) {
                if (a.isInAction()) {
                    a.reset();
                }
            }
        }
    }

    private void disableMovementModesWhenBroken() {
        if (!isBroken()) {
            return;
        }

        resetExtensionUnitStatus();
        setBoost(false);
        actionController.getAction(ACT_SUPER_BOOST).reset();
        setNoGravity(false);
    }

    private void updateBuildModePreview(Player player) {
        Vec3 newPos;

        var hitResult = player.pick(20,0,true);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            // 配置先のブロック位置
            var blockHitResult = (BlockHitResult)hitResult;
            var blockPos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());

            newPos = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        } else {
            Vec3 look = player.getLookAngle();
            Vec3 eyePos = player.getEyePosition(1.0F);

            newPos = eyePos.add(look.scale(20));
        }

        // プレビューを更新
        if (this.blockPreviewEntity != null) {
            this.blockPreviewEntity.moveTo(newPos);
        }
    }

    protected void firstTickEvent() {
        // サーバ内の情報をクライアントサイドに全て同期する
        if (this.isServerSide()) {
            this.syncAllParameter2Client();
            this.initializeAmmoManager(false);
        }

        // パーツのアニメーションを有効化させるために一回だけInventory Tickを実行しとく
        tickAllParts();
    }

    protected void tickAllParts() {
        for (var parts: itemStacks) {
            if (parts.getItem() instanceof BasePartsItem) {
                parts.inventoryTick(this.level(), this, 0, false);
            }
        }
    }

    private boolean prevCanwork = false;
    private boolean prevBroken = false;;
    private boolean prevLocked = false;

    private void handleMechActivationAction() {
        if (this.firstTick) {
            this.prevLocked = this.isLocked();
            this.prevBroken = this.isBroken();
            this.prevCanwork = this.canWork(false);
        }

        if (this.isBroken()) {
            if (!prevBroken) {
                actionController.getAction(ACT_DEACTIVATE).startAction();
                if (this.isClientSide()) {
                    this.playSoundEffect(PomkotsMechs.SE_EXPLOSION_EVENT.get());
                }
            } else if (this.getMaxHealth() == this.getHealth()) {
                this.setBroken(false);
                actionController.getAction(ACT_INACTIVATE).startAction();
            }

            if (actionController.getAction(ACT_DEACTIVATE).isInAction() && this.isClientSide()) {
                this.spawnDestructionParticles();
            }
        } else {
            boolean canWork = this.canWork(false);
            if (prevCanwork && !canWork || !prevLocked && isLocked()) {
                actionController.getAction(ACT_DEACTIVATE).startAction();
            } else if (!prevCanwork && canWork || prevLocked && !isLocked()) {
                actionController.getAction(ACT_INACTIVATE).startAction();
            }

            prevCanwork = canWork;
        }
        this.prevBroken = this.isBroken();
        this.prevLocked = this.isLocked();
    }

    // レール系機能ここから
    private final RailBindComponent railBindComponent = new RailBindComponent();

    public boolean isBoundToRail() {
        return this.entityData.get(IS_BOUND_TO_RAIL);
    }

    public boolean tryBindToRail() {
        var bp = railBindComponent.findRailBlockBelow(this.level(), this.blockPosition());
        this.entityData.set(IS_BOUND_TO_RAIL, bp != null);

        return bp != null;
    }

    public void unbindFromRail() {
        this.entityData.set(IS_BOUND_TO_RAIL, false);
    }
    // レール系機能おわり

    @Override
    public void setDeltaMovement(Vec3 movement) {
        if (Double.isNaN(movement.x) || Double.isNaN(movement.y) || Double.isNaN(movement.z)) {
//            movement = new Vec3(0,0,0);
            PomkotsMechs.LOGGER.info("" + movement);
            return;
        }
        super.setDeltaMovement(movement);
    }

    @Override
    public boolean canWork() {
        return canWork(true);
    }

    public boolean canWork(boolean consumeFuel) {
        var res = !this.getHeadParts().isEmpty()
                && !this.getBodyParts().isEmpty()
                && !this.getArmParts().isEmpty()
                && !this.getLegsParts().isEmpty()
                && !this.getGenerator().isEmpty();

        if (consumeFuel && this.isServerSide()) {
            res = res && this.consumeFuel();
        } else {
            res = res && this.getFuelNow() != 0;
        }

        if (!res) {
            resetExtensionUnitStatus();
            setBoost(false);
        }

        return res;
    }

    public void resetExtensionUnitStatus() {
        setHovering(false);
        this.unbindFromRail();
        this.setSuperBoost(false);
        this.setGliding(false);
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    public boolean isCustomNameVisible() {
        return false;
    }

    /******************************************************************************************
     * アクション/武器関係の処理
     ******************************************************************************************/

    private boolean areWeaponsChanged() {
        boolean b = false;

        b = checkWeaponChanged(INV_WEAPON_RIGHT_HAND, this.getRightArmWeapon().getItem());
        b = b || checkWeaponChanged(INV_WEAPON_LEFT_HAND, this.getLeftArmWeapon().getItem());
        b = b || checkWeaponChanged(INV_WEAPON_RIGHT_SHOULDER, this.getRightShoulderWeapon().getItem());
        b = b || checkWeaponChanged(INV_WEAPON_LEFT_SHOULDER, this.getLeftShoulderWeapon().getItem());

        return b;
    }

    private final Map<Integer, Item> weaponMap = new HashMap<>();
    private boolean checkWeaponChanged(int num, Item latest) {
        var old = weaponMap.get(num);
        if (old == latest) {
            return false;
        } else {
            weaponMap.put(num, latest);
            return true;
        }
    }

    protected static final int ACT_RIGHT_HAND = 2;
    protected static final int ACT_LEFT_HAND = 3;
    protected static final int ACT_RIGHT_SHOULDER = 4;
    protected static final int ACT_LEFT_SHOULDER = 5;
    protected static final int ACT_BIND_RAIL = 6;
    protected static final int ACT_DEACTIVATE = 7;
    protected static final int ACT_INACTIVATE = 8;
    protected static final int ACT_WALL_JUMP = 9;
    protected static final int ACT_SUPER_BOOST = 10;
    protected static final int ACT_PLACE_BLOCK = 11;

    protected static final int ACT_RELOAD_RIGHT_HAND = 12;
    protected static final int ACT_RELOAD_LEFT_HAND = 13;
    protected static final int ACT_RELOAD_RIGHT_SHOULDER = 14;
    protected static final int ACT_RELOAD_LEFT_SHOULDER = 15;

    protected static final int ACT_SCAN = 16;

    // @JOKE
    protected static final int ACT_GATTAI = 99;

    @Override
    protected void registerActions() {
        this.actionController.registerAction(ACT_EVASION, new Action(15, 0, 15), ActionController.ActionType.BASE);
        this.actionController.registerAction(ACT_JUMP, new Action(10, 6, 4), ActionController.ActionType.BASE);

        this.actionController.registerAction(ACT_RIGHT_HAND, new ActionWeapon(this, INV_WEAPON_RIGHT_HAND), ActionController.ActionType.R_ARM_MAIN);
        this.actionController.registerAction(ACT_LEFT_HAND, new ActionWeapon(this, INV_WEAPON_LEFT_HAND), ActionController.ActionType.L_ARM_MAIN);
        this.actionController.registerAction(ACT_RIGHT_SHOULDER, new ActionWeapon(this, INV_WEAPON_RIGHT_SHOULDER), ActionController.ActionType.R_SHL_MAIN);
        this.actionController.registerAction(ACT_LEFT_SHOULDER, new ActionWeapon(this, INV_WEAPON_LEFT_SHOULDER), ActionController.ActionType.L_SHL_MAIN);

        this.actionController.registerAction(ACT_BIND_RAIL, new Action(10, 0, 10), ActionController.ActionType.BASE);

        this.actionController.registerAction(ACT_DEACTIVATE, new Action(10, 0, 10), ActionController.ActionType.BASE);
        this.actionController.registerAction(ACT_INACTIVATE, new Action(10, 0, 10), ActionController.ActionType.BASE);

        this.actionController.registerAction(ACT_WALL_JUMP, new Action(9, 6, 4), ActionController.ActionType.BASE);

        this.actionController.registerAction(ACT_SUPER_BOOST, new Action(60, 14, 6), ActionController.ActionType.BASE);

        this.actionController.registerAction(ACT_PLACE_BLOCK, new Action(21, 15, 5), ActionController.ActionType.BASE);

        this.actionController.registerAction(ACT_RELOAD_RIGHT_HAND, new Action(21, 10, 5), ActionController.ActionType.BASE);
        this.actionController.registerAction(ACT_RELOAD_LEFT_HAND, new Action(21, 10, 5), ActionController.ActionType.BASE);
        this.actionController.registerAction(ACT_RELOAD_RIGHT_SHOULDER, new Action(21, 10, 5), ActionController.ActionType.BASE);
        this.actionController.registerAction(ACT_RELOAD_LEFT_SHOULDER, new Action(21, 10, 5), ActionController.ActionType.BASE);

        this.actionController.registerAction(ACT_SCAN, new Action(100, 2, 20), ActionController.ActionType.BASE);

        // @JOKE
        this.actionController.registerAction(ACT_GATTAI, new Action(60, 5, 60), ActionController.ActionType.BASE);

        this.registerWeapons();
    }

    protected void registerWeapons() {
        registerWeapon((ActionWeapon)this.actionController.getAction(ACT_RIGHT_HAND), this.getRightArmWeapon());
        registerWeapon((ActionWeapon)this.actionController.getAction(ACT_LEFT_HAND), this.getLeftArmWeapon());
        registerWeapon((ActionWeapon)this.actionController.getAction(ACT_RIGHT_SHOULDER), this.getRightShoulderWeapon());
        registerWeapon((ActionWeapon)this.actionController.getAction(ACT_LEFT_SHOULDER), this.getLeftShoulderWeapon());
    }

    protected void registerWeapon(ActionWeapon act, ItemStack itemStack) {
        BasePartsItem.WeaponInterface wpn = null;

        if (itemStack.getItem() instanceof BasePartsItem.Weapon bw){
            wpn = bw;
        } else {
            wpn = new ActionWeapon.DefaultDummyWeapon();
        }

        act.setWeapon(wpn, itemStack);
    }

    @Override
    protected void applyPlayerInput(DriverInput driverInput) {
        if (!isBroken()) {
            handleWingmanCommand(driverInput);
            getUserIntentionForDirectionFromKey(driverInput);

            this.applyPlayerInputWeapons(driverInput);
            this.applyPlayerInputBoost(driverInput);

            if (driverInput.isLockPressed() && !driverInput.isReloadPressed() && !hasHardLockCircuit()
                    && !this.actionController.getAction(ACT_SCAN).isInAction()
                    && !this.actionController.getAction(ACT_SCAN).isInCooltime()) {
                this.actionController.getAction(ACT_SCAN).startAction();

                if (this.isServerSide() && getDrivingPassenger() instanceof ServerPlayer serverPlayer) {
                    ScanUtils.spawnPersonalScan((ServerLevel)level(), serverPlayer, this.position());
                }
            }

            if (!this.isSuperBoost()) {
                this.applyPlayerInputEvasion(driverInput);
                this.applyPlayerInputJump(driverInput);
                this.applyPlayerInputInAirActions(driverInput);
            }
        }
    }

    @Override
    protected void applyPlayerInputWeapons(DriverInput driverInput) {
        if (this.isBuildMode()) {
            if (isServerSide() && this.blockPreviewEntity != null) {
                if (!actionController.getAction(ACT_PLACE_BLOCK).isInAction()) {
                    if (driverInput.isWeaponRightShoulderReleased()) {
                        this.blockPreviewEntity.setYRot(this.blockPreviewEntity.getYRot() + 90);
                    } else if (driverInput.isWeaponLeftShoulderReleased()) {
                        this.blockPreviewEntity.incrementPatternIndex();
                    }
                }
                if (actionController.getAction(ACT_PLACE_BLOCK).isOnFire()) {
                    if (level() instanceof ServerLevel l && getDrivingPassenger() instanceof ServerPlayer p) {
                        this.blockPreviewEntity.placeBlocks(l, p);
                    }
                }
            }
            if (driverInput.isWeaponRightHandReleased()
                    && !actionController.getAction(ACT_PLACE_BLOCK).isInAction()
                    && !actionController.getAction(ACT_PLACE_BLOCK).isInCooltime()
            ) {
                actionController.getAction(ACT_PLACE_BLOCK).startAction();
            }
        } else {
            applyPlayerInputWeapon(
                    (ActionWeapon)this.actionController.getAction(ACT_RIGHT_HAND),
                    (ActionWeapon)this.actionController.getAction(ACT_LEFT_HAND),
                    driverInput, driverInput.isWeaponRightHandPressed(), driverInput.isWeaponRightHandReleased(), getAmmoManager(INV_WEAPON_RIGHT_HAND));
            applyPlayerInputWeapon(
                    (ActionWeapon)this.actionController.getAction(ACT_LEFT_HAND),
                    (ActionWeapon)this.actionController.getAction(ACT_RIGHT_HAND),
                    driverInput, driverInput.isWeaponLeftHandPressed(), driverInput.isWeaponLeftHandReleased(), getAmmoManager(INV_WEAPON_LEFT_HAND));
            applyPlayerInputWeapon(
                    (ActionWeapon)this.actionController.getAction(ACT_RIGHT_SHOULDER),
                    (ActionWeapon)this.actionController.getAction(ACT_LEFT_SHOULDER),
                    driverInput, driverInput.isWeaponRightShoulderPressed(), driverInput.isWeaponRightShoulderReleased(), getAmmoManager(INV_WEAPON_RIGHT_SHOULDER));
            applyPlayerInputWeapon(
                    (ActionWeapon)this.actionController.getAction(ACT_LEFT_SHOULDER),
                    (ActionWeapon)this.actionController.getAction(ACT_RIGHT_SHOULDER),
                    driverInput, driverInput.isWeaponLeftShoulderPressed(), driverInput.isWeaponLeftShoulderReleased(), getAmmoManager(INV_WEAPON_LEFT_SHOULDER));
        }

        if (driverInput.isExtension1Released()) {
            applyPlayerInputExtension(getExtension1Weapon());
        }
        if (driverInput.isExtension2Released()) {
            applyPlayerInputExtension(getExtension2Weapon());
        }
        if (driverInput.isRepairReleased() && !driverInput.isReloadPressed()) {
            repair();
        }
    }

    private void handleWingmanCommand(DriverInput input) {
        if (!isServerSide() || !input.isReloadPressed()) return;
        if (!(getDrivingPassenger() instanceof ServerPlayer commander)) return;

        if (input.isRepairReleased()) {
            int count = commandNearbyWingmen(commander, null);
            showWingmanCommandMessage(commander, "regroup", count);
            return;
        }
        if (!input.isLockJustPressed()) return;

        Entity target = getLockTargets().getLockTargetSoft();
        if (!(target instanceof LivingEntity living) || !living.isAlive()) {
            commander.displayClientMessage(
                    Component.translatable("message.pomkotsmechs.wingman.no_soft_lock"), true);
            return;
        }
        int count = commandNearbyWingmen(commander, living);
        showWingmanCommandMessage(commander, "attack", count);
    }

    private int commandNearbyWingmen(ServerPlayer commander, LivingEntity target) {
        List<Pmvc01Entity> wingmen = level().getEntitiesOfClass(
                Pmvc01Entity.class,
                getBoundingBox().inflate(WINGMAN_COMMAND_RANGE),
                mech -> mech != this && mech.isWingmanOf(commander)
        );
        int commanded = 0;
        for (Pmvc01Entity wingman : wingmen) {
            MechAutoController controller = wingman.getOrCreateMechAutoController();
            if (controller == null) continue;
            if (target == null) controller.commandRegroup();
            else controller.commandAttack(target);
            commanded++;
        }
        return commanded;
    }

    private boolean isWingmanOf(ServerPlayer commander) {
        if (!(getDrivingPassenger() instanceof MechPilotEntity pilot)) return false;
        if (!(pilot.getOffhandItem().getItem() instanceof PilotRoleItem.PlotRoleWingman)) return false;
        return pilot.getWingmanMasterId()
                .map(commander.getUUID()::equals)
                .orElse(false);
    }

    private MechAutoController getOrCreateMechAutoController() {
        LivingEntity driver = getDrivingPassenger();
        if (!(driver instanceof Mob mob)) return null;
        if (mechAutoController == null) {
            mechAutoController = Utils.createMechAutoController(driver, this);
            mob.setNoAi(true);
        }
        return mechAutoController;
    }

    public void setNpcMissionObjective(LivingEntity target) {
        MechAutoController controller = getOrCreateMechAutoController();
        if (controller != null) {
            controller.setMissionObjective(target);
        }
    }

    private static void showWingmanCommandMessage(ServerPlayer commander, String command, int count) {
        String key = count > 0
                ? "message.pomkotsmechs.wingman." + command
                : "message.pomkotsmechs.wingman.none";
        commander.displayClientMessage(
                count > 0 ? Component.translatable(key, count) : Component.translatable(key), true);
    }

    protected void applyPlayerInputWeapon(ActionWeapon act, ActionWeapon actLinked, DriverInput driverInput, boolean isPressed, boolean isReleased, AmmoManager ammoManager) {
        //@JOKE
        if (this.getVehicle() != null) {
            return;
        }

        if (ammoManager.isReloading()) {
            if (act.isInAction()) {
                act.reset();
            }
            return;
        } else if (driverInput.isReloadPressed() && isPressed) {
            ammoManager.startReload();
            return;
        }

        if (act.getMotion().getType().equals(Motion.MotionType.MULTI_LOCK)) {
            if (isReleased) {
                // 左右で同じ武器を装備していたらリンクする
                if (shouldLinkWeapon(act.getWeaponItemSlot(), actLinked.getWeaponItemSlot())
                        && !actLinked.isInAction()) {
                    if (isServerSide()) {
                        lockTargets.syncTargetMulti(act.getWeaponItemSlot(), actLinked.getWeaponItemSlot());
                    }
                    actLinked.startAction();
                }
                act.startAction();
            }
        } else if (act.getMotion().getType().equals(Motion.MotionType.CONTINUOUS)) {
            if (act.canStartAction() && isPressed) {
                if (act.getMotion().concurrentAvailable() || !isUsingUnConcurrentWeapons()) {
                    act.startAction();
                }
            } else if (act.isInAction() && isReleased) {
                act.reset();
            }
        } else if (act.getMotion().getType().equals(Motion.MotionType.CHARGE)) {
            if (act.canStartAction() && isPressed) {
                if (act.getMotion().concurrentAvailable() || !isUsingUnConcurrentWeapons()) {
                    act.startAction();
                }
            } else if (act.isInAction() && isReleased && !act.isInFire()) {
                act.fireAction();
            }
        } else if (act.getMotion().getType().equals(Motion.MotionType.TOGGLE)) {
            if (act.canStartAction() && isPressed) {
                if (act.getMotion().concurrentAvailable() || !isUsingUnConcurrentWeapons()) {
                    act.startAction();
                }
            }
        } else {
            if (act.canStartAction() && isPressed) {
                if (act.getMotion().concurrentAvailable() || !isUsingUnConcurrentWeapons()) {
                    act.startAction();
                }
            }
        }
    }

    public boolean shouldLinkWeapon(int srcInvSlot, int dstInvSlot) {
        var srcW = getPart(srcInvSlot);
        var dstW = getPart(dstInvSlot);

        return srcW.is(dstW.getItem())
                && srcW.getItem() instanceof BasePartsItem.Weapon w
                && w.getWeaponCategory() == BasePartsItem.WeaponCategory.MISSILE;
    }

    private ItemStack getPart(int slot) {
        return switch (slot) {
            case INV_WEAPON_RIGHT_HAND -> getRightArmWeapon();
            case INV_WEAPON_LEFT_HAND -> getLeftArmWeapon();
            case INV_WEAPON_RIGHT_SHOULDER -> getRightShoulderWeapon();
            case INV_WEAPON_LEFT_SHOULDER -> getLeftShoulderWeapon();
            default -> ItemStack.EMPTY;
        };
    }

    private boolean isUsingUnConcurrentWeapons() {
        return isUsingUnConcurrentWeapon((ActionWeapon) this.actionController.getAction(ACT_RIGHT_HAND))
                || isUsingUnConcurrentWeapon((ActionWeapon) this.actionController.getAction(ACT_LEFT_HAND))
                || isUsingUnConcurrentWeapon((ActionWeapon) this.actionController.getAction(ACT_RIGHT_SHOULDER))
                || isUsingUnConcurrentWeapon((ActionWeapon) this.actionController.getAction(ACT_LEFT_SHOULDER));
    }

    private boolean isUsingUnConcurrentWeapon(ActionWeapon act) {
        return !act.getMotion().concurrentAvailable() && act.isInAction();
    }

    protected void applyPlayerInputExtension(ItemStack extensionStack) {
        if (!extensionStack.isEmpty()) {
            var extensionItem = extensionStack.getItem();
            if (extensionItem instanceof HoverUnitItem) {
                setHovering(!isHovering());
            } else if (extensionItem instanceof RailSliderItem) {
                if (this.isBoundToRail()) {
                    this.unbindFromRail();
                } else if (this.tryBindToRail()) {
                    this.actionController.getAction(ACT_BIND_RAIL).startAction();
                }
            } else if (extensionItem instanceof SBUnitProtoTypeItem) {
                if (this.isSuperBoost()) {
                    if (this.isServerSide()) {
                        this.setSuperBoost(false);
                        this.actionController.setBoost(true);
                    }
                } else if (!this.actionController.getAction(ACT_SUPER_BOOST).isInAction() && !this.isGliding()){
                    setHovering(false);
                    this.actionController.getAction(ACT_SUPER_BOOST).startAction();

                    if (this.isServerSide()) {
                        this.unbindFromRail();
                        this.actionController.setBoost(false);
                    }
                }
            } else if (extensionItem instanceof GliderUnitItem glider) {
                if (this.isServerSide()) {
                    if (this.isGliding()) {
                        this.setGliding(false);
                        glider.endUsing(this.level(), extensionStack, this);

                    } else if (!this.onGround()){
                        this.setSuperBoost(false);
                        setHovering(false);
                        this.unbindFromRail();
                        this.setGliding(true);
                        glider.startUsing(this.level(), extensionStack, this);
                    }
                }
            } else if (extensionItem instanceof BuilderUnitItem && isServerSide()) {
                this.setBuildMode(!this.isBuildMode());

            } else if (extensionItem instanceof CoreDrillItem && !this.actionController.getAction(ACT_GATTAI).isInAction()) {
                // @JOKE
                if (this.getVehicle() != null) {
                    this.stopRiding();
                    this.setNoGravity(false);
                } else {
                    this.actionController.getAction(ACT_GATTAI).startAction();
                }
            }
        }
    }

    @Override
    protected void fireWeapons() {
    }

    public Vec3 getShootingOffset() {
        return this.posHistory.getFirst();
    }

    public Vec3 getPosHistory(int index) {
        if (this.posHistory.isEmpty()) {
            return this.position();
        } else if (index >= this.posHistory.size()) {
            index = this.posHistory.size() - 1;
        }

        return this.posHistory.get(index);
    }

    public float[] getShootingAngle(Entity bullet, boolean useTarget, boolean useDeviation) {
        var targetPos = getTargetPos(useDeviation);
        var bulletPos = bullet.position();

        Vec3 bulletDir = targetPos.subtract(bulletPos).normalize();

        float horizontalDist = (float)Math.sqrt(bulletDir.x * bulletDir.x + bulletDir.z * bulletDir.z);
        float pitch = (float)Math.toDegrees(Math.atan2(bulletDir.y, horizontalDist)) * (float)-1.0;
        float yaw = (float)Math.toDegrees(Math.atan2(-bulletDir.x, bulletDir.z));

        return new float[]{pitch, yaw};

    }

    protected Vec3 getTargetPos(boolean useDeviation) {
        Vec3 targetPos;
        //仮
        Entity lockTarget = this.lockTargets.getLockTargetHard();
        if (lockTarget == null) {
            lockTarget = this.lockTargets.getLockTargetSoft();
        }

        if (lockTarget != null) {
            if (lockTarget instanceof grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity hitBox) {
                targetPos = hitBox.getStableAimPosition(false);
                return useDeviation
                        ? targetPos.add(hitBox.getStableAimVelocity().scale(4))
                        : targetPos;
            }
            if (useDeviation) {
                targetPos = lockTarget.getBoundingBox().getCenter().add(lockTarget.getDeltaMovement()).add(lockTarget.getDeltaMovement()).add(lockTarget.getDeltaMovement()).add(lockTarget.getDeltaMovement());
            } else {
                targetPos = lockTarget.getBoundingBox().getCenter();
            }
        } else {
            targetPos = getCameraTargetPosition(this.getDrivingPassenger());
            targetPos = new Vec3(targetPos.x, targetPos.y, targetPos.z);
        }

        return targetPos;
    }

    protected Vec3 getCameraTargetPosition(Entity cameraEntity) {
        if (cameraEntity == null) {
            cameraEntity = this;
        } else if (cameraEntity instanceof ServerPlayer sp) {
            cameraEntity = sp.getCamera();
        }

        HitResult hitResult = cameraEntity.pick(200, 0, true);

        if (hitResult.getType() == HitResult.Type.MISS) {
            Vec3 cameraPos = cameraEntity.getEyePosition();
            Vec3 cameraDirection = cameraEntity.getLookAngle().normalize();

            return cameraPos.add(cameraDirection.scale(300));
        } else {
            return hitResult.getLocation();
        }
    }

    public BlockHitResult performBlockRaycast(LivingEntity player, double maxDistance) {
        Vec3 start = player.getEyePosition();
        Vec3 lookVec = player.getLookAngle();
        Vec3 end = start.add(lookVec.scale(maxDistance));

        return player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }


    public void addHitParticles(Entity target) {
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

    /******************************************************************************************
     * 移動/速度関係の処理
     ******************************************************************************************/

    private static final float HOVER_WALK_SPEED_MULTIPLIER = 1.0F;
    private static final float HOVER_RUN_SPEED_MULTIPLIER = 1.0F;
    private static final float HOVER_EVASION_ACCELERATION_MULTIPLIER = 0.8F;
    private static final double HOVER_HORIZONTAL_DRAG_MOVING = 1.0D;
    private static final double HOVER_HORIZONTAL_DRAG_IDLE = 0.8D;
    private static final double HOVER_VERTICAL_STIFFNESS = 0.4D;
    private static final double HOVER_VERTICAL_DAMPING = 0.4D;
    private static final double HOVER_MAX_VERTICAL_SPEED = 0.8D;

    @Override
    protected float getWalkSpeed(){
        float speed = 0.5F * this.getSpeedModifier();
        return isHovering() ? speed * HOVER_WALK_SPEED_MULTIPLIER : speed;
    }

    @Override
    protected float getRunSpeed() {
        float speed = 1.5F * this.getSpeedModifier();
        return isHovering() ? speed * HOVER_RUN_SPEED_MULTIPLIER : speed;
    }

    @Override
    protected float getFlyingSpeed() {
        if (this.actionController.isBoost()) {
            return 0.4f * this.getSpeedModifier();
        } else {
            return 0.2f * this.getSpeedModifier();
        }
    }

    @Override
    protected float getJumpSpeed() {
        return 1.5F * this.getJumpModifier() ;
    }

    @Override
    protected float getHorizontalBoostAcceleration() {
        if (this.noHorizontalBoost()) {
            return 0.475F * 12F  * this.getSpeedModifier() * 1F;
        } else if (onGround()) {
            return 0.475F * 12F  * this.getSpeedModifier() * this.getSpeedModifierEvasion();
        } else if (isHovering()) {
            return 0.475F * 10F * HOVER_EVASION_ACCELERATION_MULTIPLIER
                    * this.getSpeedModifier() * this.getSpeedModifierEvasion();
        } else {
            return 0.475F * 7F  * this.getSpeedModifier() * this.getSpeedModifierEvasion();
        }
    }

    public boolean noHorizontalBoost() {
        return this.getBooster().isEmpty();
    }

    @Override
    protected float getVerticalBoostAcceleration() {
        return 0.3F * this.getJumpModifier() * this.getSpeedModifierVertical();
    }

    @Override
    protected float getVerticalBoostMaxSpeed() {
        return 0.7F;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 3.75F;
    }

    @Override
    protected void applyPlayerInputEvasion(DriverInput driverInput) {
        if (driverInput.isEvasionPressed()
                && !this.actionController.getAction(ACT_EVASION).isInAction()
                && !this.actionController.getAction(ACT_EVASION).isInCooltime()
                && !this.actionController.getAction(ACT_WALL_JUMP).isInAction()
                && !this.isGliding()
        ) {

            if (this.noHorizontalBoost()) {
                if (this.onGround()) {
                    this.actionController.getAction(ACT_EVASION).startAction();
                    this.startEvasion();
                }
            } else if (useEnergy(this.getEnergyConsumeEvasion())
                    && this.actionController.getAction(ACT_EVASION).startAction()) {
                this.startEvasion();
                this.actionController.setBoost(true);
            }
        }
    }

    @Override
    protected void startEvasion() {
        this.startEvasion(1.0F);
    }

    protected void startEvasion(float modifier) {
        if (isServerSide()) {
            Vec3 vel;
            if (forwardIntention == 0 && sidewayIntention == 0) {
                vel = new Vec3(0, 0, 1);
            } else {
                vel = new Vec3(sidewayIntention, 0, forwardIntention).normalize();
            }

            vel = vel.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            var distance = getHorizontalBoostAcceleration() * modifier;
            vel = vel.scale(distance);

            this.push(vel.x, vel.y, vel.z);
        }
    }

    @Override
    protected void applyPlayerInputInAirActions(DriverInput driverInput) {
        if (this.actionController.getAction(ACT_SUPER_BOOST).isInAction()) {
            if (this.actionController.getAction(ACT_SUPER_BOOST).isOnStart() && this.isClientSide()) {

                var muzzlPos = new Vec3(0, 3.4, -4);
                muzzlPos = this.position().add(muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot())));

                int targetId = this.getId();
                for (int i = 0; i < 20; i++) {
                    double px = muzzlPos.x + (this.level().random.nextDouble() - 0.5) * 3.0;
                    double py = muzzlPos.y + this.level().random.nextDouble() * 3;
                    double pz = muzzlPos.z + (this.level().random.nextDouble() - 0.5) * 3.0;

                    this.level().addParticle(PomkotsMechs.SPIRAL.get(),
                            px, py, pz,
                            targetId, 0, 0);
                }
            }

            if (this.actionController.getAction(ACT_SUPER_BOOST).isOnFire() && this.isServerSide()) {
                this.setSuperBoost(true);
            }
            return;
        }

        if (!this.onGround()) {
            if (this.actionController.getAction(ACT_WALL_JUMP).isCharging()) {
                var delta = this.getDeltaMovement();
                if (isServerSide()) this.setDeltaMovement(delta.x, 0, delta.z);
            } else if (this.actionController.getAction(ACT_WALL_JUMP).isOnFire()) {
                if (driverInput.isEvasionPressed()) {
                    this.actionController.getAction(ACT_EVASION).startAction();
                    this.startEvasion(2);
                    if (isServerSide()) this.push(0, 1.5, 0);
                } else {
                    if (isServerSide()) this.push(0, 2, 0);
                }

                if (this.isClientSide()) {
                    spawnJumpParticles();
                }
            } else if (driverInput.isJumpPressed()
                    && !this.actionController.getAction(ACT_WALL_JUMP).isInCooltime()
                    && !this.actionController.getAction(ACT_JUMP).isInCooltime()
                    && this.isTouchingWallSimple()
                    && !this.actionController.isInActionAll()) {
                this.actionController.getAction(ACT_WALL_JUMP).startAction();

            } else if (driverInput.isJumpPressed() && !this.getBooster().isEmpty()) {
//                if (this.isServerSide()) this.setNoGravity(true);

                if (!tryVerticalBoost()) {
                    if (isServerSide()) {
                        var delta = this.getDeltaMovement();
                        if (isGliding() && delta.y < 0) {
                            this.setDeltaMovement(delta.x, -0.2, delta.z);
                        } else {
                            this.push(0, -0.18 * 0.9800000190734863D, 0);
                        }
                    }
                }
            } else if (isServerSide()){
                if (isHovering()) {
                    this.handleHovering();
                } else if (!isBoundToRail()) {
                    var delta = this.getDeltaMovement();
                    if (isGliding() && delta.y < 0) {
                        this.setDeltaMovement(delta.x, -0.2, delta.z);
                    } else {
                        this.push(0, -0.18 * 0.9800000190734863D, 0);
                    }
                }
            }
        } else {
            if (isHovering()) {
                this.handleHovering();
            }
            if (this.isServerSide()) {
                this.setNoGravity(false);
                if (this.isGliding()) {
                    if (this.getExtension1Weapon().getItem() instanceof GliderUnitItem glider1) {
                        glider1.endUsing(this.level(), this.getExtension1Weapon(), this);
                    } else if (this.getExtension2Weapon().getItem() instanceof GliderUnitItem glider2) {
                        glider2.endUsing(this.level(), this.getExtension2Weapon(), this);
                    }
                    this.setGliding(false);
                }
            }
        }
    }

    public void spawnJumpParticles() {
        Vec3 pos = this.position();
        RandomSource random = this.getRandom();
        double baseY = this.getY() - 0.1;

        for (int i = 0; i < 10; i++) {
            // ランダムな速度を生成
            double dx = (random.nextDouble() - 0.5);
            double dz = (random.nextDouble() - 0.5);
            double vy = random.nextDouble() * 0.2 + 0.1;

            // パーティクルをクライアント側で発生させる
            this.level().addAlwaysVisibleParticle(PomkotsMechs.SPARK.get(),
                    pos.x + dx,
                    baseY,
                    pos.z + dz,
                    0, vy, 0
            );
        }

        BlockState dirt = Blocks.DIRT.defaultBlockState();
        for (int i = 0; i < 30; i++) {
            double dx = (random.nextDouble() - 0.5);
            double dz = (random.nextDouble() - 0.5);
            double vy = random.nextDouble() * 0.2 + 0.1;

            this.level().addParticle(
                    new BlockParticleOption(ParticleTypes.BLOCK, dirt), // 砂ぼこり系
                    pos.x + dx,
                    baseY,
                    pos.z + dz,
                    0, vy, 0
            );
        }
    }

    public enum WallSide {
        FRONT, BACK, LEFT, RIGHT, NONE
    }

    private boolean isTouchingWallSimple() {
        Level level = this.level();
        AABB box = this.getBoundingBox().inflate(0.3);

        return !level.noCollision(this, box);

//        // 現在の移動方向を元に確認
//        Vec3 motion = this.getDeltaMovement();
//        double dx = motion.x;
//        double dz = motion.z;
//
//        if (Math.abs(dx) > Math.abs(dz)) {
//            // 横方向優先
//            AABB checkBox = box.move(Math.signum(dx) * 0.05, 0, 0);
//            return !level.noCollision(this, checkBox);
//        } else {
//            // 前後方向優先
//            AABB checkBox = box.move(0, 0, Math.signum(dz) * 0.05);
//            return !level.noCollision(this, checkBox);
//        }
    }

    public WallSide getTouchingWallSide(double checkDistance) {
        Level level = this.level();
        AABB box = this.getBoundingBox();

        float yawRad = (float) Math.toRadians(this.getYRot());

        // 向きベクトル
        double forwardX = -Math.sin(yawRad);
        double forwardZ = Math.cos(yawRad);

        // 右方向（yaw + 90°）
        double rightX = Math.cos(yawRad);
        double rightZ = Math.sin(yawRad);

        // 各方向のチェック
        boolean front  = !level.noCollision(this, box.move(forwardX * checkDistance, 0, forwardZ * checkDistance));
        boolean back   = !level.noCollision(this, box.move(-forwardX * checkDistance, 0, -forwardZ * checkDistance));
        boolean right  = !level.noCollision(this, box.move(rightX * checkDistance, 0, rightZ * checkDistance));
        boolean left   = !level.noCollision(this, box.move(-rightX * checkDistance, 0, -rightZ * checkDistance));

        // 優先順位を決めて返す（前→左右→後）
        if (front) return WallSide.FRONT;
        if (right) return WallSide.RIGHT;
        if (left)  return WallSide.LEFT;
        if (back)  return WallSide.BACK;

        return WallSide.NONE;
    }


    @Override
    protected boolean tryVerticalBoost() {
        if (this.isGliding()) {
            return false;
        }

        if (useEnergy(this.getEnergyConsumeVertical())) {
            if (isServerSide() && this.getDeltaMovement().y() < getVerticalBoostMaxSpeed()) {
                if (isHovering()) {
                    this.push(0, getVerticalBoostAcceleration() * 2, 0);
                } else {
                    this.push(0, getVerticalBoostAcceleration(), 0);
                }

                return true;
            }
        }
        return false;
    }

    protected boolean isHovering() {
        return this.entityData.get(IS_HOVERING);
    }

    public boolean isHoveringEnabled() {
        return isHovering();
    }

    private void setHovering(boolean hovering) {
        this.entityData.set(IS_HOVERING, hovering);
    }

    private final float hoverHeight = 3.0f; // 最低維持高度

    private void handleHovering() {
        // 地面または液体面までの距離を計算
        float distanceToGround = getDistanceToGround();

        if (distanceToGround <= hoverHeight + 5) {
            Vec3 velocity = getDeltaMovement();
            double heightError = hoverHeight - distanceToGround;
            double verticalSpeed = Mth.clamp(
                    velocity.y * HOVER_VERTICAL_DAMPING
                            + heightError * HOVER_VERTICAL_STIFFNESS,
                    -HOVER_MAX_VERTICAL_SPEED,
                    HOVER_MAX_VERTICAL_SPEED
            );
            setDeltaMovement(velocity.x, verticalSpeed, velocity.z);
        } else {
            if (isServerSide()) this.push(0, -0.18 * 0.9800000190734863D, 0);
        }
    }

    private void applyHoveringHorizontalDrag() {
        if (!isHovering()) {
            return;
        }

        Vec3 velocity = getDeltaMovement();
        boolean hasMovementInput = forwardIntention != 0 || sidewayIntention != 0;
        double drag = hasMovementInput
                ? HOVER_HORIZONTAL_DRAG_MOVING
                : HOVER_HORIZONTAL_DRAG_IDLE;
        setDeltaMovement(velocity.x * drag, velocity.y, velocity.z * drag);
    }

    private float getDistanceToGround() {
        Level world = level();
        double entityY = getY();

        // エンティティの足元から下方向にレイキャスト
        for (int i = 0; i < 10; i++) {
            BlockPos checkPos = new BlockPos((int)getX(), (int)Math.floor(entityY - i), (int)getZ());
            BlockState blockState = world.getBlockState(checkPos);

            // 固体ブロックまたは液体をチェック
            if (!blockState.isAir() && (blockState.isSolid() ||
                    blockState.getFluidState().is(FluidTags.WATER) ||
                    blockState.getFluidState().is(FluidTags.LAVA))) {

                // エンティティの足元から地面までの正確な距離を計算
                double groundY = checkPos.getY() + 1.0; // ブロックの上面
                return (float)(entityY - groundY);
            }
        }

        return Float.MAX_VALUE; // 地面が見つからない場合
    }

    /**************************************************************************************
     * エネルギー関係の処理
     **************************************************************************************/

    private float energy = 0;
    private int overHeatCooltime = 0;
    private int OVER_HEAT_COOL_TIME_MAX = 60;

    @Override
    public int getEnergy() {
        return (int)this.energy;
    }

    public int getOverHeatCooltime() {
        return overHeatCooltime;
    }

    @Override
    protected void chargeEnergy() {
        if (overHeatCooltime-- == 0) {
            this.setOverHeat(false);
        }

        if (this.isGliding()) {
            return;
        }

        if (this.energy + this.getEnergyChargePerTick() > this.getMaxEnergy()) {
            this.energy = this.getMaxEnergy();
        } else {
            this.energy += this.getEnergyChargePerTick();
        }
    }

    @Override
    protected boolean useEnergy(int dec) {
        if (this.isOverHeat()) {
            return false;
        }

        if (this.energy - dec < 0) {
            this.energy = 0;
            this.setOverHeat(true);
            this.overHeatCooltime = OVER_HEAT_COOL_TIME_MAX;
            return true;
        } else {
            this.energy -= dec;
            return true;
        }
    }

    public boolean consumeEnergy(int dec) {
        return this.useEnergy(dec);
    }

    @Override
    public int getMaxEnergy() {
        return this.entityData.get(Pmvc01Entity.MAX_ENERGY);
    }

    protected boolean isDriverCreativeMode() {
        var driver = getDrivingPassenger();
        return driver instanceof Player p && p.getAbilities().instabuild;
    }

    protected boolean consumeFuel() {
        if (this.isServerSide()) {
            if (isDriverCreativeMode()) {
                return true;
            }

            if (fuel > 0) {
                fuel--;
                return true;
            } else {
                var fuels = this.getFuelFromInventory();

                if (fuels.getCount() > 0) {
                    var g = this.getGeneratorFromInventory().getItem();

                    if (g instanceof BasePartsItem.Generator gen) {
                        fuel = (int)getWorkSecPerFuel() * 20 * 2;
                        fuels.shrink(1);

                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } else {
            return true;
        }
    }

    private void syncFuels() {
        int maxFuel = 0;
        int nowFuel = 0;

        if (this.getGeneratorFromInventory().getItem() instanceof BasePartsItem.Generator gen) {
            var tpf = (int)getWorkSecPerFuel() * 20 * 2;
            maxFuel = tpf * this.getFuelFromInventory().getMaxStackSize();
            nowFuel =  tpf * this.getFuelFromInventory().getCount() + this.fuel;
        }

        this.entityData.set(FUEL_MAX, maxFuel);
        this.entityData.set(FUEL_NOW, nowFuel);
    }

    /**************************************************************************************
     * ターゲットロック関係の処理
     **************************************************************************************/

    @Override
    public boolean shouldLockMulti(DriverInput driverInput) {
        if (this.isBuildMode()) {
            return false;
        } else if (this.isGattai() && this.getVehicle() instanceof Pmb99Entity pmb99) {
            //@JOKE
            return pmb99.shouldLockMulti(driverInput);
        }

        boolean res = false;
        if (driverInput.isWeaponRightHandPressed()) {
            res |= isMultiLockEnabledWeapon(this.getRightArmWeapon());
        }
        if (driverInput.isWeaponLeftHandPressed()) {
            res |= isMultiLockEnabledWeapon(this.getLeftArmWeapon());
        }
        if (driverInput.isWeaponRightShoulderPressed()) {
            res |= isMultiLockEnabledWeapon(this.getRightShoulderWeapon());
        }
        if (driverInput.isWeaponLeftShoulderPressed()) {
            res |= isMultiLockEnabledWeapon(this.getLeftShoulderWeapon());
        }

        return res;
    }

    public static boolean isMultiLockEnabledWeapon(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        } else if (stack.getItem() instanceof BasePartsItem.Weapon w) {
            return w.maxMultiLockNum() > 0;
        } else {
            return false;
        }
    }

    public static int getMultiLockTargetNum(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        } else if (stack.getItem() instanceof BasePartsItem.Weapon w) {
            return w.maxMultiLockNum();
        } else {
            return 0;
        }
    }

    @Override
    public boolean shouldLockWeak(DriverInput driverInput) {
        if (this.isBuildMode()) {
            return false;
        }

        boolean hasWeaponInput =
                shouldLockWeak(driverInput.isWeaponRightHandPressed(), getRightArmWeapon())
                || shouldLockWeak(driverInput.isWeaponLeftHandPressed(), getLeftArmWeapon())
                || shouldLockWeak(driverInput.isWeaponRightShoulderPressed(), getRightShoulderWeapon())
                || shouldLockWeak(driverInput.isWeaponLeftShoulderPressed(), getLeftShoulderWeapon());
                
        return hasWeaponInput && hasLockSoftCircuit();
    }

    private boolean shouldLockWeak(boolean inputFlag, ItemStack weaponStack) {
        if (!inputFlag) {
            return false;
        }

        if (weaponStack.getItem() instanceof BasePartsItem.Weapon weapon) {
            return weapon.isSoftLockEnabled();
        } else {
            return false;
        }
    }

    private boolean hasSoftLockCircuit() {
        var i1 = this.getExtension1Weapon().getItem();
        var i2 = this.getExtension2Weapon().getItem();

        return i1 instanceof CircuitSoftLockItem || i2 instanceof  CircuitSoftLockItem;
    }

    @Override
    public boolean shouldLockStrong(DriverInput driverInput) {
        return driverInput.isLockPressed()
            && hasLockHardCircuit();
    }

    private boolean hasHardLockCircuit() {
        var i1 = this.getExtension1Weapon().getItem();
        var i2 = this.getExtension2Weapon().getItem();

        return i1 instanceof CircuitHardLockItem || i2 instanceof  CircuitHardLockItem;
    }

    /******************************************************************************************
     * モーション/サウンド関係の処理
     ******************************************************************************************/

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<PomkotsVehicleBase>(this, "controller", 0, event -> {
            //@JOKE
            if (this.actionController.getAction(ACT_GATTAI).isInAction()) {
                if (this.actionController.getAction(ACT_GATTAI).isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(
                        RawAnimation.begin().thenPlay("animation." + getMechName() + ".z_gattai_01")
                                .thenLoop("animation." + getMechName() + ".z_gattai_02")
                );
            } else if (this.getVehicle() instanceof Pmb99Entity) {
                event.getController().forceAnimationReset();
                return event.setAndContinue(
                        RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".z_gattai_03")
                );
            }

            if (this.actionController.getAction(ACT_DEACTIVATE).isInAction()) {
                if (this.actionController.getAction(ACT_DEACTIVATE).isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".deactivate"));
            } else if (this.actionController.getAction(ACT_INACTIVATE).isInAction()) {
                if (this.actionController.getAction(ACT_INACTIVATE).isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".inactivate"));
            }

            if (this.isBroken() && prevBroken) {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".idle_deactive"));
            }

            if (this.isLocked() && prevLocked) {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".idle_deactive"));
            }

            if (!this.canWork(false)) {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".idle_deactive"));
            }

            if (this.getDrivingPassenger() == null) {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".idle"));
            }

            PlayState res = controllAnimationWeapons(event);

            if (res != null) {
                return res;
            }

            return controllAnimationBasicMove(event);
        }).setSoundKeyframeHandler(soundKeyframeEvent -> {
            this.registerAnimationSoundHandlers(soundKeyframeEvent);
        }));

        controllers.add(new AnimationController<PomkotsVehicleBase>(this, "fly", 1, event -> {
            return controllAnimationFlyingMotion(event);
        }).setSoundKeyframeHandler(soundKeyframeEvent -> {
            this.registerAnimationSoundHandlers(soundKeyframeEvent);
        }));

        controllers.add(new AnimationController<PomkotsVehicleBase>(this, "rotation", 1, event -> {
            return controllAnimationRotation(event);
        }));


        controllers.add(new AnimationController<>(this, "boosters", 1, event -> {
            //@JOKE
            if (this.isGattai()) {
                return PlayState.STOP;
            }

            DriverInput driverInput = this.getDriverInput();

            if (this.isSuperBoost()) {
                this.playSoundEffectWithFade("boost", PomkotsMechs.SE_BOOST.get(),0.8F, 20, 10);

                return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".booster_super"));

            } else if (this.actionController.getAction(ACT_EVASION).isInAction()) {
                if (this.actionController.getAction(ACT_EVASION).isOnStart()) {
                    event.getController().forceAnimationReset();

                    if (this.sidewayIntention > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".booster_dash_left"));
                    } else if (sidewayIntention < 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".booster_dash_right"));
                    } else {
                        return PlayState.CONTINUE;
                    }
                }
                return PlayState.CONTINUE;
            } else if (driverInput != null && driverInput.isJumpPressed() && this.isInAirInternal() && !this.getBooster().isEmpty()) {
                this.playSoundEffectWithFade("boost", PomkotsMechs.SE_BOOST.get(),0.6F, 40, 10);

                return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".booster_vertical"));

            } else if (this.isBoost()) {
                if (onGround() || inAirTicks < 10) {
                    this.playSoundEffectWithFade("dash", PomkotsMechs.SE_DASH.get(),0.6F, 20, 10);
                } else {
                    this.stopSoundEffectWithFade("dash");
                }
                this.stopSoundEffectWithFade("boost");

                if (this.forwardIntention > 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".booster_dash_front"));
                } else if (this.sidewayIntention > 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".booster_dash_left"));
                } else if (sidewayIntention < 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".booster_dash_right"));
                } else {
                    return PlayState.CONTINUE;
                }
            }

            this.stopAllSoundEffectWithFade();
            return PlayState.STOP;

        }).setSoundKeyframeHandler(soundKeyframeEvent -> {
            this.registerAnimationSoundHandlers(soundKeyframeEvent);
        }));

        controllers.add(new AnimationController<>(this, "boosters_quick", 0, event -> {
            //@JOKE
            if (this.isGattai()) {
                return PlayState.STOP;
            }

            if (this.actionController.getAction(ACT_EVASION).isInAction()) {
                if (this.actionController.getAction(ACT_EVASION).isOnStart()) {
                    if (this.getBooster() == null || this.getBooster().isEmpty()) {
                        return PlayState.STOP;
                    }

                    event.getController().forceAnimationReset();

                    if (forwardIntention > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".booster_quick"));
                    } else if (this.sidewayIntention > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".booster_quick_left"));
                    } else if (sidewayIntention < 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".booster_quick_right"));
                    } else {
                        return PlayState.CONTINUE;
                    }
                }

                return PlayState.CONTINUE;
            }

            return PlayState.STOP;
        }));

        addExtraAnimationController(controllers);
    }

    //@JOKE
    public boolean isGattai() {
        return this.getVehicle() instanceof Pmb99Entity;
    }

    @Override
    protected PlayState controllAnimationBasicMove(AnimationState<PomkotsVehicleBase> event) {
        if (this.actionController.getAction(ACT_SUPER_BOOST).isInAction()) {
            if (this.actionController.getAction(ACT_SUPER_BOOST).isOnStart()) {
                event.getController().forceAnimationReset();
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".super_boost"));
            }
            return PlayState.CONTINUE;
        } else if (this.actionController.getAction(ACT_WALL_JUMP).isInAction()) {
            if (this.actionController.getAction(ACT_WALL_JUMP).isOnStart()) {
                event.getController().forceAnimationReset();
            }
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".jump"));

        } else if (this.actionController.getAction(ACT_JUMP).isInAction()) {
            if (this.actionController.getAction(ACT_JUMP).isOnStart()) {
                event.getController().forceAnimationReset();
            }
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".jump"));

        } else if (this.actionController.getAction(ACT_BIND_RAIL).isInAction()) {
            if (this.actionController.getAction(ACT_BIND_RAIL).isOnStart()) {
                event.getController().forceAnimationReset();
            }
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".bindrail"));
        }

        if (event.isMoving()) {
            if (this.isBoundToRail() && this.getDeltaMovement().length() > 2) {
                var legPos1 = new Vec3(-1, 0, -1).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
                var legPos2 = new Vec3(1, 0, -1).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());

                ParticleUtil.addSparkParticlesSmall(legPos1, this.level());
                ParticleUtil.addSparkParticlesSmall(legPos2, this.level());
            }

            if (this.actionController.getAction(ACT_EVASION).isInAction()) {
                if (this.noHorizontalBoost()) {
                    if (this.sidewayIntention > 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".evasion_left"));

                    } else if (sidewayIntention < 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".evasion_right"));

                    } else if (forwardIntention < 0) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".evasion_back"));

                    } else {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".evasion_front"));

                    }
                }
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".evasion"));
            } else if (this.actionController.isBoost() || this.isSuperBoost()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".dash"));
            } else {
                if (this.isBoundToRail()) {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".dash"));
                } else if (this.isInAirInternal()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".idle"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".walk"));
                }
            }
        } else {
            if (this.isBoundToRail()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".dash"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".idle"));
            }
        }
    }

    @Override
    protected PlayState controllAnimationFlyingMotion(AnimationState<PomkotsVehicleBase> event) {
        var anim = event.getController().getCurrentAnimation();

        if (anim != null && anim.animation() != null && ("animation." + getMechName() + ".onground").equals(anim.animation().name()) && !event.getController().hasAnimationFinished()) {
            return PlayState.CONTINUE;
        } else if (justLanded(event.getAnimatable())) {
            event.getController().forceAnimationReset();
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".onground"));
        }

        if (event.getAnimatable().onGround() || this.isBoundToRail() || this.actionController.getAction(ACT_WALL_JUMP).isInAction()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
        } else if (event.isMoving() && !event.getAnimatable().isBoost() && inAirTicks < 5 && this.getDeltaMovement().y <= 0) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
        } else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".flylegs"));
        }
    }

    protected boolean justLanded(Entity ent) {
        return this.onGround() && !this.onGroundPrev && inAirTicks > 5;
    }

    private boolean isInAirInternal() {
        return  isNoGravity() || (!this.onGround() && inAirTicks > 5);
    }

    @Override
    protected PlayState controllAnimationRotation(AnimationState<PomkotsVehicleBase> event) {
        if (event.isMoving()) {
            if (this.isInAirInternal() && !this.actionController.isBoost() && !this.actionController.getAction(ACT_EVASION).isInAction()) {
                if (this.forwardIntention > 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".frontfly"));
                } else if (forwardIntention < 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".backfly"));
                } else if (this.sidewayIntention < 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".rightfly"));
                } else if (sidewayIntention > 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".leftfly"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
                }
            } else if (this.actionController.getAction(ACT_EVASION).isInAction() && this.noHorizontalBoost()){
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
            } else {
                if (this.sidewayIntention < 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".right"));
                } else if (sidewayIntention > 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".left"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".forward"));
                }
            }
        } else {
            if (!this.actionController.isInActionAll()) {
                event.getController().forceAnimationReset();
            }
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
        }
    }

    @Override
    protected PlayState controllAnimationWeapons(AnimationState<PomkotsVehicleBase> event) {
        return null;
    }

    @Override
    protected void addExtraAnimationController(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "right_hand", 0, event -> {
            if (this.isBuildMode()) {
                var act = this.actionController.getAction(ACT_PLACE_BLOCK);
                if (act.isInAction()) {
                    if (act.isOnStart()) {
                        event.getController().forceAnimationReset();
                    }

                    return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".w_build_place"));
                }
                return PlayState.STOP;
            }

            var relAct = this.actionController.getAction(ACT_RELOAD_RIGHT_HAND);
            if (relAct.isInAction()) {
                if (relAct.isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".w_reload_right_hand"));
            }

            var act = (ActionWeapon)this.actionController.getAction(ACT_RIGHT_HAND);
            if (act.isInAction()) {
                if (act.isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                return event.setAndContinue(RawAnimation.begin().thenPlay(act.getMotion().getAnimationName(act, "right")));
            } else {
                if (act.getMotion().getType() == Motion.MotionType.TOGGLE && act.isToggleOnStart()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold(act.getMotion().getAnimationName(act, "right")));
                }

                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
            }
        }).setSoundKeyframeHandler(soundKeyframeEvent -> {
            this.registerAnimationSoundHandlers(soundKeyframeEvent);
        }));

        controllers.add(new AnimationController<>(this, "left_hand", 0, event -> {
            var relAct = this.actionController.getAction(ACT_RELOAD_LEFT_HAND);
            if (relAct.isInAction()) {
                if (relAct.isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".w_reload_left_hand"));
            }

            var act = (ActionWeapon)this.actionController.getAction(ACT_LEFT_HAND);
            if (act.isInAction()) {
                if (act.isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                return event.setAndContinue(RawAnimation.begin().thenPlay(act.getMotion().getAnimationName(act, "left")));
            } else {
                if (act.getMotion().getType() == Motion.MotionType.TOGGLE && act.isToggleOn()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold(act.getMotion().getAnimationName(act, "left")));
                }

                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
            }
        }).setSoundKeyframeHandler(soundKeyframeEvent -> {
            this.registerAnimationSoundHandlers(soundKeyframeEvent);
        }));

        controllers.add(new AnimationController<>(this, "right_shoulder", 1, event -> {
            var relAct = this.actionController.getAction(ACT_RELOAD_RIGHT_SHOULDER);
            if (relAct.isInAction()) {
                if (relAct.isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".w_reload_right_shoulder"));
            }

            var act = (ActionWeapon)this.actionController.getAction(ACT_RIGHT_SHOULDER);
            if (act.isInAction()) {
                if (act.isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                return event.setAndContinue(RawAnimation.begin().thenPlay(act.getMotion().getAnimationName(act, "right")));
            } else {
                if (act.getMotion().getType() == Motion.MotionType.TOGGLE && act.isToggleOnStart()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold(act.getMotion().getAnimationName(act, "right")));
                }

                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
            }
        }).setSoundKeyframeHandler(soundKeyframeEvent -> {
            this.registerAnimationSoundHandlers(soundKeyframeEvent);
        }));

        controllers.add(new AnimationController<>(this, "left_shoulder", 1, event -> {
            var relAct = this.actionController.getAction(ACT_RELOAD_LEFT_SHOULDER);
            if (relAct.isInAction()) {
                if (relAct.isOnStart()) {
                    event.getController().forceAnimationReset();
                }
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".w_reload_left_shoulder"));
            }

            var act = (ActionWeapon)this.actionController.getAction(ACT_LEFT_SHOULDER);
            if (act.isInAction()) {
                if (act.isOnStart()) {
                    event.getController().forceAnimationReset();
                }

                return event.setAndContinue(RawAnimation.begin().thenPlay(act.getMotion().getAnimationName(act, "left")));
            } else {
                if (act.getMotion().getType() == Motion.MotionType.TOGGLE && act.isToggleOnStart()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold(act.getMotion().getAnimationName(act, "left")));
                }
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
            }
        }).setSoundKeyframeHandler(soundKeyframeEvent -> {
            this.registerAnimationSoundHandlers(soundKeyframeEvent);
        }));
    }

    public void spawnReloadParticles(Vec3 spawnLocalPos, boolean isRightArm) {
        Vec3 lookVec = this.getLookAngle();

        var origin = spawnLocalPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
        origin = this.position().add(origin);

        // 右方向ベクトル（XZ平面）
        Vec3 right = lookVec.cross(new Vec3(0, 1, 0)).normalize().scale(1.5);

        if (!isRightArm) {
            right = right.scale(-1);
        }

        RandomSource rand = this.getRandom();

        // ===== 煙（上方向） =====
        for (int i = 0; i < 6; i++) {
            double vx = (rand.nextDouble() - 0.5) * 0.05;
            double vy = 0.1 + rand.nextDouble() * 0.1;
            double vz = (rand.nextDouble() - 0.5) * 0.05;

            this.level().addAlwaysVisibleParticle(ParticleTypes.SMOKE, true,
                    origin.x(), origin.y(), origin.z(), // 位置
                    vx, vy, vz // 速度
            );
        }

        // ===== 火花（横方向） =====
        for (int i = 0; i < 4; i++) {
            Vec3 dir = right.scale(0.3 + rand.nextDouble() * 0.2);

            this.level().addAlwaysVisibleParticle(PomkotsMechs.SPARK.get(), true,
                    origin.x(), origin.y(), origin.z(), // 位置
                    dir.x, 0.1 + rand.nextDouble() * 0.1, dir.z // 速度
            );
        }

        // ===== 空マガジン（黒い四角） =====
        for (int i = 0; i < 1; i++) {
            Vec3 dir = right.scale(0.3 + rand.nextDouble() * 0.2);

            var partType = PomkotsMechs.MAGAZINE_LEFT.get();
            if (isRightArm) {
                partType = PomkotsMechs.MAGAZINE_RIGHT.get();
            }

            this.level().addAlwaysVisibleParticle(partType, true,
                    origin.x(), origin.y(), origin.z(), // 位置
                    dir.x * 3F, 0.1 + rand.nextDouble() * 0.2, dir.z * 3F // 速度
            );
        }
    }

    @Override
    protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
        var sound = event.getKeyframeData().getSound();

        if ("se_jump".equals(sound)) {
            this.playSoundEffect(PomkotsMechs.SE_JUMP_EVENT.get());
        } else if ("se_booster".equals(sound)) {
            this.playSoundEffect(PomkotsMechs.SE_BOOSTER_EVENT.get());
        } else if ("se_onground".equals(sound)) {
            this.playSoundEffect(PomkotsMechs.SE_JUMP_EVENT.get());
        } else if ("se_gashan".equals(sound)) {
            this.playSoundEffect(PomkotsMechs.SE_GASHAN.get());
            var legPos1 = new Vec3(-1, 0, -1).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
            var legPos2 = new Vec3(1, 0, -1).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());

            ParticleUtil.addSparkParticles(legPos1, this.level());
            ParticleUtil.addSparkParticles(legPos2, this.level());
        } else if ("se_step".equals(sound)) {
            this.playSoundEffect(PomkotsMechs.SE_STEP.get(), 0.1F);
            this.spawnMovingParticles(5, 0.8F, false);
        } else if ("se_machine".equals(sound)) {
            if (!this.isBroken()) {
                this.playSoundEffect(PomkotsMechs.SE_MACHINE.get());
            }
        } else if ("se_charge".equals(sound)) {
            this.playSoundEffect(PomkotsMechs.SE_BOOST_CHARGE.get(), 1F);
        } else if ("se_roller1".equals(sound)) {
            this.playSoundEffect(PomkotsMechs.SE_ROLLER1.get(), 1F);
        } else if ("se_roller2".equals(sound)) {
            this.playSoundEffect(PomkotsMechs.SE_ROLLER2.get(), 1F);
        } else if ("se_lift".equals(sound)) {
            this.playSoundEffect(PomkotsMechs.SE_LIFT.get(), 1F);
        } else if ("se_reload".equals(sound)) {
            if ("right_hand".equals(event.getController().getName())) {
                spawnReloadParticles(new Vec3(-2,4,1.5), true);
            } else if ("left_hand".equals(event.getController().getName())) {
                spawnReloadParticles(new Vec3(2,4,1.5), false);
            } else if ("right_shoulder".equals(event.getController().getName())) {
                spawnReloadParticles(new Vec3(-1.5,5,-2), true);
            } else if ("left_shoulder".equals(event.getController().getName())) {
                spawnReloadParticles(new Vec3(1.5,5,-2), false);
            }
            this.playSoundEffect(PomkotsMechs.SE_RELOAD.get(), 1F);
        }
    }

    /**************************************************************************************
     * インベントリ関係の処理
     **************************************************************************************/

    public static final int INV_PARTS_HEAD = 0;
    public static final int INV_PARTS_BODY = 1;
    public static final int INV_PARTS_ARMS = 2;
    public static final int INV_PARTS_LEGS = 3;
    public static final int INV_PARTS_GENERATOR = 4;
    public static final int INV_PARTS_BOOSTER = 5;
    public static final int INV_WEAPON_RIGHT_HAND = 6;
    public static final int INV_WEAPON_LEFT_HAND = 7;
    public static final int INV_WEAPON_RIGHT_SHOULDER = 8;
    public static final int INV_WEAPON_LEFT_SHOULDER = 9;
    public static final int INV_WEAPON_EXT1 = 10;
    public static final int INV_WEAPON_EXT2 = 11;
    public static final int INV_AMMO_RA = 12;
    public static final int INV_AMMO_LA = 13;
    public static final int INV_AMMO_RS = 14;
    public static final int INV_AMMO_LS = 15;
    public static final int INV_FUEL = 16;
    public static final int INV_REPAIR_KIT = 17;

    protected ItemStack getHeadPartsFromInventory() {
        return getItem(INV_PARTS_HEAD);
    }

    protected ItemStack getBodyPartsFromInventory() {
        return getItem(INV_PARTS_BODY);
    }

    protected ItemStack getArmPartsFromInventory() {
        return getItem(INV_PARTS_ARMS);
    }

    protected ItemStack getLegsPartsFromInventory() {
        return getItem(INV_PARTS_LEGS);
    }

    protected ItemStack getRightArmWeaponFromInventory() {
        return getItem(INV_WEAPON_RIGHT_HAND);
    }

    protected ItemStack getLeftArmWeaponFromInventory() {
        return getItem(INV_WEAPON_LEFT_HAND);
    }

    protected ItemStack getRightShoulderWeaponFromInventory() {
        return getItem(INV_WEAPON_RIGHT_SHOULDER);
    }

    protected ItemStack getLeftShoulderWeaponFromInventory() {
        return getItem(INV_WEAPON_LEFT_SHOULDER);
    }

    protected ItemStack getGeneratorFromInventory() {
        return getItem(INV_PARTS_GENERATOR);
    }

    protected ItemStack getBoosterFromInventory() {
        return getItem(INV_PARTS_BOOSTER);
    }

    protected ItemStack getExtension1FromInventory() {
        return getItem(INV_WEAPON_EXT1);
    }

    protected ItemStack getExtension2FromInventory() {
        return getItem(INV_WEAPON_EXT2);
    }

    protected ItemStack getAmmoRAFromInventory() {
        return getItem(INV_AMMO_RA);
    }

    protected ItemStack getAmmoLAFromInventory() {
        return getItem(INV_AMMO_LA);
    }

    protected ItemStack getAmmoRSFromInventory() {
        return getItem(INV_AMMO_RS);
    }

    protected ItemStack getAmmoLSFromInventory() {
        return getItem(INV_AMMO_LS);
    }

    protected ItemStack getFuelFromInventory() {
        return getItem(INV_FUEL);
    }

    /**************************************************************************************
     * インベントリ関係/NBT周りの処理
     **************************************************************************************/

    private boolean shouldSave = true;

    public boolean isShouldSave() {
        return shouldSave;
    }

    public void setShouldSave(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    @Override
    public boolean shouldBeSaved() {
        return shouldSave;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        this.addChestVehicleSaveData(compound);
        this.addAmmoSaveData(compound);
        compound.putInt(PomkotsMechs.nbtName("TextureColor"), this.getTextureColor());

        compound.putInt(PomkotsMechs.nbtName("FuelNow"), fuel);

        compound.putBoolean(PomkotsMechs.nbtName("IsBroken"), isBroken());
        compound.putBoolean(PomkotsMechs.nbtName("IsLocked"), isLocked());
    }

    private void addChestVehicleSaveData(CompoundTag compoundTag) {
        if (this.getLootTable() != null) {
            compoundTag.putString("LootTable", this.getLootTable().toString());
            if (this.getLootTableSeed() != 0L) {
                compoundTag.putLong("LootTableSeed", this.getLootTableSeed());
            }
        } else {
            ContainerHelper.saveAllItems(compoundTag, this.getItemStacks());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.readChestVehicleSaveData(compound);

        this.setTextureColor(compound.getInt(PomkotsMechs.nbtName("TextureColor")));

        this.syncAllParameter2Client(true);
//        this.syncAllParameter2Client(false);

        this.readAmmoSaveData(compound);

        this.fuel = compound.getInt(PomkotsMechs.nbtName("FuelNow"));
        this.setBroken(compound.getBoolean(PomkotsMechs.nbtName("IsBroken")));
        this.setLocked(compound.getBoolean(PomkotsMechs.nbtName("IsLocked")));
    }

    void readChestVehicleSaveData(CompoundTag compoundTag) {
        this.clearItemStacks();
        if (compoundTag.contains("LootTable", 8)) {
            this.setLootTable(new ResourceLocation(compoundTag.getString("LootTable")));
            this.setLootTableSeed(compoundTag.getLong("LootTableSeed"));
        } else {
            ContainerHelper.loadAllItems(compoundTag, this.getItemStacks());
        }
    }

    @Override
    public void setLevelCallback(EntityInLevelCallback entityInLevelCallback) {
        EntityInLevelCallback2 hoge = new EntityInLevelCallback2(entityInLevelCallback, this);
        super.setLevelCallback(hoge);
    }

    public static class EntityInLevelCallback2 implements EntityInLevelCallback {
        EntityInLevelCallback del;
        Pmvc01Entity ent;
        EntityInLevelCallback2(EntityInLevelCallback delegate, Pmvc01Entity ent) {
            this.del = delegate;
            this.ent = ent;
        }

        public void onMove() {
            del.onMove();
        }

        public void onRemove(Entity.RemovalReason removalReason) {
            del.onRemove(removalReason);

            if (ent.level() instanceof ServerLevel serverLevel && (removalReason == RemovalReason.UNLOADED_TO_CHUNK || removalReason == RemovalReason.UNLOADED_WITH_PLAYER)) {
                PomkotsMechsSaveData.get(serverLevel).updateMech(ent.getUUID(), serverLevel.dimension(), new ChunkPos(ent.blockPosition()));
            }
        }
    }

    @Override
    public void remove(Entity.RemovalReason removalReason) {
        if (!this.level().isClientSide && removalReason.shouldDestroy()) {
            if (removalReason != RemovalReason.DISCARDED) {
                Containers.dropContents(this.level(), this, this);
            }
        }

        if (this.blockPreviewEntity != null) {
            this.blockPreviewEntity.discard();
        }

        if (this.isServerSide() && this.havingEntity().isPresent()) {
            var tgt = ((ServerLevel)level()).getEntity(this.havingEntity().get());
            if (tgt != null && tgt.isAlive()) {
                tgt.setNoGravity(false);
            }
            this.setHavingEntity(Optional.empty());
        }

        super.remove(removalReason);
    }

    /**************************************************************************************
     * インベントリ関係/メニュー周りの処理
     **************************************************************************************/

    public boolean isLocked(Player player) {
        if (isLocked()) {
            if (this.isServerSide()) {
                player.sendSystemMessage(Utils.string2Component("{text.pomkotsmechs.messages.pmvc01.locked}"));
            }
            return true;
        }
        return false;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        ItemStack stack = player.getItemInHand(interactionHand);
        Level level = this.level();

        if ((stack.is(PomkotsMechs.REPAIRKIT_ITEM.get()) && this.canRepair(this) ) || stack.is(PomkotsMechs.KEYCARD_ITEM.get()) || stack.is(PomkotsMechs.MECH_CAPSULE2_ITEM.get())) {
            return InteractionResult.PASS;

        } else if (this.canAddPassenger(player) && !player.isSecondaryUseActive()) {
            if (this.isLocked(player)) {
                return InteractionResult.CONSUME;
            } else if (this.isBroken()) {
                if (this.isServerSide()) {
                    player.sendSystemMessage(Utils.string2Component("{text.pomkotsmechs.messages.pmvc01.broken}"));
                }
                return InteractionResult.CONSUME;
            }

            player.setSprinting(false);

            var res = super.interact(player, interactionHand);
            this.tickAllParts();

            if (this.isServerSide() && InteractionResult.SUCCESS.equals(res)) {
                if (this.canWork(true) ) {
                    Utils.completeAdvancement(PomkotsMechs.id("pomkots_mechs/ride_the_mech"), player);
                }
            }

            return res;

        } else {
            return InteractionResult.FAIL;
        }
    }

    public void healWithEffect(ServerLevel level, float amount) {
        // 体力回復（LivingEntityを継承していない場合は独自HP管理で加算）
        this.heal(amount);
        healEffect(level);
    }

    public void healEffect(ServerLevel level) {
        // パーティクルを出す
        for (int i = 0; i < 5; i++) {
            double dx = this.getX() + (level.random.nextDouble() - 0.5) * this.getBbWidth();
            double dy = this.getY() + level.random.nextDouble() * this.getBbHeight();
            double dz = this.getZ() + (level.random.nextDouble() - 0.5) * this.getBbWidth();
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, dx, dy, dz, 1, 0.0, 0.1, 0.0, 0.1);
        }
    }

    // @TODO healと統合する
    public void repair() {
        ItemStack repairKitItemStack = getItem(INV_REPAIR_KIT);

        if (this.level() instanceof ServerLevel serverLevel
                && canRepair(this)
                && repairKitItemStack.getItem() instanceof RepairKitItem
                && repairKitItemStack.getCount() > 0
        ) {
            float healed = Math.min(this.getMaxHealth(), this.getHealth() + RepairKitItem.HEAL_AMOUNT);
            this.setHealth(healed);
            repairKitItemStack.shrink(1);
            this.entityData.set(REPAIR_KIT_NUM, this.getItem(INV_REPAIR_KIT).getCount());
        }

        if (this.isClientSide() && canRepair(this) && getRepairKitNum() > 0) {
            healEffectClient(this.level(), this);
        }
    }

    protected boolean canRepair(LivingEntity target) {
        return target.getMaxHealth() > target.getHealth() && target instanceof Pmvc01Entity;
    }

    public void healEffectClient(Level level, LivingEntity target) {
        // パーティクルを出す
        for (int i = 0; i < 30; i++) {
            double dx = target.getX() + (level.random.nextDouble() - 0.5) * target.getBbWidth();
            double dy = target.getY() + level.random.nextDouble() * target.getBbHeight();
            double dz = target.getZ() + (level.random.nextDouble() - 0.5) * target.getBbWidth();

            level.addAlwaysVisibleParticle(
                    ParticleTypes.HAPPY_VILLAGER,
                    true,
                    dx, dy, dz,
                    0, 0.1, 0
            );
        }
        this.playSoundPublic(SoundEvents.PLAYER_LEVELUP);
    }

    public void warpEffect(ServerLevel level) {
        // スパーク＋ポータル粒子をミックス
        for (int i = 0; i < 20; i++) {
            double dx = this.getX() + (level.random.nextDouble() - 0.5) * this.getBbWidth();
            double dy = this.getY() + level.random.nextDouble() * this.getBbHeight();
            double dz = this.getZ() + (level.random.nextDouble() - 0.5) * this.getBbWidth();

            // 転送エネルギーっぽいキラキラ（END_ROD）
            level.sendParticles(ParticleTypes.END_ROD, dx, dy, dz, 1, 0.0, 0.1, 0.0, 0.1);
            level.sendParticles(ParticleTypes.PORTAL, dx, dy, dz, 1, 0.0, 0.1, 0.0, 0.1);
        }

        // SEを再生
        level.playSound(null, this.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.5f, 1.2f);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        if (passenger instanceof Player) {
            this.resetExtensionUnitStatus();
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    private InteractionResult interactWithContainerVehicle(Player player) {
        player.openMenu(this);
        return !player.level().isClientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        player.openMenu(this);
//        if (!player.level().isClientSide) {
//            this.gameEvent(GameEvent.CONTAINER_OPEN, player);
//        }
    }

    private MechWorkbenchBlockEntity consoleAccessor = null;
    public void setConsoleAccessor(MechWorkbenchBlockEntity ac) {
        consoleAccessor = ac;
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (this.lootTable != null && player.isSpectator()) {
            return null;

        } else {
            if (isLocked(player)) {
                return null;
            }

            this.unpackLootTable(inventory.player);

            AbstractContainerMenu menu = null;

            var dataPad = getDatapadOfDriver(player);

            if (dataPad != null) {
                menu = new DataPadMenu(i, inventory, player, dataPad);

            } else {
                short mode = MechWorkbenchMenu.MODE_VIEW;
                if (this.getPassengers().isEmpty()) {
                    mode = MechWorkbenchMenu.MODE_ASSEMBLE;
                }

                var mechMenu = new MechWorkbenchMenu(i, inventory, this, this, mode, consoleAccessor);

                mechMenu.setEntityId(this.getUUID().hashCode());
                mechMenu.setTextureColor(this.getTextureColor());
                mechMenu.setMode(mode);
                mechMenu.sendAllDataToRemote();

                menu = mechMenu;
            }


            consoleAccessor = null;

            return menu;
        }
    }

    private ItemStack getDatapadOfDriver(Player player) {
        if (player instanceof ServerPlayer serverPlayer && player.equals(this.getDrivingPassenger())) {
            for (var stack: serverPlayer.getInventory().items) {
                if (stack.getItem() instanceof PomkotsDatapadItem) {
                    return stack;
                }
            }
        }

        return null;
    }

    @Override
    public void setChanged() {
        this.syncAllParameter2Client();
        this.initializeAmmoManager(true);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.isChestVehicleStillValid(player);
    }

    boolean isChestVehicleStillValid(Player player) {
        return !this.isRemoved() && this.position().closerThan(player.position(), 8.0);
    }

    @Override
    public void clearContent() {
        this.clearChestVehicleContent();
    }

    void clearChestVehicleContent() {
        this.unpackChestVehicleLootTable((Player)null);
        this.getItemStacks().clear();
    }

    void unpackChestVehicleLootTable(@Nullable Player player) {
        MinecraftServer minecraftServer = this.level().getServer();
        if (this.getLootTable() != null && minecraftServer != null) {
            LootTable lootTable = minecraftServer.getLootData().getLootTable(this.getLootTable());
            if (player != null) {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, this.getLootTable());
            }

            this.setLootTable((ResourceLocation)null);
            LootParams.Builder builder = (new LootParams.Builder((ServerLevel)this.level())).withParameter(LootContextParams.ORIGIN, this.position());
            if (player != null) {
                builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
            }

            lootTable.fill(this, builder.create(LootContextParamSets.CHEST), this.getLootTableSeed());
        }

    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return this.isChestVehicleEmpty();
    }

    boolean isChestVehicleEmpty() {
        Iterator var1 = this.getItemStacks().iterator();

        ItemStack itemStack;
        do {
            if (!var1.hasNext()) {
                return true;
            }

            itemStack = (ItemStack)var1.next();
        } while(itemStack.isEmpty());

        return false;
    }

    @Override
    public ItemStack getItem(int i) {
        return this.getChestVehicleItem(i);
    }

    ItemStack getChestVehicleItem(int i) {
        this.unpackChestVehicleLootTable((Player)null);
        return (ItemStack)this.getItemStacks().get(i);
    }

    @Override
    public ItemStack removeItem(int i, int j) {
        return this.removeChestVehicleItem(i, j);
    }

    ItemStack removeChestVehicleItem(int i, int j) {
        this.unpackChestVehicleLootTable((Player)null);
        return ContainerHelper.removeItem(this.getItemStacks(), i, j);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return this.removeChestVehicleItemNoUpdate(i);
    }

    ItemStack removeChestVehicleItemNoUpdate(int i) {
        this.unpackChestVehicleLootTable((Player)null);
        ItemStack itemStack = (ItemStack)this.getItemStacks().get(i);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.getItemStacks().set(i, ItemStack.EMPTY);
            return itemStack;
        }
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {
        this.setChestVehicleItem(i, itemStack);
    }

    @Override
    public SlotAccess getSlot(int i) {
        return this.getChestVehicleSlot(i);
    }

    SlotAccess getChestVehicleSlot(final int i) {
        return i >= 0 && i < this.getContainerSize() ? new SlotAccess() {
            public ItemStack get() {
                return Pmvc01Entity.this.getChestVehicleItem(i);
            }

            public boolean set(ItemStack itemStack) {
                Pmvc01Entity.this.setChestVehicleItem(i, itemStack);
                return true;
            }
        } : SlotAccess.NULL;
    }

    void setChestVehicleItem(int i, ItemStack itemStack) {
        this.unpackChestVehicleLootTable((Player)null);
        this.getItemStacks().set(i, itemStack);
        if (!itemStack.isEmpty() && itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }

    }

    public void unpackLootTable(Player player) {
        this.unpackChestVehicleLootTable(player);
    }

    public ResourceLocation getLootTable() {
        return this.lootTable;
    }

    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    public void setLootTable(ResourceLocation resourceLocation) {
        this.lootTable = resourceLocation;
    }

    public void setLootTableSeed(long l) {
        this.lootTableSeed = l;
    }

    private ResourceLocation lootTable;
    private long lootTableSeed;

    public NonNullList<ItemStack> getItemStacks() {
        return this.itemStacks;
    }

    public void clearItemStacks() {
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    }

    @Override
    public void stopOpen(Player player) {
        this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(player));

    }

    /**************************************************************************************
     * 弾薬関係
     **************************************************************************************/

    private final List<AmmoManager> ammoManagers = List.of(
            new AmmoManager(this::getAmmoRAFromInventory, this::getRightArmWeaponFromInventory, RELOAD_RA, this, actionController.getAction(ACT_RELOAD_RIGHT_HAND)),
            new AmmoManager(this::getAmmoLAFromInventory, this::getLeftArmWeaponFromInventory, RELOAD_LA, this, actionController.getAction(ACT_RELOAD_LEFT_HAND)),
            new AmmoManager(this::getAmmoRSFromInventory, this::getRightShoulderWeaponFromInventory, RELOAD_RS, this, actionController.getAction(ACT_RELOAD_RIGHT_SHOULDER)),
            new AmmoManager(this::getAmmoLSFromInventory, this::getLeftShoulderWeaponFromInventory, RELOAD_LS, this, actionController.getAction(ACT_RELOAD_LEFT_SHOULDER)));

    protected void tickAmmos() {
        for (var ammoData: ammoManagers) {
            ammoData.tick();
        }
    }

    protected void addAmmoSaveData(CompoundTag compound) {
        compound.putInt(PomkotsMechs.nbtName("BulletNum1"), this.ammoManagers.get(0).bulletNum);
        compound.putInt(PomkotsMechs.nbtName("BulletNum2"), this.ammoManagers.get(1).bulletNum);
        compound.putInt(PomkotsMechs.nbtName("BulletNum3"), this.ammoManagers.get(2).bulletNum);
        compound.putInt(PomkotsMechs.nbtName("BulletNum4"), this.ammoManagers.get(3).bulletNum);
    }

    protected void readAmmoSaveData(CompoundTag compound) {
        this.ammoManagers.get(0).bulletNum = compound.getInt(PomkotsMechs.nbtName("BulletNum1"));
        this.ammoManagers.get(1).bulletNum = compound.getInt(PomkotsMechs.nbtName("BulletNum2"));
        this.ammoManagers.get(2).bulletNum = compound.getInt(PomkotsMechs.nbtName("BulletNum3"));
        this.ammoManagers.get(3).bulletNum = compound.getInt(PomkotsMechs.nbtName("BulletNum4"));

        initializeAmmoManager(false);
    }

    protected void initializeAmmoManager(boolean resetBulletNum) {
        if (this.isServerSide()) {
            for (var ammoData: ammoManagers) {
                ammoData.initialize(resetBulletNum);
            }
        }
    }

    public boolean consumeBullet(int num, ItemStack weaponStack, int weaponItemSlot) {
        var ammoManager = getAmmoManager(weaponItemSlot);
        return ammoManager.consumeBullet(num);

    }

    public boolean consumeBulletFromServerSide(int num, ItemStack weaponStack, int weaponItemSlot) {
        var ammoManager = getAmmoManager(weaponItemSlot);
        return ammoManager.consumeBulletFromServerSide(num);

    }

    public AmmoManager getAmmoManager(int weaponItemSlot) {
        return ammoManagers.get(weaponItemSlot - INV_WEAPON_RIGHT_HAND);
    }

    public static class AmmoManager {
        public int RELOAD_TICKS = 60;

        private int bulletNum;
        private int bulletNumPerMagazine;
        private int magazineNum;
        private final Pmvc01Entity owner;

        private int prevState;
        private int reloadTicks;

        private Action reloadAction;

        private final Supplier<ItemStack> ammoStackSupplier;
        private final Supplier<ItemStack> weaponStackSupplier;
        private final EntityDataAccessor<Integer> reloadStateAccessor;

        AmmoManager(Supplier<ItemStack> ammoStackSupplier, Supplier<ItemStack> weaponStackSupplier, EntityDataAccessor<Integer> r, Pmvc01Entity owner, Action reloadAction) {
            this.ammoStackSupplier = ammoStackSupplier;
            this.weaponStackSupplier = weaponStackSupplier;
            this.reloadStateAccessor = r;
            this.owner = owner;
            this.reloadAction = reloadAction;
        }

        private void initialize(boolean resetBulletNum) {
            if (owner.isServerSide()) {
                // TODO 流石に汚すぎるのでなんとかしたい
                int tmpBN = this.bulletNum;
                int tmpBNPM = this.bulletNumPerMagazine;

                this.bulletNum = 0;
                this.bulletNumPerMagazine = 0;
                this.magazineNum = 0;

                var ammoStack = this.ammoStackSupplier.get();
                var weaponStack = this.weaponStackSupplier.get();

                if (ammoStack != null && weaponStack != null) {
                    if (ammoStack.getItem() instanceof BasePartsItem.Magazine mag
                            && weaponStack.getItem() instanceof BasePartsItem.Weapon weapon) {
                        if (weapon.isMatchAmmo(mag)) {
                            this.bulletNumPerMagazine = mag.getBulletsPerMagazine(ammoStack);
                            this.magazineNum = ammoStack.getCount();

                            if (resetBulletNum) {
                                if (this.bulletNumPerMagazine != tmpBNPM) {
                                    this.bulletNum = Math.min(bulletNumPerMagazine, tmpBN);
                                } else {
                                    this.bulletNum = tmpBN;
                                }
                            } else {
                                this.bulletNum = tmpBN;
                            }
                        }
                    }
                }
                syncClient();
            }
        }

        public boolean isReloading() {
            return reloadTicks > 0;
        }

        protected void tick() {
            if (owner.isClientSide()) {
                int state = owner.entityData.get(this.reloadStateAccessor);
                if (prevState != state) {
                    deserialize(state);
                    prevState = state;
                }
            }

            if (this.reloadTicks > 0) {
                if (reloadTicks == RELOAD_TICKS && !reloadAction.isInAction()) {
                    reloadAction.startAction();
                }

                this.reloadTicks--;

                if (owner.isServerSide() && this.reloadTicks == 0) {
                    doActualReload(true);
                }
            }
        }

        private boolean doActualReload(boolean doSync) {
            var ammoStack = this.ammoStackSupplier.get();

            if (ammoStack.getCount() > 0 && ammoStack.getItem() instanceof BasePartsItem.Magazine mag) {
                ammoStack.shrink(1);
                this.bulletNum = this.bulletNumPerMagazine = mag.getBulletsPerMagazine(ammoStack);
                this.magazineNum = ammoStack.getCount();

                if (doSync) {
                    syncClient();
                }

                return true;
            }

            return false;
        }

        protected void syncClient() {
            owner.entityData.set(this.reloadStateAccessor, this.serialize());
        }

        protected boolean consumeBullet(int num) {
            if (this.bulletNumPerMagazine > 0) {
                if (this.bulletNum == 0 && this.reloadTicks > 0) {
                    return false;
                }

                this.bulletNum = Math.max(0, this.bulletNum - num);
                if (this.bulletNum  > 0) {
                    return true;

                } else if (weaponStackSupplier.get().getItem() instanceof BasePartsItem.Weapon w
                        && w.getWeaponCategory() == BasePartsItem.WeaponCategory.MISSILE
                        && owner.isServerSide()) {
                    if (doActualReload(false)) {
                        this.bulletNum -= num;
                        syncClient();
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    startReload();
                    return false;
                }
            } else {
                return false;
            }
        }

        public void startReload() {
            if (this.owner.isServerSide() && this.reloadTicks == 0){
                this.reloadTicks = RELOAD_TICKS;
                this.syncClient();
            }
        }

        protected boolean consumeBulletFromServerSide(int num) {
            boolean b = this.consumeBullet(num);
            syncClient();
            return b;
        }

        public int getBulletNum() {
            return bulletNum;
        }

        public int getBulletNumPerMagazine() {
            return bulletNumPerMagazine;
        }

        public int getMagazineNum() {
            return magazineNum;
        }

        public int getReloadTicks() {
            return reloadTicks;
        }

        private int serialize() {
            int result = 0;

            result |= (bulletNumPerMagazine & 0x3FF) << 22;
            result |= (bulletNum & 0x3FF) << 12;
            result |= (magazineNum & 0x3F) << 6;
            result |= (reloadTicks & 0x3F);


            return result;
        }

        private void deserialize(int serialized) {
            bulletNumPerMagazine = (serialized >> 22) & 0x3FF;
            bulletNum = (serialized >> 12) & 0x3FF;
            magazineNum = (serialized >> 6) & 0x3F;
            reloadTicks = serialized & 0x3F;
        }
    }

    /**************************************************************************************
     * ロボのデータでサーバ→クライアントに同期したいでーたは全部ここに入れとく
     **************************************************************************************/

    protected static final EntityDataAccessor<ItemStack> P_HEAD = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> P_ARMS = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> P_BODY = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> P_LEGS = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);

    protected static final EntityDataAccessor<ItemStack> P_GENERATOR = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> P_BOOSTER = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);

    protected static final EntityDataAccessor<ItemStack> W_RIGHT_ARM = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> W_LEFT_ARM = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> W_RIGHT_SHOULDER = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> W_LEFT_SHOULDER = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);

    protected static final EntityDataAccessor<ItemStack> W_EXTENSION1 = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);
    protected static final EntityDataAccessor<ItemStack> W_EXTENSION2 = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.ITEM_STACK);

    protected static final EntityDataAccessor<Integer> DURABILITY = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> WEIGHT = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> MAX_WEIGHT = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> SPEED = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> JUMP = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.FLOAT);

    protected static final EntityDataAccessor<Integer> MAX_ENERGY = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Float> ENERGY_CHARGE_PER_TICK = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> WORK_SEC_PER_FUEL = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);

    protected static final EntityDataAccessor<Float> SPEED_MODIFIER_EVASION = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> SPEED_MODIFIER_VERTICAL = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Integer> ENERGY_CONSUME_EVASION = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> ENERGY_CONSUME_VERTICAL = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);

    protected static final EntityDataAccessor<Boolean> HAS_LOCK_SOFT = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> HAS_LOCK_HARD = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);

    protected static final EntityDataAccessor<Integer> RELOAD_RA = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> RELOAD_LA = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> RELOAD_RS = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> RELOAD_LS = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);

    protected static final EntityDataAccessor<Integer> FUEL_MAX = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> FUEL_NOW = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);

    protected static final EntityDataAccessor<Integer> REPAIR_KIT_NUM = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);

    protected static final EntityDataAccessor<Boolean> IS_BOUND_TO_RAIL = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);

    protected static final EntityDataAccessor<Integer> TEXTURE_COLOR = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.INT);

    protected static final EntityDataAccessor<Boolean> IS_BROKEN = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> IS_LOCKED = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);

    protected static final EntityDataAccessor<Boolean> IS_OVER_HEAT = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);

    protected static final EntityDataAccessor<Boolean> IS_SUPER_BOOST = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);

    protected static final EntityDataAccessor<Boolean> SHOW_PLAYER_INV_SLOT = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);

    protected static final EntityDataAccessor<Boolean> BUILD_MODE = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);

    protected static final EntityDataAccessor<Optional<UUID>> HAVING_ENTITY = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.OPTIONAL_UUID);

    protected static final EntityDataAccessor<Boolean> IS_GLIDING = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Boolean> IS_HOVERING = SynchedEntityData.defineId(Pmvc01Entity.class, EntityDataSerializers.BOOLEAN);


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(P_HEAD, ItemStack.EMPTY);
        this.entityData.define(P_ARMS, ItemStack.EMPTY);
        this.entityData.define(P_BODY, ItemStack.EMPTY);
        this.entityData.define(P_LEGS, ItemStack.EMPTY);

        this.entityData.define(P_GENERATOR, ItemStack.EMPTY);
        this.entityData.define(P_BOOSTER, ItemStack.EMPTY);

        this.entityData.define(W_RIGHT_ARM, ItemStack.EMPTY);
        this.entityData.define(W_LEFT_ARM, ItemStack.EMPTY);
        this.entityData.define(W_RIGHT_SHOULDER, ItemStack.EMPTY);
        this.entityData.define(W_LEFT_SHOULDER, ItemStack.EMPTY);

        this.entityData.define(W_EXTENSION1, ItemStack.EMPTY);
        this.entityData.define(W_EXTENSION2, ItemStack.EMPTY);

        this.entityData.define(DURABILITY, 0);
        this.entityData.define(WEIGHT, 0);
        this.entityData.define(MAX_WEIGHT, 0);
        this.entityData.define(SPEED, 0F);
        this.entityData.define(JUMP, 0F);
        this.entityData.define(MAX_ENERGY, 0);
        this.entityData.define(ENERGY_CHARGE_PER_TICK, 0F);
        this.entityData.define(WORK_SEC_PER_FUEL, 0);
        this.entityData.define(SPEED_MODIFIER_EVASION, 0F);
        this.entityData.define(SPEED_MODIFIER_VERTICAL, 0F);
        this.entityData.define(ENERGY_CONSUME_EVASION, 0);
        this.entityData.define(ENERGY_CONSUME_VERTICAL, 0);

        this.entityData.define(HAS_LOCK_SOFT, false);
        this.entityData.define(HAS_LOCK_HARD, false);

        this.entityData.define(RELOAD_RA, 0);
        this.entityData.define(RELOAD_LA, 0);
        this.entityData.define(RELOAD_RS, 0);
        this.entityData.define(RELOAD_LS, 0);

        this.entityData.define(FUEL_MAX, 0);
        this.entityData.define(FUEL_NOW, 0);

        this.entityData.define(REPAIR_KIT_NUM, 0);

        this.entityData.define(IS_BOUND_TO_RAIL, false);

        this.entityData.define(TEXTURE_COLOR, 0);

        this.entityData.define(IS_BROKEN, false);
        this.entityData.define(IS_LOCKED, false);

        this.entityData.define(IS_OVER_HEAT, false);

        this.entityData.define(IS_SUPER_BOOST, false);

        this.entityData.define(SHOW_PLAYER_INV_SLOT, false);

        this.entityData.define(BUILD_MODE, false);
        this.entityData.define(IS_GLIDING, false);
        this.entityData.define(IS_HOVERING, false);

        this.entityData.define(HAVING_ENTITY, Optional.empty());
    }

    protected void syncAllParameter2Client() {
        syncAllParameter2Client(true);
    }

    protected void syncAllParameter2Client(boolean updateHealth) {
        this.entityData.set(P_HEAD, this.getHeadPartsFromInventory());
        this.entityData.set(P_ARMS, this.getArmPartsFromInventory());
        this.entityData.set(P_BODY, this.getBodyPartsFromInventory());
        this.entityData.set(P_LEGS, this.getLegsPartsFromInventory());

        this.entityData.set(P_GENERATOR, this.getGeneratorFromInventory());
        this.entityData.set(P_BOOSTER, this.getBoosterFromInventory());

        this.entityData.set(W_RIGHT_ARM, this.getRightArmWeaponFromInventory());
        this.entityData.set(W_LEFT_ARM, this.getLeftArmWeaponFromInventory());
        this.entityData.set(W_RIGHT_SHOULDER, this.getRightShoulderWeaponFromInventory());
        this.entityData.set(W_LEFT_SHOULDER, this.getLeftShoulderWeaponFromInventory());

        this.entityData.set(W_EXTENSION1, this.getExtension1FromInventory());
        this.entityData.set(W_EXTENSION2, this.getExtension2FromInventory());

        this.entityData.set(REPAIR_KIT_NUM, this.getItem(INV_REPAIR_KIT).getCount());

        this.updateShowPlaeyrInv();

        this.updateMechParams(updateHealth);
        this.registerWeapons();
    }

    private void updateShowPlaeyrInv() {
        if (
                this.getRightArmWeaponFromInventory().getItem() instanceof AmagiItem
                        || this.getLeftArmWeaponFromInventory().getItem() instanceof AmagiItem
                        || this.getRightArmWeaponFromInventory().getItem() instanceof ShoutouItem
                        || this.getLeftArmWeaponFromInventory().getItem() instanceof ShoutouItem
        ) {
            this.entityData.set(SHOW_PLAYER_INV_SLOT, true);
        } else {
            this.entityData.set(SHOW_PLAYER_INV_SLOT, false);
        }
    }

    private void updateMechParams(boolean updateHealth) {
        MechParam param = new MechParam();

        sumPartsParameter(this.getHeadPartsFromInventory(), param);
        sumPartsParameter(this.getArmPartsFromInventory(), param);
        sumPartsParameter(this.getBodyPartsFromInventory(), param);
        sumPartsParameter(this.getLegsPartsFromInventory(), param);
        sumPartsParameter(this.getRightArmWeaponFromInventory(), param);
        sumPartsParameter(this.getLeftArmWeaponFromInventory(), param);
        sumPartsParameter(this.getRightShoulderWeaponFromInventory(), param);
        sumPartsParameter(this.getLeftShoulderWeaponFromInventory(), param);
        sumPartsParameter(this.getGeneratorFromInventory(), param);
        sumPartsParameter(this.getBoosterFromInventory(), param);
        sumPartsParameter(this.getExtension1FromInventory(), param);
        sumPartsParameter(this.getExtension2FromInventory(), param);

        param.durability = param.durability + (int) BattleBalance.MECH_HEALTH/2;
        param.speed = calculateSpeedModifier(param.maxWeight, param.weight) * param.speed;
        param.jump = calculateSpeedModifier(param.maxWeight, param.weight) * param.jump;

        var mechStatus = applySkillEffects(param);
        var mechStats = mechStatus.stats();

        this.entityData.set(DURABILITY, (int) mechStats.health());
        this.entityData.set(WEIGHT, param.weight);
        this.entityData.set(MAX_WEIGHT, param.maxWeight);
        this.entityData.set(SPEED, (float) mechStats.moveSpeed());
        this.entityData.set(JUMP, (float) mechStats.jumpPower());
        this.entityData.set(MAX_ENERGY, (int) mechStats.energyCapacity());
        this.entityData.set(ENERGY_CHARGE_PER_TICK, (float) mechStats.energyRecovery());
        this.entityData.set(WORK_SEC_PER_FUEL, param.workSecPerFuel);
        this.entityData.set(SPEED_MODIFIER_EVASION, (float) mechStats.dashSpeed());
        this.entityData.set(SPEED_MODIFIER_VERTICAL, (float) mechStats.verticalSpeed());
        this.entityData.set(ENERGY_CONSUME_EVASION, (int)(mechStats.energyCost() * param.energyConsumeEvasion));
        this.entityData.set(ENERGY_CONSUME_VERTICAL, (int)(mechStats.energyCost() * param.energyConsumeVertical));

        this.entityData.set(HAS_LOCK_SOFT, hasSoftLockCircuit() || mechStatus.features().isEnabled(MechFeatureType.TARGET_ASSIST_SOFT));
        this.entityData.set(HAS_LOCK_HARD, hasHardLockCircuit() || mechStatus.features().isEnabled(MechFeatureType.TARGET_ASSIST_HARD));

//        this.entityData.set(DURABILITY, param.durability);
//        this.entityData.set(WEIGHT, param.weight);
//        this.entityData.set(MAX_WEIGHT, param.maxWeight);
//        this.entityData.set(SPEED, calculateSpeedModifier(param.maxWeight, param.weight) * param.speed);
//        this.entityData.set(JUMP, calculateSpeedModifier(param.maxWeight, param.weight) * param.jump);
//        this.entityData.set(MAX_ENERGY, param.maxEnergy);
//        this.entityData.set(ENERGY_CHARGE_PER_TICK, param.energyChargePerTick);
//        this.entityData.set(WORK_SEC_PER_FUEL, param.workSecPerFuel);
//        this.entityData.set(SPEED_MODIFIER_EVASION, param.speedModifierEvasion);
//        this.entityData.set(SPEED_MODIFIER_VERTICAL, param.speedModifierVertical);
//        this.entityData.set(ENERGY_CONSUME_EVASION, param.energyConsumeEvasion);
//        this.entityData.set(ENERGY_CONSUME_VERTICAL, param.energyConsumeVertical);

        this.syncFuels();

        if (updateHealth) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(mechStats.health());
            this.setHealth(Math.min(this.getHealth(), (float)mechStats.health()));
        }

        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(mechStats.knockBackResistance());
    }

    private MechStatus applySkillEffects(MechParam param) {
        this.modifiedMechStatus = new MechStatus();
        this.modifiedMechStatus.setupBaseMechParams(param);

        SkillEffectApplicator.Accumulator accumulator =
                SkillEffectApplicator.createAccumulator();

        for (int i = CONTAINER_ADDITIONAL_CIRCUIT_START_INDEX;
             i < CONTAINER_ADDITIONAL_CIRCUIT_START_INDEX + 18;
             i++) {

            var item = getItem(i);

            if (item.getItem() instanceof CircuitItem) {
                var circuit = CircuitItemStackHelper.getCircuit(item);

                if (circuit != null) {
                    accumulator.add(circuit);
                }
            }
        }

        accumulator.applyTo(this.modifiedMechStatus);

        return this.modifiedMechStatus;
    }

//    public static void updateMaxHealth(MechEntity mech) {
//        int newMaxHealth = calculateMaxHealth(mech);
//
//        if (PlatformHelper.isForge()) {
//            mech.getAttribute(Attributes.MAX_HEALTH).setBaseValue(newMaxHealth);
//        } else if (PlatformHelper.isFabric()) {
//            mech.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(newMaxHealth);
//        }
//
//        mech.setHealth(Math.min(mech.getHealth(), newMaxHealth));
//    }

    private void sumPartsParameter(ItemStack stack, MechParam param) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        if (stack.getItem() instanceof BasePartsItem partsItem) {
            param.durability += partsItem.getDurability(stack);
            param.weight += partsItem.getWeight(stack);

            if (partsItem instanceof BasePartsItem.Legs legs) {
                param.maxWeight += legs.getMaxWeight(stack);
                param.speed += legs.getSpeedModifier(stack);
                param.jump += legs.getJumpModifier(stack);
            } else if (partsItem instanceof BasePartsItem.Generator g) {
                param.maxEnergy += g.getMaxEnergy(stack);
                param.energyChargePerTick += g.getEnergyChargePerTick(stack);
                param.workSecPerFuel += g.getWorkSecPerFuel(stack);
            } else if (partsItem instanceof BasePartsItem.Booster b) {
                param.speedModifierEvasion += b.getSpeedModifierEvasion(stack);
                param.speedModifierVertical += b.getSpeedModifierVertical(stack);
                param.energyConsumeEvasion += b.getEnergyConsumeEvasion(stack);
                param.energyConsumeVertical += b.getEnergyConsumeVertical(stack);
            }
        }
    }

    public float calculateSpeedModifier(float maxLoad, float currentLoad) {
        double ratio = currentLoad / maxLoad;

        if (ratio <= 1.0) {
            // 最大積載量以下（線形補間）
            return (float)(1.0 - 0.4 * Math.max(0, ratio - 0.5));
        } else {
            // 最大積載量超過（二次減衰）
            double excess = ratio - 1.0;
            return (float)(0.2 / (1 + excess * excess * excess * excess));
        }
    }

    public int getDurability() {
        return this.entityData.get(DURABILITY);
    }

    public int getWeight() {
        return this.entityData.get(WEIGHT);
    }

    public int getMaxWeight() {
        return this.entityData.get(MAX_WEIGHT);
    }

    public float getSpeedModifier() {
        return this.entityData.get(SPEED);
    }

    public float getJumpModifier() {
        return this.entityData.get(JUMP);
    }

    public float getEnergyChargePerTick() {
        return this.entityData.get(ENERGY_CHARGE_PER_TICK);
    }

    public float getWorkSecPerFuel() {
        return this.entityData.get(WORK_SEC_PER_FUEL);
    }

    public float getSpeedModifierEvasion() {
        return this.entityData.get(SPEED_MODIFIER_EVASION);
    }

    public float getSpeedModifierVertical() {
        return this.entityData.get(SPEED_MODIFIER_VERTICAL);
    }

    public int getEnergyConsumeEvasion() {
        return this.entityData.get(ENERGY_CONSUME_EVASION);
    }

    public int getEnergyConsumeVertical() {
        return this.entityData.get(ENERGY_CONSUME_VERTICAL);
    }

    public boolean hasLockSoftCircuit() {
        return this.entityData.get(HAS_LOCK_SOFT);
    }

    public boolean hasLockHardCircuit() {
        return this.entityData.get(HAS_LOCK_HARD);
    }

    public ItemStack getHeadParts() {
        return this.entityData.get(P_HEAD);
    }

    public ItemStack getBodyParts() {
        return this.entityData.get(P_BODY);
    }

    public ItemStack getArmParts() {
        return this.entityData.get(P_ARMS);
    }

    public ItemStack getLegsParts() {
        return this.entityData.get(P_LEGS);
    }

    public ItemStack getBooster() {
        return this.entityData.get(P_BOOSTER);
    }

    public ItemStack getGenerator() {
        return this.entityData.get(P_GENERATOR);
    }

    public ItemStack getRightArmWeapon() {
        return this.entityData.get(W_RIGHT_ARM);
    }

    public ItemStack getLeftArmWeapon() {
        return this.entityData.get(W_LEFT_ARM);
    }

    public ItemStack getRightShoulderWeapon() {
        return this.entityData.get(W_RIGHT_SHOULDER);
    }

    public ItemStack getLeftShoulderWeapon() {
        return this.entityData.get(W_LEFT_SHOULDER);
    }

    public ItemStack getExtension1Weapon() {
        return this.entityData.get(W_EXTENSION1);
    }

    public ItemStack getExtension2Weapon() {
        return this.entityData.get(W_EXTENSION2);
    }

    public void setTextureColor(int i) {
        this.entityData.set(TEXTURE_COLOR, i);
    }

    public int getTextureColor() {
        return this.entityData.get(TEXTURE_COLOR);
    }

    public int getMaxFuel() {
        return this.entityData.get(FUEL_MAX);
    }

    public int getFuelNow() {
        return this.entityData.get(FUEL_NOW);
    }

    public int getRepairKitNum() {
        return this.entityData.get(REPAIR_KIT_NUM);
    }

    public boolean isBroken() {
        return this.entityData.get(IS_BROKEN);
    }

    public void setBroken(boolean broken) {
        this.entityData.set(IS_BROKEN, broken);
    }

    public boolean isLocked() {
        return this.entityData.get(IS_LOCKED);
    }

    public void setLocked(boolean broken) {
        this.entityData.set(IS_LOCKED, broken);
    }

    public boolean isOverHeat() {
        return this.entityData.get(IS_OVER_HEAT);
    }

    public void setOverHeat(boolean broken) {
        this.entityData.set(IS_OVER_HEAT, broken);
    }

    public boolean isSuperBoost() {
        return this.entityData.get(IS_SUPER_BOOST);
    }

    public void setSuperBoost(boolean flag) {
        if (this.isServerSide()) {
            this.entityData.set(IS_SUPER_BOOST, flag);
            this.setNoGravity(flag);
        }
    }

    public boolean isShowPlayerInventory() {
        return this.entityData.get(SHOW_PLAYER_INV_SLOT);
    }

    public boolean isBuildMode() {
        return this.entityData.get(BUILD_MODE);
    }

    public void setBuildMode(boolean b) {
        this.entityData.set(BUILD_MODE, b);

        if (b) {
            // プレビューエンティティを作成
            if (this.blockPreviewEntity == null || this.blockPreviewEntity.isRemoved()) {
                this.blockPreviewEntity = new BlockPlacementPreviewEntity(
                        this.level(),
                        this.position()
                );
                this.level().addFreshEntity(this.blockPreviewEntity);
            }
        } else {
            // プレビューエンティティを削除
            if (this.blockPreviewEntity != null) {
                this.blockPreviewEntity.discard();
                this.blockPreviewEntity = null;
            }
        }
    }

    public Optional<UUID> havingEntity() {
        return this.entityData.get(HAVING_ENTITY);
    }

    public void setHavingEntity(Optional<UUID> b) {
        this.entityData.set(HAVING_ENTITY, b);
    }

    protected boolean isGliding() {
        return entityData.get(IS_GLIDING);
    }

    protected void setGliding(boolean b) {
        this.entityData.set(IS_GLIDING, b);
    }

    public static class MechParam {
        public int durability;
        public int weight;
        public int maxWeight;
        public float speed;
        public float jump;
        public int maxEnergy;
        public float energyChargePerTick;
        public int workSecPerFuel;
        public int energyConsumeEvasion;
        public int energyConsumeVertical;
        public float speedModifierEvasion;
        public float speedModifierVertical;
    }

    /**************************************************************************************
     * そのほか
     **************************************************************************************/

    // クライアントのみ、銃口位置
    private Vec3 muzzlePosRA = Vec3.ZERO;
    private Vec3 muzzlePosLA = Vec3.ZERO;
    private Vec3 muzzlePosRS = Vec3.ZERO;
    private Vec3 muzzlePosLS = Vec3.ZERO;

    public Vec3 getMuzzlePosLA() {
        return muzzlePosLA;
    }

    public void setMuzzlePosLA(Vec3 muzzlePosLA) {
        this.muzzlePosLA = muzzlePosLA;
    }

    public Vec3 getMuzzlePosLS() {
        return muzzlePosLS;
    }

    public void setMuzzlePosLS(Vec3 muzzlePosLS) {
        this.muzzlePosLS = muzzlePosLS;
    }

    public Vec3 getMuzzlePosRA() {
        return muzzlePosRA;
    }

    public void setMuzzlePosRA(Vec3 muzzlePosRA) {
        this.muzzlePosRA = muzzlePosRA;
    }

    public Vec3 getMuzzlePosRS() {
        return muzzlePosRS;
    }

    public void setMuzzlePosRS(Vec3 muzzlePosRS) {
        this.muzzlePosRS = muzzlePosRS;
    }

    public boolean isUsingWeapon(BasePartsItem.Weapon w) {
        BasePartsItem.WeaponInterface.WeaponAttachPoint attchPoint = w.getWeaponAttachPoint();
        BasePartsItem.AttachSide side = w.getSide();

        if (BasePartsItem.AttachSide.RIGHT == side) {
            if (BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_ARM == attchPoint
                    || BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_HAND == attchPoint) {
                return this.actionController.getAction(ACT_RIGHT_HAND).isInAction();
            } else if (BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_SHOULDER == attchPoint) {
                return this.actionController.getAction(ACT_RIGHT_SHOULDER).isInAction();
            }
        } else if (BasePartsItem.AttachSide.LEFT == side) {
            if (BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_ARM == attchPoint
                    || BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_HAND == attchPoint){
                return this.actionController.getAction(ACT_LEFT_HAND).isInAction();
            } else if (BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_SHOULDER == attchPoint) {
                return this.actionController.getAction(ACT_LEFT_SHOULDER).isInAction();
            }
        }

        return false;
    }

    public boolean isSelfPublic(Entity ent) {
        return isSelf(ent);
    }

    protected Vec3 mainCameraPosition = null;

    public Vec3 getMainCameraPosition() {
        if (mainCameraPosition == null) {
            return Vec3.ZERO;
        } else {
            return mainCameraPosition;
        }
    }

    public void setMainCameraPosition(Vec3 pos) {
        this.mainCameraPosition = pos;
    }

    @Override
    public void travel(Vec3 pos) {
        if (!isBroken()) {
            if (this.isSuperBoost()) {
                ItemStack sb = getSuperBoostUnit();
                if (!(sb.getItem() instanceof BasePartsItem p) || !this.useEnergy((p.getEnergyConsumePerTick(sb)))) {
                    if (isServerSide()) {
                        this.setSuperBoost(false);
                    }
                }

                if (isServerSide()) {
                    // --- クライアントのカメラ方向に進む（360°可）
                    Player owner = getDrivingPassenger() instanceof Player p ? p : null;
                    if (owner == null) return;

                    Vec3 look = owner.getLookAngle(); // カメラの向きベクトル（Pitch/Yaw反映済）
                    Vec3 velocity = look.normalize().scale(4);

                    // --- サーバ側でも同期させる（tickでsetDeltaMovementを適用）
                    setDeltaMovement(velocity);

                    // --- 向きをカメラ方向に合わせる
                    setYRot(owner.getYRot());
                    setXRot(owner.getXRot());

                    // --- GeckoLibモデル回転用に保持
                    this.yRotO = this.getYRot();
                    this.xRotO = this.getXRot();
                    this.yBodyRot = this.getYRot();
                    this.yHeadRot = this.getYRot();

                    // --- 重力無効化
                    this.setNoGravity(true);

                    this.hasImpulse = true;
                    // --- 実際に移動
                    super.travelBypass(this.position().add(this.getDeltaMovement()));
                }
            } else {
                travelInternal(pos);
            }
        } else {
            super.travelBypass(pos);
        }
    }

    public void travelInternal(Vec3 pos) {
        if (this.isAlive() && this.isVehicle() && this.canWork()) {
            LivingEntity pilot = this.getDrivingPassenger();

            if (pilot != null) {
                if (!(this.getVehicle() instanceof ElevatorEntity elv)) {
                    this.setYRot(pilot.getYRot());
                    this.setXRot(pilot.getXRot() * 0.5F);

                    this.setYBodyRot(this.getYRot());
                    this.setYHeadRot(this.getYRot());

                } else {
                    this.setYRot(elv.getFixedYaw());
                    this.yRotO = elv.getFixedYaw();
                    this.setYBodyRot(elv.getFixedYaw());
                    this.yBodyRotO = elv.getFixedYaw();
                    this.setYHeadRot(elv.getFixedYaw());
                }

                float strafe = pilot.xxa * 0.5F;
                float forward = pilot.zza * 0.5F;

                if (!this.level().isClientSide) {
                    if (this.actionController.isBoost()) {
                        this.setSpeed(this.getRunSpeed());
                    } else {
                        this.setSpeed(this.getWalkSpeed());
                    }
                    this.hasImpulse = true;
                }

                super.travelBypass(new Vec3(strafe, pos.y, forward));
                return;
            }
        }

        super.travelBypass(pos);
    }

    private ItemStack getSuperBoostUnit() {
        if (this.getExtension1Weapon().getItem() instanceof SBUnitProtoTypeItem bst) {
            return this.getExtension1Weapon();
        } else if (this.getExtension2Weapon().getItem() instanceof SBUnitProtoTypeItem bst) {
            return this.getExtension2Weapon();
        }

        return ItemStack.EMPTY;
    }

    private boolean wasHurtClient = false;

    private boolean consumeWasHurtClient() {
        if (!wasHurtClient) {
            return false;
        } else {
            wasHurtClient = false;
            return true;
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isClientSide()) {
            wasHurtClient = true;
        } else if (isServerSide() && mechAutoController != null) {
            mechAutoController.handleHurt(source, amount);
        }

        if (this.isBroken()) {
            if (isForceKillSource(source)) {
                return super.hurt(source, amount);
            } else {
                return false;
            }
        } else {
            return super.hurt(source, amount);
        }
    }

    private boolean isForceKillSource(DamageSource source) {
        // /killコマンドやサーバーシステムによる即死
        if (source == this.damageSources().genericKill() || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return true;
        }

        // 特殊な強制削除の可能性
        String msgId = source.getMsgId();
        return msgId.equals("kill") || msgId.equals("out_of_world");
    }

    @Override
    protected void actuallyHurt(DamageSource damageSource, float f) {
        if (breakCompatible()) {
            super.actuallyHurt(damageSource, f);
        } else {
            f = calcActualDamage(damageSource, f);

            if (!isForceKillSource(damageSource) && f > this.getHealth()) {
                this.setHealth(1);
                this.setBroken(true);

                for (Entity passenger : this.getPassengers()) {
                    passenger.stopRiding();
                    if (passenger instanceof Mob mob) {
                        mob.setNoAi(false);
                    }
                }
            } else {
                super.actuallyHurt(damageSource, f);
            }
        }
    }

    protected float calcActualDamage(DamageSource damageSource, float f) {
        if (!this.isInvulnerableTo(damageSource)) {
            f = this.getDamageAfterArmorAbsorb(damageSource, f);
            f = this.getDamageAfterMagicAbsorb(damageSource, f);
            f = Math.max(f - this.getAbsorptionAmount(), 0.0F);

            return f;
        } else {
            return 0;
        }
    }

    protected boolean breakCompatible() {
        return false;
    }

    @Override
    protected void tickDeath() {
        super.tickDeath();
        if (this.deathTime == 10) {
            if (isServerSide()) {
                var level = this.level();
                ExplosionEntity e = new ExplosionEntity(PomkotsMechs.EXPLOSION.get(), level);
                e.setPos(this.position());
                level.addFreshEntity(e);
            }
        }
    }

    @Override
    protected void checkInsideBlocks() {
    }

    @Override
    public boolean ignoreExplosion() {
        return this.getDrivingPassenger() == null;
    }

    private void spawnDestructionParticles() {
        // ランダムに数個の爆発パーティクルを出す
        for (int i = 0; i < 5; i++) {
            double offsetX = (random.nextDouble() - 0.5) * this.getBbWidth();
            double offsetY = random.nextDouble() * this.getBbHeight();
            double offsetZ = (random.nextDouble() - 0.5) * this.getBbWidth();

            double x = this.getX() + offsetX;
            double y = this.getY() + offsetY;
            double z = this.getZ() + offsetZ;

            ParticleOptions particle = switch (random.nextInt(4)) {
                case 0 -> ParticleTypes.SMOKE;
                case 1 -> ParticleTypes.SMALL_FLAME;
                case 2 -> ParticleTypes.ELECTRIC_SPARK;
                default -> ParticleTypes.EXPLOSION;
            };

            level().addParticle(particle, x, y, z, 0, 0.01, 0);
        }

        // 一定確率で音も出す（例: 20%）
        if (random.nextFloat() < 1f) {
            this.playSoundEffect(SoundEvents.GENERIC_EXPLODE);
        }
    }

    public Action getActionEvasion() {
        return actionController.getAction(ACT_EVASION);
    }

    private boolean hasLoopSound = false;

    protected void playSoundEffectWithFade(String key, SoundEvent event, float volume, int fadein, int fadeout) {
        key = this.getId() + ":" + key;
        PomkotsSoundManager.playSound(key, event,
                        SoundConfig.loopFadeSE()
                                .followEntity(this) // 動的な位置
                                .source(SoundSource.PLAYERS)
                                .relative(false)
                                .volume(volume)
                                .fadeIn(fadein)
                                .fadeOut(fadeout)
        );
        hasLoopSound = true;
    }

    protected void stopSoundEffectWithFade(String key) {
        key = this.getId() + ":" + key;
        PomkotsSoundManager.stopSound(key, true);
    }

    protected void stopAllSoundEffectWithFade() {
        if (hasLoopSound) {
            PomkotsSoundManager.stopAllSounds(this.getId(), true);
            hasLoopSound = false;
        }
    }

    @Override
    public void onClientRemoval() {
        super.onClientRemoval();
        PomkotsSoundManager.stopAllSounds(this.getId(), true);
    }

    @Override
    public boolean shouldRenderDefaultHud(String hudName) {
        return "renderHotbar".equals(hudName) && (this.isShowPlayerInventory() || this.isBuildMode());
    }

    private boolean showCustomHealthBar = true;

    public boolean showCustomHealthBar() {
        return showCustomHealthBar;
    }

    public void setShowCustomHealthBar(boolean b) {
        showCustomHealthBar = b;
    }

    @Override
    public int decreaseAirSupply(int air) {
        return air;
    }
}
