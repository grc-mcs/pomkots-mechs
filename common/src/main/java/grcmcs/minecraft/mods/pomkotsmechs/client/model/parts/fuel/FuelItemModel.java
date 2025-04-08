package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.fuel;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.resources.ResourceLocation;

public class FuelItemModel extends BasePartsItemModel<BasePartsItem.Fuel> {
    @Override
    public ResourceLocation getModelResource(BasePartsItem.Fuel animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pellet.geo.json");
    }
}
