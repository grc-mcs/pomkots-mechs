package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.DenebItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.DenebItem;

public class DenebItemRenderer {
    public static class Head extends BasePartsItemRenderer.Head<DenebItem.Head> {
        public Head() {
            super(new DenebItemModel.Head());
        }
    }

    public static class Body extends BasePartsItemRenderer.Body<DenebItem.Body> {
        public Body() {
            super(new DenebItemModel.Body());
        }
    }

    public static class Arm extends BasePartsItemRenderer.Arm<DenebItem.Arm> {
        public Arm() { super(new DenebItemModel.Arm()); }
    }

    public static class Legs extends BasePartsItemRenderer.Legs<DenebItem.Legs> {
        public Legs() {
            super(new DenebItemModel.Legs());
        }
    }
}
