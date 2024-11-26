package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements RenderLayerParent<T, M> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    protected LivingEntityRendererMixin(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void onRender(T entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        // エンティティのレンダリングの前にカスタムヘルスバーを描画する
        if (!(entity instanceof Player)) {
//            renderHealthBar(matrixStack, entity, Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation(), buffer);
        }
    }

    private void renderHealthBar(PoseStack matrixStack, T entity, Quaternionf rotation, MultiBufferSource buffer) {
        Minecraft client = Minecraft.getInstance();

        // ヘルスバーを表示する距離制限（64ブロック）
        double distance = client.gameRenderer.getMainCamera().getPosition().distanceTo(entity.position());
        if (distance > 64 * 64) {
            return;
        }

        // エンティティの上にヘルスバーを表示
        matrixStack.pushPose(); // 現在のレンダリング状態を保存
        matrixStack.translate(0, entity.getBbHeight() + 0.5, 0); // エンティティの頭上に移動
        matrixStack.scale(-0.1F, 0.1F, 0.1F);
        matrixStack.mulPose(rotation); // カメラの向きに合わせて回転

        // ヘルスバーの幅や高さ
        int barWidth = 40;
        int barHeight = 6;
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
        addQuad(bufferBuilder, matrixStack, -barWidth / 2, 0, barWidth / 2, barHeight, 0x555555AA);

        // ヘルスバーの描画（緑）
        addQuad(bufferBuilder, matrixStack, -barWidth / 2, 0, -barWidth / 2 + healthBarWidth, barHeight, 0xFF00AAFF);

        tesselator.end(); // 描画を終了してバッファを送り込む
        matrixStack.popPose(); // 状態を元に戻す
    }

    // 四角形を描画するヘルパー関数
    private void addQuad(BufferBuilder buffer, PoseStack poseStack, float minX, float minY, float maxX, float maxY, int color) {
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