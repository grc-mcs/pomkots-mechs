package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile.GiantTomahawkModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.GiantTomahawkEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class GiantTomahawkRenderer extends EntityRenderer<GiantTomahawkEntity> {
    private static final ResourceLocation TEMPORARY_TEXTURE =
            PomkotsMechs.id("textures/entity/projectile/giant_tomahawk.png");
    private static final float SPIN_DEGREES_PER_TICK = 72.0F;
    private static final float STUCK_ANGLE_DEGREES = -45.0F;

    private final GiantTomahawkModel model;

    public GiantTomahawkRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new GiantTomahawkModel(GiantTomahawkModel.createBodyLayer().bakeRoot());
        shadowRadius = 0.8F;
    }

    @Override
    public void render(GiantTomahawkEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();

        // Use the exact yaw supplied by KintokiItem. The minus sign converts
        // Minecraft yaw to the model's +Z-forward coordinate system.
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getFlightYaw()));
        if (entity.isStuck()) {
            // With the recommended Blockbench orientation, this leaves the axe head down at 45 degrees.
            poseStack.mulPose(Axis.XP.rotationDegrees(STUCK_ANGLE_DEGREES));
        } else {
            float spin = (entity.tickCount + partialTick) * SPIN_DEGREES_PER_TICK;
            poseStack.mulPose(Axis.XP.rotationDegrees(-entity.getXRot() + spin));
        }

        VertexConsumer vertexConsumer = buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        model.renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GiantTomahawkEntity entity) {
        return TEMPORARY_TEXTURE;
    }
}
