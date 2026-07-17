package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.ElevetorEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.ElevatorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ElevatorEntityRenderer extends GeoEntityRenderer<ElevatorEntity> {
    public ElevatorEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ElevetorEntityModel());
    }

    @Override
    public void render(ElevatorEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        if (entity.getMode() == ElevatorEntity.Mode.WAITING
                && entity.getPassengers().isEmpty()) {
            var mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.getUUID().equals(entity.getPassengerUUID())) {
                RenderUtils.renderMarking(entity, poseStack, bufferSource, this.entityRenderDispatcher, 1.3F);
            }
        }
    }

    @Override
    public boolean shouldShowName(ElevatorEntity entity) {
        return false;
    }
}