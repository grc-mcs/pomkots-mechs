package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.MineEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.RockLargeEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MineEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.RockLargeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MineEntityRenderer extends GeoEntityRenderer<MineEntity> {
    public MineEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MineEntityModel());
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, MineEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
//        this.scaleHeight = 2.0F;
//        this.scaleWidth = 2.0F;
    }
}
