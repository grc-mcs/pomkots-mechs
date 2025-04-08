package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmvc01EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import java.util.function.Predicate;

public class Pmvc01EntityRenderer extends GeoEntityRenderer<Pmvc01Entity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    ItemRenderer itemRenderer;

    public Pmvc01EntityRenderer(EntityRendererProvider.Context renderManager) {
        this(renderManager, new Pmvc01EntityModel());
    }

    public Pmvc01EntityRenderer(EntityRendererProvider.Context renderManager, Pmvc01EntityModel model) {
        super(renderManager, model);
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
        addRenderLayer(new Pmvc01EntityPartsLayer<>(this, renderManager.getItemRenderer()));
        this.itemRenderer = renderManager.getItemRenderer();
    }

    @Override
    public void actuallyRender(PoseStack poseStack, Pmvc01Entity animatable, BakedGeoModel model, RenderType renderType,
                               MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
                               int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType,
                bufferSource, buffer, isReRender, partialTick,
        packedLight, packedOverlay, red, green, blue, alpha);

        this.renderParts(poseStack, animatable, model, renderType,
                bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);

        if (animatable.getDrivingPassenger() != null) {
            animatable.setClientSeatPos(getSeatPosition(model));
            animatable.setMainCameraPosition(getMainCameraPosition(model, animatable, partialTick, Pmvc01Entity.DEFAULT_SCALE));

        }

        RenderUtils.renderAdditionalHud(poseStack, animatable, this.entityRenderDispatcher.cameraOrientation(), bufferSource);
    }

    public void renderParts(PoseStack poseStack, Pmvc01Entity animatable, BakedGeoModel bakedModel, RenderType renderType,
                            MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
                            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

        poseStack.pushPose();
        float yaw = lerpYaw(partialTick, animatable.yBodyRotO, animatable.yBodyRot) + 180;
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw)); // 反転を考慮

        renderPart(animatable.getHeadParts(), (item) -> item instanceof BasePartsItem.Head, poseStack, animatable, bakedModel, renderType, bufferSource, packedLight, packedOverlay);
        renderPart(animatable.getBodyParts(), (item) -> item instanceof BasePartsItem.Body, poseStack, animatable, bakedModel, renderType, bufferSource, packedLight, packedOverlay);
        renderPart(animatable.getArmParts(), (item) -> item instanceof BasePartsItem.Arm, poseStack, animatable, bakedModel, renderType, bufferSource, packedLight, packedOverlay);
        renderPart(animatable.getLegsParts(), (item) -> item instanceof BasePartsItem.Legs, poseStack, animatable, bakedModel, renderType, bufferSource, packedLight, packedOverlay);

        poseStack.popPose();

    }

    private float lerpYaw(float partialTick, float prevYaw, float currentYaw) {
        float delta = Mth.wrapDegrees(currentYaw - prevYaw); // 角度差を -180～180° に正規化
        return prevYaw + partialTick * delta; // 補間
    }

    public void renderPart(ItemStack itemStack, Predicate<Item> isTarget, PoseStack poseStack, Pmvc01Entity animatable, BakedGeoModel bakedModel, RenderType renderType,
                           MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderPart(itemStack, isTarget, null, poseStack, animatable, bakedModel, renderType, bufferSource, packedLight, packedOverlay);
    }

    public void renderPart(ItemStack itemStack, Predicate<Item> isTarget, String side, PoseStack poseStack, Pmvc01Entity animatable, BakedGeoModel bakedModel, RenderType renderType,
                           MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!itemStack.isEmpty()) {
            Item i = itemStack.getItem();

            if (isTarget.test(i)) {
                var ti = (BasePartsItem)i;
                if (ti instanceof BasePartsItem.Weapon w) {
                    w.setSide(side);
                }

                ti.setParentEntity(animatable);
                ti.setCachedBoneFinder(animatable.getBoneFinder());
                this.itemRenderer.renderStatic(animatable, itemStack, ItemDisplayContext.NONE, false, poseStack, bufferSource, null, packedLight, packedOverlay, packedLight);
                ti.setCachedBoneFinder(null);
                ti.setParentEntity(null);

                if (ti instanceof BasePartsItem.Weapon w) {
                    w.setSide(null);
                }
            }
        }
    }

    private Vec3 getSeatPosition(BakedGeoModel model) {
        if (model != null) {
            GeoBone vcSeat = model.getBone("head").get();
            GeoBone vcRoot = model.getBone("root").get();

            Vector3d seatPos = vcSeat.getLocalPosition();
            Vector3d rootPos = vcRoot.getLocalPosition();

            return new Vec3(
                    seatPos.x * Pmvc01Entity.DEFAULT_SCALE - rootPos.x * Pmvc01Entity.DEFAULT_SCALE,
                    seatPos.y * Pmvc01Entity.DEFAULT_SCALE - rootPos.y * Pmvc01Entity.DEFAULT_SCALE - 4.2F,
                    seatPos.z * Pmvc01Entity.DEFAULT_SCALE - rootPos.z * Pmvc01Entity.DEFAULT_SCALE);
        } else {
            return Vec3.ZERO;
        }
    }

    private Vec3 getMainCameraPosition(BakedGeoModel model, Pmvc01Entity entity, float partialTick, float scale) {
        if (model != null) {
            GeoBone vcSeat = model.getBone("maincam").get();
            Vector3d seatPos = vcSeat.getLocalPosition();

            return new Vec3(
                    seatPos.x,
                    seatPos.y * scale,
                    seatPos.z
            );

        } else {
            return Vec3.ZERO;
        }
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, Pmvc01Entity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.scaleHeight = Pmvc01Entity.DEFAULT_SCALE;
        this.scaleWidth = Pmvc01Entity.DEFAULT_SCALE;
        animatable.setupBoneFinder(model);
        setWeaponsVisibility(model, animatable, animatable.isMainMode());
        setBoosterVisibilitty(model, animatable);

        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }

    private void setBoosterVisibilitty(BakedGeoModel model, Pmvc01Entity ent) {
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
            model.getBone("fire_bpcr").get().setHidden(false);
            model.getBone("fire_bpcl").get().setHidden(false);

        } else {
            model.getBone("fire_bpcr").get().setHidden(true);
            model.getBone("fire_bpcl").get().setHidden(true);

        }
    }


    private void setWeaponsVisibility(BakedGeoModel model,Pmvc01Entity ent, boolean b) {

        model.getBone("left_shoulder_hunger").get().setHidden(animatable.getLeftShoulderWeapon().isEmpty());
        model.getBone("right_shoulder_hunger").get().setHidden(animatable.getRightShoulderWeapon().isEmpty());
    }
}
