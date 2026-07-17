package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.TargetLocker;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3d;

public class RenderUtils {

    public static void renderAdditionalHud2(
            PomkotsVehicleBase entity,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            EntityRenderDispatcher erd,
            float heightOffset) {
        Minecraft client = Minecraft.getInstance();
        if (
                client.options.hideGui
                || !PomkotsMechs.CONFIG.enableHudHealthBar
                || entity.equals(client.player.getVehicle())
                || (entity instanceof Pmvc01Entity pmvc01 && !pmvc01.showCustomHealthBar())
        ) {
            return;
        }

        double distance = client.gameRenderer.getMainCamera().getPosition().distanceTo(entity.position());
        if (distance > 64 * 64) {
            return;
        }
        renderVehicleBars(entity, poseStack, bufferSource, erd, heightOffset);
    }

    public static void renderVehicleBars(
            PomkotsVehicleBase entity,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            EntityRenderDispatcher erd,
            float heightOffset
    ) {
        Minecraft client = Minecraft.getInstance();

        if (client.options.hideGui) {
            return;
        }

        poseStack.pushPose();

        // 頭の上
        poseStack.translate(0.0, entity.getBbHeight() + heightOffset, 0.0);

        // カメラ向き
        poseStack.mulPose(erd.cameraOrientation());

        // ===== 距離補正スケール =====
        Vec3 camPos = erd.camera.getPosition();
        double dist = camPos.distanceTo(entity.position());

        // ここが肝
        float baseScale = 0.002f;
        float scale = (float) (baseScale * dist);
        scale = Math.min(scale, 0.1F);
        poseStack.scale(-scale, -scale, scale);

        PoseStack.Pose pose = poseStack.last();

        VertexConsumer vc = bufferSource.getBuffer(RenderType.debugQuads());

        float barWidth = 40f;
        float barHeight = 3f;
        float gap = 3f;

        // ===== 値（仮）=====
        float hpRatio = entity.getHealth() / entity.getMaxHealth();
//        float spRatio = Math.min(entity.getStunPoint(), 100) / 100F;

        // Y位置
        float hpY = -(barHeight + gap);
        float spY = 0;

        // HPバー
        drawBar(vc, pose, barWidth, barHeight, hpY, hpRatio,
                0, 0, 0,
                224, 224, 224);

//        // 特殊ゲージ
//        if (entity.isStunning()) {
//            drawBar(vc, pose, barWidth, barHeight, spY, spRatio,
//                    0, 0, 0,
//                    128, 0, 0);
//        } else {
//            drawBar(vc, pose, barWidth, barHeight, spY, spRatio,
//                    0, 0, 0,
//                    224, 224, 224);
//        }

        poseStack.popPose();
    }

    public static void renderBossBars(
            BaseBossEntity entity,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            EntityRenderDispatcher erd,
            float heightOffset
    ) {
        Minecraft client = Minecraft.getInstance();

        if (entity.getAiMode() == BaseBossEntity.AI_MODE_INACTIVE || client.options.hideGui  || entity.tickCount < 5) {
            return;
        }

        poseStack.pushPose();

        // 頭の上
        poseStack.translate(0.0, entity.getBbHeight() + heightOffset, 0.0);

        // カメラ向き
        poseStack.mulPose(erd.cameraOrientation());

        // ===== 距離補正スケール =====
        Vec3 camPos = erd.camera.getPosition();
        double dist = camPos.distanceTo(entity.position());

        // ここが肝
        float baseScale = 0.002f;
        float scale = (float) (baseScale * dist);
        poseStack.scale(-scale, -scale, scale);

        PoseStack.Pose pose = poseStack.last();

        VertexConsumer vc = bufferSource.getBuffer(RenderType.debugQuads());

        float barWidth = 40f;
        float barHeight = 3f;
        float gap = 3f;

        // ===== 値（仮）=====
        float hpRatio = entity.getHealth() / entity.getMaxHealth();
        float spRatio = Math.min(entity.getStunPoint(), BaseBossEntity.STUN_STUN_START) / (float)BaseBossEntity.STUN_STUN_START;

        // Y位置
        float hpY = -(barHeight + gap);
        float spY = 0;

        // HPバー
        drawBar(vc, pose, barWidth, barHeight, hpY, hpRatio,
                0, 0, 0,
                224, 224, 224);

        // 特殊ゲージ
        if (entity.isStunning()) {
            drawBar(vc, pose, barWidth, barHeight, spY, spRatio,
                    0, 0, 0,
                    128, 0, 0);
        } else {
            drawBar(vc, pose, barWidth, barHeight, spY, spRatio,
                    0, 0, 0,
                    224, 224, 224);
        }

        poseStack.popPose();
    }

    public static void renderMobBars(
            GenericPomkotsMonster entity,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            EntityRenderDispatcher erd,
            float heightOffset
    ) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.options.hideGui || entity.tickCount < 5) {
            return;
        }

        poseStack.pushPose();

        // 頭の上
        poseStack.translate(0.0, entity.getBbHeight() + heightOffset, 0.0);

        // カメラ向き
        poseStack.mulPose(erd.cameraOrientation());

        // ===== 距離補正スケール =====
        Vec3 camPos = erd.camera.getPosition();
        double dist = camPos.distanceTo(entity.position());

        // ここが肝
        float baseScale = 0.002f;
        float scale = (float) (baseScale * dist);
        poseStack.scale(-scale, -scale, scale);

        PoseStack.Pose pose = poseStack.last();

        VertexConsumer vc = bufferSource.getBuffer(RenderType.debugQuads());

        float barWidth = 20f;
        float barHeight = 3f;
        float gap = 3f;

        // ===== 値（仮）=====
        float hpRatio = entity.getHealth() / entity.getMaxHealth();

        // Y位置
        float hpY = -(barHeight + gap);

        // HPバー
        drawBar(vc, pose, barWidth, barHeight, hpY, hpRatio,
                0, 0, 0,
                224, 224, 224);

        poseStack.translate(0.0, -15, 0.0);

        if (entity.isInEvent()) {
            VertexConsumer vc2 =
                    bufferSource.getBuffer(NO_DEPTH_TRIANGLES);
            triangle(
                    vc2,
                    pose,
                    7F,
                    0F,
                    255, 0, 0, 255
            );
        }

        poseStack.popPose();
    }


    public static void renderMarking(
            LivingEntity entity,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            EntityRenderDispatcher erd,
            float heightOffset
    ) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.options.hideGui || entity.tickCount < 5) {
            return;
        }

        poseStack.pushPose();

        poseStack.translate(0.0, entity.getBbHeight() + heightOffset, 0.0);
        poseStack.mulPose(erd.cameraOrientation());

        Vec3 camPos = erd.camera.getPosition();
        double dist = camPos.distanceTo(entity.position());
        float baseScale = 0.002f;
        float scale = (float) (baseScale * dist);

        poseStack.scale(-scale, -scale, scale);
        PoseStack.Pose pose = poseStack.last();
        poseStack.translate(0.0, -15, 0.0);

        VertexConsumer vc2 =
                bufferSource.getBuffer(NO_DEPTH_TRIANGLES);
        triangle(
                vc2,
                pose,
                7F,
                0F,
                255, 0, 0, 255
        );

        poseStack.popPose();
    }

    private static void triangle(
            VertexConsumer vc,
            PoseStack.Pose pose,
            float size,
            float z,
            int r, int g, int b, int a
    ) {
        vc.vertex(pose.pose(), 0, size/2, z).color(r,g,b,a).endVertex();
        vc.vertex(pose.pose(), -size, -size/2, z).color(r,g,b,a).endVertex();
        vc.vertex(pose.pose(), size, -size/2, z).color(r,g,b,a).endVertex();
    }

    public static final RenderType NO_DEPTH_TRIANGLES =
            RenderType.create(
                    "no_depth_triangles",
                    DefaultVertexFormat.POSITION_COLOR,
                    VertexFormat.Mode.TRIANGLES,
                    131072,
                    false,
                    true,
                    RenderType.CompositeState.builder()
                            .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                            .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                            .setCullState(RenderStateShard.NO_CULL)
                            .createCompositeState(false)
            );

    private static void drawBar(
            VertexConsumer vc,
            PoseStack.Pose pose,
            float width,
            float height,
            float y,
            float ratio,
            int bgR, int bgG, int bgB,
            int fgR, int fgG, int fgB
    ) {
        ratio = Mth.clamp(ratio, 0f, 1f);

        float left = -width;
        float right = width;
        float filledRight = left + (right - left) * ratio;

        // ===== 背景 =====
        quad(vc, pose, left, y, right, y + height, 1F, bgR, bgG, bgB, 128);

        // ===== 中身 =====
        quad(vc, pose, left, y, filledRight, y + height, -1F, fgR, fgG, fgB, 253);
    }

    private static void quad(
            VertexConsumer vc,
            PoseStack.Pose pose,
            float x1, float y1,
            float x2, float y2,
            float z,
            int r, int g, int b, int a
    ) {
        vc.vertex(pose.pose(), x1, y1, z).color(r, g, b, a).endVertex();
        vc.vertex(pose.pose(), x1, y2, z).color(r, g, b, a).endVertex();
        vc.vertex(pose.pose(), x2, y2, z).color(r, g, b, a).endVertex();
        vc.vertex(pose.pose(), x2, y1, z).color(r, g, b, a).endVertex();
    }

    // ここから↓は旧バージョン互換用

    public static void renderAdditionalHud(PoseStack matrixStack, LivingEntity entity, Quaternionf rotation, MultiBufferSource buffer) {
        Minecraft client = Minecraft.getInstance();

        if (!client.options.hideGui) {
            double distance = client.gameRenderer.getMainCamera().getPosition().distanceTo(entity.position());
            if (distance > 64 * 64) {
                return;
            }

            if (!entity.equals(client.player.getVehicle())) {
                if (PomkotsMechs.CONFIG.enableHudHealthBar) {
                    renderHealthBar(matrixStack, entity, rotation, buffer);
                }
            }
        }
    }

    private static void renderTargetLock(PoseStack matrixStack, LivingEntity entity, Quaternionf rotation, MultiBufferSource buffer) {
        int lockState = TargetLocker.getInstance().isEntityLocked(entity);

        switch (lockState) {
            case TargetLocker.SOFT:
                renderTargetLockTexture("crosshair1.png", matrixStack, entity, rotation, buffer);
                break;
            case TargetLocker.HARD:
                renderTargetLockTexture("crosshair2.png", matrixStack, entity, rotation, buffer);
                break;
            case TargetLocker.MULTI:
                renderTargetLockTexture("crosshair3.png", matrixStack, entity, rotation, buffer);
                break;
            case TargetLocker.NONE:
                break;

        }
    }

    private static void renderTargetLockTexture(String textureName, PoseStack matrixStack, LivingEntity entity, Quaternionf rotation, MultiBufferSource buffer) {
        ResourceLocation reticleTexture = new ResourceLocation(PomkotsMechs.MODID, "textures/crosshair/" + textureName);

        matrixStack.pushPose();
        matrixStack.translate(0, entity.getBbHeight()/2, 0);
        matrixStack.scale(-0.1F, -0.1F, -0.1F);
        matrixStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, reticleTexture);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();

        float size = 32;
        addQuadTex(bufferBuilder, matrixStack, -size/2,-size/2,size/2,size/2,40);

        tesselator.end();
        matrixStack.popPose();
    }

    // 四角形を描画するヘルパー関数
    private static void addQuadTex(BufferBuilder buffer, PoseStack poseStack, float minX, float minY, float maxX, float maxY, int depth) {
        buffer.vertex(poseStack.last().pose(), minX, minY, depth).uv(0,0).endVertex();
        buffer.vertex(poseStack.last().pose(), minX, maxY, depth).uv(0,1).endVertex();
        buffer.vertex(poseStack.last().pose(), maxX, maxY, depth).uv(1,1).endVertex();
        buffer.vertex(poseStack.last().pose(), maxX, minY, depth).uv(1,0).endVertex();
    }

    private static void renderHealthBar(PoseStack matrixStack, LivingEntity entity, Quaternionf rotation, MultiBufferSource buffer) {
        // エンティティの上にヘルスバーを表示
        matrixStack.pushPose(); // 現在のレンダリング状態を保存
        matrixStack.translate(0, entity.getBbHeight() + 2.0, 0); // エンティティの頭上に移動
        matrixStack.scale(-0.1F, -0.1F, -0.1F);
        matrixStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation()); // カメラの向きに合わせて回転

        // ヘルスバーの幅や高さ
        int barWidth = 40;
        int barHeight = 3;
        int maxHealth = (int) entity.getMaxHealth();
        int currentHealth = (int) entity.getHealth();
        int healthBarWidth = (int) ((currentHealth / (float) maxHealth) * barWidth);

        // テッセレーターを使って描画する
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        // 描画開始
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        RenderSystem.setShader(GameRenderer::getPositionColorShader); // 位置と色を使うシェーダーを設定
        RenderSystem.enableBlend();              // ブレンディングを有効にする
        RenderSystem.disableDepthTest();

        // 背景の描画（グレー）
        addQuad(bufferBuilder, matrixStack, -barWidth / 2, 0, barWidth / 2, barHeight, 0x55555555);

        // ヘルスバーの描画（緑）
//        addQuad(bufferBuilder, matrixStack, -barWidth / 2, 0, -barWidth / 2 + healthBarWidth, barHeight, 0x990086C9);
        if (entity instanceof PomkotsVehicle) {
            addQuad(bufferBuilder, matrixStack, -barWidth / 2, 0, -barWidth / 2 + healthBarWidth, barHeight, 0x990000AA);
        } else {
            addQuad(bufferBuilder, matrixStack, -barWidth / 2, 0, -barWidth / 2 + healthBarWidth, barHeight, 0x99DE0000);
        }

        tesselator.end(); // 描画を終了してバッファを送り込む
        matrixStack.popPose(); // 状態を元に戻す
    }

    // 四角形を描画するヘルパー関数
    private static void addQuad(BufferBuilder buffer, PoseStack poseStack, float minX, float minY, float maxX, float maxY, int color) {
        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;

        buffer.vertex(poseStack.last().pose(), minX, minY, 0).color(r, g, b, a).endVertex();
        buffer.vertex(poseStack.last().pose(), minX, maxY, 0).color(r, g, b, a).endVertex();
        buffer.vertex(poseStack.last().pose(), maxX, maxY, 0).color(r, g, b, a).endVertex();
        buffer.vertex(poseStack.last().pose(), maxX, minY, 0).color(r, g, b, a).endVertex();
    }

    public static void renderBlocks(BlockState state, Vector3d offset, Float yaw, PoseStack pose, MultiBufferSource buffer, int packedLight) {
        BlockRenderDispatcher dispatcher =
                Minecraft.getInstance().getBlockRenderer();

        pose.pushPose();
        pose.translate(offset.x, offset.y, offset.z);

//        pose.translate(4.5, 0, 4.5);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));
//        pose.translate(-4.5, 0, -4.5);

        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -1; z <= 1; z++) {
                    pose.pushPose();
                    pose.translate(x, y, z);

                    dispatcher.renderSingleBlock(
                            state,
                            pose,
                            buffer,
                            packedLight, // 明るさ最大
                            OverlayTexture.NO_OVERLAY
                    );

                    pose.popPose();
                }
            }
        }

        pose.popPose();
    }
}
