package grcmcs.minecraft.mods.pomkotsmechs.items.parts.generators;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.generators.GeneratorItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class SagaItem extends BasePartsItem.Generator {

    public SagaItem(Properties properties) {
        super(properties);
    }

    @Override
    public GeneratorItemRenderer newRenderer() {
        return new GeneratorItemRenderer();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public String getPartsSeriesName() {
        return "saga";
    }
}
