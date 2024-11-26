package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.Pms05Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pms05EntityModel extends GeoModel<Pms05Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pms05Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pms05.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pms05Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pms05.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pms05Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pms05.png");
    }

    @Override
    public void setCustomAnimations(Pms05Entity animatable, long instanceId, AnimationState animationState) {
        EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
        }

        CoreGeoBone weapon = getAnimationProcessor().getBone("weapon");
        if (weapon != null) {
            weapon.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
        }
    }
}
