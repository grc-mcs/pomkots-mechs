package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.KasumiItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.SuwaItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KasumiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.SuwaItem;

public class SuwaItemRenderer extends BasePartsItemRenderer.Weapon<SuwaItem> {
    public SuwaItemRenderer() {
            super(new SuwaItemModel());
    }
}
