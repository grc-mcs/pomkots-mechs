package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.AldebaranItemRenderer;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class AldebaranItem {
    public static String SERIES_NAME = "aldebaran";
    private static final String DEFAULT_COLOR = "green";

    public static class Head extends BasePartsItem.Head {
        public Head(Properties properties) {
            super(properties);
        }

        @Override
        public AldebaranItemRenderer.Head newRenderer() {
            return new AldebaranItemRenderer.Head();
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
        public AldebaranItemRenderer.Body newRenderer() {
            return new AldebaranItemRenderer.Body();
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
        public AldebaranItemRenderer.Arm newRenderer() {
            return new AldebaranItemRenderer.Arm();
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
        public AldebaranItemRenderer.Legs newRenderer() {
            return new AldebaranItemRenderer.Legs();
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
