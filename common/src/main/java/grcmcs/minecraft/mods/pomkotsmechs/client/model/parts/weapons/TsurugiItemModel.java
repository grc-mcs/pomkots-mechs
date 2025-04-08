package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.JinbaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KagenobuItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TsurugiItem;
import net.minecraft.resources.ResourceLocation;

public class TsurugiItemModel extends BasePartsItemModel<TsurugiItem> {
    @Override
    public ResourceLocation getModelResource(TsurugiItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/tsurugi.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(TsurugiItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/tsurugi.animation.json");
    }
}
