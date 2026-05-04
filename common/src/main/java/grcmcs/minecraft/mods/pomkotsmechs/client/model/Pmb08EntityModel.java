package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb08Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmb08EntityModel extends GeoModel<Pmb08Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pmb08Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmb08.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmb08Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmb08.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmb08Entity animatable) {
        if (animatable.getAiMode() == BaseBossEntity.AI_MODE_INACTIVE) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmb01inactive.png");

        } else {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmb01.png");
        }
    }
}
