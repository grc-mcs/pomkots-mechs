package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.generators;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.generators.GeneratorItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;

public class GeneratorItemRenderer extends BasePartsItemRenderer.Generator<BasePartsItem.Generator> {
    public GeneratorItemRenderer() {
        super(new GeneratorItemModel());
    }
}
