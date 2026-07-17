package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;

public abstract class AbstractArenaMatchStateProcessor
        implements IArenaMatchStateProcessor {

    @Override
    public void onEnter(
            ArenaMatchContext context
    ) {
    }

    @Override
    public void onExit(
            ArenaMatchContext context
    ) {
    }

    @Override
    public void onCancel(
            ArenaMatchContext context
    ) {
    }

    protected boolean elapsed(
            ArenaMatchContext context,
            long millis
    ) {
        return context.elapsedMillis()
                >= millis;
    }
}
