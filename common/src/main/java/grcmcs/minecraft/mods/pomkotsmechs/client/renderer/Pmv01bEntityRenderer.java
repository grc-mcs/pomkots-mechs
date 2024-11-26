package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmv01bEntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class Pmv01bEntityRenderer extends Pmv01EntityRenderer {
    public Pmv01bEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new Pmv01bEntityModel());
    }
}