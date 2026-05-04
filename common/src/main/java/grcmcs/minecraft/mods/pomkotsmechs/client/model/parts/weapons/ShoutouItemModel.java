package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.DaigomaruItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShoutouItem;
import net.minecraft.resources.ResourceLocation;

public class ShoutouItemModel extends BasePartsItemModel<ShoutouItem> {
    @Override
    public ResourceLocation getModelResource(ShoutouItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/shoutou.geo.json");
    }
}
