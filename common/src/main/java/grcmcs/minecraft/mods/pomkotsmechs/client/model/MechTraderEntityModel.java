package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader.MechTraderEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class MechTraderEntityModel extends GeoModel<MechTraderEntity> {
    @Override
    public ResourceLocation getAnimationResource(MechTraderEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/mech_trader.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(MechTraderEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/mech_trader.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(MechTraderEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/mech_trader.png");
    }

    @Override
    public void setCustomAnimations(MechTraderEntity animatable, long instanceId, AnimationState animationState) {
        EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        CoreGeoBone head = getAnimationProcessor().getBone("Head");
        if (head != null) {
            head.setRotY((entityData.netHeadYaw() - 90) * Mth.DEG_TO_RAD);
            head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
        }
    }
}
