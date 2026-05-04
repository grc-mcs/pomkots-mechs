package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.ShinobazuItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletMachineEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineMachineGunItem;
import net.minecraft.world.phys.Vec3;

public class ShinobazuItem extends BasePartsItem.WeaponArm {
    public ShinobazuItem(Properties properties) {
        super(properties);
    }

    @Override
    public ShinobazuItemRenderer newRenderer() {
        return new ShinobazuItemRenderer();
    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();
        if (isOnFire) {
            if (!mechInterface.consumeAmmo(1)) {
                return;
            }

            if (!world.isClientSide()) {
                BulletMachineEntity be = new BulletMachineEntity(
                        PomkotsMechs.BULLET_MACHINE.get(), world,
                        mechInterface.getMechEntity(),
                        this.getDamage(mechInterface.getItemStack()));

                var offset = mechInterface.getOffset();

                // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                var muzzlPos = new Vec3(1.8 * mechInterface.isRight(), 3.0F, 5F);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot()));
                be.setPos(offset.add(muzzlPos));

                float[] angle = mechInterface.getShootingAngle(be, true);

                be.shootFromRotation(be, angle[0], angle[1], mechInterface.getFallFlyingTicks(), 7, 2F);

                world.addFreshEntity(be);
            } else {
                if (tick % 5 == 1) {
                    mechInterface.playSoundEffect(PomkotsMechs.SE_GATLING_EVENT.get());
                }
            }
        }
    }

    @Override
    public boolean isMatchAmmo(Magazine mag) {
        return mag instanceof MagazineMachineGunItem;
    }

    @Override
    public WeaponAttachPoint getWeaponAttachPoint() {
        return WeaponAttachPoint.ATTACH_POINT_HAND;
    }

    @Override
    public WeaponCategory getWeaponCategory() {
        return WeaponCategory.MACHINE_GUN;
    }

    @Override
    public Motion getMotion() {
        return Motion.SUB_MACHINE_GUN;
    }

    @Override
    public int getCoolTime() {
        return 20;
    }

    @Override
    public String getPartsSeriesName() {
        return "shinobazu";
    }
}
