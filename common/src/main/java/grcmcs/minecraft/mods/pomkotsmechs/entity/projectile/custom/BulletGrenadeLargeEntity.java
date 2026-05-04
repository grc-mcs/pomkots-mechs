package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class BulletGrenadeLargeEntity extends BulletGrenadeEntity {
    public static float scale = 6;

    public BulletGrenadeLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        this(entityType, level, null, 80, BattleBalance.MECH_BULLET_GRENADE_DAMAGE, 15);
    }

    public BulletGrenadeLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, float damage) {
        this(entityType, level, shooter, 80, damage, 20);
    }

    public BulletGrenadeLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter, int maxLifeTicks, float damage, int stun) {
        super(entityType, level, shooter,
                maxLifeTicks, damage,stun);
    }
}
