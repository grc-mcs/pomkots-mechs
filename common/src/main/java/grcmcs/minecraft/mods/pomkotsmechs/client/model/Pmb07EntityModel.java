package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb04Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb07Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmb07EntityModel extends GeoModel<Pmb07Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pmb07Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmb07.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmb07Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmb07.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmb07Entity animatable) {
        if (animatable.getAiMode() == BaseBossEntity.AI_MODE_INACTIVE) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmb07inactive.png");

        } else {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmb07.png");
        }
    }
}
