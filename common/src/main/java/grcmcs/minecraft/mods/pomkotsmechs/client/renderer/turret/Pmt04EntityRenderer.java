package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.turret;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.turret.Pmt03EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.turret.Pmt04EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.RenderUtils;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt03Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt04Entity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmt04EntityRenderer extends GeoEntityRenderer<Pmt04Entity> {
    public Pmt04EntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Pmt04EntityModel());
    }

    @Override
    public void render(Pmt04Entity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        RenderUtils.renderMobBars(entity, poseStack, bufferSource, this.entityRenderDispatcher, 1.3F);
    }
}