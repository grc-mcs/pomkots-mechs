package grcmcs.minecraft.mods.pomkotsmechs.items.parts.boosters;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.boosters.BoosterItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class NaritaItem extends BasePartsItem.Booster {

    public NaritaItem(Properties properties) {
        super(properties);
    }

    @Override
    public BoosterItemRenderer newRenderer() {
        return new BoosterItemRenderer();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public String getPartsSeriesName() {
        return "narita";
    }
}
