package grcmcs.minecraft.mods.pomkotsmechs.fabric;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.fabricmc.api.ModInitializer;

public final class PomkotsMechsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        PomkotsMechs.initialize();
    }
}
