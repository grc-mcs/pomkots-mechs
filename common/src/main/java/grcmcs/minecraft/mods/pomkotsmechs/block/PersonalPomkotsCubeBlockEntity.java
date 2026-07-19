package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class PersonalPomkotsCubeBlockEntity extends ChestBlockEntity implements GeoBlockEntity {
    public static final int CONTAINER_SIZE = 54;
    private static final String OWNER_TAG = PomkotsMechs.nbtName("PersonalPomkotsCubeOwner");

    private final AnimatableInstanceCache animationCache = GeckoLibUtil.createInstanceCache(this);
    private NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    private UUID ownerUuid;
    private float previousOpenness;

    public PersonalPomkotsCubeBlockEntity(BlockPos pos, BlockState state) {
        this(PomkotsMechs.PERSONAL_POMKOTS_CUBE_BLOCK_ENTITY.get(), pos, state);
    }

    protected PersonalPomkotsCubeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    public boolean isOwner(Player player) {
        return this.ownerUuid != null && this.ownerUuid.equals(player.getUUID());
    }

    public void setOwnerIfAbsent(UUID ownerUuid) {
        if (this.ownerUuid == null) {
            this.ownerUuid = ownerUuid;
            this.setChanged();
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.pomkotsmechs.personal_pomkots_cube");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        if (!this.isOwner(player) || !canOpen(player)) {
            return null;
        }
        this.unpackLootTable(player);
        return ChestMenu.sixRows(id, inventory, this);
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        // ChestBlockEntity normally owns a 27-slot list. Persist our 54-slot list
        // explicitly so the BlockEntityTag carried by the item contains every slot.
        ContainerHelper.saveAllItems(tag, this.items);
        if (this.ownerUuid != null) {
            tag.putUUID(OWNER_TAG, this.ownerUuid);
        }
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
        super.load(tag);
        ContainerHelper.loadAllItems(tag, this.items);
        this.ownerUuid = tag.hasUUID(OWNER_TAG) ? tag.getUUID(OWNER_TAG) : null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, event -> {
            float openness = event.getAnimatable().getOpenNess(event.getPartialTick());
            float oldOpenness = this.previousOpenness;
            this.previousOpenness = openness;

            if (oldOpenness == 0.0F && openness > 0.0F) {
                return event.setAndContinue(RawAnimation.begin()
                        .thenPlayAndHold("animation.pomkotscube.open"));
            }
            if (oldOpenness == 1.0F && openness < 1.0F) {
                return event.setAndContinue(RawAnimation.begin()
                        .thenPlayAndHold("animation.pomkotscube.close"));
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animationCache;
    }
}
