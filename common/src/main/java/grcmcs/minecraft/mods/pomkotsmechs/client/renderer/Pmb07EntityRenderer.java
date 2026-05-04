package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmb04EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmb07EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb04Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb07Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmb07EntityRenderer extends GeoEntityRenderer<Pmb07Entity> {
    public Pmb07EntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Pmb07EntityModel());
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, Pmb07Entity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);

        this.scaleHeight = Pmb07Entity.DEFAULT_SCALE;
        this.scaleWidth = Pmb07Entity.DEFAULT_SCALE;
    }

    @Override
    public void actuallyRender(PoseStack poseStack, Pmb07Entity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

    }

    @Override
    public void render(Pmb07Entity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        RenderUtils.renderBossBars(entity, poseStack, bufferSource, this.entityRenderDispatcher, 7);
    }
}