package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.DaigomaruItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.KasumiItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletMachineEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineGatlingItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class DaigomaruItem extends BasePartsItem.WeaponArm {
    public DaigomaruItem(Properties properties) {
        super(properties);
    }

    @Override
    public DaigomaruItemRenderer newRenderer() {
        return new DaigomaruItemRenderer();
    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();

        if (isOnFire) {
            if (!world.isClientSide()) {
                var player = mechInterface.getPlayer();
                if (player != null) {
                    var hit = player.pick(100, 0, true);

                    if (hit.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult bh = (BlockHitResult) hit;
                        Utils.destroyBlock(world, bh.getBlockPos(), PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
                    }
                }
            } else {
                mechInterface.playSoundEffect(PomkotsMechs.SE_NEEDLE.get());
            }
        }
    }

    @Override
    public WeaponAttachPoint getWeaponAttachPoint() {
        return WeaponAttachPoint.ATTACH_POINT_ARM;
    }

    @Override
    public WeaponCategory getWeaponCategory() {
        return WeaponCategory.RIFLE;
    }

    @Override
    public Motion getMotion() {
        return Motion.RIFLE;
    }

    @Override
    public int getCoolTime() {
        return 11;
    }

    @Override
    public String getPartsSeriesName() {
        return "daigomaru";
    }
}
