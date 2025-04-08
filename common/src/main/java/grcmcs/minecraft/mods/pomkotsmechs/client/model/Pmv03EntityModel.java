package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv03Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmv03EntityModel extends GeoModel<Pmv03Entity> {

    @Override
    public ResourceLocation getAnimationResource(Pmv03Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmv03.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmv03Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmv03.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmv03Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmv03.png");
    }
}
