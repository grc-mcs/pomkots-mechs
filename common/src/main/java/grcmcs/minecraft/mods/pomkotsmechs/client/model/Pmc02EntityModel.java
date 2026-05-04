package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.Pmc02Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BossBoxEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmc02EntityModel extends GeoModel<Pmc02Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pmc02Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/bossbox.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmc02Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/bossbox.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmc02Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/bossbox.png");
    }
}
