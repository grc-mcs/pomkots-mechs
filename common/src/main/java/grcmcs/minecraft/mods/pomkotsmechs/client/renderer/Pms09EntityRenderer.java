package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pms08EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pms09EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms08Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms09Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class Pms09EntityRenderer extends GeoEntityRenderer<Pms09Entity> {
    public Pms09EntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Pms09EntityModel());
    }

    @Override
    public void render(Pms09Entity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        RenderUtils.renderMobBars(entity, poseStack, bufferSource, this.entityRenderDispatcher, 1.3F);
    }
}