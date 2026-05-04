package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.NeedleEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletRifleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NeedleEntityModel extends GeoModel<NeedleEntity> {
    @Override
    public ResourceLocation getAnimationResource(NeedleEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/needle.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(NeedleEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/needle.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NeedleEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/needle.png");
    }
}
