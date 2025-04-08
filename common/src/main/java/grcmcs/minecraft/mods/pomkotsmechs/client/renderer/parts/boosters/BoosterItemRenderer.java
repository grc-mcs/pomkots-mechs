package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.boosters;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.boosters.BoosterItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;

public class BoosterItemRenderer extends BasePartsItemRenderer.Booster<BasePartsItem.Booster> {
    public BoosterItemRenderer() {
        super(new BoosterItemModel());
    }
}
