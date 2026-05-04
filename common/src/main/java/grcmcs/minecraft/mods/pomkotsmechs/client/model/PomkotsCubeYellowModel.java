package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockYellowEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PomkotsCubeYellowModel extends GeoModel<PomkotsCubeBlockYellowEntity>  {
    @Override
    public ResourceLocation getModelResource(PomkotsCubeBlockYellowEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pomkotscube_yellow.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PomkotsCubeBlockYellowEntity animatable) {
        switch (animatable.getMode()) {
            case PomkotsCubeBlockEntity.MODE_BLUE:
                return new ResourceLocation(PomkotsMechs.MODID, "textures/block/pomkotscube.png");
            case PomkotsCubeBlockEntity.MODE_YELLOW:
                return new ResourceLocation(PomkotsMechs.MODID, "textures/block/pomkotscubeyellow.png");
            case PomkotsCubeBlockEntity.MODE_RED:
                return new ResourceLocation(PomkotsMechs.MODID, "textures/block/pomkotscubered.png");
            case PomkotsCubeBlockEntity.MODE_PURPLE:
                return new ResourceLocation(PomkotsMechs.MODID, "textures/block/pomkotscubepurple.png");
            default:
                return new ResourceLocation(PomkotsMechs.MODID, "textures/block/pomkotscube.png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(PomkotsCubeBlockYellowEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pomkotscube.animation.json");
    }
}
