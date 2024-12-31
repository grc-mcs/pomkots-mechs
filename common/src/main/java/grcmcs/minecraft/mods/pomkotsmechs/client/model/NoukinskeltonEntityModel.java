package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.NoukinSkeltonEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class NoukinskeltonEntityModel extends GeoModel<NoukinSkeltonEntity> {
    @Override
    public ResourceLocation getAnimationResource(NoukinSkeltonEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/noukinskelton.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(NoukinSkeltonEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/noukinskelton.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NoukinSkeltonEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/noukinskelton.png");
    }

    @Override
    public void setCustomAnimations(NoukinSkeltonEntity animatable, long instanceId, AnimationState animationState) {
        EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        if (animationState.isCurrentAnimationStage("animation.noukinskelton.walk") || animationState.isCurrentAnimationStage("animation.noukinskelton.idle")) {
            CoreGeoBone head = getAnimationProcessor().getBone("head");
            if (head != null) {
                head.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
                head.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
            }

        }
    }
}
