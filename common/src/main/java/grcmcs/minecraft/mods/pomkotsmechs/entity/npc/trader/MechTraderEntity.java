package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechTraderMenu;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPack;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MechTraderEntity extends Mob implements GeoEntity, GeoAnimatable, MenuProvider {
    private static List<TraderPoolEntry> POOL = null;

    public enum TraderCategory {
        BLUEPRINT,
        MATERIAL,
        CONSUMABLE,
        FOOD
    }

    public record TraderPoolEntry(
            TraderCategory category,

            Item offerItem,
            int offerCount,
            Item priceItem,
            int priceCount,
            int weight
    ) {
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0F)
                .add(Attributes.FOLLOW_RANGE, 20)
                .add(Attributes.MAX_HEALTH, 500);
    }

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final TraderInventory inventory = new TraderInventory();
    private long despawnGameTime = -1;
    private int leavingTicks = 0;

    public MechTraderEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.setMaxUpStep(2.0F);
    }

    LookAtPlayerGoal2 g;

    @Override
    protected void registerGoals() {
        super.registerGoals();
        g = new LookAtPlayerGoal2(
                this,
                Player.class,
                20.0F
        );
        goalSelector.addGoal(
                1,
                g
        );
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this) {
            @Override
            public void clientTick() {
                // 何もしない → ボディは回転しない
            }
        };
    }

    @Override
    public SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData spawnData,
            @Nullable CompoundTag tag
    ) {
        SpawnGroupData result =
                super.finalizeSpawn(
                        level,
                        difficulty,
                        reason,
                        spawnData,
                        tag
                );

        if (!this.level().isClientSide) {
            if (POOL == null) {
                generatePool(PomkotsDataPackManager.getInstance().getDataPack().getTraderPoolItems());
            }
            generateOffers(POOL);
        }

        despawnGameTime =
                level.getLevel()
                        .getDayTime()
                        + 12000L;
        setDespawnTime(despawnGameTime);

        return result;
    }

    // ==============================================================================================================
    // メインループ
    // ==============================================================================================================

    @Override
    public void tick() {
        soundEffectVolume = -1;

        super.tick();

        if (g != null && !g.isUsing) {
            this.yHeadRot = this.getYRot() - 90.0F;
        }

        if (level().isClientSide()) {
            return;
        }

        if (despawnGameTime < 0) {
            return;
        }

        if (level().getDayTime() >= despawnGameTime && !isLeaving()) {
            setLeaving(true);
            triggerAnim("departure", "start_departure");
        }

        if (isLeaving()) {
            tickLeaving();
        }
    }

    private void tickLeaving() {
        leavingTicks++;

        if (leavingTicks > 120) {
            ((ServerLevel) level())
                    .sendParticles(
                            ParticleTypes.CLOUD,
                            getX(),
                            getY() + 1,
                            getZ(),
                            20,
                            0.5,
                            0.5,
                            0.5,
                            0.1
                    );
            this.discard();

        } else if (leavingTicks > 40) {
            Vec3 motion = getDeltaMovement();

            Vec3 direction =
                    Vec3.directionFromRotation(
                            0,
                            getYRot()
                    );

            motion =
                    motion.add(
                            direction.scale(0.5)
                    );

            double maxSpeed = 2;

            if (motion.horizontalDistance() > maxSpeed) {
                motion =
                        motion.normalize()
                                .scale(maxSpeed);
            }

            setDeltaMovement(motion);

            hasImpulse = true;
        }
    }

    @Override
    public void travel(Vec3 pos) {
        super.travel(pos);
    }

    // ==============================================================================================================
    // メニュー関連
    // ==============================================================================================================

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isServerSide()) {
            player.openMenu(this);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        var menu = new MechTraderMenu(id, playerInv, this);
        menu.setEntityId(this.getUUID().hashCode());

        return menu;
    }

    public TraderInventory getInventory() {
        return inventory;
    }

    // ==============================================================================================================
    // 取引品目生成関連
    // ==============================================================================================================

    public void generatePool(List<PomkotsDataPack.TraderPoolItem> config) {
        POOL = new ArrayList<>();
        for (var ent: config) {
            TraderCategory cat;
            switch (ent.category) {
                case "BLUEPRINT" -> cat = TraderCategory.BLUEPRINT;
                case "MATERIAL" -> cat = TraderCategory.MATERIAL;
                case "CONSUMABLE" -> cat = TraderCategory.CONSUMABLE;
                default -> cat = TraderCategory.FOOD;
            }

            Item offer = BuiltInRegistries.ITEM.get(new ResourceLocation(ent.offer_item));
            Item price = BuiltInRegistries.ITEM.get(new ResourceLocation(ent.price_item));

            POOL.add(new TraderPoolEntry(
                    cat,
                    offer,
                    ent.offer_count,
                    price,
                    ent.price_count,
                    ent.weight
            ));
        }
    }

    public void generateOffers(
            List<TraderPoolEntry> pool
    ) {
        inventory.clearContent();

        for (TraderCategory category
                : TraderCategory.values()) {

            List<TraderPoolEntry> categoryPool =
                    pool.stream()
                            .filter(e ->
                                    e.category()
                                            == category)
                            .collect(Collectors.toList());

            generateCategoryOffers(
                    category,
                    categoryPool
            );
        }

        inventory.setChanged();
    }

    private void generateCategoryOffers(
            TraderCategory category,
            List<TraderPoolEntry> pool
    ) {

        int count = TraderInventory.OFFERS_PER_CATEGORY;

        switch(category) {
            case BLUEPRINT -> {
                count = random.nextInt(TraderInventory.OFFERS_PER_CATEGORY/2) + 1;
            }
            case MATERIAL, FOOD -> {
                count = random.nextInt(TraderInventory.OFFERS_PER_CATEGORY) + 1;
            }
        }

        count = Math.min(
                        count,
                        pool.size()
                );

        for (int i = 0; i < count; i++) {

            TraderPoolEntry entry =
                    pickWeighted(pool);

            if (entry == null) {
                break;
            }

            pool.remove(entry);

            var offer = new ItemStack(
                            entry.offerItem(),
                            entry.offerCount()
                    );

            if (offer.getItem() instanceof BasePartsItem) {
                var tag = offer.getOrCreateTag();
                tag.putInt(PomkotsMechs.nbtName("Level"), 1);
                offer.setTag(tag);
            }

            inventory.setItem(
                    TraderInventory.getOfferSlot(
                            category,
                            i
                    ), offer
            );

            inventory.setItem(
                    TraderInventory.getPriceSlot(
                            category,
                            i
                    ),
                    new ItemStack(
                            entry.priceItem(),
                            entry.priceCount()
                    )
            );
        }
    }

    @Nullable
    private TraderPoolEntry pickWeighted(
            List<TraderPoolEntry> entries
    ) {

        if (entries.isEmpty()) {
            return null;
        }

        int totalWeight = 0;

        for (TraderPoolEntry entry : entries) {
            totalWeight += entry.weight();
        }

        int roll = random.nextInt(totalWeight);

        for (TraderPoolEntry entry : entries) {

            roll -= entry.weight();

            if (roll < 0) {
                return entry;
            }
        }

        return entries.get(0);
    }

    // ==============================================================================================================
    // 取引関連
    // ==============================================================================================================

    public boolean buy(
            ServerPlayer player,
            int slotIdx
    ) {
        ItemStack offer = inventory.getItem(slotIdx);
        ItemStack price = inventory.getItem(slotIdx + 1);

        if (offer.isEmpty()) {
            return false;
        }

        if (!canPay(player, price)) {
            notifyPlayer(player, PomkotsMechs.SE_BEEP.get(), "Not enough currency");
            return false;
        }

        if (!player.getInventory().add(offer.copy())) {
            notifyPlayer(player, PomkotsMechs.SE_BEEP.get(), "Inventory full");
            return false;
        }

        consumePrice(player, price);

        inventory.setItem(
                slotIdx,
                new ItemStack(PomkotsMechs.SOLD_ITEM.get())
        );

        inventory.setChanged();

        notifyPlayer(player, PomkotsMechs.SE_REGISTER.get(), "Purchased");

        return true;
    }

    private void notifyPlayer(ServerPlayer player, SoundEvent se, String message) {
        player.displayClientMessage(
                Component.literal(
                    message
                ),
                true
        );

        player.playNotifySound(
                se,
                SoundSource.PLAYERS,
                1.0F,
                1.0F
        );
    }

    private boolean canPay(Player player, ItemStack price) {
        int remain = price.getCount();

        for (ItemStack stack : player.getInventory().items) {

            if (stack.isEmpty()) {
                continue;
            }

            if (!ItemStack.isSameItemSameTags(
                    stack,
                    price
            )) {
                continue;
            }

            remain -= stack.getCount();

            if (remain <= 0) {
                return true;
            }
        }

        return false;
    }

    private void consumePrice(Player player, ItemStack price) {
        int remain = price.getCount();

        for (ItemStack stack : player.getInventory().items) {

            if (remain <= 0) {
                break;
            }

            if (stack.isEmpty()) {
                continue;
            }

            if (!ItemStack.isSameItemSameTags(
                    stack,
                    price
            )) {
                continue;
            }

            int consume =
                    Math.min(
                            remain,
                            stack.getCount()
                    );

            stack.shrink(consume);

            remain -= consume;
        }

        player.getInventory().setChanged();
    }

    // ==============================================================================================================
    // アニメーション関連
    // ==============================================================================================================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0, event -> {
            if (!isLeaving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.mech_trader.opened"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.mech_trader.idle"));
            }
        }));

        controllers.add(new AnimationController<>(this, "departure", state -> PlayState.STOP)
                .triggerableAnim("start_departure", RawAnimation.begin().thenPlay("animation.mech_trader.departure").thenLoop("animation.mech_trader.run"))
                .setSoundKeyframeHandler(soundKeyframeEvent -> {
                    if ("se_close_door".equals(soundKeyframeEvent.getKeyframeData().getSound())) {
                        playSoundEffect(PomkotsMechs.SE_CLOSE_DOOR.get());
                    }
                    else if ("se_start_car".equals(soundKeyframeEvent.getKeyframeData().getSound())) {
                        playSoundEffect(PomkotsMechs.SE_START_CAR.get());
                    }
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
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains("Items")) {
            this.inventory.clearContent();
            ContainerHelper.loadAllItems(compound, this.inventory.items);
        }

        despawnGameTime =
                compound.getLong(
                        PomkotsMechs.nbtName("DespawnGameTime")
                );
        setDespawnTime(despawnGameTime);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        ContainerHelper.saveAllItems(compound, this.inventory.items);

        compound.putLong(
                PomkotsMechs.nbtName("DespawnGameTime"),
                despawnGameTime
        );
    }

    private static final EntityDataAccessor<Long> DESPAWN_TIME = SynchedEntityData.defineId(GenericPomkotsMonster.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Boolean> IS_LEAVING = SynchedEntityData.defineId(GenericPomkotsMonster.class, EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DESPAWN_TIME, -1L);
        this.entityData.define(IS_LEAVING, false);
    }

    public void setDespawnTime(long l) {
        this.entityData.set(DESPAWN_TIME, l);
    }

    public long getDespawnTime() {
        return this.entityData.get(DESPAWN_TIME);
    }

    public void setLeaving(boolean b) {
        this.entityData.set(IS_LEAVING, b);
    }

    public boolean isLeaving() {
        return this.entityData.get(IS_LEAVING);
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

    public static class LookAtPlayerGoal2 extends LookAtPlayerGoal {
        public boolean isUsing = false;

        public LookAtPlayerGoal2(Mob mob, Class<? extends LivingEntity> class_, float f) {
            super(mob, class_, f, 0.5F);
        }

        public void start() {
            super.start();
            isUsing = true;
        }

        public void stop() {
            super.stop();
            isUsing = false;
        }
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

    public void playSoundPublic(SoundEvent event) {
        this.playSoundEffect(event);
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
