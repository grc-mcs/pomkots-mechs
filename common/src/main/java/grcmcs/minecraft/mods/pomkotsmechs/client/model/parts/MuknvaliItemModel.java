package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.MuknvaliItem;
import net.minecraft.resources.ResourceLocation;

public class MuknvaliItemModel {
    public static String name = MuknvaliItem.SERIES_NAME;

    public static class Head extends BasePartsItemModel<MuknvaliItem.Head> {
        @Override
        public ResourceLocation getModelResource(MuknvaliItem.Head animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "head.geo.json");
        }
    }

    public static class Body extends BasePartsItemModel<MuknvaliItem.Body> {
        @Override
        public ResourceLocation getModelResource(MuknvaliItem.Body animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "body.geo.json");
        }
    }

    public static class Arm extends BasePartsItemModel<MuknvaliItem.Arm> {
        @Override
        public ResourceLocation getModelResource(MuknvaliItem.Arm animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "arm.geo.json");
        }
    }

    public static class Legs extends BasePartsItemModel<MuknvaliItem.Legs> {
        @Override
        public ResourceLocation getModelResource(MuknvaliItem.Legs animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "legs.geo.json");
        }
    }
}
