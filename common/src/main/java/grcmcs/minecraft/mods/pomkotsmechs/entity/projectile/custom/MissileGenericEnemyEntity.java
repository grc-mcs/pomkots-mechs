package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;

public class MissileGenericEnemyEntity extends MissileGenericEntity {
    public MissileGenericEnemyEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        super(entityType, world);
    }

    public MissileGenericEnemyEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, LivingEntity target) {
        this(entityType, world, shooter, target, BattleBalance.MECH_MISSILE_DAMAGE, 1F);
    }

    public MissileGenericEnemyEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, LivingEntity target, float damage, float speed) {
        super(entityType, world, shooter, target, damage, speed);
    }

    protected Class getTargetClass() {
        return Player.class;
    }
}
