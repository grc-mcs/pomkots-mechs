package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class PomkotsCubeBlockRedEntity extends PomkotsCubeBlockEntity {
    public PomkotsCubeBlockRedEntity(BlockPos pos, BlockState state) {
        super(PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_RED.get(), pos, state);
    }

    @Override
    public int getDefaultMode() {
        return MODE_RED;
    }
}
