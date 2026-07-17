package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader.MechTraderEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class MechPilotEntityModel extends GeoModel<MechPilotEntity> {
    @Override
    public ResourceLocation getAnimationResource(MechPilotEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/mech_pilot.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(MechPilotEntity animatable) {
        return new ResourceLocation(animatable.getModelLocation());
    }

    @Override
    public ResourceLocation getTextureResource(MechPilotEntity animatable) {
        return new ResourceLocation(animatable.getTextureLocation());
    }

    @Override
    public void setCustomAnimations(MechPilotEntity animatable, long instanceId, AnimationState animationState) {
        EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        CoreGeoBone head = getAnimationProcessor().getBone("Head");
        if (head != null) {
            head.setRotY((entityData.netHeadYaw()) * Mth.DEG_TO_RAD);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
        }
    }
}
