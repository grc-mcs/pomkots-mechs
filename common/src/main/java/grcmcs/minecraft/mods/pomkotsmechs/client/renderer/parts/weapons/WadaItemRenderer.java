package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.TenpouItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.WadaItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TenpouItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.WadaItem;

public class WadaItemRenderer extends BasePartsItemRenderer.Weapon<WadaItem> {
    public WadaItemRenderer() {
            super(new WadaItemModel());
    }
}
