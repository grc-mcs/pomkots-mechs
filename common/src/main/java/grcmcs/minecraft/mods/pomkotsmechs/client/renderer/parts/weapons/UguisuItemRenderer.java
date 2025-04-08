package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.NosuriItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.UguisuItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.NosuriItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.UguisuItem;

public class UguisuItemRenderer extends BasePartsItemRenderer.Weapon<UguisuItem> {
    public UguisuItemRenderer() {
            super(new UguisuItemModel());
    }
}
