package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms10Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pms10EntityModel extends GeoModel<Pms10Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pms10Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pms10.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pms10Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pms10.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pms10Entity animatable) {
        if (animatable.getMode() == Pms10Entity.MODE_BLUE) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pms01blue.png");
        } else if (animatable.getMode() == Pms10Entity.MODE_RED) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pms01.png");
        } else {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pms01yellow.png");
        }
    }
}
