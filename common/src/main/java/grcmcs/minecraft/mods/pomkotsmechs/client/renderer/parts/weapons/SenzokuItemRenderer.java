package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.SenzokuItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.ShakujiItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.SenzokuItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShakujiItem;

public class SenzokuItemRenderer extends BasePartsItemRenderer.Weapon<SenzokuItem> {
    public SenzokuItemRenderer() {
            super(new SenzokuItemModel());
    }
}
