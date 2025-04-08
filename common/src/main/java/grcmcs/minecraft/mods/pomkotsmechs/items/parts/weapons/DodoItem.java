package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.BiwaItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.DodoItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericLargeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineGrenadeItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineMissileLargeItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class DodoItem extends BasePartsItem.WeaponShoulder{
    public DodoItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();

        if (isOnFire) {
            if (!world.isClientSide()) {
                var target = mechInterface.consumeMultiLockTargetsSingle();

                if (target != null && mechInterface.consumeAmmoFromServerSide(1)) {
                    var offset = mechInterface.getOffset();

                    // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                    var muzzlPos = new Vec3(1.4 * mechInterface.isRight(), 4.8F, 2F);
                    var worldMuzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot()));

                    MissileGenericLargeEntity be = new MissileGenericLargeEntity(PomkotsMechs.MISSILE_GENERIC_LARGE.get(), world, mechInterface.getMechEntity(), (LivingEntity) target, this.getDamage(mechInterface.getItemStack()), BattleBalance.MECH_MISSILE_GENERIC_SPEED);

                    be.setPos(offset.add(worldMuzzlPos));

                    be.shootFromRotation(be, -80, mechInterface.getYRot(), mechInterface.getFallFlyingTicks(), BattleBalance.MECH_MISSILE_GENERIC_SPEED, 0F);

                    world.addFreshEntity(be);

                }
            } else {
                if (tick == 4) {
                    mechInterface.playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get());
                }
            }
        }
    }

    @Override
    public boolean isMatchAmmo(Magazine mag) {
        return mag instanceof MagazineMissileLargeItem;
    }

    @Override
    public int maxMultiLockNum() {
        return 1;
    }

    @Override
    public DodoItemRenderer newRenderer() {
        return new DodoItemRenderer();
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
        return Motion.LARGE_MISSILE;
    }

    @Override
    public int getCoolTime() {
        return 100;
    }

    @Override
    public String getPartsSeriesName() {
        return "dodo";
    }
}
