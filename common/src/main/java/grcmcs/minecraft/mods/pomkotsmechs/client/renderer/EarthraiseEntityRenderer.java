package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.EarthraiseEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.EarthraiseEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class EarthraiseEntityRenderer extends GeoEntityRenderer<EarthraiseEntity> {
    public EarthraiseEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new EarthraiseEntityModel());

        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, EarthraiseEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        this.scaleHeight = animatable.getScaleHeight();
        this.scaleWidth = animatable.getScaleWidth();
    }
}
