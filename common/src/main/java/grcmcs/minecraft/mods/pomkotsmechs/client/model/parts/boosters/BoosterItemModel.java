package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.boosters;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.resources.ResourceLocation;

public class BoosterItemModel extends BasePartsItemModel<BasePartsItem.Booster> {
    @Override
    public ResourceLocation getModelResource(BasePartsItem.Booster animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/" + animatable.getPartsSeriesName() + ".geo.json");
    }
}
