package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KagamiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShakujiItem;
import net.minecraft.resources.ResourceLocation;

public class KagamiItemModel extends BasePartsItemModel<KagamiItem> {
    @Override
    public ResourceLocation getModelResource(KagamiItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/kagami.geo.json");
    }
}
