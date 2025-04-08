package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.SiriusItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.VegaItem;
import net.minecraft.resources.ResourceLocation;

public class SiriusItemModel {
    public static String name = SiriusItem.SERIES_NAME;

    public static class Head extends BasePartsItemModel<SiriusItem.Head> {
        @Override
        public ResourceLocation getModelResource(SiriusItem.Head animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "head.geo.json");
        }
    }

    public static class Body extends BasePartsItemModel<SiriusItem.Body> {
        @Override
        public ResourceLocation getModelResource(SiriusItem.Body animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "body.geo.json");
        }
    }

    public static class Arm extends BasePartsItemModel<SiriusItem.Arm> {
        @Override
        public ResourceLocation getModelResource(SiriusItem.Arm animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "arm.geo.json");
        }
    }

    public static class Legs extends BasePartsItemModel<SiriusItem.Legs> {
        @Override
        public ResourceLocation getModelResource(SiriusItem.Legs animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "legs.geo.json");
        }
    }
}
