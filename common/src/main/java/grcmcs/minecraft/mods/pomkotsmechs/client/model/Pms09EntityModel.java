package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms08Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms09Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class Pms09EntityModel extends GeoModel<Pms09Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pms09Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pms09.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pms09Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pms09.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pms09Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pms01.png");
    }

//    @Override
//    public void setCustomAnimations(Pms06Entity animatable, long instanceId, AnimationState animationState) {
//        EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);
//
//        CoreGeoBone head = getAnimationProcessor().getBone("head");
//        if (head != null) {
//            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
//        }
//
//        CoreGeoBone weapon = getAnimationProcessor().getBone("weapon");
//        if (weapon != null) {
//            weapon.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
//        }
//    }
}
