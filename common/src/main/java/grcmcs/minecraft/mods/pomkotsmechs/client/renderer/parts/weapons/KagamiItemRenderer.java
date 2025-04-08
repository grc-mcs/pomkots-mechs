package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.KagamiItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons.ShakujiItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KagamiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShakujiItem;

public class KagamiItemRenderer extends BasePartsItemRenderer.Weapon<KagamiItem> {
    public KagamiItemRenderer() {
            super(new KagamiItemModel());
    }
}
