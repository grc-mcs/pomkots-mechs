package grcmcs.minecraft.mods.pomkotsmechs.forge;

import dev.architectury.platform.forge.EventBuses;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechsClient;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsArmorItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.WandererArmorItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.config.PomkotsConfig;
import grcmcs.minecraft.mods.pomkotsmechs.items.CartonItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.PomkotsArmorItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.WandererArmorItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.ServerBGMTracker;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

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

        WandererArmorItem.regForForge = new BiConsumer<Consumer<Object>, WandererArmorItem>() {
            @Override
            public void accept(Consumer<Object> consumer, WandererArmorItem partsItem) {
                consumer.accept(new IClientItemExtensions() {
                    private GeoArmorRenderer<?> renderer;

                    @Override
                    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                        if (this.renderer == null)
                            this.renderer = new WandererArmorItemRenderer();

                        this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);

                        return this.renderer;
                    }
                });
            }
        };

        PomkotsArmorItem.regForForge = new BiConsumer<Consumer<Object>, PomkotsArmorItem>() {
            @Override
            public void accept(Consumer<Object> consumer, PomkotsArmorItem partsItem) {
                consumer.accept(new IClientItemExtensions() {
                    private GeoArmorRenderer<?> renderer;

                    @Override
                    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                        if (this.renderer == null)
                            this.renderer = new PomkotsArmorItemRenderer();

                        this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);

                        return this.renderer;
                    }
                });
            }
        };

        CartonItem.regForForge = new BiConsumer<Consumer<Object>, CartonItem>() {
            @Override
            public void accept(Consumer<Object> consumer, CartonItem partsItem) {
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

        if (ModList.get().isLoaded("lostcities")) {
            PomkotsMechs.LOGGER.info("Lost Cities is loaded. enabled specific sound system");
            ServerBGMTracker.lostCitiesAccessor = new LostCitiesCompatibleForge();
        }

        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () ->
                        () -> {
                            PomkotsMechsClient.initialize();
                            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((client, parent) -> {
                                return AutoConfig.getConfigScreen(PomkotsConfig.class, parent).get();
                            }));
                            ClientRenderEvents.init();
                        });
    }
}
