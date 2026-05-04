package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MissileGenericLargeEntity extends MissileGenericEntity {
    public final float defaultScale = 10f;

    public MissileGenericLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        this(entityType, world, null, null);
    }

    public MissileGenericLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, LivingEntity target) {
        this(entityType, world, shooter, target, BattleBalance.MECH_MISSILE_DAMAGE, 1F);
    }

    public MissileGenericLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, LivingEntity target, float damage, float speed) {
        super(entityType, world, shooter, target, damage, speed);
    }

    public float getScale() {
        return defaultScale;
    }

    protected int getSwitchTick() {
        return 5;
    }

    protected float getMaxRotationAnglePerTick() {
        return 5;
    }

    @Override
    protected void createExplosion(Vec3 pos) {
        Level world = level();
        if (!world.isClientSide) {

            if (Utils.isBlockDestructionAllowed(shooter)) {
                Utils.explode(this, pos.x, pos.y, pos.z, 12, false, Level.ExplosionInteraction.BLOCK, this.level());
            } else {
                Utils.explode(this,  pos.x, pos.y, pos.z, 12, false, Level.ExplosionInteraction.NONE, this.level());
            }

            ExplosionEntity e = new ExplosionEntity(PomkotsMechs.EXPLOSION.get(), world);
            e.setPos(this.position());
            world.addFreshEntity(e);
        }
    }
}
