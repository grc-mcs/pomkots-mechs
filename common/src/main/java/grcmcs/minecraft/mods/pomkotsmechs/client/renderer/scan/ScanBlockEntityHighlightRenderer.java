package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.scan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockPurpleEntity;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockRedEntity;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockYellowEntity;
import grcmcs.minecraft.mods.pomkotsmechs.block.ScanTargetBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.scan.ClientScanManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ScanBlockEntityHighlightRenderer {

    private ScanBlockEntityHighlightRenderer() {
    }

    public static void render(
            PoseStack poseStack,
            Camera camera,
            MultiBufferSource.BufferSource bufferSource
    ) {
        ClientLevel level = Minecraft.getInstance().level;

        if (level == null) {
            return;
        }
        if (Minecraft.getInstance().player == null) {
            return;
        }
        if (ClientScanManager.isBlockHighlightEmpty()) {
            return;
        }

        Vec3 cameraPos =
                camera.getPosition();

        /*
         * 壁越し描画を明示。
         */
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        try {
            VertexConsumer consumer =
                    bufferSource.getBuffer(
                            ScanRenderTypes.SCAN_HIGHLIGHT_LINES
                    );

            boolean rendered = false;

            for (BlockPos pos
                    : ClientScanManager.getHighlightedBlockEntities()) {

                BlockEntity blockEntity = level.getBlockEntity(pos);

                if (!(blockEntity
                        instanceof ScanTargetBlockEntity scanTarget)) {
                    continue;
                }

                AABB renderBox = createHighlightBox(blockEntity)
                        .inflate(0.02D)
                        .move(
                                -cameraPos.x,
                                -cameraPos.y,
                                -cameraPos.z
                        );
                var color = getColor(blockEntity);

                LevelRenderer.renderLineBox(
                        poseStack,
                        consumer,
                        renderBox,
                        color[0],
                        color[1],
                        color[2],
                        0.65F
                );

                rendered = true;
            }

            if (rendered) {
                bufferSource.endBatch(
                        ScanRenderTypes.SCAN_HIGHLIGHT_LINES
                );
            }
        } finally {
            /*
             * 後続描画を壊さないよう必ず戻す。
             */
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
        }
    }

    private static float[] getColor(BlockEntity be) {
        if (be instanceof PomkotsCubeBlockPurpleEntity) {
            return COLOR_PURPLE;
        } else if (be instanceof PomkotsCubeBlockRedEntity) {
            return COLOR_RED;
        } else if (be instanceof PomkotsCubeBlockYellowEntity) {
            return COLOR_YELLOW;
        } else {
            return COLOR_BLUE;
        }
    }

    private static final float[] COLOR_BLUE = {0.0F, 0.8F, 1.0F};
    private static final float[] COLOR_YELLOW = {1F, 1F, 0F};
    private static final float[] COLOR_RED = {1F, 0F, 0F};
    private static final float[] COLOR_PURPLE = {0.6F, 0F, 1.0F};

    private static AABB createHighlightBox(
            BlockEntity blockEntity
    ) {
        BlockPos pos =
                blockEntity.getBlockPos();

        /*
         * 当面は1ブロックサイズ。
         * 独自チェストが複数ブロックサイズなら、
         * BlockEntity側にAABBを返すメソッドを作る。
         */
        return new AABB(pos);
    }
}
