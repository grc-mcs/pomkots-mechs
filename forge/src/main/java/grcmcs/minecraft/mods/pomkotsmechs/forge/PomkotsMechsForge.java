package grcmcs.minecraft.mods.pomkotsmechs.forge;

import dev.architectury.platform.forge.EventBuses;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechsClient;
import grcmcs.minecraft.mods.pomkotsmechs.config.PomkotsConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;

@Mod(PomkotsMechs.MODID)
public final class PomkotsMechsForge {
    public PomkotsMechsForge() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        EventBuses.registerModEventBus(PomkotsMechs.MODID, bus);
        PomkotsMechs.initialize();


        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () ->
                        () -> {
                            PomkotsMechsClient.initialize();
                            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> {
                                return AutoConfig.getConfigScreen(PomkotsConfig.class, parent).get();
                            }));

                        });
    }
}
