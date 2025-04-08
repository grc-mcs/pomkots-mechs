package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.DenebItem;
import net.minecraft.resources.ResourceLocation;

public class DenebItemModel {
    public static String name = DenebItem.SERIES_NAME;

    public static class Head extends BasePartsItemModel<DenebItem.Head> {
        @Override
        public ResourceLocation getModelResource(DenebItem.Head animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "head.geo.json");
        }
    }

    public static class Body extends BasePartsItemModel<DenebItem.Body> {
        @Override
        public ResourceLocation getModelResource(DenebItem.Body animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "body.geo.json");
        }
    }

    public static class Arm extends BasePartsItemModel<DenebItem.Arm> {
        @Override
        public ResourceLocation getModelResource(DenebItem.Arm animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "arm.geo.json");
        }
    }

    public static class Legs extends BasePartsItemModel<DenebItem.Legs> {
        @Override
        public ResourceLocation getModelResource(DenebItem.Legs animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "legs.geo.json");
        }
    }
}
