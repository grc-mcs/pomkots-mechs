package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.NeedleEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile.BulletRifleEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.NeedleEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletRifleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class NeedleEntityRenderer extends GeoEntityRenderer<NeedleEntity> {
    public NeedleEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new NeedleEntityModel());

        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, NeedleEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);

        model.getBone("root").get().setRotX((float)Math.toRadians(animatable.getXRot()));
        model.getBone("root").get().setRotY((float)Math.toRadians(animatable.getYRot()));
    }
}
