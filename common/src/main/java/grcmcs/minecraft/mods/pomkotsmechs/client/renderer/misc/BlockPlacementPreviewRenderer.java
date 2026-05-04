package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.BlockPlacementPreviewEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public class BlockPlacementPreviewRenderer extends EntityRenderer<BlockPlacementPreviewEntity> {
    public BlockPlacementPreviewRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    int i = 0;
    @Override
    public void render(
            BlockPlacementPreviewEntity entity,
            float yaw,
            float partial,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light
    ) {
        poseStack.pushPose();

        poseStack.translate(0.0, 0.0, 0.0);

        VertexConsumer vc = buffer.getBuffer(RenderType.lines());

        int q = entity.yawToQuarter(entity.getYRot());
        for (var bPos: entity.getCurrentPattern()){
            var bp = entity.rotate(bPos, q);

            poseStack.pushPose();
            poseStack.translate(bp.getX(), bp.getY(), bp.getZ());

            LevelRenderer.renderLineBox(
                    poseStack,
                    vc,
                    new AABB(0, 0, 0, 1, 1, 1),
                    0f, 1f, 0f, 1f   // RGBA（緑）
            );

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(BlockPlacementPreviewEntity entity) {
        return null;
    }
}

