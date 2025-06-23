package grcmcs.minecraft.mods.pomkotsmechs.client.model.cockpit;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CockpitHudModel extends GeoModel<CockpitHudAnimatable> {
    @Override
    public ResourceLocation getAnimationResource(CockpitHudAnimatable animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/cockpit.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(CockpitHudAnimatable animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/cockpit.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CockpitHudAnimatable animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/cockpit/cockpit.png");
    }
}
