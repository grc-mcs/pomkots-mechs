package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.Pmc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms01Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pmc01EntityModel extends GeoModel<Pmc01Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pmc01Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmc01.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmc01Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmc01.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmc01Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmc01.png");
    }
}
