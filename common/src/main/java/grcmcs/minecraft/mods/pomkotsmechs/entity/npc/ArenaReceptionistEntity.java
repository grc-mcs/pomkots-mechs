package grcmcs.minecraft.mods.pomkotsmechs.entity.npc;

import dev.architectury.registry.menu.MenuRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaMenuData;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaReceptionistMenu;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

public class ArenaReceptionistEntity extends PathfinderMob implements GeoEntity, GeoAnimatable {
    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.FOLLOW_RANGE, 20)
                .add(Attributes.MAX_HEALTH, 20);
    }

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public ArenaReceptionistEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(2.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

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

    @Override
    public InteractionResult mobInteract(
            Player player,
            InteractionHand hand
    ) {

        if (!level().isClientSide()
                && player instanceof ServerPlayer serverPlayer
                && arenaId != null && !arenaId.isEmpty()
        ) {
            var extendedData = new ArenaMenuData.Entry(
                    arenaId,
                    ArenaReceptionistMenu.MODE_RECEPTION,
                    ItemStack.EMPTY,
                    ArenaManager.buildRanking(
                            serverPlayer.server,
                            arenaId
                    )
            );

            MenuRegistry.openExtendedMenu(
                    serverPlayer,

                    new SimpleMenuProvider(
                            (id, container, p)-> new ArenaReceptionistMenu(
                                    id,
                                    container,
                                    extendedData
                            ),
                            Component.literal(
                                    "Arena Reception"
                            )
                    ),

                    buf -> {
                        ArenaMenuData.write(
                                buf,
                                extendedData
                        );

                    }
            );
        }

        return InteractionResult.sidedSuccess(
                level().isClientSide()
        );
    }

    // ==============================================================================================================
    // メインループ
    // ==============================================================================================================

    @Override
    public void tick() {
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

    private String arenaId = "";

    public String getArenaId() {
        return arenaId;
    }

    public void setArenaId(String arenaId) {
        this.arenaId = arenaId;
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        arenaId = compound.getString(PomkotsMechs.nbtName("ArenaId"));
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (arenaId != null) {
            compound.putString(PomkotsMechs.nbtName("ArenaId"), arenaId);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    // ==============================================================================================================
    // その他
    // ==============================================================================================================

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
