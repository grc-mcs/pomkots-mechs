package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.magazine;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.resources.ResourceLocation;

public class MagazineItemModel extends BasePartsItemModel<BasePartsItem.Magazine> {
    @Override
    public ResourceLocation getModelResource(BasePartsItem.Magazine animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/magazine.geo.json");
    }
}
