package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.JinbaItem;
import net.minecraft.resources.ResourceLocation;

public class JinbaItemModel extends BasePartsItemModel<JinbaItem> {
    @Override
    public ResourceLocation getModelResource(JinbaItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/jinba.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(JinbaItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/jinba.animation.json");
    }
}
