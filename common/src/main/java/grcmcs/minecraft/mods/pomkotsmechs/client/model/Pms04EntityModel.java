package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.Pms04Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pms04EntityModel extends GeoModel<Pms04Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pms04Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pms04.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pms04Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pms04.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pms04Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pms04.png");
    }

    @Override
    public void setCustomAnimations(Pms04Entity animatable, long instanceId, AnimationState animationState) {
        EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        CoreGeoBone head = getAnimationProcessor().getBone("head");
        if (head != null) {
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
        }
    }
}
