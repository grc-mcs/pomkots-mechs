package grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension.RailSliderItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class RailSliderItem extends BasePartsItem.Extension {

    public RailSliderItem(Properties properties) {
        super(properties);
    }

    @Override
    public RailSliderItemRenderer newRenderer() {
        return new RailSliderItemRenderer();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public String getPartsSeriesName() {
        return "railslider";
    }
}
