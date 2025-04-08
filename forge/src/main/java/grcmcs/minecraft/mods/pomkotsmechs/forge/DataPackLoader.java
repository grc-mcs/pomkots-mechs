package grcmcs.minecraft.mods.pomkotsmechs.forge;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.level.DataPackConfig;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = PomkotsMechs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DataPackLoader {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        ResourceManager manager = server.getResourceManager();
        PomkotsMechs.loadDataPack(manager);
    }
}