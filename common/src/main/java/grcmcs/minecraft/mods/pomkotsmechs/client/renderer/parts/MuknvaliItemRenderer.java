package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.MuknvaliItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.MuknvaliItem;

public class MuknvaliItemRenderer {
    public static class Head extends BasePartsItemRenderer.Head<MuknvaliItem.Head> {
        public Head() {
            super(new MuknvaliItemModel.Head());
        }
    }

    public static class Body extends BasePartsItemRenderer.Body<MuknvaliItem.Body> {
        public Body() {
            super(new MuknvaliItemModel.Body());
        }
    }

    public static class Arm extends BasePartsItemRenderer.Arm<MuknvaliItem.Arm> {
        public Arm() { super(new MuknvaliItemModel.Arm()); }
    }

    public static class Legs extends BasePartsItemRenderer.Legs<MuknvaliItem.Legs> {
        public Legs() {
            super(new MuknvaliItemModel.Legs());
        }
    }
}
