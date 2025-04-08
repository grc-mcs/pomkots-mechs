package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.SenzokuItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShakujiItem;
import net.minecraft.resources.ResourceLocation;

public class SenzokuItemModel extends BasePartsItemModel<SenzokuItem> {
    @Override
    public ResourceLocation getModelResource(SenzokuItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/senzoku.geo.json");
    }
}
