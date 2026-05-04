package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.RockSmallEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RockSmallEntityModel extends GeoModel<RockSmallEntity> {
    @Override
    public ResourceLocation getAnimationResource(RockSmallEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/rocksmall.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(RockSmallEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/rocksmall.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RockSmallEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/rock.png");
    }
}
