package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.AltairItemRenderer;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class AltairItem {
    public static String SERIES_NAME = "altair";
    private static final String DEFAULT_COLOR = "red";

    public static class Head extends BasePartsItem.Head {
        public Head(Properties properties) {
            super(properties);
        }

        @Override
        public AltairItemRenderer.Head newRenderer() {
            return new AltairItemRenderer.Head();
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

    public static class Body extends BasePartsItem.Body {
        public Body(Properties properties) {
            super(properties);
        }

        @Override
        public AltairItemRenderer.Body newRenderer() {
            return new AltairItemRenderer.Body();
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
        public AltairItemRenderer.Arm newRenderer() {
            return new AltairItemRenderer.Arm();
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
        public AltairItemRenderer.Legs newRenderer() {
            return new AltairItemRenderer.Legs();
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
