package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.GrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BulletGrenadeEntityModel extends GeoModel<BulletGrenadeEntity> {
    @Override
    public ResourceLocation getAnimationResource(BulletGrenadeEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/grenade.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(BulletGrenadeEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/grenade.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BulletGrenadeEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/grenade.png");
    }
}
