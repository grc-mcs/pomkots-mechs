package grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension.BuilderUnitItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension.SBUnitProtoTypeItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class BuilderUnitItem extends BasePartsItem.Extension {

    public BuilderUnitItem(Properties properties) {
        super(properties);
    }

    @Override
    public BuilderUnitItemRenderer newRenderer() {
        return new BuilderUnitItemRenderer();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public String getPartsSeriesName() {
        return "builderunit";
    }
}
