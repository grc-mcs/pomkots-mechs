package grcmcs.minecraft.mods.pomkotsmechs.fabric.client;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechsClient;
import grcmcs.minecraft.mods.pomkotsmechs.fabric.PomkotsMechsFabric;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;

public final class PomkotsMechsClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PomkotsMechsClient.initialize();
    }
}
