package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.controller.PlayerDummyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PlayerDummyEntityModel extends GeoModel<PlayerDummyEntity> {
    @Override
    public ResourceLocation getAnimationResource(PlayerDummyEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/ossan.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(PlayerDummyEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/ossan.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PlayerDummyEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/ossan-genba.png");
    }
}
