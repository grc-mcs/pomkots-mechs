package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.magazine;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.*;
import net.minecraft.resources.ResourceLocation;

public class MagazineItemModel extends BasePartsItemModel<BasePartsItem.Magazine> {
    @Override
    public ResourceLocation getModelResource(BasePartsItem.Magazine animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "geo/magazine.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BasePartsItem.Magazine animatable) {
        if (animatable instanceof MagazineRifleItem) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/magazine/magazine_1.png");

        } else if (animatable instanceof MagazineShotGunItem) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/magazine/magazine_2.png");

        } else if (animatable instanceof MagazineMachineGunItem) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/magazine/magazine_3.png");

        } else if (animatable instanceof MagazineGatlingItem) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/magazine/magazine_4.png");

        } else if (animatable instanceof MagazineMissileItem) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/magazine/magazine_5.png");

        } else if (animatable instanceof MagazineMissileLargeItem) {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/magazine/magazine_6.png");

        }

        return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/magazine/magazine_7.png");
    }
}
