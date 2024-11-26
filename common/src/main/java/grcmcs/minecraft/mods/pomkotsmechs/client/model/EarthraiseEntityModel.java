package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.EarthraiseEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EarthraiseEntityModel extends GeoModel<EarthraiseEntity> {
    @Override
    public ResourceLocation getAnimationResource(EarthraiseEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/earthraise.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(EarthraiseEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/earthraise.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EarthraiseEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/earthraise.png");
    }
}
