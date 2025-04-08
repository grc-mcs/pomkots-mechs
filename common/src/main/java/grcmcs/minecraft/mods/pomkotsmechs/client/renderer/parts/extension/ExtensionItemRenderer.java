package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.extension.ExtensionItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.generators.GeneratorItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;

public class ExtensionItemRenderer extends BasePartsItemRenderer.Extension<BasePartsItem.Extension> {
    public ExtensionItemRenderer() {
        super(new ExtensionItemModel());
    }
}
