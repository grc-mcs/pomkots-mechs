package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BulletMiddleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BulletMiddleEntityModel extends GeoModel<BulletMiddleEntity> {
    @Override
    public ResourceLocation getAnimationResource(BulletMiddleEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/bulletmiddle.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(BulletMiddleEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/bulletmiddle.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BulletMiddleEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/bulletmiddle.png");
    }
}
