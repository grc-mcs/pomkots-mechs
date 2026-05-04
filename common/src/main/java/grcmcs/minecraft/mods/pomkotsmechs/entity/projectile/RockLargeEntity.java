package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RockLargeEntity extends PomkotsThrowableProjectile implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final int MAX_LIFE_TICKS = 160;
    private int lifeTicks = 0;
    private float damage = 0;
    private LivingEntity shooter = null;
    private float explosionScale = 10;

    public RockLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        this(entityType, world, null);
    }

    public RockLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
        this(entityType, world, shooter, BattleBalance.MECH_BULLET_GRENADE_DAMAGE);
    }

    public RockLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, float damage) {
        super(entityType, shooter, world);
        this.shooter = shooter;
        this.damage = damage;
    }

    public void setExplosionScale(float scale) {
        this.explosionScale = scale;
    }

    @Override
    public void tick() {
        // 弾速が早すぎると、ティック間にすり抜けちゃうのでレイキャスティングで補完
        var hitResult = ProjectileUtil.raycastBoundingCheck(this);
        if (hitResult.getType() != HitResult.Type.MISS) {
            this.onHit(hitResult);
        }

        super.tick();
        this.hasImpulse = true;

        if(this.lifeTicks++ >= MAX_LIFE_TICKS) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        var entity = entityHitResult.getEntity();
        if (entity.equals(shooter) || entity instanceof BossHitBoxEntity) {
            return;
        }

        entity.hurt(entity.damageSources().thrown(this, this.getOwner() != null ? this.getOwner() : this), damage);
        entity.invulnerableTime = 0;

        explode();
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        explode();
    }

    private void explode() {
        if (this.level().isClientSide) return;

        explodeVisual();
        spawnSmallRocks();
        this.discard();
    }


    private void spawnSmallRocks() {
        int count = 12;
        RandomSource rand = this.random;

        for (int i = 0; i < count; i++) {
            RockSmallEntity rock = new RockSmallEntity(PomkotsMechs.ROCK_SMALL.get(), level(), this.shooter);

            rock.setPos(this.getX(), this.getY() + 0.5, this.getZ());
            rock.setExplosionScale(explosionScale);

            Vec3 dir = new Vec3(
                    rand.nextGaussian(),
                    rand.nextGaussian() * 0.6,
                    rand.nextGaussian()
            ).normalize();

            rock.setDeltaMovement(dir.scale(0.8 + rand.nextFloat()));

            level().addFreshEntity(rock);
        }
    }

    private void explodeVisual() {
        Utils.explode(
                this,
                this.getX(),
                this.getY(),
                this.getZ(),
                2.0F,
                Level.ExplosionInteraction.NONE, // ブロック破壊なし
                this.level()
        );
    }

    private void createExplosion(Vec3 pos) {
        Level world = level();
        if (!world.isClientSide) {
            if (Utils.isBlockDestructionAllowed(shooter)) {
                Utils.explode(this,  pos.x, pos.y, pos.z, explosionScale, false, Level.ExplosionInteraction.BLOCK, world);
            } else {
                Utils.explode(this,  pos.x, pos.y, pos.z, explosionScale, false, Level.ExplosionInteraction.NONE, world);
            }
        } else {
            addParticles(this.position());
        }
    }

    @Override
    public void onClientRemoval() {
        if (this.level().isClientSide) {
            addParticles(this.position());
        }
    }

    private void addParticles(Vec3 offset) {
        Level world = level();
        for (int i = 0; i < 5; i++) {
            int rad = Math.abs(i - 3);

            for (int j = 0; j < 5 - rad; j++) {
                for (int k = 0; k < 5 - rad; k++) {
                    world.addParticle(
                            ParticleTypes.EXPLOSION,
                            offset.x - (k - (3 - rad/2)) * 2,
                            offset.y - (i - 3) * 2,
                            offset.z - (j - (3 - rad/2)) * 2,
                            0,0,0);
                }
            }
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rotation", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.rock.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
