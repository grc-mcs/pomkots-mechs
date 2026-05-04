package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MineEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.RockLargeEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class MineEntityModel extends GeoModel<MineEntity> {
    @Override
    public ResourceLocation getAnimationResource(MineEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/mine.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(MineEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/mine.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MineEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pms01.png");
    }
}
