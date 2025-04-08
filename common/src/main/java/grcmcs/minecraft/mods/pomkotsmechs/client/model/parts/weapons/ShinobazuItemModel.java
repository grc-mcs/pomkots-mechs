package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShakujiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShinobazuItem;
import net.minecraft.resources.ResourceLocation;

public class ShinobazuItemModel extends BasePartsItemModel<ShinobazuItem> {
    @Override
    public ResourceLocation getModelResource(ShinobazuItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/shinobazu.geo.json");
    }
}
