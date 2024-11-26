package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.KujiraEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class KujiraEntityModel extends GeoModel<KujiraEntity> {
    @Override
    public ResourceLocation getAnimationResource(KujiraEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/kujira.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(KujiraEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/kujira.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KujiraEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/kujira.png");
    }
}
