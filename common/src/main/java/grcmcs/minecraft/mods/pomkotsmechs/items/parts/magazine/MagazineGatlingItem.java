package grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.magazine.MagazineItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class MagazineGatlingItem extends BasePartsItem.Magazine {

    public MagazineGatlingItem(Properties properties) {
        super(properties);
    }

    @Override
    public MagazineItemRenderer newRenderer() {
        return new MagazineItemRenderer();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public String getPartsSeriesName() {
        return "magazinegatling";
    }
}
