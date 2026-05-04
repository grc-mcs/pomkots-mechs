package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PlateEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PlateEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PlateEntityRenderer extends GeoEntityRenderer<PlateEntity> {
    public PlateEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PlateEntityModel());
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, PlateEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.scaleHeight = 1;
        this.scaleWidth = 1;
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }
}
