package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.KintokiItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.GiantTomahawkEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.world.phys.Vec3;

public class KintokiItem extends BasePartsItem.WeaponShoulder {
    public KintokiItem(Properties properties) {
        super(properties);
    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();

        if (isOnFire) {
            if (!world.isClientSide()) {
                GiantTomahawkEntity be = new GiantTomahawkEntity(
                        PomkotsMechs.GIANT_TOMAHAWK.get(),
                        world,
                        mechInterface.getMechEntity(),
                        mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory())
                );
                var offset = mechInterface.getOffset();

                // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                var muzzlPos = new Vec3(1.8 * mechInterface.isRight(), 3.0F, 6.5F);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot()));
                be.setPos(offset.add(muzzlPos));

                float[] angle = mechInterface.getShootingAngle(be, true);

                be.shootFromRotation(be, angle[0], angle[1], mechInterface.getFallFlyingTicks(), 4, 0);
                be.setFlightYaw(angle[1]);

                world.addFreshEntity(be);

            } else {
                mechInterface.playSoundEffect(PomkotsMechs.SE_RELOAD.get());
            }
        }
    }

    @Override
    public KintokiItemRenderer newRenderer() {
        return new KintokiItemRenderer();
    }

    @Override
    public WeaponAttachPoint getWeaponAttachPoint() {
        return WeaponAttachPoint.ATTACH_POINT_SHOULDER;
    }

    @Override
    public WeaponCategory getWeaponCategory() {
        return WeaponCategory.MELEE;
    }

    @Override
    public Motion getMotion() {
        return Motion.TOMAHAWK;
    }

    @Override
    public int getCoolTime() {
        return 40;
    }

    @Override
    public String getPartsSeriesName() {
        return "kintoki";
    }
    
    @Override
    public boolean isSoftLockEnabled() {
        return true;
    }
}
