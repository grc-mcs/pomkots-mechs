package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PlayerDummyEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.controller.PlayerDummyEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PlayerDummyEntityRenderer extends GeoEntityRenderer<PlayerDummyEntity> {
    public PlayerDummyEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PlayerDummyEntityModel());

    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, PlayerDummyEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);

        this.scaleHeight = PlayerDummyEntity.DEFAULT_SCALE;
        this.scaleWidth = PlayerDummyEntity.DEFAULT_SCALE;

//        if (animatable.getVehicle() != null) {
//            BakedGeoModel vehicleModel = ((ProtoBotEntity) (animatable.getVehicle())).getBakedGeoModel();
//
//            if (vehicleModel != null) {
//                GeoBone dmRoot = model.getBone("hip").get();
//                GeoBone vcSeat = vehicleModel.getBone("head").get();
//
//                Vector3d seatPos = vcSeat.getModelPosition();
//                Vector3d headPos = dmRoot.getModelPosition();
//
//                dmRoot.setPosX((float) seatPos.x * ProtoBotEntity.DEFAULT_SCALE - (float) headPos.x);
//                dmRoot.setPosY((float) seatPos.y * ProtoBotEntity.DEFAULT_SCALE - (float) headPos.y);
//                dmRoot.setPosZ((float) seatPos.z * ProtoBotEntity.DEFAULT_SCALE - (float) headPos.z);
//            }
//        }
    }
}