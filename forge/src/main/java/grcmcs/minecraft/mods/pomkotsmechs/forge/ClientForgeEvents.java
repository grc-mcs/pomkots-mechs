package grcmcs.minecraft.mods.pomkotsmechs.forge;

import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechSalvagerScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechWorkbenchScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.PartsWorkbenchScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.narration.IntroNarrationScreen;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsCubeRedRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsCubeRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsCubeYellowRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.survival.SurvivalInitActions;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = PomkotsMechs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {
    @SubscribeEvent
    public static void onClientPlayerJoin(ClientPlayerNetworkEvent.LoggingIn event) {
//        Minecraft mc = Minecraft.getInstance();
//        mc.tell(() -> {
//            SurvivalInitActions.onClientJoin(event.getPlayer());
//        });
    }

    //TODO 後で消す。MultiPlayerGameModelMixinで解決するようなら消す
//    @SubscribeEvent
//    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
//        if (event.getEntity() instanceof LocalPlayer localPlayer
//                && Utils.isRidingPomkotsMechs(localPlayer)) {
//            event.setCanceled(true);
//        }
//    }
//
//    @SubscribeEvent
//    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
//        if (event.getEntity() instanceof LocalPlayer localPlayer
//                && Utils.isRidingPomkotsMechs(localPlayer)) {
//            event.setCanceled(true);
//        }
//    }
}
