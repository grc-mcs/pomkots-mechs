package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.TakaoItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;

public class TakaoItem extends BasePartsItem.WeaponArm {
    public TakaoItem(Properties properties) {
        super(properties);
    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();

        if (isOnFire) {
            Entity driver = mechInterface.getDrivingPassenger();
            var pilePos1 = new Vec3(6.5 * mechInterface.isRight(), 4.0F, 18F).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot())).add(mechInterface.position());
            var pilePos2 = new Vec3(-6.5 * mechInterface.isRight(), -4F, -4F).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot())).add(mechInterface.position());

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
                        float damage = this.getDamage(mechInterface.getItemStack());

                        var chargeModifier = 1;
                        if (tick > 60) {
                            chargeModifier = 3;
                        } else if (tick > 40) {
                            chargeModifier = 2;
                        }

                        if (le instanceof BossHitBoxEntity hit) {
                            hit.addStunPoint(10 * chargeModifier);
                        }
                        le.hurt(ds, damage * chargeModifier);
                    } else {
                        mechInterface.addHitParticles(le);
                    }
                }
            }

            if (world.isClientSide) {
                mechInterface.playSoundEffect(PomkotsMechs.SE_PILEBUNKER_EVENT.get());

            } else if (Utils.isBlockDestructionAllowed(mechInterface.getMechEntity())) {
                int rad = 5;
                if (tick > 60) {
                    rad = 15;
                } else if (tick > 40) {
                    rad = 10;
                }

                if (mechInterface.onGround()) {
                    mechInterface.breakBlocksCube(rad);
                } else {
                    mechInterface.breakBlockSphere(rad, new Vec3(0,0,5));
                }
            }
        }
    }

    @Override
    public TakaoItemRenderer newRenderer() {
        return new TakaoItemRenderer();
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
        return WeaponCategory.MELEE;
    }

    @Override
    public Motion getMotion() {
        return Motion.CHARGE_HUMMER;
    }

    @Override
    public int getCoolTime() {
        return 60;
    }

    @Override
    public String getPartsSeriesName() {
        return "takao";
    }
}
