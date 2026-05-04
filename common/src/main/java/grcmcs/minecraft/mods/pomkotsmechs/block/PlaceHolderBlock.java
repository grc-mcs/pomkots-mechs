package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class PlaceHolderBlock  extends BaseEntityBlock {

    public PlaceHolderBlock() {
        super(Properties.of()
                .strength(2.5f)
                .sound(SoundType.METAL));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // JSONモデルで描画
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (lvl, pos, blockState, be) -> {
            if (!lvl.getGameRules().getBoolean(PomkotsMechs.RULE_MAP_EDIT)) {
                lvl.removeBlock(pos, false);
            }
        };
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlaceHolderBlockEntity(pos, state);
    }
}
