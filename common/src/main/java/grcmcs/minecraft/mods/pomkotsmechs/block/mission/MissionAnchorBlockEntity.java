package grcmcs.minecraft.mods.pomkotsmechs.block.mission;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class MissionAnchorBlockEntity extends BlockEntity {
    public static final String NBT_ANCHOR_ID = PomkotsMechs.nbtName("MissionAnchorId");
    private String anchorId = "";

    public MissionAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(PomkotsMechs.MISSION_ANCHOR_BLOCK_ENTITY.get(), pos, state);
    }

    public String anchorId() {
        return anchorId;
    }

    public void setAnchorId(String anchorId) {
        this.anchorId = anchorId == null ? "" : anchorId.trim();
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(NBT_ANCHOR_ID, anchorId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        anchorId = tag.getString(NBT_ANCHOR_ID).trim();
    }
}
