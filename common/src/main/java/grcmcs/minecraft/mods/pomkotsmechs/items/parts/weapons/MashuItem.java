package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.MashuItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletBeamEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class MashuItem extends BasePartsItem.WeaponArm {
    public MashuItem(Properties properties) {
        super(properties);
    }

    @Override
    public MashuItemRenderer newRenderer() {
        return new MashuItemRenderer();
    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();

        if (isOnFire) {
            if (!mechInterface.consumeEnergy(50)) {
                return;
            }

            if (!world.isClientSide()) {
                BulletBeamEntity be = new BulletBeamEntity(PomkotsMechs.BULLET_BEAM.get(), world, mechInterface.getMechEntity(), this.getDamage(mechInterface.getItemStack()));

                var offset = mechInterface.getOffset();

                // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                var muzzlPos = new Vec3(1.8 * mechInterface.isRight(), 3.0F, 5F);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot()));
                be.setPos(offset.add(muzzlPos));

                float[] angle = mechInterface.getShootingAngle(be, true);

                be.shootFromRotation(be, angle[0], angle[1], mechInterface.getFallFlyingTicks(), 7, 0);

                world.addFreshEntity(be);

            } else {
                mechInterface.playSoundEffect(PomkotsMechs.SE_RIFLE.get());
            }
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public WeaponAttachPoint getWeaponAttachPoint() {
        return WeaponAttachPoint.ATTACH_POINT_HAND;
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
        return 20;
    }

    @Override
    public String getPartsSeriesName() {
        return "mashu";
    }
}
