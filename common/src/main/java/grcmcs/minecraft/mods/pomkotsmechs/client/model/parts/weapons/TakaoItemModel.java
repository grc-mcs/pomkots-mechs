package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShakujiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TakaoItem;
import net.minecraft.resources.ResourceLocation;

public class TakaoItemModel extends BasePartsItemModel<TakaoItem> {
    @Override
    public ResourceLocation getModelResource(TakaoItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/takao.geo.json");
    }
}
