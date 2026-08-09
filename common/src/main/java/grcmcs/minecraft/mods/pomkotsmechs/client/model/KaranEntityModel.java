package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.KaranEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class KaranEntityModel extends GeoModel<KaranEntity> {
    @Override
    public ResourceLocation getModelResource(KaranEntity animatable) {
        return PomkotsMechs.id("geo/karan.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KaranEntity animatable) {
        return PomkotsMechs.id("textures/entity/pilot/karan.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KaranEntity animatable) {
        return PomkotsMechs.id("animations/mech_pilot.animation.json");
    }

    @Override
    public void setCustomAnimations(
            KaranEntity animatable,
            long instanceId,
            AnimationState<KaranEntity> animationState
    ) {
        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        CoreGeoBone head = getAnimationProcessor().getBone("Head");
        if (head != null) {
            head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
        }
    }
}
