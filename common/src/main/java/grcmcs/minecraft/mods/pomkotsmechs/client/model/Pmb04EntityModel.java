package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb04Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pmb04EntityModel extends GeoModel<Pmb04Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pmb04Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmb04.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmb04Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmb04.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmb04Entity animatable) {
        if (animatable.getAiMode() == BaseBossEntity.AI_MODE_INACTIVE) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmb01inactive.png");

        } else {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmb04.png");
        }
    }
//    @Override
//    public void setCustomAnimations(Pmb03Entity animatable, long instanceId, AnimationState animationState) {
//        EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);
//
//        CoreGeoBone body = getAnimationProcessor().getBone("body");
//        CoreGeoBone wep1 = getAnimationProcessor().getBone("right_gatling");
//        CoreGeoBone wep2 = getAnimationProcessor().getBone("left_gatling");
//
//        if (body != null && wep1 != null) {
//            body.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
//            wep1.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
//            wep2.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
//        }
//    }
}
