package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class PomkotsCubeBlockRed extends PomkotsCubeBlock {
    public PomkotsCubeBlockRed() {
        super(BlockBehaviour.Properties.of()
                .strength(-1.0F, 3600000.0F)
                .sound(SoundType.METAL), PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_RED::get);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? createTickerHelper(blockEntityType, this.blockEntityType(), ChestBlockEntity::lidAnimateTick) :
                createTickerHelper(blockEntityType, PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_RED.get(), PomkotsCubeBlockRedEntity::serverTick);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        var ent = new PomkotsCubeBlockRedEntity(pos, state);
        ent.mode = PomkotsCubeBlockEntity.MODE_RED;
        return ent;
    }
}
