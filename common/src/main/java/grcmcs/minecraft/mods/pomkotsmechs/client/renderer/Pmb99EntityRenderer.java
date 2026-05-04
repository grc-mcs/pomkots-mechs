package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmb07EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmb99EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb07Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb99Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmb99EntityRenderer extends GeoEntityRenderer<Pmb99Entity> {
    public Pmb99EntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Pmb99EntityModel());
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, Pmb99Entity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);

        this.scaleHeight = Pmb07Entity.DEFAULT_SCALE;
        this.scaleWidth = Pmb07Entity.DEFAULT_SCALE;
    }

    @Override
    public void actuallyRender(PoseStack poseStack, Pmb99Entity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        if (animatable.getControllingPassenger() != null) {
            animatable.seatPos = getSeatPosition(model);
            animatable.seatRots[0] = getSeatPitch(model);
        }
    }

    private Vec3 getSeatPosition(BakedGeoModel model) {
        if (model != null) {
            GeoBone vcSeat = model.getBone("seet").get();
            GeoBone vcRoot = model.getBone("root").get();

            Vector3d seatPos = vcSeat.getLocalPosition();
            Vector3d rootPos = vcRoot.getLocalPosition();

            return new Vec3(
                    seatPos.x * 1 - rootPos.x * 1,
                    seatPos.y * 1 - rootPos.y * 1 - animatable.getPassengersRidingOffset(),
                    seatPos.z * 1 - rootPos.z * 1);
        } else {
            return Vec3.ZERO;
        }
    }

    private float getSeatPitch(BakedGeoModel model) {
        if (model != null) {
            GeoBone vcSeat = model.getBone("seet").get();

            if (vcSeat != null) {
                return vcSeat.getRotX();
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    private void seatInfo(BakedGeoModel model, Pmb99Entity animatable) {
        if (model != null) {
            GeoBone vcSeat = model.getBone("seet").get();

            Vector3d seatPos = vcSeat.getWorldPosition();

            animatable.seatPos = new Vec3(seatPos.x, seatPos.y, seatPos.z);
            animatable.seatRots[0] = vcSeat.getRotX();
            animatable.seatRots[1] = vcSeat.getRotY();

        } else {
            animatable.seatPos = Vec3.ZERO;
            animatable.seatRots[0] = 0;
            animatable.seatRots[1] = 0;
        }
    }

    @Override
    public void render(Pmb99Entity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        RenderUtils.renderBossBars(entity, poseStack, bufferSource, this.entityRenderDispatcher, 7);
    }
}