package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.TransportShipEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class TransportShipEntityModel extends GeoModel<TransportShipEntity> {
    @Override
    public ResourceLocation getModelResource(TransportShipEntity animatable) {
        return PomkotsMechs.id("geo/transport_ship.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TransportShipEntity animatable) {
        return PomkotsMechs.id("textures/entity/transport_ship.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TransportShipEntity animatable) {
        return PomkotsMechs.id("animations/transport_ship.animation.json");
    }
}
