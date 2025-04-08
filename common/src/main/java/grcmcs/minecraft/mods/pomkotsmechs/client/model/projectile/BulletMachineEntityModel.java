package grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BulletEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletMachineEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class BulletMachineEntityModel extends GeoModel<BulletMachineEntity> {
    @Override
    public ResourceLocation getAnimationResource(BulletMachineEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/projectile/bulletmachine.animation.json");
    }

    @Override
    public ResourceLocation getModelResource(BulletMachineEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/projectile/bulletmachine.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BulletMachineEntity animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/projectile/bulletmachine.png");
    }
}
