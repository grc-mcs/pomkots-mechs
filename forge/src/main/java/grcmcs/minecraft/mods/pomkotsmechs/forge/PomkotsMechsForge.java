package grcmcs.minecraft.mods.pomkotsmechs.forge;

import dev.architectury.platform.forge.EventBuses;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechsClient;
import grcmcs.minecraft.mods.pomkotsmechs.config.PomkotsConfig;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod(PomkotsMechs.MODID)
public final class PomkotsMechsForge {
    public PomkotsMechsForge() {
        BasePartsItem.regForForge = new BiConsumer<Consumer<Object>, BasePartsItem>() {
            @Override
            public void accept(Consumer<Object> consumer, BasePartsItem partsItem) {
                consumer.accept(new IClientItemExtensions() {
                    private BlockEntityWithoutLevelRenderer renderer;

                    @Override
                    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                        if (this.renderer == null)
                            this.renderer = (BlockEntityWithoutLevelRenderer) partsItem.newRenderer();

                        return this.renderer;
                    }
                });
            }
        };

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
