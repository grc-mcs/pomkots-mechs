package grcmcs.minecraft.mods.pomkotsmechs.block;

import dev.architectury.hooks.item.ItemStackHooks;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MechWorkbenchBlockEntity extends BlockEntity {
//    private final ItemStackHooks itemHandler = new ItemStackHooks(9); // 9スロットの例

    public MechWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(PomkotsMechs.MECH_WORKBENCH_BLOCK_ENTITY.get(), pos, state);
    }

//    // NBTにデータを保存する
//    @Override
//    public void saveAdditional(CompoundTag tag) {
//        super.saveAdditional(tag);
//        tag.put("Inventory", itemHandler.serializeNBT());
//    }
//
//    // NBTからデータを読み込む
//    @Override
//    public void load(CompoundTag tag) {
//        super.load(tag);
//        if (tag.contains("Inventory")) {
//            itemHandler.deserializeNBT(tag.getCompound("Inventory"));
//        }
//    }
//
//    // Capability を提供する例（Inventory）
//    @Override
//    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable net.minecraft.core.Direction side) {
//        if (cap == ForgeCapabilities.ITEM_HANDLER) {
//            return net.minecraftforge.common.util.LazyOptional.of(() -> itemHandler).cast();
//        }
//        return super.getCapability(cap, side);
//    }

}
