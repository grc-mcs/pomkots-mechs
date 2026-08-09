package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.DefenseShelterEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.DefenseShelterEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DefenseShelterEntityRenderer extends GeoEntityRenderer<DefenseShelterEntity> {
    public DefenseShelterEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new DefenseShelterEntityModel());
        this.shadowRadius = 0.0F;
    }

    @Override
    public boolean shouldShowName(DefenseShelterEntity entity) {
        return false;
    }

    @Override
    public void render(DefenseShelterEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        RenderUtils.renderHealthBar(entity, poseStack, bufferSource, this.entityRenderDispatcher, 2.3F);
    }
}
