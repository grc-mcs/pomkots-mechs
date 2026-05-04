package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
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

public class KujiraEntity extends LivingEntity implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final int MAX_LIFE_TICKS = 300;
    private int lifeTicks = 0;

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.MAX_HEALTH, 50);
    }

    public KujiraEntity(EntityType<? extends KujiraEntity> entityType, Level world) {
        super(entityType, world);
        this.setNoGravity(true);
        this.noCulling = true;
    }

    @Override
    public void tick() {
        this.setNoGravity(true);
        super.tick();

        if (!this.level().isClientSide) {
//            var pos = this.position();
//            this.setPos(pos.x, pos.y, pos.z + 0.01);
            this.hasImpulse = true;
        }
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

//    @Override
//    protected void onHitEntity(EntityHitResult entityHitResult) {
//
//    }
//
//    @Override
//    protected void onHitBlock(BlockHitResult blockHitResult) {
//
//    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
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
    public Iterable<ItemStack> getArmorSlots() {
        return NonNullList.withSize(4, ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {

    }

    @Override
    public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
        return false;
    }

    @Override
    public HumanoidArm getMainArm() {
        return null;
    }
}
