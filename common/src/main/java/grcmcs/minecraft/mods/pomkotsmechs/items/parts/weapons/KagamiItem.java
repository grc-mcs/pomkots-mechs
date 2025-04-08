package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.KagamiItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineGrenadeItem;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class KagamiItem extends BasePartsItem.WeaponArm {
    public KagamiItem(Properties properties) {
        super(properties);
    }

    @Override
    public KagamiItemRenderer newRenderer() {
        return new KagamiItemRenderer();
    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();

        if (isOnFire) {
            if (!mechInterface.consumeAmmo(1)) {
                return;
            }

            if (!world.isClientSide()) {
                BulletGrenadeEntity be = new BulletGrenadeEntity(PomkotsMechs.BULLET_GRENADE.get(), world, mechInterface.getMechEntity(), this.getDamage(mechInterface.getItemStack()));

                var offset = mechInterface.getOffset();

                // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                var muzzlPos = new Vec3(1.6 * mechInterface.isRight(), 5.2, 4F);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot()));
                be.setPos(offset.add(muzzlPos));

                float[] angle = mechInterface.getShootingAngle(be, true);

                be.shootFromRotation(be, angle[0], angle[1], mechInterface.getFallFlyingTicks(), BattleBalance.MECH_BULLET_GRENADE_SPEED, 0F);

                world.addFreshEntity(be);

                mechInterface.knockBack(5F);
            } else {
                mechInterface.playSoundEffect(PomkotsMechs.SE_GRENADE_EVENT.get());
            }
        }
    }

    @Override
    public boolean isMatchAmmo(Magazine mag) {
        return mag instanceof MagazineGrenadeItem;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public String getWeaponAttachPoint() {
        return WeaponInterface.ATTACH_POINT_HAND;
    }

    @Override
    public Motion getMotion() {
        return Motion.GRENADE_HAND;
    }

    @Override
    public int getCoolTime() {
        return 100;
    }

    @Override
    public String getPartsSeriesName() {
        return "kagami";
    }
}
