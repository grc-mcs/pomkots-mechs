package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.MashuItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.MitakeItem;
import net.minecraft.resources.ResourceLocation;

public class MitakeItemModel extends BasePartsItemModel<MitakeItem> {
    @Override
    public ResourceLocation getModelResource(MitakeItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/mitake.geo.json");
    }
}
