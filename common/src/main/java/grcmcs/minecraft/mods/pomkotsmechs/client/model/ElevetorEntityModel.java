package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.ElevatorEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.KujiraEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ElevetorEntityModel extends GeoModel<ElevatorEntity> {
    @Override
    public ResourceLocation getAnimationResource(ElevatorEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/elevator.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(ElevatorEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/elevator.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ElevatorEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pms01.png");
    }
}
