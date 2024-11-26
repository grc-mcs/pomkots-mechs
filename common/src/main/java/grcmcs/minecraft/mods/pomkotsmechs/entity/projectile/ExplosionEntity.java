package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ExplosionEntity extends ThrowableProjectile implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final int MAX_LIFE_TICKS = 50;
    private int lifeTicks = 0;

    public ExplosionEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        this.setNoGravity(true);
        super.tick();

        if(this.lifeTicks++ >= MAX_LIFE_TICKS) {
            this.discard();
        } else if (this.lifeTicks == 5) {
            this.generateLava();
            if (this.level().isClientSide) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), PomkotsMechs.SE_EXPLOSION_EVENT.get(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
            }
        }
    }

    private void generateLava() {
        if (this.level().isClientSide) {
            RandomSource random = this.level().getRandom();

            // 100個のLavaパーティクルをバラまく
            for (int i = 0; i < 20; i++) {
                // ランダムな速度を生成
                double velocityX = random.nextDouble() * 2.0 - 1;
                double velocityY = random.nextDouble() * 2.0 - 1;
                double velocityZ = random.nextDouble() * 2.0 - 1;

                // パーティクルをクライアント側で発生させる
                this.level().addParticle(ParticleTypes.LAVA,
                        this.getX(), this.getY(), this.getZ(), // 位置
                        velocityX, velocityY, velocityZ // 速度
                );
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {

    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {

    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "rotation", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.explosion.new"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

}
