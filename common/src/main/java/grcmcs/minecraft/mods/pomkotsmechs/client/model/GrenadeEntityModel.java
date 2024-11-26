package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.GrenadeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GrenadeEntityModel extends GeoModel<GrenadeEntity> {
    @Override
    public ResourceLocation getAnimationResource(GrenadeEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/grenade.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(GrenadeEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/grenade.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GrenadeEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/grenade.png");
    }
}
