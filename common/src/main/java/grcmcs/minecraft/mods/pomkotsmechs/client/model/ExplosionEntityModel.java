package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ExplosionEntityModel extends GeoModel<ExplosionEntity> {
    @Override
    public ResourceLocation getAnimationResource(ExplosionEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/explosion.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(ExplosionEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/explosion.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ExplosionEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/explosion.png");
    }
}
