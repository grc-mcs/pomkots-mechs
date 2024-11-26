package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MissileVerticalEntity extends MissileBaseEntity {
    public MissileVerticalEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    public MissileVerticalEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, LivingEntity target) {
        this(entityType, world);
        this.shooter = shooter;
        this.target = target;
    }

    protected int getSwitchTick() {
        return 7;
    }

    protected double getSpeed() {
        return 2;
    }

    protected int getSeekRange() {
        return 40;
    }

    protected float getMaxRotationAnglePerTick() {
        return 10;
    }

    protected float getDamage() {
        return BattleBalance.MECH_MISSILE_DAMAGE;
    }

    protected int getMaxLifeTicks() {
        return 120;
    }

    protected Vec3 getNonTargetVelocity() {
        return new Vec3(0,-1, 1).yRot((float) Math.toRadians((-1.0) * firstYaw));
    }
    
    protected Class getTargetClass() {
        return LivingEntity.class;
    }

    protected LivingEntity findTarget() {
        return null;
    }
}
