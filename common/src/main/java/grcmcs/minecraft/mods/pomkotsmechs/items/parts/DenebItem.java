package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.DenebItemRenderer;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class DenebItem {
    public static String SERIES_NAME = "deneb";
    private static final String DEFAULT_COLOR = "blue";

    public static class Head extends BasePartsItem.Head {
        public Head(Properties properties) {
            super(properties);
        }

        @Override
        public DenebItemRenderer.Head newRenderer() {
            return new DenebItemRenderer.Head();
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
        public DenebItemRenderer.Body newRenderer() {
            return new DenebItemRenderer.Body();
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
        public DenebItemRenderer.Arm newRenderer() {
            return new DenebItemRenderer.Arm();
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
        public DenebItemRenderer.Legs newRenderer() {
            return new DenebItemRenderer.Legs();
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
