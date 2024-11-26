package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileBaseEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MissileBaseEntityModel extends GeoModel<MissileBaseEntity> {
    @Override
    public ResourceLocation getAnimationResource(MissileBaseEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/missile.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(MissileBaseEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/missile.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MissileBaseEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/missile.png");
    }
}
