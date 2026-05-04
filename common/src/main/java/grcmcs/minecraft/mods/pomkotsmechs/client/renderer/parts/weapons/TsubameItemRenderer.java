package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.MukudoriItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.TsubameItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.MukudoriItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TsubameItem;

public class TsubameItemRenderer extends BasePartsItemRenderer.Weapon<TsubameItem> {
    public TsubameItemRenderer() {
            super(new TsubameItemModel());
    }
}
