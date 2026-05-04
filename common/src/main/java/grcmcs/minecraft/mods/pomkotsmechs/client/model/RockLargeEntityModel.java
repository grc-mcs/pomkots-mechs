package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.RockLargeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.RockSmallEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class RockLargeEntityModel extends GeoModel<RockLargeEntity> {
    @Override
    public ResourceLocation getAnimationResource(RockLargeEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/rocklarge.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(RockLargeEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/rocklarge.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RockLargeEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/rock.png");
    }
}
