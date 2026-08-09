package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KintokiItem;
import net.minecraft.resources.ResourceLocation;

public class KintokiItemModel extends BasePartsItemModel<KintokiItem> {
    @Override
    public ResourceLocation getModelResource(KintokiItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/kintoki.geo.json");
    }
}
