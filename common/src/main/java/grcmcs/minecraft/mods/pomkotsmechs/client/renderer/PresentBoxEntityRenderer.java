package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.KujiraEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.PresentBoxEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.KujiraEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PresentBoxEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class PresentBoxEntityRenderer extends GeoEntityRenderer<PresentBoxEntity> {
    public PresentBoxEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PresentBoxEntityModel());

        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, PresentBoxEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }
}
