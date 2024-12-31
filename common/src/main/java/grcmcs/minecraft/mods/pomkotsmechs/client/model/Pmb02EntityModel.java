package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb02Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmb02EntityModel extends GeoModel<Pmb02Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pmb02Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmb02.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmb02Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmb02.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmb02Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmb02.png");
    }
}
