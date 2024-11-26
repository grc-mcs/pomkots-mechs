package grcmcs.minecraft.mods.pomkotsmechs.client.model;


import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01Entity;
import net.minecraft.resources.ResourceLocation;

public class Pmv01bEntityModel extends Pmv01EntityModel {
    @Override
    public ResourceLocation getAnimationResource(Pmv01Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmv01b.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmv01Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmv01b.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmv01Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmv01b.png");
    }
}