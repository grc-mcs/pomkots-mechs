package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.KaranEntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.KaranEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class KaranEntityRenderer extends GeoEntityRenderer<KaranEntity> {
    public KaranEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new KaranEntityModel());
        this.shadowRadius = 0.35F;
    }
}
