package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.JinbaItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.JinbaItem;

public class JinbaItemRenderer extends BasePartsItemRenderer.Weapon<JinbaItem> {
    public JinbaItemRenderer() {
            super(new JinbaItemModel());
    }
}
