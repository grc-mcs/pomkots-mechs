package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.BossBoxEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmc02EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.Pmc02Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BossBoxEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class Pmc02EntityRenderer extends GeoEntityRenderer<Pmc02Entity> {
    public Pmc02EntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Pmc02EntityModel());
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, Pmc02Entity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.scaleHeight = animatable.scale;
        this.scaleWidth = animatable.scale;
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);

        model.getBone("root").get().setRotY((float)Math.toRadians(animatable.getYRot()));
    }
}
