package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.DaigomaruItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.ShoutouItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.DaigomaruItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShoutouItem;

public class ShoutouItemRenderer extends BasePartsItemRenderer.Weapon<ShoutouItem> {
    public ShoutouItemRenderer() {
            super(new ShoutouItemModel());
    }
}
