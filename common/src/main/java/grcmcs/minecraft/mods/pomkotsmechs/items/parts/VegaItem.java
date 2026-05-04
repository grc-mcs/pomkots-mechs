package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.DenebItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.VegaItemRenderer;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class VegaItem {
    public static String SERIES_NAME = "vega";
    private static final String DEFAULT_COLOR = "orange";

    public static class Head extends BasePartsItem.Head {
        public Head(Properties properties) {
            super(properties);
        }

        @Override
        public VegaItemRenderer.Head newRenderer() {
            return new VegaItemRenderer.Head();
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

        }

        @Override
        public String getPartsSeriesName() {
            return SERIES_NAME;
        }

        @Override
        public String getDefaultColor() {
            return DEFAULT_COLOR;
        }

        @Override
        public boolean isFullCovered() {
            return true;
        }
    }

    public static class Body extends BasePartsItem.Body {
        public Body(Properties properties) {
            super(properties);
        }

        @Override
        public VegaItemRenderer.Body newRenderer() {
            return new VegaItemRenderer.Body();
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

        }

        @Override
        public String getPartsSeriesName() {
            return SERIES_NAME;
        }

        @Override
        public String getDefaultColor() {
            return DEFAULT_COLOR;
        }

        @Override
        public float getNeckPos() {
            return 0.4F;
        }
    }

    public static class Arm extends BasePartsItem.Arm {
        public Arm(Properties properties) {
            super(properties);
        }

        @Override
        public VegaItemRenderer.Arm newRenderer() {
            return new VegaItemRenderer.Arm();
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

        }

        @Override
        public String getPartsSeriesName() {
            return SERIES_NAME;
        }

        @Override
        public String getDefaultColor() {
            return DEFAULT_COLOR;
        }
    }

    public static class Legs extends BasePartsItem.Legs {
        public Legs(Properties properties) {
            super(properties);
        }

        @Override
        public VegaItemRenderer.Legs newRenderer() {
            return new VegaItemRenderer.Legs();
        }

        @Override
        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

        }

        @Override
        public String getPartsSeriesName() {
            return SERIES_NAME;
        }

        @Override
        public String getDefaultColor() {
            return DEFAULT_COLOR;
        }
    }
}
