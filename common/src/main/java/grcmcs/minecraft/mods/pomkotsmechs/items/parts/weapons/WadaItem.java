package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.WadaItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class WadaItem extends BasePartsItem.WeaponArm {
    public WadaItem(Properties properties) {
        super(properties);
    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();

        if (isOnFire) {
            var mech = mechInterface.getMechEntity();
            if (mechInterface.isToggleOnStart()) {
                if (world.isClientSide) {
                    mechInterface.playSoundEffect(PomkotsMechs.SE_LIFT.get());
                } else {
                    Entity p = findFirstEntityInFront3x3x3(mech);
                    if (p != null && isTargetClass(p)) {
                        var pos = mech.position();
                        p.setPos(pos.x, pos.y + 7.5, pos.z);
                        p.setNoGravity(true);
                        mech.setHavingEntity(Optional.of(p.getUUID()));
                    } else {
                        mech.setHavingEntity(Optional.empty());
                    }
                }
            } else {
                if (world.isClientSide) {
                    mechInterface.playSoundEffect(PomkotsMechs.SE_THROW.get());
                } else {
                    Entity p = ((ServerLevel)world).getEntity(mech.havingEntity().get());
                    if (p != null) {
                        p.invulnerableTime = 20;
                        var muzzlPos = new Vec3(0, 0, 6);
                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot()));
                        p.setPos(mech.position().add(muzzlPos));
                        p.setNoGravity(false);
                    }
                    mech.setHavingEntity(Optional.empty());
                }
            }
        }
    }

    public static void shootFromRotation(Entity ent, Entity entity, float f, float g, float h, float i, float j) {
        float k = -Mth.sin(g * 0.017453292F) * Mth.cos(f * 0.017453292F);
        float l = -Mth.sin((f + h) * 0.017453292F);
        float m = Mth.cos(g * 0.017453292F) * Mth.cos(f * 0.017453292F);
        shoot(ent, (double)k, (double)l, (double)m, i, j);
        Vec3 vec3 = entity.getDeltaMovement();
        ent.setDeltaMovement(ent.getDeltaMovement().add(vec3.x, entity.onGround() ? 0.0 : vec3.y, vec3.z));
    }

    public static void shoot(Entity ent, double d, double e, double f, float g, float h) {
        Vec3 vec3 = (new Vec3(d, e, f)).normalize().scale((double)g);
        ent.setDeltaMovement(vec3);
        double i = vec3.horizontalDistance();
        ent.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 57.2957763671875));
        ent.setXRot((float)(Mth.atan2(vec3.y, i) * 57.2957763671875));
        ent.yRotO = ent.getYRot();
        ent.xRotO = ent.getXRot();
    }

    public static Entity findFirstEntityInFront3x3x3(Entity self) {
        Level level = self.level();

        Vec3 forward = Vec3.directionFromRotation(0, self.getYRot());

        Vec3 center = self.position()
                .add(0, 0, 0)
                .add(forward.scale(5));

        AABB box = new AABB(
                center.x - 1.5, center.y - 0, center.z - 1.5,
                center.x + 1.5, center.y + 3, center.z + 1.5
        );

        List<Entity> list = level.getEntities(
                self,
                box,
                e -> e.isAlive() && e != self
        );

        return list.isEmpty() ? null : list.get(0);
    }

    private boolean isTargetClass(Entity e) {
       boolean onBlackList =
               e instanceof BossHitBoxEntity
               || e instanceof EnderDragon
               || e instanceof EnderDragonPart;

        return !onBlackList;
    }

    @Override
    public boolean isToggleOn(ActionWeapon.WeaponMechInterface context) {
        return context.getMechEntity() != null && context.getMechEntity().havingEntity().isPresent();
    }
// Christmas Edition
//    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
//        var world = mechInterface.getWorld();
//
//        if (isOnFire) {
//            var mech = mechInterface.getMechEntity();
//            if (mech.havingEntity().isEmpty()) {
//                if (world.isClientSide) {
//                    mechInterface.playSoundEffect(PomkotsMechs.SE_LIFT.get());
//                } else {
//                    PresentBoxEntity p = new PresentBoxEntity(PomkotsMechs.PRESENT_BOX.get(), world);
//                    var pos = mech.position();
//                    p.setPos(pos.x, pos.y + 7.5, pos.z);
//                    p.setNoGravity(true);
//                    world.addFreshEntity(p);
//                    mech.setHavingEntity(Optional.of(p.getUUID()));
//                }
//            } else {
//                if (world.isClientSide) {
//                    mechInterface.playSoundEffect(PomkotsMechs.SE_THROW.get());
//                } else {
//                    PresentBoxEntity p = (PresentBoxEntity) ((ServerLevel)world).getEntity(mech.havingEntity().get());
//                    if (p != null) {
//                        var muzzlPos = new Vec3(0, 0, 6);
//                        muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot()));
//                        p.setPos(mech.position().add(muzzlPos));
//                        p.setNoGravity(false);
//                    }
//                    mech.setHavingEntity(Optional.empty());
//                }
//
//            }
//        }
//    }

    @Override
    public WadaItemRenderer newRenderer() {
        return new WadaItemRenderer();
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
        return Motion.RAISE_THROW;
    }

    @Override
    public int getCoolTime() {
        return 20;
    }

    @Override
    public String getPartsSeriesName() {
        return "wada";
    }
}
