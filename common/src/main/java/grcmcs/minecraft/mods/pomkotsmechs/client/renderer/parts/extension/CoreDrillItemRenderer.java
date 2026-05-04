package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.extension.CoreDrillItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.extension.HoverUnitItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;

public class CoreDrillItemRenderer extends BasePartsItemRenderer.Extension<BasePartsItem.Extension> {
    public CoreDrillItemRenderer() {
        super(new CoreDrillItemModel());
    }
}
