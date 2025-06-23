package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public abstract class PomkotsThrowableProjectile extends ThrowableProjectile {
    private LivingEntity shooter;

    public PomkotsThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, LivingEntity shooter, Level world) {
        super(entityType, world);
        this.shooter = shooter;
    }

    public void onHitEntityPublic(Entity entity) {
        this.onHitEntity(new EntityHitResult(entity));
    }

    public float getHitDamage() {
        return 0;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    public LivingEntity getShooter() {
        return this.shooter;
    }
}
