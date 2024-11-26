package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmv01EntityModel extends GeoModel<Pmv01Entity> {

    @Override
    public ResourceLocation getAnimationResource(Pmv01Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmv01.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmv01Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmv01.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmv01Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmv01.png");
    }
}
