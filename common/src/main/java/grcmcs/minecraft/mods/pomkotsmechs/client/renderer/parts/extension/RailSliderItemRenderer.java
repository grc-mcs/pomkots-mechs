package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.extension.HoverUnitItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.extension.RailSliderItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;

public class RailSliderItemRenderer extends BasePartsItemRenderer.Extension<BasePartsItem.Extension> {
    public RailSliderItemRenderer() {
        super(new RailSliderItemModel());
    }
}
