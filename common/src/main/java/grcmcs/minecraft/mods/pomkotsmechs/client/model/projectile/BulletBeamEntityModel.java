package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletBeamEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletRifleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BulletBeamEntityModel extends GeoModel<BulletBeamEntity> {
    @Override
    public ResourceLocation getAnimationResource(BulletBeamEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/projectile/bulletbeam.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(BulletBeamEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/projectile/bulletbeam.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BulletBeamEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/projectile/bulletbeam.png");
    }
}
