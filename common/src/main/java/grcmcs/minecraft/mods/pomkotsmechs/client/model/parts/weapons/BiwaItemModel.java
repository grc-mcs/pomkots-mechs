package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.BiwaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.JinbaItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.ShakujiItem;
import net.minecraft.resources.ResourceLocation;

public class BiwaItemModel extends BasePartsItemModel<BiwaItem> {
    @Override
    public ResourceLocation getModelResource(BiwaItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/biwa.geo.json");
    }

    @Override
    public ResourceLocation getAnimationResource(BiwaItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/biwa.animation.json");
    }
}
