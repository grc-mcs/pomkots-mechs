package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeLargeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BulletGrenadeLargeEntityModel extends GeoModel<BulletGrenadeLargeEntity> {
    @Override
    public ResourceLocation getAnimationResource(BulletGrenadeLargeEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/grenade.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(BulletGrenadeLargeEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/grenade.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BulletGrenadeLargeEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/grenade.png");
    }
}
