package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectileUtil {
    public static boolean isDestructionAllowed(Entity ent) {
        return !ent.level().isClientSide && PomkotsMechs.CONFIG.enableEntityBlockDestruction;
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
