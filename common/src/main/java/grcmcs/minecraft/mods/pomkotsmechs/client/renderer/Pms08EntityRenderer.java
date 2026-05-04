package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pms07EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pms08EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms07Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms08Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class Pms08EntityRenderer extends GeoEntityRenderer<Pms08Entity> {
    public Pms08EntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Pms08EntityModel());
    }

    @Override
    public void render(Pms08Entity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        RenderUtils.renderMobBars(entity, poseStack, bufferSource, this.entityRenderDispatcher, 1.3F);
    }
}