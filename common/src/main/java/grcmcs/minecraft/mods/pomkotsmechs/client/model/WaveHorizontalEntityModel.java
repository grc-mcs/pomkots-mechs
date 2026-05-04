package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.SlashEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.WaveHorizontalEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WaveHorizontalEntityModel extends GeoModel<WaveHorizontalEntity> {
    @Override
    public ResourceLocation getAnimationResource(WaveHorizontalEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/wave_h.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(WaveHorizontalEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/wave_h.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WaveHorizontalEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/wave.png");
    }
}
