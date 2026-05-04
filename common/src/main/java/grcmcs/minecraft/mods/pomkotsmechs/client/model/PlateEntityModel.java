package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PlateEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlateEntityModel extends GeoModel<PlateEntity> {
    @Override
    public ResourceLocation getAnimationResource(PlateEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/plate.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(PlateEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/plate.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PlateEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/bossbox.png");
    }
}
