package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmss01EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmss02EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.Pmss01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.Pmss02Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmss02EntityRenderer extends GeoEntityRenderer<Pmss02Entity> {
    public Pmss02EntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Pmss02EntityModel());
    }

    @Override
    public void render(Pmss02Entity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        RenderUtils.renderMobBars(entity, poseStack, bufferSource, this.entityRenderDispatcher, 1);
    }
}