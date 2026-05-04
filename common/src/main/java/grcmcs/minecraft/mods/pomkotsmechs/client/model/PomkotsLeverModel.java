package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsLeverBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PomkotsLeverModel extends GeoModel<PomkotsLeverBlockEntity> {
    @Override
    public ResourceLocation getModelResource(PomkotsLeverBlockEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/lever.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PomkotsLeverBlockEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/block/pomkotslever.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PomkotsLeverBlockEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/lever.animation.json");
    }
}
