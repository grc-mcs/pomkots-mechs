package grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension.HoverUnitItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class HoverUnitItem extends BasePartsItem.Extension {

    public HoverUnitItem(Properties properties) {
        super(properties);
    }

    @Override
    public HoverUnitItemRenderer newRenderer() {
        return new HoverUnitItemRenderer();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public String getPartsSeriesName() {
        return "hoverunit";
    }
}
