package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.resources.ResourceLocation;

public class BuilderUnitItemModel extends BasePartsItemModel<BasePartsItem.Extension> {
    @Override
    public ResourceLocation getModelResource(BasePartsItem.Extension animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/builderunit.geo.json");
    }
}
