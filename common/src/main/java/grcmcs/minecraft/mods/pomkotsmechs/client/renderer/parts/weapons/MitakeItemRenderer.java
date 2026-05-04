package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.MashuItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.MitakeItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.MashuItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.MitakeItem;

public class MitakeItemRenderer extends BasePartsItemRenderer.Weapon<MitakeItem> {
    public MitakeItemRenderer() {
            super(new MitakeItemModel());
    }
}
