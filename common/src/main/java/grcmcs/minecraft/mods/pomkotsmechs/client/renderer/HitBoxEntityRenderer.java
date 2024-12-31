package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.HitBoxEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class HitBoxEntityRenderer extends EntityRenderer<HitBoxEntity> {
    public HitBoxEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

//    @Override
//    public boolean shouldRender(HitBoxEntity entity, Frustum frustum, double x, double y, double z) {
//        // 当たり判定はレンダリングしないが、判定が必要な場合はここで処理
//        return false; // 描画は常に無効
//    }

    @Override
    public void render(HitBoxEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
//        poseStack.pushPose();
//
//        // ブロックの大きさや位置の調整
//        poseStack.translate(-0.1D, 0.0D, -0.1D); // 中心に合わせる
//
//        // キューブのモデルをレンダリング (例えば、STONEブロック)
//        BlockState blockState = Blocks.STONE.defaultBlockState();
//        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockState, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
//
//        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(HitBoxEntity entity) {
        // テクスチャは必要ないのでnullを返す
        return null;
    }
}
