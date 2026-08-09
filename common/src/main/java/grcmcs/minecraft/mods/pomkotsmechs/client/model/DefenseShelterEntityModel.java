package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.DefenseShelterEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DefenseShelterEntityModel extends GeoModel<DefenseShelterEntity> {
    @Override
    public ResourceLocation getModelResource(DefenseShelterEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/defense_shelter.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DefenseShelterEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/defense_shelter.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DefenseShelterEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/defense_shelter.animation.json");
    }
}
