package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.MissileGenericEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.WaveHorizontalEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.WaveHorizontalEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class WaveHorizontalEntityRenderer extends GeoEntityRenderer<WaveHorizontalEntity> {
    public WaveHorizontalEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WaveHorizontalEntityModel());

        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, WaveHorizontalEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);

        model.getBone("root").get().setRotX((float)Math.toRadians(animatable.getXRot()));
        model.getBone("root").get().setRotY((float)Math.toRadians(animatable.getYRot()));
    }
}
