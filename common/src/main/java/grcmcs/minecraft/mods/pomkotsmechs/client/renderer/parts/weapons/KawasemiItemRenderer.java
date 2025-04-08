package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.BiwaItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.KawasemiItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.BiwaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KawasemiItem;

public class KawasemiItemRenderer extends BasePartsItemRenderer.Weapon<KawasemiItem> {
    public KawasemiItemRenderer() {
            super(new KawasemiItemModel());
    }
}
