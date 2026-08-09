package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.KintokiItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KintokiItem;

public class KintokiItemRenderer extends BasePartsItemRenderer.Weapon<KintokiItem> {
    public KintokiItemRenderer() {
            super(new KintokiItemModel());
    }
}
