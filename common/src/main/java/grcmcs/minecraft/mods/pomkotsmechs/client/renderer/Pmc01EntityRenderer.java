package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmc01EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pms01EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.Pmc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileBaseEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmc01EntityRenderer extends GeoEntityRenderer<Pmc01Entity> {
    public Pmc01EntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Pmc01EntityModel());
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, Pmc01Entity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
//        model.getBone("root").get().setRotY((float)Math.toRadians(animatable.getYRot()));
    }

    @Override
    public void actuallyRender(PoseStack poseStack, Pmc01Entity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        RenderUtils.renderAdditionalHud(poseStack, animatable, this.entityRenderDispatcher.cameraOrientation(), bufferSource);
    }
}