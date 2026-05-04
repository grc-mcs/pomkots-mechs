package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.AltairItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.RustyItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.AltairItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.RustyItem;

public class RustyItemRenderer {
    public static class Head extends BasePartsItemRenderer.Head<RustyItem.Head> {
        public Head() {
            super(new RustyItemModel.Head());
        }
    }

    public static class Body extends BasePartsItemRenderer.Body<RustyItem.Body> {
        public Body() {
            super(new RustyItemModel.Body());
        }
    }

    public static class Arm extends BasePartsItemRenderer.Arm<RustyItem.Arm> {
        public Arm() { super(new RustyItemModel.Arm()); }
    }

    public static class Legs extends BasePartsItemRenderer.Legs<RustyItem.Legs> {
        public Legs() {
            super(new RustyItemModel.Legs());
        }
    }
}
