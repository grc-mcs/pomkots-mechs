package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.KagenobuItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.TsurugiItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KagenobuItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TsurugiItem;

public class TsurugiItemRenderer extends BasePartsItemRenderer.Weapon<TsurugiItem> {
    public TsurugiItemRenderer() {
            super(new TsurugiItemModel());
    }
}
