package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.extension.BuilderUnitItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.extension.SBUnitProtoTypeItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;

public class BuilderUnitItemRenderer extends BasePartsItemRenderer.Extension<BasePartsItem.Extension> {
    public BuilderUnitItemRenderer() {
        super(new BuilderUnitItemModel());
    }
}
