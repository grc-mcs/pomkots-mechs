package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.TsurugiItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class TsurugiItem extends BasePartsItem.WeaponArm {
    public TsurugiItem(Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();

        if (tick == 2) {
            if (!world.isClientSide) {
                var angle = mechInterface.getShootingAngle(mechInterface.getMechEntity(), true);
                mechInterface.addVelocity(angle[0], angle[1], 5F);

            } else {
                mechInterface.playSoundEffect(PomkotsMechs.SE_SABER.get());
            }
        }

        if (isOnFire) {
            if (!mechInterface.consumeEnergy(50)) {
                return;
            }

            Entity driver = mechInterface.getDrivingPassenger();
            var pilePos1 = new Vec3(6.5 * mechInterface.isRight(), 4.0F, 18F).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot())).add(mechInterface.position());
            var pilePos2 = new Vec3(-2 * mechInterface.isRight(), -4F, -4F).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot())).add(mechInterface.position());

            var kbVel = new Vec3(0, 0, -1F).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot()));
            for (var ent : world.getEntities(null, new AABB(pilePos1, pilePos2))) {
                if (mechInterface.isSelf(ent)) {
                    continue;
                }

                if (ent instanceof LivingEntity le) {
                    if (!world.isClientSide()) {
                        le.knockback(2, kbVel.x, kbVel.z);

                        DamageSource ds;
                        if (driver instanceof Player p) {
                            ds = mechInterface.damageSources().playerAttack(p);
                        } else {
                            ds = mechInterface.damageSources().generic();
                        }

                        if (le instanceof BossHitBoxEntity hit) {
                            hit.addStunPoint(30);
                        }
                        le.hurt(ds, this.getDamage(mechInterface.getItemStack()));
                    } else {
                        mechInterface.addHitParticles(le);
                    }
                }
            }
        }
    }

    @Override
    public void startUsing(ActionWeapon.WeaponMechInterface mechInterface) {
        if (!mechInterface.getWorld().isClientSide && mechInterface.getPlayer() != null && mechInterface.getItemStack() != null) {
            triggerAnim(mechInterface.getPlayer(), GeoItem.getOrAssignId(mechInterface.getItemStack(), (ServerLevel) mechInterface.getWorld()), "Activation", "use");
        }
    }

    @Override
    public void endUsing(ActionWeapon.WeaponMechInterface mechInterface) {
        if (!mechInterface.getWorld().isClientSide && mechInterface.getPlayer() != null && mechInterface.getItemStack() != null) {
            triggerAnim(mechInterface.getPlayer(), GeoItem.getOrAssignId(mechInterface.getItemStack(), (ServerLevel) mechInterface.getWorld()), "Activation", "stop");
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
    public void inventoryTick(ItemStack stack, Level level, Entity ent, int slotIndex, boolean bl) {
        super.inventoryTick(stack, level, ent, slotIndex, bl);

        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            GeoItem.getOrAssignId(stack, serverLevel);
        }
    }

    @Override
    public TsurugiItemRenderer newRenderer() {
        return new TsurugiItemRenderer();
    }

    @Override
    public WeaponAttachPoint getWeaponAttachPoint() {
        return WeaponAttachPoint.ATTACH_POINT_ARM;
    }

    @Override
    public WeaponCategory getWeaponCategory() {
        return WeaponCategory.MELEE;
    }

    @Override
    public Motion getMotion() {
        return Motion.SABER;
    }

    @Override
    public int getCoolTime() {
        return 60;
    }

    @Override
    public String getPartsSeriesName() {
        return "tsurugi";
    }
}
