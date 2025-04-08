package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.entity.Entity;
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
}
