package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.BiwaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.DodoItem;
import net.minecraft.resources.ResourceLocation;

public class DodoItemModel extends BasePartsItemModel<DodoItem> {
    @Override
    public ResourceLocation getModelResource(DodoItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/dodo.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(DodoItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/dodo.animation.json");
    }
}
