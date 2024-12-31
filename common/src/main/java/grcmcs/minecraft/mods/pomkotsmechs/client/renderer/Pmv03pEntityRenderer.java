package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmv02EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmv03pEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv02Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv03pEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class Pmv03pEntityRenderer extends GeoEntityRenderer<Pmv03pEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    public Pmv03pEntityRenderer(EntityRendererProvider.Context renderManager) {
        this(renderManager, new Pmv03pEntityModel());
    }

    public Pmv03pEntityRenderer(EntityRendererProvider.Context renderManager, Pmv03pEntityModel model) {
        super(renderManager, model);
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void actuallyRender(PoseStack poseStack, Pmv03pEntity animatable, BakedGeoModel model, RenderType renderType,
                               MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
                               int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        //
        float xrot = Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());
//        PomkotsMechs.LOGGER.info("p" + partialTick + ",r0" + animatable.xRotO + ",r" + animatable.getXRot() + ",xx" + xrot);

        model.getBone("body").get().setRotX((float)Math.toRadians(-xrot));
//        model.getBone("body").get().setRotY((float)Math.toRadians(animatable.getYRot()));

        super.actuallyRender(poseStack, animatable, model, renderType,
                bufferSource, buffer, isReRender, partialTick,
        packedLight, packedOverlay, red, green, blue, alpha);

        if (animatable.getDrivingPassenger() != null) {
            var p1 = getSeatPosition(model, 1);
            var p2 = getSeatPosition(model, 2);

            animatable.setClientSeatPos(p1);
            animatable.setClientSeatPos1(p1);
            animatable.setClientSeatPos2(p2);
        }

        RenderUtils.renderAdditionalHud(poseStack, animatable, this.entityRenderDispatcher.cameraOrientation(), bufferSource);
    }

    private Vec3 getSeatPosition(BakedGeoModel model, int num) {
        if (model != null) {
            GeoBone vcSeat = model.getBone("seat" + num).get();
            GeoBone vcRoot = model.getBone("trot").get();

            Vector3d seatPos = vcSeat.getLocalPosition();
            Vector3d rootPos = vcRoot.getLocalPosition();

            return new Vec3(
                    seatPos.x * Pmv03pEntity.DEFAULT_SCALE - rootPos.x * Pmv03pEntity.DEFAULT_SCALE,
                    seatPos.y * Pmv03pEntity.DEFAULT_SCALE - rootPos.y * Pmv03pEntity.DEFAULT_SCALE - 2.8F,
                    seatPos.z * Pmv03pEntity.DEFAULT_SCALE - rootPos.z * Pmv03pEntity.DEFAULT_SCALE);
        } else {
            return Vec3.ZERO;
        }
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, Pmv03pEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.scaleHeight = Pmv03pEntity.DEFAULT_SCALE;
        this.scaleWidth = Pmv03pEntity.DEFAULT_SCALE;


        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);

    }

}
