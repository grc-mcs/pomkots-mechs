package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.DenebItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.VegaItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.DenebItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.VegaItem;

public class VegaItemRenderer {
    public static class Head extends BasePartsItemRenderer.Head<VegaItem.Head> {
        public Head() {
            super(new VegaItemModel.Head());
        }
    }

    public static class Body extends BasePartsItemRenderer.Body<VegaItem.Body> {
        public Body() {
            super(new VegaItemModel.Body());
        }
    }

    public static class Arm extends BasePartsItemRenderer.Arm<VegaItem.Arm> {
        public Arm() { super(new VegaItemModel.Arm()); }
    }

    public static class Legs extends BasePartsItemRenderer.Legs<VegaItem.Legs> {
        public Legs() {
            super(new VegaItemModel.Legs());
        }
    }
}
