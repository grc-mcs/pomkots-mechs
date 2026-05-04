package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.AltairItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.RustyItem;
import net.minecraft.resources.ResourceLocation;

public class RustyItemModel {
    public static String name = RustyItem.SERIES_NAME;

    public static class Head extends BasePartsItemModel<RustyItem.Head> {
        @Override
        public ResourceLocation getModelResource(RustyItem.Head animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "head.geo.json");
        }
    }

    public static class Body extends BasePartsItemModel<RustyItem.Body> {
        @Override
        public ResourceLocation getModelResource(RustyItem.Body animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "body.geo.json");
        }
    }

    public static class Arm extends BasePartsItemModel<RustyItem.Arm> {
        @Override
        public ResourceLocation getModelResource(RustyItem.Arm animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "arm.geo.json");
        }
    }

    public static class Legs extends BasePartsItemModel<RustyItem.Legs> {
        @Override
        public ResourceLocation getModelResource(RustyItem.Legs animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "legs.geo.json");
        }
    }
}
