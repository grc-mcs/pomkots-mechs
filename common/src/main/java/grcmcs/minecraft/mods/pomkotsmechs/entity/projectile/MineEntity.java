package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class MineEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final int MAX_LIFE_TICKS = 160;
    private int lifeTicks = 0;
    private float damage;
    private LivingEntity shooter = null;
    private int onGroundTicks = 0;

    public void setExplosionScale(float explosionScale) {
        this.explosionScale = explosionScale;
    }

    private float explosionScale = 5;

    public MineEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        this(entityType, level, null);
    }

    public MineEntity(EntityType<? extends ThrowableProjectile> entityType, Level level, LivingEntity shooter) {
        this(entityType, level, shooter, BattleBalance.MECH_RIFLE_DAMAGE);
    }

    public MineEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, float damage) {
        super(entityType, shooter, world);
        this.setNoGravity(false);
        this.noCulling = true;
        this.noPhysics = true;
        this.shooter = shooter;
        this.damage = damage;
    }

    @Override
    public boolean shouldRender(double a, double b, double c) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double e) {
        return true;
    }

    @Override
    public void tick() {
        if (!onGround()) {
            super.tick();
            this.hasImpulse = true;
        } else {
            onGroundTicks++;

            if (onGroundTicks > 5 && !this.level().isClientSide) {
                AABB mineAabb = this.getBoundingBox();

                List<Player> players = this.level().getEntitiesOfClass(
                        Player.class,
                        mineAabb,
                        player -> !player.isSpectator()
                );

                if (!players.isEmpty()) {
                    createExplosion(position());
                    this.alreadyExplode = true;
                    this.discard();
                }
            }
        }

        if (this.lifeTicks++ >= MAX_LIFE_TICKS) {
            this.discard();
        }
    }

    private boolean alreadyExplode = false;

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {

    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        this.setDeltaMovement(0,0,0);
        this.setOnGround(true);
    }

    private void createExplosion(Vec3 pos) {
        Level world = level();
        if (!world.isClientSide) {
            if (ProjectileUtil.isDestructionAllowed(this)) {
                Utils.explode(this,  pos.x, pos.y + 1, pos.z, explosionScale, false, Level.ExplosionInteraction.BLOCK, this.level());

            } else {
                Utils.explode(this,  pos.x, pos.y + 1, pos.z, explosionScale, false, Level.ExplosionInteraction.NONE, this.level());
            }
        }
    }

    @Override
    public void onClientRemoval() {
        if (this.level().isClientSide) {
            ParticleUtil.addSparkParticles(this.position(), this.level());
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rotation", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.pms.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

}
