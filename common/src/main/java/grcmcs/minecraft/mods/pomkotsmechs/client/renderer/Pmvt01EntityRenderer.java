package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmvt01EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.turret.Pmt01EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.turret.Pmvt01Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmvt01EntityRenderer extends GeoEntityRenderer<Pmvt01Entity> {
    public Pmvt01EntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Pmvt01EntityModel());
    }

    @Override
    public void actuallyRender(PoseStack poseStack, Pmvt01Entity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        RenderUtils.renderAdditionalHud(poseStack, animatable, this.entityRenderDispatcher.cameraOrientation(), bufferSource);

    }
}