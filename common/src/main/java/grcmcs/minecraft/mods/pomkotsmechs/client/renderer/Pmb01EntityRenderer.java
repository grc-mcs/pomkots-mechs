package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmb01EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.Pmb01Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class Pmb01EntityRenderer extends GeoEntityRenderer<Pmb01Entity> {
    public Pmb01EntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Pmb01EntityModel());

        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, Pmb01Entity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.scaleHeight = Pmb01Entity.DEFAULT_SCALE;
        this.scaleWidth = Pmb01Entity.DEFAULT_SCALE;

        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }

    @Override
    public void actuallyRender(PoseStack poseStack, Pmb01Entity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
//        RenderUtils.renderAdditionalHud(poseStack, animatable, this.entityRenderDispatcher.cameraOrientation(), bufferSource);

        if (animatable.getControllingPassenger() != null) {
            animatable.setClientSeatPos(getSeatPosition(model));
        }
    }

    private Vec3 getSeatPosition(BakedGeoModel model) {
        if (model != null) {
            GeoBone vcSeat = model.getBone("seet").get();
            GeoBone vcRoot = model.getBone("root").get();

            Vector3d seatPos = vcSeat.getLocalPosition();
            Vector3d rootPos = vcRoot.getLocalPosition();

            return new Vec3(
                    seatPos.x * Pmb01Entity.DEFAULT_SCALE - rootPos.x * Pmb01Entity.DEFAULT_SCALE,
                    seatPos.y * Pmb01Entity.DEFAULT_SCALE - rootPos.y * Pmb01Entity.DEFAULT_SCALE - 19F,
                    seatPos.z * Pmb01Entity.DEFAULT_SCALE - rootPos.z * Pmb01Entity.DEFAULT_SCALE);
        } else {
            return Vec3.ZERO;
        }
    }
}