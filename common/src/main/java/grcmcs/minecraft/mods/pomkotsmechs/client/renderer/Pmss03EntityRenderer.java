package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmss02EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmss03EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.Pmss02Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.Pmss03Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmss03EntityRenderer extends GeoEntityRenderer<Pmss03Entity> {
    public Pmss03EntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Pmss03EntityModel());
    }

    @Override
    public void render(Pmss03Entity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        RenderUtils.renderMobBars(entity, poseStack, bufferSource, this.entityRenderDispatcher, 1);
    }

    @Override
    public int getPackedOverlay(Pmss03Entity entity, float packedLight, float partialTick) {
        if (!entity.isSelfDestructing()) {
            return super.getPackedOverlay(entity, packedLight, partialTick);
        }

        float fuse = entity.getFuse() - partialTick;

        // 点滅周期
        if ((int)(fuse / 5) % 2 == 0) {
            return OverlayTexture.pack(OverlayTexture.u(1.0F), 10);
        }

        return OverlayTexture.NO_OVERLAY;
    }
}