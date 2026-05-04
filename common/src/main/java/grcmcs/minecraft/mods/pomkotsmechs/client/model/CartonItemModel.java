package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.CartonItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CartonItemModel extends GeoModel<CartonItem> {
    @Override
    public ResourceLocation getAnimationResource(CartonItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/carton.animation.json");
    }

    @Override
    public ResourceLocation getTextureResource(CartonItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/item/carton.png");

    }

    @Override
    public ResourceLocation getModelResource(CartonItem animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/carton.geo.json");
    }
}
