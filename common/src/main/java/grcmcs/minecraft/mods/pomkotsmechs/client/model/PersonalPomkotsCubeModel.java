package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.PersonalPomkotsCubeBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PersonalPomkotsCubeModel extends GeoModel<PersonalPomkotsCubeBlockEntity> {
    @Override
    public ResourceLocation getModelResource(PersonalPomkotsCubeBlockEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pomkotscube.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PersonalPomkotsCubeBlockEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/block/personal_pomkots_cube.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PersonalPomkotsCubeBlockEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pomkotscube.animation.json");
    }
}
