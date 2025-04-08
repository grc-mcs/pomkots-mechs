package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.MukudoriItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.NosuriItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.MukudoriItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.NosuriItem;

public class MukudoriItemRenderer extends BasePartsItemRenderer.Weapon<MukudoriItem> {
    public MukudoriItemRenderer() {
            super(new MukudoriItemModel());
    }
}
