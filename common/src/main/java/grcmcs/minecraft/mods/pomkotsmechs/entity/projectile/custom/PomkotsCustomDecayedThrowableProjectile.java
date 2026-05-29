package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class PomkotsCustomDecayedThrowableProjectile extends PomkotsCustomThrowableProjectile {
    public PomkotsCustomDecayedThrowableProjectile(
            EntityType<? extends ThrowableProjectile> entityType, Level world,
            LivingEntity shooter,
            int maxLifeTicks, float damage, int stun) {
        super(entityType, world, shooter, maxLifeTicks, damage, stun);
    }

    @Override
    protected float calcFinalDamage(float baseDamage) {
        var finalDistance = calcFinalDistance(this.lifeTicks);
        var range = calcRangeCategory(finalDistance);

        return baseDamage * getDamageModifier(range);
    }

    protected int calcFinalDistance(int forwardingTicks) {
        return (int)(forwardingTicks * initialSpeed);
    }

    protected RangeCategory calcRangeCategory(int distance) {
        return RangeCategory.OPTIMAL;
    }

    protected float getDamageModifier(RangeCategory range) {
        return 1;
    }
}
