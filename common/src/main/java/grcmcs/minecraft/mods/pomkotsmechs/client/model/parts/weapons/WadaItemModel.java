package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TenpouItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.WadaItem;
import net.minecraft.resources.ResourceLocation;

public class WadaItemModel extends BasePartsItemModel<WadaItem> {
    @Override
    public ResourceLocation getModelResource(WadaItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/wada.geo.json");
    }
}
