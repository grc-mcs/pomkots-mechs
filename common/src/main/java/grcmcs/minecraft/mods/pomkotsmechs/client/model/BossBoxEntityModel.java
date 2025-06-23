package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.AlertEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.AlertRedEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BossBoxEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BossBoxEntityModel extends GeoModel<BossBoxEntity> {
    @Override
    public ResourceLocation getAnimationResource(BossBoxEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/bossbox.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(BossBoxEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/bossbox.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BossBoxEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/bossbox.png");
    }
}
