package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.fuel;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.fuel.FuelItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;

public class FuelItemRenderer extends BasePartsItemRenderer.Fuel<BasePartsItem.Fuel> {
    public FuelItemRenderer() {
        super(new FuelItemModel());
    }
}
