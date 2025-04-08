package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.MashuItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShakujiItem;
import net.minecraft.resources.ResourceLocation;

public class MashuItemModel extends BasePartsItemModel<MashuItem> {
    @Override
    public ResourceLocation getModelResource(MashuItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/mashu.geo.json");
    }
}
