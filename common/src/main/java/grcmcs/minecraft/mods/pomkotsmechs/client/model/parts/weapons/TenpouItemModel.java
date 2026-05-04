package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KagamiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TenpouItem;
import net.minecraft.resources.ResourceLocation;

public class TenpouItemModel extends BasePartsItemModel<TenpouItem> {
    @Override
    public ResourceLocation getModelResource(TenpouItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/tenpou.geo.json");
    }
}
