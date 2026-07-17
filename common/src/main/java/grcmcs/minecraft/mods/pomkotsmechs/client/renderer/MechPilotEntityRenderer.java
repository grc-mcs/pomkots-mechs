package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.MechPilotEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MechPilotEntityRenderer extends GeoEntityRenderer<MechPilotEntity> {
    public MechPilotEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MechPilotEntityModel());
    }

    @Override
    public void actuallyRender(PoseStack poseStack, MechPilotEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (animatable.getVehicle() instanceof PomkotsVehicle robot) {
            Vec3 seatPos = robot.getClientSeatPos(animatable);

            poseStack.pushPose();

            // エンティティの位置を座席位置に変更
            if (robot instanceof Pmvc01Entity pmvc && pmvc.getHeadParts().getItem() instanceof BasePartsItem.Head h && h.isFullCovered()) {
                poseStack.translate(seatPos.x(), seatPos.y() - 1, seatPos.z());
            } else {
                poseStack.translate(seatPos.x(), seatPos.y(), seatPos.z());
            }

            super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

            poseStack.popPose();
        } else {
            super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        }
    }
}