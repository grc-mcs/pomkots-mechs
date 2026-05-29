package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.extension.GliderUnitItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;

public class GliderUnitItemRenderer extends BasePartsItemRenderer.Extension<BasePartsItem.Extension> {
    public GliderUnitItemRenderer() {
        super(new GliderUnitItemModel());
    }
}
