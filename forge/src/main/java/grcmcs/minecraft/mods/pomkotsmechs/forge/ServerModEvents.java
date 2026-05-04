package grcmcs.minecraft.mods.pomkotsmechs.forge;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.survival.SurvivalInitActions;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PomkotsMechs.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerModEvents {
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.getServer() != null) {
                SurvivalInitActions.onServerStarted(serverPlayer.getServer());
                SurvivalInitActions.onPlayerJoin(serverPlayer);
            }
        }
    }
}
