package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BulletEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BulletEntityModel extends GeoModel<BulletEntity> {
    @Override
    public ResourceLocation getAnimationResource(BulletEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/bullet.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(BulletEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/bullet.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BulletEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/bullet.png");
    }
}
