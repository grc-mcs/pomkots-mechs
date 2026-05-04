package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileBaseEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MissileGenericEntityModel extends GeoModel<MissileGenericEntity> {
    @Override
    public ResourceLocation getAnimationResource(MissileGenericEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/missile.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(MissileGenericEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/missile.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MissileGenericEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/missile.png");
    }
}
