package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.SiriusItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.VegaItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.SiriusItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.VegaItem;

public class SiriusItemRenderer {
    public static class Head extends BasePartsItemRenderer.Head<SiriusItem.Head> {
        public Head() {
            super(new SiriusItemModel.Head());
        }
    }

    public static class Body extends BasePartsItemRenderer.Body<SiriusItem.Body> {
        public Body() {
            super(new SiriusItemModel.Body());
        }
    }

    public static class Arm extends BasePartsItemRenderer.Arm<SiriusItem.Arm> {
        public Arm() { super(new SiriusItemModel.Arm()); }
    }

    public static class Legs extends BasePartsItemRenderer.Legs<SiriusItem.Legs> {
        public Legs() {
            super(new SiriusItemModel.Legs());
        }
    }
}
