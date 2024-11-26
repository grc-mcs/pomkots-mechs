package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.Pms03Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pms03EntityModel extends GeoModel<Pms03Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pms03Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pms03.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pms03Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pms03.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pms03Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pms03.png");
    }

    @Override
    public void setCustomAnimations(Pms03Entity animatable, long instanceId, AnimationState animationState) {
        EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
        }
    }
}
