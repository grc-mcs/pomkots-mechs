package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KagenobuItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TsurugiItem;
import net.minecraft.resources.ResourceLocation;

public class KagenobuItemModel extends BasePartsItemModel<KagenobuItem> {
    @Override
    public ResourceLocation getModelResource(KagenobuItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/kagenobu.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(KagenobuItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/kagenobu.animation.json");
    }
}
