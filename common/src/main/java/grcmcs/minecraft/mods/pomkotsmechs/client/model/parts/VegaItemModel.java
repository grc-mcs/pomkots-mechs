package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.DenebItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.VegaItem;
import net.minecraft.resources.ResourceLocation;

public class VegaItemModel {
    public static String name = VegaItem.SERIES_NAME;

    public static class Head extends BasePartsItemModel<VegaItem.Head> {
        @Override
        public ResourceLocation getModelResource(VegaItem.Head animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "head.geo.json");
        }
    }

    public static class Body extends BasePartsItemModel<VegaItem.Body> {
        @Override
        public ResourceLocation getModelResource(VegaItem.Body animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "body.geo.json");
        }
    }

    public static class Arm extends BasePartsItemModel<VegaItem.Arm> {
        @Override
        public ResourceLocation getModelResource(VegaItem.Arm animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "arm.geo.json");
        }
    }

    public static class Legs extends BasePartsItemModel<VegaItem.Legs> {
        @Override
        public ResourceLocation getModelResource(VegaItem.Legs animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "legs.geo.json");
        }
    }
}
