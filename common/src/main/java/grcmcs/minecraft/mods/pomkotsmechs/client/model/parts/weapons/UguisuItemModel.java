package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShakujiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.UguisuItem;
import net.minecraft.resources.ResourceLocation;

public class UguisuItemModel extends BasePartsItemModel<UguisuItem> {
    @Override
    public ResourceLocation getModelResource(UguisuItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/uguisu.geo.json");
    }
}
