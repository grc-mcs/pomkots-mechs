package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.KasumiItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.TakaoItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KasumiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TakaoItem;

public class KasumiItemRenderer extends BasePartsItemRenderer.Weapon<KasumiItem> {
    public KasumiItemRenderer() {
            super(new KasumiItemModel());
    }
}
