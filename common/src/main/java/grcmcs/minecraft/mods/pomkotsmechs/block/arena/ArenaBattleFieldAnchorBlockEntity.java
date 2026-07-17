package grcmcs.minecraft.mods.pomkotsmechs.block.arena;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ArenaBattleFieldAnchorBlockEntity
        extends BlockEntity {

    public ArenaBattleFieldAnchorBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                PomkotsMechs.ARENA_BATTLEFIELD_ANCHOR_BLOCK_ENTITY.get(),
                pos,
                state
        );
    }
}