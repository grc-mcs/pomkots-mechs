package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb07Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb99Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmb99EntityModel extends GeoModel<Pmb99Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pmb99Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmb99.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmb99Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmb99.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmb99Entity animatable) {
        if (animatable.getAiMode() == BaseBossEntity.AI_MODE_INACTIVE) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmb07inactive.png");

        } else {
            if (animatable.getRiding()) {
                return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmb99red.png");
            } else {
                return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmb99.png");
            }
        }
    }
}
