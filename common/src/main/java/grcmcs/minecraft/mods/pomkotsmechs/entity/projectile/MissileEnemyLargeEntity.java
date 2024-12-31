package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.HitBoxEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class MissileEnemyLargeEntity extends MissileBaseEntity {
    public final float defaultScale = 10f;

    public MissileEnemyLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public MissileEnemyLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
        super(entityType, world, shooter, null);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        var entity = entityHitResult.getEntity();
        if (entity instanceof LivingEntity) {
            if (entity.equals(shooter) || entity instanceof HitBoxEntity) {
                return;
            }
            entity.hurt(entity.damageSources().thrown(this, this.getOwner() != null ? this.getOwner() : this), getDamage());
            entity.invulnerableTime = 0;
        }
        this.createExplosion(this.position());

        this.discard();
    }

    protected int getSwitchTick() {
        return 5;
    }

    protected double getSpeed() {
        return 2;
    }

    protected int getSeekRange() {
        return 100;
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
        return 5;
    }

    @Override
    protected float getDamage() {
        return BattleBalance.BOSS_MISSILE_LARGE_DAMAGE;
    }

    @Override
    public float getScale() {
        return 10.0F;
    }

    @Override
    protected int getMaxLifeTicks() {
        return 300;
    }

    @Override
    protected void createExplosion(Vec3 pos) {
        Level world = level();
        if (!world.isClientSide) {
            var exp = ProjectileUtil.isDestructionAllowed(this)?Level.ExplosionInteraction.BLOCK: Level.ExplosionInteraction.NONE;

            world.explode(this,  pos.x, pos.y, pos.z, BattleBalance.BOSS_MISSILE_LARGE_EXPLOSION, false, exp);
            ExplosionEntity e = new ExplosionEntity(PomkotsMechs.EXPLOSION.get(), world);
            e.setPos(this.position());
            world.addFreshEntity(e);
        }
    }
}
