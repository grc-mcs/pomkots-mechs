package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.DaigomaruItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KagamiItem;
import net.minecraft.resources.ResourceLocation;

public class DaigomaruItemModel extends BasePartsItemModel<DaigomaruItem> {
    @Override
    public ResourceLocation getModelResource(DaigomaruItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/daigomaru.geo.json");
    }
}
