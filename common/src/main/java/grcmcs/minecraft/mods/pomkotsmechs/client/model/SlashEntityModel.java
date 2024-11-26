package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.SlashEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SlashEntityModel extends GeoModel<SlashEntity> {
    @Override
    public ResourceLocation getAnimationResource(SlashEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/slash.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(SlashEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/slash.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SlashEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/slash.png");
    }
}
