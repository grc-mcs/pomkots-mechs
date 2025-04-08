package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.GassanItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TakaoItem;
import net.minecraft.resources.ResourceLocation;

public class GassanItemModel extends BasePartsItemModel<GassanItem> {
    @Override
    public ResourceLocation getModelResource(GassanItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/gassan.geo.json");
    }
}
