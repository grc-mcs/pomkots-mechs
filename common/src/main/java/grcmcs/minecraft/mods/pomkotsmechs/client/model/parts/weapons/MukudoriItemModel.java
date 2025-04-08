package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.MukudoriItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.NosuriItem;
import net.minecraft.resources.ResourceLocation;

public class MukudoriItemModel extends BasePartsItemModel<MukudoriItem> {
    @Override
    public ResourceLocation getModelResource(MukudoriItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/mukudori.geo.json");
    }
}
