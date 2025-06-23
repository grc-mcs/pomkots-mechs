package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissilePodEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MissilePodEntityModel extends GeoModel<MissilePodEntity> {
    @Override
    public ResourceLocation getAnimationResource(MissilePodEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/projectile/missilepod.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(MissilePodEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/projectile/missilepod.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MissilePodEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmb02.png");
    }
}
