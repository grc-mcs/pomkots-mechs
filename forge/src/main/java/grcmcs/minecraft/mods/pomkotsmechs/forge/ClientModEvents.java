package grcmcs.minecraft.mods.pomkotsmechs.forge;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.*;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaBattleResultScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaReceptionistScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadKeyCardScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.pilot.PilotScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.*;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.MechCapsule2Item;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItemProperties;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItemStackHelper;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = PomkotsMechs.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuRegistry.registerScreenFactory(
                    PomkotsMechs.MECH_WORKBENCH_GUI.get(), MechWorkbenchScreen::new
            );
            MenuRegistry.registerScreenFactory(
                    PomkotsMechs.PARTS_WORKBENCH_GUI.get(), PartsWorkbenchScreen::new
            );
            MenuRegistry.registerScreenFactory(
                    PomkotsMechs.MECH_SALVAGER_GUI.get(), MechSalvagerScreen::new
            );
            MenuRegistry.registerScreenFactory(
                    PomkotsMechs.POMKOTS_RADAR_GUI.get(), RadarTargetSelectScreen::new
            );
            MenuRegistry.registerScreenFactory(
                    PomkotsMechs.MECH_TRADER_GUI.get(), MechTraderScreen::new
            );
            MenuRegistry.registerScreenFactory(
                    PomkotsMechs.PILOT_CONFIG_GUI.get(), PilotScreen::new
            );
            MenuRegistry.registerScreenFactory(
                    PomkotsMechs.ARENA_RECEPTIONIST.get(), ArenaReceptionistScreen::new
            );
            MenuRegistry.registerScreenFactory(
                    PomkotsMechs.ARENA_RESULT_MENU.get(), ArenaBattleResultScreen::new
            );
            MenuRegistry.registerScreenFactory(
                    PomkotsMechs.POMKOTS_DATAPAD_GUI.get(), DataPadScreen::new
            );
            MenuRegistry.registerScreenFactory(
                    PomkotsMechs.POMKOTS_DATAPAD_KEYCARD_GUI.get(), DataPadKeyCardScreen::new
            );


            BlockEntityRendererRegistry.register(PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY.get(), (context)->{
                return new PomkotsCubeRenderer(context);
            });
            BlockEntityRendererRegistry.register(PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_YELLOW.get(), (context)->{
                return new PomkotsCubeYellowRenderer(context);
            });
            BlockEntityRendererRegistry.register(PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_RED.get(), (context)->{
                return new PomkotsCubeRedRenderer(context);
            });
            BlockEntityRendererRegistry.register(PomkotsMechs.POMKOTS_CUBE_BLOCK_ENTITY_PURPLE.get(), (context)->{
                return new PomkotsCubePurpleRenderer(context);
            });

            BlockEntityRendererRegistry.register(PomkotsMechs.POMKOTS_LEVER_BLOCK_ENTITY.get(), (context)->{
                return new PomkotsLeverRenderer(context);
            });

            ColorHandlerRegistry.registerItemColors(
                    (stack, tintIndex) -> {
                        CompoundTag tag = stack.getTag();
                        if (tag == null) return 0xFFFFFFFF;

                        if (tintIndex == 0 && tag.contains(PomkotsMechs.nbtName("PrimaryColor"))) return tag.getInt(PomkotsMechs.nbtName("PrimaryColor"));
                        if (tintIndex == 1 && tag.contains(PomkotsMechs.nbtName("SecondaryColor"))) return tag.getInt(PomkotsMechs.nbtName("SecondaryColor"));

                        return 0xFFFFFFFF;
                    },
                    PomkotsMechs.MECH_CAPSULE_ITEM.get()
            );

            ColorHandlerRegistry.registerItemColors(
                    (stack, tintIndex) -> {
                        if (!(stack.getItem() instanceof MechCapsule2Item)) {
                            return 0xFFFFFFFF;
                        }

                        if (tintIndex == 0) {
                            return 0xFFADADAD;

                        } else if (tintIndex == 1) {
                            if (MechCapsule2Item.hasStoredMech(stack)) {
                                return 0xFF7DFF70;
                            } else {
                                return 0xFFFFFFFF;
                            }
                        }

                        return 0xFFFFFFFF;
                    },
                    PomkotsMechs.MECH_CAPSULE2_ITEM.get()
            );

            ColorHandlerRegistry.registerItemColors(
                    (stack, tintIndex) -> {
                        if (tintIndex != 0) {
                            return 0xFFFFFF;
                        }

                        CircuitRarity rarity = CircuitItemStackHelper.getRarityOrDefault(stack);

                        return rarity.tintColor();
                    },
                    PomkotsMechs.CIRCUIT_BASE.get()
            );
            CircuitItemProperties.register();
        });
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("hide_mech_hud", (gui, poseStack, partialTick, screenWidth, screenHeight) -> {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;

            if (player != null && player.getVehicle() instanceof Pmvc01Entity) {
                gui.leftHeight = 0; // 体力バーを非表示
                gui.rightHeight = 0; // 空腹バーを非表示
            }
        });
    }
}
