package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.KawasemiItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.NosuriItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KawasemiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.NosuriItem;

public class NosuriItemRenderer extends BasePartsItemRenderer.Weapon<NosuriItem> {
    public NosuriItemRenderer() {
            super(new NosuriItemModel());
    }
}
