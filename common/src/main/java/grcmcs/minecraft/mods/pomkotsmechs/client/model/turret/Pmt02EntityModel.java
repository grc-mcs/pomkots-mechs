package grcmcs.minecraft.mods.pomkotsmechs.client.model.turret;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt02Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pmt02EntityModel extends GeoModel<Pmt02Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pmt02Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmt02.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmt02Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmt02.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmt02Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pms01.png");
    }

    @Override
    public void setCustomAnimations(Pmt02Entity animatable, long instanceId, AnimationState animationState) {
        EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        CoreGeoBone body = getAnimationProcessor().getBone("body");
        CoreGeoBone main = getAnimationProcessor().getBone("main");
        if (body != null && main != null) {
            body.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
            main.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
        }
    }
}
