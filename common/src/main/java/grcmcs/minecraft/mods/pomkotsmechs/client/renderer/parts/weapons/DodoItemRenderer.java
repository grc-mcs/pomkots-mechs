package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.BiwaItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.DodoItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.BiwaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.DodoItem;

public class DodoItemRenderer extends BasePartsItemRenderer.Weapon<DodoItem> {
    public DodoItemRenderer() {
            super(new DodoItemModel());
    }
}
