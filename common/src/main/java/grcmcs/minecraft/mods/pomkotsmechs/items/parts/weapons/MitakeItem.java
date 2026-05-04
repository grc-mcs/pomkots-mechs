package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.KagenobuItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.MitakeItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class MitakeItem extends BasePartsItem.WeaponShoulder {
    public MitakeItem(Properties properties) {
        super(properties);
    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();

        if (isOnFire) {
            Entity driver = mechInterface.getDrivingPassenger();

            int radius = 5;
            var pos = mechInterface.position();

            var aabbPos1 = new Vec3(-radius/2F + mechInterface.isRight(), -radius/2F, 0).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot())).add(mechInterface.position());
            var aabbPos2 = new Vec3(radius/2F + mechInterface.isRight(), radius, radius * 2).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot())).add(mechInterface.position());

            AABB box = new AABB(
                    aabbPos1,
                    aabbPos2
            );
            for (var ent : world.getEntities(null, box)) {
                if (mechInterface.isSelf(ent)) {
                    continue;
                }

                if (ent instanceof LivingEntity le) {
                    if (!world.isClientSide()) {
                        Vec3 kbVel = ent.position().subtract(pos).normalize();
                        le.knockback(4, -kbVel.x * 2, -kbVel.z * 2);

                        DamageSource ds;
                        if (driver instanceof Player p) {
                            ds = mechInterface.damageSources().playerAttack(p);
                        } else {
                            ds = mechInterface.damageSources().generic();
                        }
                        var baseDamage = this.getDamage(mechInterface.getItemStack());

                        if (le instanceof BossHitBoxEntity hit) {
                            hit.addStunPoint(30);
                        }
                        le.hurt(ds, mechInterface.isSuperBoost()? baseDamage * 2F: baseDamage);
                    } else {
                        mechInterface.addHitParticles(le);
                    }
                }
            }

            if (world.isClientSide) {
                mechInterface.playSoundEffect(PomkotsMechs.SE_PILEBUNKER_EVENT.get());
            }
        }
    }

    @Override
    public MitakeItemRenderer newRenderer() {
        return new MitakeItemRenderer();
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
        return Motion.SHOULDER_BLADE;
    }

    @Override
    public int getCoolTime() {
        return 40;
    }

    @Override
    public String getPartsSeriesName() {
        return "mitake";
    }
}
