package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.SiriusItemRenderer;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class SiriusItem {
    public static String SERIES_NAME = "sirius";
    private static final String DEFAULT_COLOR = "white";

    public static class Head extends BasePartsItem.Head {
        public Head(Properties properties) {
            super(properties);
        }

        @Override
        public SiriusItemRenderer.Head newRenderer() {
            return new SiriusItemRenderer.Head();
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
        public SiriusItemRenderer.Body newRenderer() {
            return new SiriusItemRenderer.Body();
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

    public static class Arm extends BasePartsItem.Arm {
        public Arm(Properties properties) {
            super(properties);
        }

        @Override
        public SiriusItemRenderer.Arm newRenderer() {
            return new SiriusItemRenderer.Arm();
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
        public SiriusItemRenderer.Legs newRenderer() {
            return new SiriusItemRenderer.Legs();
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
