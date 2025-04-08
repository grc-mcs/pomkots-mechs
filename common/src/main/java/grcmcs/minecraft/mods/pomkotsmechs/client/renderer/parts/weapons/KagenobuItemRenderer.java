package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.KagenobuItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KagenobuItem;

public class KagenobuItemRenderer extends BasePartsItemRenderer.Weapon<KagenobuItem> {
    public KagenobuItemRenderer() {
            super(new KagenobuItemModel());
    }
}
