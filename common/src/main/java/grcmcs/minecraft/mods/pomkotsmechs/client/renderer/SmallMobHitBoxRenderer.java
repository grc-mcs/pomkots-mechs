package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.SmallMobHitBoxEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SmallMobHitBoxRenderer extends EntityRenderer<SmallMobHitBoxEntity> {
    public SmallMobHitBoxRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SmallMobHitBoxEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        super.render(entity, yaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SmallMobHitBoxEntity entity) {
        return null;
    }
}
