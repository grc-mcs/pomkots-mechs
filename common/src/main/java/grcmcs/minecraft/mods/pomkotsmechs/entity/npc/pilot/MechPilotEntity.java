package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai.FindAndEnterMechGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

public class MechPilotEntity extends PathfinderMob implements GeoEntity, GeoAnimatable {
    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.FOLLOW_RANGE, 20)
                .add(Attributes.MAX_HEALTH, 20);
    }

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public MechPilotEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(2.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
//        goalSelector.addGoal(
//                1,
//                new FindAndEnterMechGoal(this)
//        );

//        // 適当に歩き回る
//        this.goalSelector.addGoal(
//                2,
//                new WaterAvoidingRandomStrollGoal(
//                        this,
//                        0.5D
//                )
//        );

        // プレイヤーを見る
        this.goalSelector.addGoal(
                3,
                new LookAtPlayerGoal(
                        this,
                        Player.class,
                        8.0F
                )
        );

        // 周囲をキョロキョロ
        this.goalSelector.addGoal(
                4,
                new RandomLookAroundGoal(this)
        );
    }


    // ==============================================================================================================
    // メインループ
    // ==============================================================================================================

    @Override
    public void tick() {
        if (MissionManager.discardOrphanedMissionEntity(this)) {
            return;
        }

        if (this.getVehicle() instanceof Pmvc01Entity) {
            this.setNoAi(true);
        } else {
            this.setNoAi(false);
        }

        super.tick();
    }


    @Override
    public void travel(@NotNull Vec3 pos) {
        super.travel(pos);
    }

    // ==============================================================================================================
    // アニメーション関連
    // ==============================================================================================================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0, event -> {
            if (this.isPassenger()) {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.mech_pilot.sit"));
            }

            if (event.isMoving()) {
                if (this.getDeltaMovement().horizontalDistanceSqr() > 0.04D) {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation.mech_pilot.run"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation.mech_pilot.walk"));
                }
            }

            return PlayState.STOP;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    // ==============================================================================================================
    // セーブデータ/クラサバ同期関連
    // ==============================================================================================================

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        wingmanMasterId = compound.hasUUID(PomkotsMechs.nbtName("WingmanMaster"))
                ? compound.getUUID(PomkotsMechs.nbtName("WingmanMaster")) : null;
        String actionRangeKey = PomkotsMechs.nbtName("CombatActionRange");
        combatActionRange = compound.contains(actionRangeKey)
                ? Math.max(0.0D, compound.getDouble(actionRangeKey)) : 0.0D;
        String homeXKey = PomkotsMechs.nbtName("CombatHomeX");
        String homeYKey = PomkotsMechs.nbtName("CombatHomeY");
        String homeZKey = PomkotsMechs.nbtName("CombatHomeZ");
        combatHome = compound.contains(homeXKey)
                && compound.contains(homeYKey)
                && compound.contains(homeZKey)
                ? new Vec3(
                        compound.getDouble(homeXKey),
                        compound.getDouble(homeYKey),
                        compound.getDouble(homeZKey))
                : null;

        if (!compound.contains(PomkotsMechs.nbtName("PilotTexture")) || compound.getString(PomkotsMechs.nbtName("PilotTexture")).isEmpty()) {
            setTextureLocation(PomkotsMechs.id("textures/entity/pilot/mech_pilot_wide_john.png").toString());
        } else {
            setTextureLocation(compound.getString(PomkotsMechs.nbtName("PilotTexture")));
        }

        if (!compound.contains(PomkotsMechs.nbtName("PilotModel")) || compound.getString(PomkotsMechs.nbtName("PilotModel")).isEmpty()) {
            setModelLocation(PomkotsMechs.id("geo/mech_pilot_wide.geo.json").toString());
        } else {
            setModelLocation(compound.getString(PomkotsMechs.nbtName("PilotModel")));
        }
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (wingmanMasterId != null) {
            compound.putUUID(PomkotsMechs.nbtName("WingmanMaster"), wingmanMasterId);
        }
        compound.putDouble(PomkotsMechs.nbtName("CombatActionRange"), combatActionRange);
        if (combatHome != null) {
            compound.putDouble(PomkotsMechs.nbtName("CombatHomeX"), combatHome.x);
            compound.putDouble(PomkotsMechs.nbtName("CombatHomeY"), combatHome.y);
            compound.putDouble(PomkotsMechs.nbtName("CombatHomeZ"), combatHome.z);
        }

        compound.putString(PomkotsMechs.nbtName("PilotTexture"), getTextureLocation());
        compound.putString(PomkotsMechs.nbtName("PilotModel"), getModelLocation());
    }

    private static final EntityDataAccessor<String> MODEL_LOCATION = SynchedEntityData.defineId(MechPilotEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> TEXTURE_LOCATION = SynchedEntityData.defineId(MechPilotEntity.class, EntityDataSerializers.STRING);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MODEL_LOCATION, PomkotsMechs.id("geo/mech_pilot_wide.geo.json").toString());
        this.entityData.define(TEXTURE_LOCATION, PomkotsMechs.id("textures/entity/pilot/mech_pilot_wide.png").toString());
    }

    public void setModelLocation(String s) {
        this.entityData.set(MODEL_LOCATION, s);
    }

    public String getModelLocation() {
        return this.entityData.get(MODEL_LOCATION);
    }

    public void setTextureLocation(String s) {
        this.entityData.set(TEXTURE_LOCATION, s);
    }

    public String getTextureLocation() {
        return this.entityData.get(TEXTURE_LOCATION);
    }

    // ==============================================================================================================
    // その他
    // ==============================================================================================================

    private boolean shouldSave = true;
    private UUID wingmanMasterId;
    private Vec3 combatHome;
    private double combatActionRange;

    public boolean isShouldSave() {
        return shouldSave;
    }

    public void setShouldSave(boolean shouldSave) {
        this.shouldSave = shouldSave;
    }

    public Optional<UUID> getWingmanMasterId() {
        return Optional.ofNullable(wingmanMasterId);
    }

    public void setWingmanMasterId(UUID wingmanMasterId) {
        this.wingmanMasterId = wingmanMasterId;
    }

    public void setCombatActionRange(Vec3 home, double range) {
        this.combatHome = home;
        this.combatActionRange = Math.max(0.0D, range);
    }

    public Optional<Vec3> getCombatHome() {
        return Optional.ofNullable(combatHome);
    }

    public double getCombatActionRange() {
        return combatActionRange;
    }

    public boolean isWithinCombatActionRange(Vec3 position) {
        if (combatHome == null || combatActionRange <= 0.0D) {
            return true;
        }
        double dx = position.x - combatHome.x;
        double dz = position.z - combatHome.z;
        return dx * dx + dz * dz <= combatActionRange * combatActionRange;
    }

    @Override
    public boolean shouldBeSaved() {
        return shouldSave;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float f) {
        if (this.getVehicle() instanceof Pmvc01Entity) {
            return false;
        } else {
            return super.hurt(damageSource, f);
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

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
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

    protected void playSoundEffect(SoundEvent event) {
        this.playSoundEffect(event, 1.0F);
    }

    private float soundEffectVolume = -1;

    protected void playSoundEffect(SoundEvent event, float volume) {
        if (this.soundEffectVolume < 0) {
            this.soundEffectVolume = computeVolume(100);
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
