package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.AlertEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.AlertRedEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class AlertEntityModel extends GeoModel<AlertEntity> {
    @Override
    public ResourceLocation getAnimationResource(AlertEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/alert.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(AlertEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/alert.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(AlertEntity animatable) {
        if (animatable instanceof AlertRedEntity) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/alertred.png");
        } else {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/alert.png");
        }
    }
}
