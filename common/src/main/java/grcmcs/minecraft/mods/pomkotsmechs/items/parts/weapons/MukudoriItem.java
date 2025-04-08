package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.MukudoriItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineMissileItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class MukudoriItem extends BasePartsItem.WeaponShoulder {
    public MukudoriItem(Properties properties) {
        super(properties);
    }

    @Override
    public MukudoriItemRenderer newRenderer() {
        return new MukudoriItemRenderer();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();

        if (isOnFire) {
            if (!world.isClientSide()) {
                var target = mechInterface.consumeMultiLockTargetsSingle();

                if (target != null && mechInterface.consumeAmmoFromServerSide(1)) {
                    var offset = mechInterface.getOffset();

                    // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                    var muzzlPos = new Vec3(1.4 * mechInterface.isRight(), 4.8F, 2F);

                    int i = tick / 2 - 1;
                    if (i < 3) {
                        var worldMuzzlPos = muzzlPos.add(mechInterface.isRight() * 1.4,1 - 0.5 * (i % 3),0).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot()));

                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), world, mechInterface.getMechEntity(), (LivingEntity) target, this.getDamage(mechInterface.getItemStack()), BattleBalance.MECH_MISSILE_GENERIC_SPEED);

                        be.setPos(offset.add(worldMuzzlPos));

                        be.shootFromRotation(be, 0, mechInterface.getYRot() - 45 * mechInterface.isRight(), mechInterface.getFallFlyingTicks(), BattleBalance.MECH_MISSILE_GENERIC_SPEED, 0F);

                        world.addFreshEntity(be);

                    } else if (i < 6) {
                        var worldMuzzlPos = muzzlPos.add(0.5 * mechInterface.isRight() * (i % 3),0,0).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot()));

                        MissileGenericEntity be = new MissileGenericEntity(PomkotsMechs.MISSILE_GENERIC.get(), world, mechInterface.getMechEntity(), (LivingEntity) target, this.getDamage(mechInterface.getItemStack()), BattleBalance.MECH_MISSILE_GENERIC_SPEED);

                        be.setPos(offset.add(worldMuzzlPos));

                        be.shootFromRotation(be, -45, mechInterface.getYRot(), mechInterface.getFallFlyingTicks(), BattleBalance.MECH_MISSILE_GENERIC_SPEED, 0F);

                        world.addFreshEntity(be);

                    }
                }
            } else {
                int i = tick / 2 - 1;
                if (i == 0) {
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
    public String getWeaponAttachPoint() {
        return WeaponInterface.ATTACH_POINT_SHOULDER;
    }

    @Override
    public Motion getMotion() {
        return Motion.MULTI_MISSILE;
    }

    @Override
    public int maxMultiLockNum() {
        return 6;
    }

    @Override
    public int getCoolTime() {
        return 100;
    }

    @Override
    public String getPartsSeriesName() {
        return "mukudori";
    }

    @Override
    public String getWeaponCategory() {
        return "missile";
    }
}
