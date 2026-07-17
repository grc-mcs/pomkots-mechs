package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaCameraEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class ArenaCameraRenderer
        extends EntityRenderer<ArenaCameraEntity> {

    public ArenaCameraRenderer(
            EntityRendererProvider.Context context
    ) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(
            ArenaCameraEntity entity
    ) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public void render(
            ArenaCameraEntity entity,
            float entityYaw,
            float partialTicks,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight
    ) {
        // 描画しない
    }
}
