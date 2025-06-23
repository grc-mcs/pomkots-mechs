package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.cockpit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.cockpit.CockpitHudAnimatable;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.cockpit.CockpitHudModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;

public class CockpitHudRendererManager {
    private static Pair<CockpitHudAnimatable, CockpitHudRenderer> pitDefault = new Pair<>(new CockpitHudAnimatable(), new CockpitHudRenderer(new CockpitHudModel()));

    public static void renderCockpit(PoseStack poseStack, MultiBufferSource buffer, int light, PomkotsVehicleBase vehicle, Player player) {
        if (Utils.shouldRenderCockpit(vehicle)) {
            pitDefault.getSecond().render(poseStack, pitDefault.getFirst(), buffer, null, null, 130);
        }
    }
}
