package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class KujiraEntity extends ThrowableProjectile implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final int MAX_LIFE_TICKS = 300;
    private int lifeTicks = 0;

    public KujiraEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
        super(entityType, world);
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        this.setNoGravity(true);
        super.tick();

        if(this.lifeTicks++ >= MAX_LIFE_TICKS) {
            this.discard();
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
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.kujira.happy"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }


    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        // カメラからの距離に関係なくレンダリングする
        double maxDistance = 1000; // 必要に応じて距離を設定
//        return this.distanceToSqr(cameraX, cameraY, cameraZ) < maxDistance * maxDistance;
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return true;
    }


    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(30.0); // 必要に応じて調整
    }

}
