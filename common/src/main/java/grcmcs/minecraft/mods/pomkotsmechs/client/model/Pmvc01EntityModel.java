package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmvc01EntityModel extends GeoModel<Pmvc01Entity> {

    @Override
    public ResourceLocation getAnimationResource(Pmvc01Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmvc01.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmvc01Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmvc01.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmvc01Entity animatable) {
        if (animatable.canWork(false) && !animatable.isBroken()) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmvc01.png");
        } else {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmvc01stop.png");
        }
    }
}
