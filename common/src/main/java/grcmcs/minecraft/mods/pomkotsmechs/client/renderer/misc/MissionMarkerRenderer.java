package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.MissionMarkerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class MissionMarkerRenderer extends EntityRenderer<MissionMarkerEntity> {
    public MissionMarkerRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MissionMarkerEntity entity, float yaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int light) {
        poseStack.pushPose();
        VertexConsumer panels = buffers.getBuffer(MissionMarkerRenderTypes.GLOWING_PANELS);
        if (entity.markerMode() == MissionMarkerEntity.REACHED_AREA) {
            float closing = entity.isClosing()
                    ? Mth.clamp((entity.closingTicks() + partialTick) / 12.0F, 0.0F, 1.0F) : 0.0F;
            renderSidePanels(poseStack.last(), panels,
                    entity.minX(), entity.minY(), entity.minZ(),
                    entity.maxX(), Mth.lerp(closing, entity.maxY(), entity.minY()), entity.maxZ(),
                    Math.max(0, Math.round(82 * (1.0F - closing))));
        } else {
            renderSidePanels(poseStack.last(), panels,
                    -0.35F, 0.0F, -0.35F,
                    0.35F, 2.5F, 0.35F, 82);
        }
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffers, light);
    }

    private static void renderSidePanels(PoseStack.Pose pose, VertexConsumer buffer,
                                         float minX, float minY, float minZ,
                                         float maxX, float maxY, float maxZ, int alpha) {
        int red = 26;
        int green = 255;
        int blue = 230;
        quad(buffer, pose, minX, minY, minZ, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ,
                red, green, blue, alpha);
        quad(buffer, pose, maxX, minY, maxZ, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ,
                red, green, blue, alpha);
        quad(buffer, pose, minX, minY, maxZ, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ,
                red, green, blue, alpha);
        quad(buffer, pose, maxX, minY, minZ, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ,
                red, green, blue, alpha);
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float x3, float y3, float z3, float x4, float y4, float z4,
                             int red, int green, int blue, int alpha) {
        buffer.vertex(pose.pose(), x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.vertex(pose.pose(), x2, y2, z2).color(red, green, blue, alpha).endVertex();
        buffer.vertex(pose.pose(), x3, y3, z3).color(red, green, blue, alpha).endVertex();
        buffer.vertex(pose.pose(), x4, y4, z4).color(red, green, blue, alpha).endVertex();
    }

    @Override public ResourceLocation getTextureLocation(MissionMarkerEntity entity) { return null; }
}
