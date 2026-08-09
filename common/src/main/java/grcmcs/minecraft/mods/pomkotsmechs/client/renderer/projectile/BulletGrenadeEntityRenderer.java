package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.GrenadeEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile.BulletGrenadeEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.GrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class BulletGrenadeEntityRenderer extends GeoEntityRenderer<BulletGrenadeEntity> {
    public BulletGrenadeEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new BulletGrenadeEntityModel());

        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, BulletGrenadeEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.scaleHeight = 2;
        this.scaleWidth = 2;
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }
}
