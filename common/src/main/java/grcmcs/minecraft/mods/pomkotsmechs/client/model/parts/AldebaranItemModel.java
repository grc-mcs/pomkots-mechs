package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.*;
import net.minecraft.resources.ResourceLocation;

public class AldebaranItemModel {
    public static String name = AldebaranItem.SERIES_NAME;

    public static class Head extends BasePartsItemModel<AldebaranItem.Head> {
        @Override
        public ResourceLocation getModelResource(AldebaranItem.Head animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "head.geo.json");
        }
    }

    public static class Body extends BasePartsItemModel<AldebaranItem.Body> {
        @Override
        public ResourceLocation getModelResource(AldebaranItem.Body animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "body.geo.json");
        }
    }

    public static class Arm extends BasePartsItemModel<AldebaranItem.Arm> {
        @Override
        public ResourceLocation getModelResource(AldebaranItem.Arm animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "arm.geo.json");
        }
    }

    public static class Legs extends BasePartsItemModel<AldebaranItem.Legs> {
        @Override
        public ResourceLocation getModelResource(AldebaranItem.Legs animatable) {
            return new ResourceLocation(PomkotsMechs.MODID, "geo/" + name + "legs.geo.json");
        }
    }
}
