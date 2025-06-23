package grcmcs.minecraft.mods.pomkotsmechs.client.model.turret;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt03Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt04Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pmt04EntityModel extends GeoModel<Pmt04Entity> {
    @Override
    public ResourceLocation getAnimationResource(Pmt04Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/pmt04.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(Pmt04Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/pmt04.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Pmt04Entity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/pms01.png");
    }
}
