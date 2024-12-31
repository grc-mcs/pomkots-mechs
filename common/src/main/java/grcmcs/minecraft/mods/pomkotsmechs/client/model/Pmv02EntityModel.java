package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv02Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmv02EntityModel extends GeoModel<Pmv02Entity> {

    @Override
    public ResourceLocation getAnimationResource(Pmv02Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmv02.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmv02Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmv02.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmv02Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmv02.png");
    }
}
