package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.TransportShipEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.DefenseShelterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.TransportShipEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TransportShipEntityRenderer extends GeoEntityRenderer<TransportShipEntity> {
    public TransportShipEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new TransportShipEntityModel());
        shadowRadius = 0.0F;
    }

    @Override
    public boolean shouldShowName(TransportShipEntity entity) {
        return false;
    }

        @Override
    public void render(TransportShipEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        RenderUtils.renderHealthBar(entity, poseStack, bufferSource, this.entityRenderDispatcher, 2.3F);
    }
}
