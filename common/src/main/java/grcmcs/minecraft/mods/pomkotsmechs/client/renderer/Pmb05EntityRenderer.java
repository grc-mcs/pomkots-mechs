package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmb05EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb05Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class Pmb05EntityRenderer extends GeoEntityRenderer<Pmb05Entity> {
    public Pmb05EntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Pmb05EntityModel());
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, Pmb05Entity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);

        this.scaleHeight = Pmb05Entity.DEFAULT_SCALE;
        this.scaleWidth = Pmb05Entity.DEFAULT_SCALE;

    }

    private void updateLaserBones(Pmb05Entity entity) {
        GeoBone laserBeamBone = getGeoModel().getBone("beam").orElse(null);
        if (laserBeamBone == null) {
            return;
        }
        if (entity.getLaserLength() > 0) {
            laserBeamBone.setScaleZ(entity.getLaserLength());
        } else {
            laserBeamBone.setScaleZ(1);
        }

    }

    @Override
    public void actuallyRender(PoseStack poseStack, Pmb05Entity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        updateLaserBones(animatable);
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}