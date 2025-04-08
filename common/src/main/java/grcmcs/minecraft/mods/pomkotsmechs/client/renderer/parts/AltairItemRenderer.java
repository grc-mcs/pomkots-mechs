package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.AltairItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.AltairItem;

public class AltairItemRenderer {
    public static class Head extends BasePartsItemRenderer.Head<AltairItem.Head> {
        public Head() {
            super(new AltairItemModel.Head());
        }
    }

    public static class Body extends BasePartsItemRenderer.Body<AltairItem.Body> {
        public Body() {
            super(new AltairItemModel.Body());
        }
    }

    public static class Arm extends BasePartsItemRenderer.Arm<AltairItem.Arm> {
        public Arm() { super(new AltairItemModel.Arm()); }
    }

    public static class Legs extends BasePartsItemRenderer.Legs<AltairItem.Legs> {
        public Legs() {
            super(new AltairItemModel.Legs());
        }
    }
}
