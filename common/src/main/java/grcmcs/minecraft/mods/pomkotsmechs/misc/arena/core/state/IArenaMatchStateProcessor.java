package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;

public interface IArenaMatchStateProcessor {

    void onEnter(
            ArenaMatchContext context
    );

    void tick(
            ArenaMatchContext context
    );

    void onExit(
            ArenaMatchContext context
    );

    void onCancel(
            ArenaMatchContext context
    );
}