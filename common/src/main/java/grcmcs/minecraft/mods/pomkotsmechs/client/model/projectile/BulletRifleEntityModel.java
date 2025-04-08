package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletRifleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BulletRifleEntityModel extends GeoModel<BulletRifleEntity> {
    @Override
    public ResourceLocation getAnimationResource(BulletRifleEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/projectile/bulletrifle.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(BulletRifleEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/projectile/bulletrifle.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BulletRifleEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/projectile/bulletrifle.png");
    }
}
