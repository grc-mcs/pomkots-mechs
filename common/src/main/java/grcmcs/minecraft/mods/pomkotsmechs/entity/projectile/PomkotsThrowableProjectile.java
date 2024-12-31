package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public abstract class PomkotsThrowableProjectile extends ThrowableProjectile {
    public PomkotsThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public void onHitEntityPublic(Entity entity) {
        this.onHitEntity(new EntityHitResult(entity));
    }
}
