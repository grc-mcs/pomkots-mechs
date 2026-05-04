package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.AmagiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KagenobuItem;
import net.minecraft.resources.ResourceLocation;

public class AmagiItemModel extends BasePartsItemModel<AmagiItem> {
    @Override
    public ResourceLocation getModelResource(AmagiItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/amagi.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(AmagiItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/amagi.animation.json");
    }
}
