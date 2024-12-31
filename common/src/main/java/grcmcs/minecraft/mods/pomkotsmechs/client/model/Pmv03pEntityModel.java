package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv02Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv03pEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmv03pEntityModel extends GeoModel<Pmv03pEntity> {

    @Override
    public ResourceLocation getAnimationResource(Pmv03pEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmv03p.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmv03pEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmv03p.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmv03pEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmv03p.png");
    }
}
