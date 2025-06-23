package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ProjectileUtil {
    public static boolean isDestructionAllowed(Entity ent) {
        return Utils.isBlockDestructionAllowed(ent);
    }

    public static HitResult raycastBoundingCheck(Projectile entity) {
        // 弾速が早すぎると、ティック間にすり抜けちゃうのでレイキャスティングで補完
        Vec3 currentPosition = entity.position();
        Vec3 nextPosition = currentPosition.add(entity.getDeltaMovement());

        HitResult hitResult = entity.level().clip(new ClipContext(
                currentPosition,
                nextPosition,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
        ));

        return hitResult;
    }

    public static void hurt(Entity target, PomkotsThrowableProjectile prj, Entity cause, float damage) {
        if (target instanceof PomkotsVehicleBase v1 && cause instanceof PomkotsVehicleBase v2) {
            var d1 = v1.getDrivingPassenger();
            var d2 = v2.getDrivingPassenger();

            if (d1 != null && d2 !=null && d1.isAlliedTo(d2)) {
                if (d1.getTeam() != null && !d1.getTeam().isAllowFriendlyFire()) {
                    return;
                }
            }
        }

        Entity rootCause = null;
        if (cause instanceof  PomkotsVehicleBase vehicle) {
            var driver = vehicle.getDrivingPassenger();
            if (driver != null) {
                rootCause = driver;
            } else {
                rootCause = cause;
            }
        } else {
            rootCause = cause;
        }

        DamageSource ds;

        if (prj == null) {
            if (rootCause instanceof Player p) {
                ds = cause.damageSources().playerAttack(p);
            } else {
                ds = cause.damageSources().generic();
            }
        } else {
            if (rootCause == null) {
                rootCause = prj;
            }
            ds = target.damageSources().thrown(prj, rootCause);
        }

        target.hurt(ds, damage);
    }
}
