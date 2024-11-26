package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MissileEnemyEntity extends MissileBaseEntity {


    public MissileEnemyEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public MissileEnemyEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
        super(entityType, world, shooter, null);
    }
    protected int getSwitchTick() {
        return 4;
    }

    protected double getSpeed() {
        return 2;
    }

    protected int getSeekRange() {
        return 40;
    }

    protected Class getTargetClass() {
        return Player.class;
    }

    @Override
    protected Vec3 getNonTargetVelocity() {
        return this.getDeltaMovement();
    }

    @Override
    protected float getMaxRotationAnglePerTick() {
        return 7;
    }

    @Override
    protected float getDamage() {
        return BattleBalance.MOB_MISSILE_EXPLOSION;
    }

    @Override
    protected int getMaxLifeTicks() {
        return 120;
    }
}
