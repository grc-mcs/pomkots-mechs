package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.misc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.PlacementPreviewEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public class PlacementPreviewRenderer
        extends EntityRenderer<PlacementPreviewEntity> {

    public PlacementPreviewRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    public void render2(
            PlacementPreviewEntity entity,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        poseStack.pushPose();

        // === エンティティ中心に描画 ===
        poseStack.translate(0.0, 0.0, 0.0);

        // 必要なら回転（ロボ or プレイヤー向き）
        poseStack.mulPose(
                Axis.YP.rotationDegrees(-entity.getYRot())
        );

        // 9x9x9 の AABB（中心基準）
        AABB box = new AABB(
                -4.5, -4.5, -4.5,
                4.5,  4.5,  4.5
        );

        // === 半透明塗り ===
        VertexConsumer solid =
                buffer.getBuffer(RenderType.entityTranslucentCull(
                        TextureAtlas.LOCATION_BLOCKS
                ));

        LevelRenderer.renderLineBox(
                poseStack,
                solid,
                box,
                1.0f, 0.0f, 0.0f, 0.25f
        );

        // === ワイヤフレーム ===
        VertexConsumer lines =
                buffer.getBuffer(RenderType.lines());

        LevelRenderer.renderLineBox(
                poseStack,
                lines,
                box,
                1.0f, 0.2f, 0.2f, 0.9f
        );

        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }
int i = 0;
    @Override
    public void render(
            PlacementPreviewEntity entity,
            float yaw,
            float partial,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int light
    ) {
        poseStack.pushPose();

        poseStack.translate(0.0, 0.0, 0.0);
        VertexConsumer vc = buffer.getBuffer(RenderType.lines());

        LevelRenderer.renderLineBox(
                poseStack,
                vc,
                new AABB(-1, -1, -1, 1, 1, 1),
                0f, 1f, 0f, 1f
        );

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(PlacementPreviewEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

