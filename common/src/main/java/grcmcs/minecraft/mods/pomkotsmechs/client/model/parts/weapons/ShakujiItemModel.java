package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShakujiItem;
import net.minecraft.resources.ResourceLocation;

public class ShakujiItemModel extends BasePartsItemModel<ShakujiItem> {
    @Override
    public ResourceLocation getModelResource(ShakujiItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/shakuji.geo.json");
    }
}
