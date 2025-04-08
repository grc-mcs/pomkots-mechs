package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.ShakujiItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.TakaoItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShakujiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TakaoItem;

public class TakaoItemRenderer extends BasePartsItemRenderer.Weapon<TakaoItem> {
    public TakaoItemRenderer() {
            super(new TakaoItemModel());
    }
}
