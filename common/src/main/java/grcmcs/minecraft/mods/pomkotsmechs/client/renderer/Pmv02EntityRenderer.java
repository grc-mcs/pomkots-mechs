package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmv02EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv02Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class Pmv02EntityRenderer extends GeoEntityRenderer<Pmv02Entity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    public Pmv02EntityRenderer(EntityRendererProvider.Context renderManager) {
        this(renderManager, new Pmv02EntityModel());
    }

    public Pmv02EntityRenderer(EntityRendererProvider.Context renderManager, Pmv02EntityModel model) {
        super(renderManager, model);
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void actuallyRender(PoseStack poseStack, Pmv02Entity animatable, BakedGeoModel model, RenderType renderType,
                               MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
                               int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType,
                bufferSource, buffer, isReRender, partialTick,
        packedLight, packedOverlay, red, green, blue, alpha);

        if (animatable.getDrivingPassenger() != null) {
            animatable.setClientSeatPos(getSeatPosition(model));
        }

        RenderUtils.renderAdditionalHud(poseStack, animatable, this.entityRenderDispatcher.cameraOrientation(), bufferSource);
    }

    private Vec3 getSeatPosition(BakedGeoModel model) {
        if (model != null) {
            GeoBone vcSeat = model.getBone("head").get();
            GeoBone vcRoot = model.getBone("trot").get();

            Vector3d seatPos = vcSeat.getLocalPosition();
            Vector3d rootPos = vcRoot.getLocalPosition();

            return new Vec3(
                    seatPos.x * Pmv02Entity.DEFAULT_SCALE - rootPos.x * Pmv02Entity.DEFAULT_SCALE,
                    seatPos.y * Pmv02Entity.DEFAULT_SCALE - rootPos.y * Pmv02Entity.DEFAULT_SCALE - 2.8F,
                    seatPos.z * Pmv02Entity.DEFAULT_SCALE - rootPos.z * Pmv02Entity.DEFAULT_SCALE);
        } else {
            return Vec3.ZERO;
        }
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, Pmv02Entity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.scaleHeight = Pmv02Entity.DEFAULT_SCALE;
        this.scaleWidth = Pmv02Entity.DEFAULT_SCALE;

        if (animatable.isDrilling() && animatable.getDrivingPassenger() != null) {
            var look = animatable.getDrivingPassenger().getLookAngle();
            float pitch = (float) (Math.asin(look.y));
            model.getBone("left_upper_arm").get().setRotX(pitch);
        }
//        setHummerVisibility(model, animatable.isSubMode());
//        setWeaponsVisibility(model, animatable.isMainMode());
        setBoosterVisibilitty(model, animatable);

        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }

    private void setBoosterVisibilitty(BakedGeoModel model, Pmv02Entity ent) {
        var vel = ent.getDeltaMovement();
        vel = vel.yRot((float) Math.toRadians(ent.getYRot()));

        if (vel.x > 1) {
            model.getBone("fire_rsb").get().setHidden(false);
            model.getBone("fire_lsb").get().setHidden(true);
        } else if (vel.x < -1) {
            model.getBone("fire_rsb").get().setHidden(true);
            model.getBone("fire_lsb").get().setHidden(false);
        } else {
            model.getBone("fire_rsb").get().setHidden(true);
            model.getBone("fire_lsb").get().setHidden(true);
        }

        if (vel.z > 1) {
            model.getBone("fire_bpcr").get().setHidden(false);
            model.getBone("fire_bpcl").get().setHidden(false);
            model.getBone("fire_rl").get().setHidden(false);
            model.getBone("fire_ll").get().setHidden(false);

        } else {
            model.getBone("fire_bpcr").get().setHidden(true);
            model.getBone("fire_bpcl").get().setHidden(true);
            model.getBone("fire_rl").get().setHidden(true);
            model.getBone("fire_ll").get().setHidden(true);

        }

        if (ent.getDriverInput() != null && ent.getDriverInput().isJumpPressed()) {
            model.getBone("fire_bpr").get().setHidden(false);
            model.getBone("fire_bpl").get().setHidden(false);

        } else {
            model.getBone("fire_bpr").get().setHidden(true);
            model.getBone("fire_bpl").get().setHidden(true);

        }
    }

    private void setHummerVisibility(BakedGeoModel model,boolean b) {
        model.getBone("hummer").get().setHidden(!b);
    }

    private void setWeaponsVisibility(BakedGeoModel model,boolean b) {
        model.getBone("gun").get().setHidden(!b);
        model.getBone("pilebunker").get().setHidden(!b);
        model.getBone("vmissile").get().setHidden(!b);
        model.getBone("granadecanon").get().setHidden(!b);

    }
}
