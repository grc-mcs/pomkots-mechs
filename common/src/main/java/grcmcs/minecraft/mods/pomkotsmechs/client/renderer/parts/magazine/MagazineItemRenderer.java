package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.magazine;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.extension.ExtensionItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.magazine.MagazineItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;

public class MagazineItemRenderer extends BasePartsItemRenderer.Magazine<BasePartsItem.Magazine> {
    public MagazineItemRenderer() {
        super(new MagazineItemModel());
    }
}
