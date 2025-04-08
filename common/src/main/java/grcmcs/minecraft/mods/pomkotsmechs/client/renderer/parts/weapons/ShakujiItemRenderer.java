package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.ShakujiItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShakujiItem;

public class ShakujiItemRenderer extends BasePartsItemRenderer.Weapon<ShakujiItem> {
    public ShakujiItemRenderer() {
            super(new ShakujiItemModel());
    }
}
