package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.GrenadeLargeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class GrenadeLargeEntityModel extends GeoModel<GrenadeLargeEntity> {
    @Override
    public ResourceLocation getAnimationResource(GrenadeLargeEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/grenade.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(GrenadeLargeEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/grenade.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GrenadeLargeEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/grenade.png");
    }
}
