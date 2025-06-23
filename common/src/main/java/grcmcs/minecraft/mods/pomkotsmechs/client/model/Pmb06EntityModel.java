package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb06Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pmb06EntityModel extends GeoModel<Pmb06Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pmb06Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmb06.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmb06Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmb06.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmb06Entity animatable) {
        if (animatable.getAiMode() == BaseBossEntity.AI_MODE_INACTIVE) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmb01inactive.png");

        } else {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pmb05.png");
        }
    }

    @Override
    public void setCustomAnimations(Pmb06Entity animatable, long instanceId, AnimationState animationState) {
        EntityModelData entityData = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        CoreGeoBone body = getAnimationProcessor().getBone("root");
        CoreGeoBone beam = getAnimationProcessor().getBone("beam_scale");

        if (beam != null && body != null && animatable.getLaserLength() > 1) {

            beam.setRotX((entityData.headPitch()) * Mth.DEG_TO_RAD);
//            body.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
            //            var target = animatable.level().getEntity(animatable.getTargetEntityID());
//            if (target != null) {
//                pointBonePitchTowards(beam, animatable.position().add(0,3.6F,0), target.position().add(0, target.getBbHeight() / 2, 0) );
//            } else {
//                beam.setRotX(entityData.headPitch() * Mth.DEG_TO_RAD);
//                body.setRotY(entityData.netHeadYaw() * Mth.DEG_TO_RAD);
//            }
        }
    }

    public static void pointBonePitchTowards(CoreGeoBone bone, Vec3 fromPos, Vec3 toPos) {
        Vec3 delta = toPos.subtract(fromPos);
        double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);

        // ピッチ（上向きが負、下向きが正）→ ラジアンで計算
        float pitchRadians = (float) -Math.atan2(delta.y, horizontalDistance);

        // GeckoLibはラジアンで setRotX に設定
        bone.setRotX(pitchRadians);
    }
}
