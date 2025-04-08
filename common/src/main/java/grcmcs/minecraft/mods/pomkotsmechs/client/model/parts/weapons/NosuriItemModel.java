package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KawasemiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.NosuriItem;
import net.minecraft.resources.ResourceLocation;

public class NosuriItemModel extends BasePartsItemModel<NosuriItem> {
    @Override
    public ResourceLocation getModelResource(NosuriItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/nosuri.geo.json");
    }
}
