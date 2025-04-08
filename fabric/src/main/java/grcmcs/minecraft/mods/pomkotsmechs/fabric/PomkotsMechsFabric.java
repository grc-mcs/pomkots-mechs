package grcmcs.minecraft.mods.pomkotsmechs.fabric;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

public final class PomkotsMechsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        registerDataPackLoader();

        // Run our common setup.
        PomkotsMechs.initialize();

    }

    public static void registerDataPackLoader() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                PomkotsMechs.loadDataPack(resourceManager);
            }

            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation(PomkotsMechs.MODID, "datapack_loader");
            }
        });
    }

}
