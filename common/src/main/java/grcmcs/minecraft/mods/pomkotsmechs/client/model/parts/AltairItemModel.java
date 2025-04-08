package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.AltairItem;
import net.minecraft.resources.ResourceLocation;

public class AltairItemModel {
    public static String name = AltairItem.SERIES_NAME;

    public static class Head extends BasePartsItemModel<AltairItem.Head> {
        @Override
        public ResourceLocation getModelResource(AltairItem.Head animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "head.geo.json");
        }
    }

    public static class Body extends BasePartsItemModel<AltairItem.Body> {
        @Override
        public ResourceLocation getModelResource(AltairItem.Body animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "body.geo.json");
        }
    }

    public static class Arm extends BasePartsItemModel<AltairItem.Arm> {
        @Override
        public ResourceLocation getModelResource(AltairItem.Arm animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "arm.geo.json");
        }
    }

    public static class Legs extends BasePartsItemModel<AltairItem.Legs> {
        @Override
        public ResourceLocation getModelResource(AltairItem.Legs animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "legs.geo.json");
        }
    }
}
