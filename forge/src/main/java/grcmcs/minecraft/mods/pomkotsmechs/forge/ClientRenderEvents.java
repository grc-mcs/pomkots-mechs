package grcmcs.minecraft.mods.pomkotsmechs.forge;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientRenderEvents {
    public static void init() {
        MinecraftForge.EVENT_BUS.register(ClientRenderEvents.class);
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        // HPゲージを消す場合

        Minecraft client = Minecraft.getInstance();
        var p = client.player;
        if (p != null && p.getVehicle() instanceof PomkotsVehicle) {
            var id = event.getOverlay().id().toString();
            if ("minecraft:player_health".equals(id) || "minecraft:mount_health".equals(id)) {
                event.setCanceled(true);
            }
        }
//
//        if (event.getOverlay().overlay().equals(VanillaGuiOverlay.PLAYER_HEALTH)) {
//            if (shouldCancel(event.getOverlay())) {
//                event.setCanceled(true);
//            }
//        }
//
//        // 乗り物ゲージ（馬のジャンプバーなど）を消す場合
//        if (event.getOverlay().overlay().equals(VanillaGuiOverlay.MOUNT_HEALTH)) {
//            if (shouldCancel(event.getOverlay())) {
//                event.setCanceled(true);
//            }
//        }
    }

    private static boolean shouldCancel(Object v) {
        Minecraft client = Minecraft.getInstance();
        var p = client.player;

        return p != null && p.getVehicle() instanceof PomkotsVehicle;
    }
}
