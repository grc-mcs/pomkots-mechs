package grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension.HoverUnitItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.extension.SBUnitProtoTypeItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class SBUnitProtoTypeItem extends BasePartsItem.Extension {

    public SBUnitProtoTypeItem(Properties properties) {
        super(properties);
    }

    @Override
    public SBUnitProtoTypeItemRenderer newRenderer() {
        return new SBUnitProtoTypeItemRenderer();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public String getPartsSeriesName() {
        return "protosbunit";
    }
}
