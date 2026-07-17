package grcmcs.minecraft.mods.pomkotsmechs.block.arena;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ArenaTeleportBlockEntity
        extends BlockEntity {

    public ArenaTeleportBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                PomkotsMechs.ARENA_TELEPORT_BLOCK_ENTITY.get(),
                pos,
                state
        );
    }
}