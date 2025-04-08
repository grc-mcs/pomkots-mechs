package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.generators;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.generators.ChibaItem;
import net.minecraft.resources.ResourceLocation;

public class GeneratorItemModel extends BasePartsItemModel<BasePartsItem.Generator> {
    @Override
    public ResourceLocation getModelResource(BasePartsItem.Generator animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/" + animatable.getPartsSeriesName() + ".geo.json");
    }
}
