package grcmcs.minecraft.mods.pomkotsmechs.items.parts.fuel;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.fuel.FuelItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class PelletItem extends BasePartsItem.Fuel {

    public PelletItem(Properties properties) {
        super(properties);
    }

    @Override
    public FuelItemRenderer newRenderer() {
        return new FuelItemRenderer();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public String getPartsSeriesName() {
        return "pellet";
    }
}
