package grcmcs.minecraft.mods.pomkotsmechs.client.model.turret;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt02Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt03Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pmt03EntityModel extends GeoModel<Pmt03Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pmt03Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmt03.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmt03Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmt03.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmt03Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pms01.png");
    }

    @Override
    public void setCustomAnimations(Pmt03Entity animatable, long instanceId, AnimationState animationState) {
        EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        CoreGeoBone body = getAnimationProcessor().getBone("body");
        CoreGeoBone main = getAnimationProcessor().getBone("main");
        if (body != null && main != null) {
            body.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
            main.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
        }
    }
}
