package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.EarthbreakEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EarthbreakEntityModel extends GeoModel<EarthbreakEntity> {
    @Override
    public ResourceLocation getAnimationResource(EarthbreakEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/earthbreak.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(EarthbreakEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/earthbreak.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EarthbreakEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/earthbreak.png");
    }
}
