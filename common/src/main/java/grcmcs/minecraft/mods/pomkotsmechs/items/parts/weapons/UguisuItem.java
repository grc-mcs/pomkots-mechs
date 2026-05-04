package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.UguisuItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineMissileItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class UguisuItem extends BasePartsItem.WeaponArm {
    public UguisuItem(Properties properties) {
        super(properties);
    }

    @Override
    public UguisuItemRenderer newRenderer() {
        return new UguisuItemRenderer();
    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();

        if (isOnFire) {
            if (!world.isClientSide) {
                var offset = mechInterface.getOffset();

                // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                var muzzlPos = new Vec3(1.8 * mechInterface.isRight(), 3.0F, 5F);

                var targets = mechInterface.consumeMultiLockTargets();
                for (int i = 0; i < targets.size(); i++) {

                    if (mechInterface.consumeAmmoFromServerSide(1)) {
                        var worldMuzzlPos = muzzlPos.add(1 * (i / 2 - 0.5),1 * (i % 2),0).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot()));

                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), world, mechInterface.getMechEntity(),
                                (LivingEntity) targets.get(i), this.getDamage(mechInterface.getItemStack()),
                                BattleBalance.MECH_MISSILE_GENERIC_SPEED2);

                        be.setPos(offset.add(worldMuzzlPos));
                        be.setMaxRotationAnglePerTick(1);

                        be.shootFromRotation(be, 0, mechInterface.getYRot(), mechInterface.getFallFlyingTicks(),
                                BattleBalance.MECH_MISSILE_GENERIC_SPEED2, 0F);

                        world.addFreshEntity(be);
                    }
                }
            } else {
                if (tick == 2) {
                    mechInterface.playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get());
                }
            }
        }
    }

    @Override
    public boolean isMatchAmmo(Magazine mag) {
        return mag instanceof MagazineMissileItem;
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
        return WeaponCategory.MISSILE;
    }

    @Override
    public int maxMultiLockNum() {
        return 4;
    }

    @Override
    public Motion getMotion() {
        return Motion.HAND_MISSILE;
    }

    @Override
    public int getCoolTime() {
        return 60;
    }

    @Override
    public String getPartsSeriesName() {
        return "uguisu";
    }
}
