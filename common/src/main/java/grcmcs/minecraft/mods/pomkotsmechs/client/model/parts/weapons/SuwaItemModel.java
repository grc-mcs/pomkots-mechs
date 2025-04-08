package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KasumiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.SuwaItem;
import net.minecraft.resources.ResourceLocation;

public class SuwaItemModel extends BasePartsItemModel<SuwaItem> {
    @Override
    public ResourceLocation getModelResource(SuwaItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/suwa.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(SuwaItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/suwa.animation.json");
    }
}
