package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.MukudoriItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TsubameItem;
import net.minecraft.resources.ResourceLocation;

public class TsubameItemModel extends BasePartsItemModel<TsubameItem> {
    @Override
    public ResourceLocation getModelResource(TsubameItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/tsubame.geo.json");
    }
}
