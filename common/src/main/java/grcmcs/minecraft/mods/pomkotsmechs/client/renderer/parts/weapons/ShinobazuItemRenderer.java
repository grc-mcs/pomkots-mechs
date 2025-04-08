package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.ShakujiItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.ShinobazuItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShakujiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShinobazuItem;

public class ShinobazuItemRenderer extends BasePartsItemRenderer.Weapon<ShinobazuItem> {
    public ShinobazuItemRenderer() {
            super(new ShinobazuItemModel());
    }
}
