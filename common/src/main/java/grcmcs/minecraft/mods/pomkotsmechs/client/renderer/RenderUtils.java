package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.TargetLocker;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.Pmb01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

public class RenderUtils {

    public static void renderAdditionalHud(PoseStack matrixStack, LivingEntity entity, Quaternionf rotation, MultiBufferSource buffer) {
        Minecraft client = Minecraft.getInstance();

        if (!client.options.hideGui) {
            double distance = client.gameRenderer.getMainCamera().getPosition().distanceTo(entity.position());
            if (distance > 64 * 64) {
                return;
            }

            if (!entity.equals(client.player.getVehicle())) {
                renderTargetLock(matrixStack, entity, rotation, buffer);

                if (PomkotsMechs.CONFIG.enableHudHealthBar && !(entity instanceof Pmb01Entity) && !(entity instanceof Pmv01Entity)) {
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

        // エンティティの上にヘルスバーを表示
        matrixStack.pushPose(); // 現在のレンダリング状態を保存
        matrixStack.translate(0, entity.getBbHeight()/2, 0); // エンティティの頭上に移動
        matrixStack.scale(-0.1F, -0.1F, -0.1F);
        matrixStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation()); // カメラの向きに合わせて回転

        // テッセレーターを使って描画する
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        // 描画開始
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        RenderSystem.setShader(GameRenderer::getPositionTexShader); // 位置と色を使うシェーダーを設定
        RenderSystem.setShaderTexture(0, reticleTexture);
        RenderSystem.enableBlend();              // ブレンディングを有効にする
        RenderSystem.enableDepthTest();

        float size = 32;
        addQuadTex(bufferBuilder, matrixStack, -size/2,-size/2,size/2,size/2,40);

        tesselator.end(); // 描画を終了してバッファを送り込む
        matrixStack.popPose(); // 状態を元に戻す
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
        if (entity instanceof Pmv01Entity) {
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
}
