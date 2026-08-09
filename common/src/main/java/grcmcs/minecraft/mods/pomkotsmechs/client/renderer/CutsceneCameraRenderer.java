package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.cutscene.CutsceneCameraEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public final class CutsceneCameraRenderer extends EntityRenderer<CutsceneCameraEntity> {
    public CutsceneCameraRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(CutsceneCameraEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
