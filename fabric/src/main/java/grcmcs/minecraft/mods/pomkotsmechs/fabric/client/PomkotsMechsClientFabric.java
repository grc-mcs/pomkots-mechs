package grcmcs.minecraft.mods.pomkotsmechs.fabric.client;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechsClient;
import net.fabricmc.api.ClientModInitializer;

public final class PomkotsMechsClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PomkotsMechsClient.initialize();
    }
}
