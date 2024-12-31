package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.KujiraEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PresentBoxEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PresentBoxEntityModel extends GeoModel<PresentBoxEntity> {
    @Override
    public ResourceLocation getAnimationResource(PresentBoxEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/presentbox.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(PresentBoxEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/presentbox.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PresentBoxEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/presentbox.png");
    }
}
