package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.RaidDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.AlertEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;

public class PomkotsCubeBlockEntity extends ChestBlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final long REFILL_INTERVAL = 24000L;

    // ====================== NBT系 ======================

    protected int mode = getDefaultMode();

    private int summonActionTickCount = 0;

    private Player opener = null;

    private boolean openedOnce = false;

    private long lastRefillGameTime = -1;

    private ResourceLocation refillLootTable = null;

    private UUID raidControllerUUID;
    private RaidControllerEntity raidControllerEntity;

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putInt(PomkotsMechs.nbtName("CubeMode"), mode);
        tag.putBoolean(PomkotsMechs.nbtName("OpenedOnce"), openedOnce);
        tag.putLong(PomkotsMechs.nbtName("LastRefillTime"), lastRefillGameTime);

        if (refillLootTable != null) {
            tag.putString(PomkotsMechs.nbtName("RefillLootTable"), refillLootTable.toString());
        }

        if (this.raidControllerUUID != null) {
            tag.putUUID(PomkotsMechs.nbtName("RaidControllerUUID"), raidControllerUUID);
        } else {
            tag.remove(PomkotsMechs.nbtName("RaidControllerUUID"));
        }

    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);

        this.mode = tag.getInt(PomkotsMechs.nbtName("CubeMode"));
        this.openedOnce = tag.getBoolean(PomkotsMechs.nbtName("OpenedOnce"));
        this.lastRefillGameTime = tag.getLong(PomkotsMechs.nbtName("LastRefillTime"));
        if (tag.contains(PomkotsMechs.nbtName("RefillLootTable"))) {
            refillLootTable = new ResourceLocation(tag.getString(PomkotsMechs.nbtName("RefillLootTable")));
        }
        if (tag.contains(PomkotsMechs.nbtName("RaidControllerUUID"))) {
            raidControllerUUID = tag.getUUID(PomkotsMechs.nbtName("RaidControllerUUID"));

            if (this.level instanceof ServerLevel sl) {
                if (sl.getEntity(raidControllerUUID) instanceof RaidControllerEntity raid) {
                    raidControllerEntity = raid;
                } else {
                    raidControllerUUID = null;
                }
            }
        }
        tryRefill();
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    // ====================== コンストラクタ ======================

    public PomkotsCubeBlockEntity(BlockPos pos, BlockState state) {
        this(PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY.get(), pos, state);
    }

    protected PomkotsCubeBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    // ====================== モード関連 ======================

    public static final int MODE_BLUE = 0;
    public static final int MODE_YELLOW = 1;
    public static final int MODE_RED = 2;
    public static final int MODE_PURPLE = 3;

    public int getMode() {
        return mode;
    }

    public void incrementMode() {
        mode++;
        if (mode > MODE_PURPLE) {
            mode = MODE_BLUE;
        }
        updateMode(mode);
    }

    public void updateMode(int mode) {
        updateMode(mode, this.level);
    }

    public void updateMode(int mode, Level level) {
        this.mode = mode;

        this.setChanged();
        BlockState state = this.getBlockState();
        level.sendBlockUpdated(this.worldPosition, state, state, 3);
    }

    public int getDefaultMode() {
        return MODE_BLUE;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ====================== 基本機能 ======================

    public void openBox(Player p) {
        this.opener = p;

        switch (getMode()) {
            case PomkotsCubeBlockEntity.MODE_BLUE:
                if (!level.isClientSide) {
                    if (!openedOnce) {
                        openedOnce = true;
                        lastRefillGameTime = level.dayTime();
                        setChanged();
                    }
                }

                break;

            case PomkotsCubeBlockEntity.MODE_YELLOW:
                if (!this.level.isClientSide) {
                    if (raidControllerUUID == null && summonActionTickCount == 0) {
                        summonActionTickCount = 1;
                    }
                } else {
                    playOpenFailAnimation();
                }
                break;

            case PomkotsCubeBlockEntity.MODE_RED:
                if (!this.level.isClientSide) {
                    if (p.getMainHandItem().is(PomkotsMechs.CUBEKEY_ITEM.get())) {
                        if (raidControllerUUID == null && summonActionTickCount == 0 && this.lootTable != null) {
                            var chestData = PomkotsDataPackManager.getInstance().getDataPack().getChestData(this.lootTable.toString());
                            var raidData = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(chestData.raid_id);

                            if (raidData != null) {
                                if ("activate".equals(raidData.type)) {
                                    var bosses = RaidControllerEntity.getInactiveBossesAroundPos(this.level, this.getBlockPos());
                                    if (bosses.isEmpty()) {
                                        sendMessageForOpener("{text.pomkotsmechs.messages.pomkotscube.01}");
                                        break;
                                    }
                                }

                                summonActionTickCount = 1;

                                if (!PomkotsMechs.CONFIG.debugModeEnabled) {
                                    p.getMainHandItem().shrink(1);
                                }
                            } else {
                                break;
                            }
                        }
                    } else {
                        sendMessageForOpener("{text.pomkotsmechs.messages.pomkotscube.02}");
                        break;
                    }
                } else {
                    playOpenFailAnimation();
                }
                break;

            case PomkotsCubeBlockEntity.MODE_PURPLE:
                if (!this.level.isClientSide) {
                    if (p.getMainHandItem().is(PomkotsMechs.CUBEKEY_ITEM_PURPLE.get())) {
                        if (raidControllerUUID == null && summonActionTickCount == 0 && this.lootTable != null) {
                            var chestData = PomkotsDataPackManager.getInstance().getDataPack().getChestData(this.lootTable.toString());
                            var raidData = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(chestData.raid_id);

                            if (raidData != null) {
                                if ("activate".equals(raidData.type)) {
                                    var bosses = RaidControllerEntity.getInactiveBossesAroundPos(this.level, this.getBlockPos());
                                    if (bosses.isEmpty()) {
                                        sendMessageForOpener("{text.pomkotsmechs.messages.pomkotscube.01}");
                                        break;
                                    }
                                }

                                summonActionTickCount = 1;
                                p.getMainHandItem().shrink(1);
                            } else {
                                break;
                            }
                        }
                    } else {
                        sendMessageForOpener("{text.pomkotsmechs.messages.pomkotscube.03}");
                        break;
                    }
                } else {
                    playOpenFailAnimation();
                }
                break;
            default:
                playOpenFailAnimation();
                break;
        }
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, PomkotsCubeBlockEntity entity) {
        entity.tick();
    }

    public void tick() {
        if (level != null && !level.isClientSide) {
            if (this.mode == MODE_BLUE) {
                tryRefill();
            } else {
                if (summonActionTickCount > 0) {
                    if (opener == null || !opener.isAlive()) {
                        summonActionTickCount = 0;
                        return;
                    }

                    summonActionTickCount++;

                    if (summonActionTickCount == 10) {
                        spawnAlertEffect();

                    } else if (summonActionTickCount > 50) {
                        startRaid();
                        summonActionTickCount = 0;
                    }
                } else if (raidControllerEntity == null || !raidControllerEntity.isAlive()) {
                    this.raidControllerUUID = null;
                    this.raidControllerEntity = null;
                    this.setChanged();
                }
            }
        }
    }

    private void spawnAlertEffect() {
        AlertEntity e;

        if (getMode() == MODE_YELLOW) {
            e = new AlertEntity(PomkotsMechs.ALERT.get(), level);
        } else {
            e = new AlertEntity(PomkotsMechs.ALERTRED.get(), level);
        }

        var bp = this.getBlockPos();
        e.setPos(bp.getX() + 0.5, bp.getY(), bp.getZ() + 0.5);
        level.addFreshEntity(e);
    }

    private void startRaid() {
        if (opener != null
                && this.lootTable != null
                && this.lootTable.getNamespace().equals("pomkotsmechs")
                && PomkotsDataPackManager.getInstance().getDataPack().getChestData(this.lootTable.toString()) != null) {

            var chestData = PomkotsDataPackManager.getInstance().getDataPack().getChestData(this.lootTable.toString());
            var raidData = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(chestData.raid_id);

            if (raidData != null) {
                RaidControllerEntity rce = PomkotsMechs.RAID_CONTROLLER.get().create(level);

                if (rce != null) {
                    var raidEntityPos = getRaidEntitySpawnPos(raidData);
                    rce.setPos(raidEntityPos.getX(), raidEntityPos.getY(), raidEntityPos.getZ());

                    BlockPos cubePos = this.getBlockPos();

                    String failCommand;

                    if (raidData.type.equals(RaidControllerEntity.RaidType.ACTIVATE.getId())) {
                        var bosses = RaidControllerEntity.getInactiveBossesAroundPos(level, this.getBlockPos());

                        if (bosses == null || bosses.isEmpty()) {
                            failCommand = "say mission failed...";
                        } else {
                            var boss = bosses.get(0);
                            UUID uuid = boss.getUUID();
                            BlockPos pos = boss.blockPosition();

                            failCommand = String.format(
                                    "pomkots:reset_boss_pos %s %d %d %d",
                                    uuid,
                                    pos.getX(),
                                    pos.getY(),
                                    pos.getZ()
                            );
                        }
                    } else {
                        failCommand = "say mission failed...";
                    }

                    CompoundTag tag = RaidControllerEntity.buildCompoundTag(
                            chestData.raid_id, opener.getUUID(), "data modify block " + cubePos.getX() + " " + cubePos.getY() + " " + cubePos.getZ() + " pomkotsmechsCubeMode set value " + MODE_BLUE, failCommand
                    );
                    rce.readAdditionalSaveData(tag);
                    level.addFreshEntity(rce);

                    this.raidControllerUUID = rce.getUUID();
                    this.raidControllerEntity = rce;

                    this.setChanged();
                }
            } else {
                PomkotsMechs.LOGGER.error("Specified raid does not exist:" + chestData.raid_id);
            }
        }
    }

    private BlockPos getRaidEntitySpawnPos(RaidDefinition raidData) {
        if (RaidControllerEntity.RaidType.SWEEP_BOSS_BOX.getId().equals(raidData.type)) {
            Direction facing = this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
            Direction behind = facing.getOpposite();

            return this.getBlockPos().relative(behind, 15);
        } else {
            return this.getBlockPos();
        }

    }

    @Override
    public void unpackLootTable(Player player) {
        if (!level.isClientSide && refillLootTable == null) {
            refillLootTable = this.lootTable;

            super.unpackLootTable(player);

            compactStacksRespectNBT();

            setChanged();
        }
    }

    private void compactStacksRespectNBT() {
        NonNullList<ItemStack> items = this.getItems();

        List<ItemStack> merged = new ArrayList<>();

        // --- スタック統合 ---
        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;

            for (ItemStack existing : merged) {
                if (ItemStack.isSameItemSameTags(existing, stack)
                        && existing.getCount() < existing.getMaxStackSize()) {

                    int move = Math.min(
                            stack.getCount(),
                            existing.getMaxStackSize() - existing.getCount()
                    );

                    existing.grow(move);
                    stack.shrink(move);

                    if (stack.isEmpty()) {
                        break;
                    }
                }
            }

            if (!stack.isEmpty()) {
                merged.add(stack.copy());
            }
        }

        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            slots.add(i);
        }
        Collections.shuffle(slots, new Random());

        items.clear();

        for (int i = 0; i < merged.size() && i < slots.size(); i++) {
            items.set(slots.get(i), merged.get(i));
        }
    }

    private void compactStacksRespectNBT2() {
        List<ItemStack> merged = new ArrayList<>();

        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack stack = this.getItem(i);
            if (stack.isEmpty()) continue;

            boolean mergedFlag = false;

            for (ItemStack existing : merged) {
                if (ItemStack.isSameItemSameTags(existing, stack)
                        && existing.getCount() < existing.getMaxStackSize()) {

                    int move = Math.min(
                            stack.getCount(),
                            existing.getMaxStackSize() - existing.getCount()
                    );

                    existing.grow(move);
                    stack.shrink(move);

                    if (stack.isEmpty()) {
                        mergedFlag = true;
                        break;
                    }
                }
            }

            if (!stack.isEmpty()) {
                merged.add(stack.copy());
            }
        }

        // 一旦全スロットクリア
        for (int i = 0; i < this.getContainerSize(); i++) {
            this.setItem(i, ItemStack.EMPTY);
        }

        // 再配置
        int slot = 0;
        for (ItemStack stack : merged) {
            while (!stack.isEmpty() && slot < this.getContainerSize()) {
                ItemStack split = stack.split(stack.getMaxStackSize());
                this.setItem(slot++, split);
            }
        }
    }

    private void tryRefill() {
        if (!openedOnce) return;
        if (level == null || level.isClientSide) return;

        long now = level.dayTime();

        if (now - lastRefillGameTime >= REFILL_INTERVAL) {
            refillLoot();
            lastRefillGameTime = now;
            openedOnce = false;
            updateMode(getDefaultMode());
            setChanged();
        }
    }

    private void refillLoot() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        if (refillLootTable == null) return;

        // 中身クリア
        this.clearContent();
        this.setLootTable(refillLootTable, new Random().nextLong());
        refillLootTable = null;

//        LootTable lootTable = serverLevel.getServer()
//                .getLootData()
//                .getLootTable(this.refillLootTable);
//
//        LootParams params = new LootParams.Builder(serverLevel)
//                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
//                .create(LootContextParamSets.CHEST);
//
//        lootTable.fill(this, params, serverLevel.random.nextLong());
    }

    // ====================== アニメーション関係 ======================

    private boolean playLockedAnimation = false;

    public void playOpenFailAnimation() {
        if (this.level.isClientSide) {
            this.playLockedAnimation = true;
        }
    }

    private float prevOpeness = 0;

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, event -> {
            float curOpenness = event.getAnimatable().getOpenNess(event.getPartialTick());
            float prevOpenness = this.prevOpeness;

            this.prevOpeness = curOpenness;

            if (playLockedAnimation) {
                this.playLockedAnimation = false;
                event.getController().forceAnimationReset();
                return event.setAndContinue(RawAnimation.begin().thenPlay("animation.pomkotscube.openfail"));
            } else if (prevOpenness == 0 && curOpenness> 0) {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pomkotscube.open"));
            } else if (prevOpenness == 1 && curOpenness < 1) {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pomkotscube.close"));
            } else {
                return PlayState.CONTINUE;
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object blockEntity) {
        return level != null ? level.getGameTime() : 0;
    }

    // ====================== メッセージ関係 ======================

    private void sendMessageForOpener(String message) {
        if (opener instanceof ServerPlayer sp) {
            var l = new ArrayList<ServerPlayer>();
            l.add(sp);
            sendMessage(message, l);
        }
    }

    private void sendMessage(String message, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            player.sendSystemMessage(Utils.string2Component(message));
        }
    }
}
