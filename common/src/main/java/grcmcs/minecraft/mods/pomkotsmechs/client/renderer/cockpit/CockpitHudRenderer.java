package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.cockpit;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.cockpit.CockpitHudAnimatable;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.cockpit.CockpitHudModel;
import software.bernie.geckolib.renderer.GeoObjectRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class CockpitHudRenderer extends GeoObjectRenderer<CockpitHudAnimatable> {
    private CockpitHudModel model;
    private CockpitHudAnimatable animatable;

    public CockpitHudRenderer(CockpitHudModel model) {
        super(model);
        this.model = model;
    }

    public GeoObjectRenderer<CockpitHudAnimatable> addRenderLayer(GeoRenderLayer<CockpitHudAnimatable> renderLayer) {
        this.renderLayers.addLayer(renderLayer);

        return this;
    }
}
