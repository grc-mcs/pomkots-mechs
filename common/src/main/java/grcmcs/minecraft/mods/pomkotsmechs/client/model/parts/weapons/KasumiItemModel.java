package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KagenobuItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.KasumiItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShakujiItem;
import net.minecraft.resources.ResourceLocation;

public class KasumiItemModel extends BasePartsItemModel<KasumiItem> {
    @Override
    public ResourceLocation getModelResource(KasumiItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/kasumi.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(KasumiItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/kasumi.animation.json");
    }
}
