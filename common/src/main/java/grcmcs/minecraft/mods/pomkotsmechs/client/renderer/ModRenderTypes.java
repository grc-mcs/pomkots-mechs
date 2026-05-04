package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class ModRenderTypes {
    public static final RenderType ENTITY_CULL = RenderType.create(
            "entity_cull",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            true,   // useDelegate
            false,  // needsSorting
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityCutoutNoCullShader))
//                    .setTextureState(new RenderStateShard.TextureStateShard(new ResourceLocation("modid", "textures/entity/robot.png"), false, false))
                    .setCullState(RenderStateShard.CULL) // ← 裏面カリング有効化！
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .createCompositeState(true)
    );
}
