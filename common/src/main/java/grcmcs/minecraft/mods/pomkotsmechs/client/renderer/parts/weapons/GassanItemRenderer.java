package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.GassanItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.TakaoItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.GassanItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TakaoItem;

public class GassanItemRenderer extends BasePartsItemRenderer.Weapon<GassanItem> {
    public GassanItemRenderer() {
            super(new GassanItemModel());
    }
}
