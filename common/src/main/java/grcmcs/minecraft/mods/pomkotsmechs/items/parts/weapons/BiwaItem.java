package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.BiwaItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineGrenadeItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class BiwaItem extends BasePartsItem.WeaponShoulder{
    public BiwaItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
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
                var muzzlPos = new Vec3(1.6 * mechInterface.isRight(), 5.5, 4F);
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
    public BiwaItemRenderer newRenderer() {
        return new BiwaItemRenderer();
    }

    @Override
    public void startUsing(ActionWeapon.WeaponMechInterface context) {
        if (!context.getWorld().isClientSide && context.getPlayer() != null && context.getItemStack() != null) {
            triggerAnim(context.getPlayer(), GeoItem.getOrAssignId(context.getItemStack(), (ServerLevel) context.getWorld()), "Activation", "use");
        }
    }

    @Override
    public void endUsing(ActionWeapon.WeaponMechInterface context) {
        if (!context.getWorld().isClientSide && context.getPlayer() != null && context.getItemStack() != null) {
            triggerAnim(context.getPlayer(), GeoItem.getOrAssignId(context.getItemStack(), (ServerLevel) context.getWorld()), "Activation", "stop");
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Activation", 0, state -> PlayState.STOP)
                .triggerableAnim("use", RawAnimation.begin().thenPlay("animation.weapon.use"))
                .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.weapon.idle"))
        );
    }

    @Override
    public String getWeaponAttachPoint() {
        return WeaponInterface.ATTACH_POINT_SHOULDER;
    }

    @Override
    public Motion getMotion() {
        return Motion.GRENADE;
    }

    @Override
    public int getCoolTime() {
        return 100;
    }

    @Override
    public String getPartsSeriesName() {
        return "biwa";
    }
}
