package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PlateEntity extends Mob implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final int MAX_LIFE_TICKS = 300;
    private int breakTicks = 0;

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0F)
                .add(Attributes.FOLLOW_RANGE, 200)
                .add(Attributes.MAX_HEALTH, 300);
    }

    public PlateEntity(EntityType<? extends PlateEntity> entityType, Level world) {
        super(entityType, world);
        this.setNoGravity(true);
        this.noCulling = true;
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
//            this.setNoGravity(breakTicks < 5);
        }

        super.tick();

        if (!level().isClientSide) {
            if (this.breakTicks > 0) {
                if (this.breakTicks++ >= MAX_LIFE_TICKS) {
                    this.discard();
                }
                if (!this.isNoGravity()) {
                    this.push(0, -0.18 * 0.9800000190734863D, 0);
                }
            } else {
                if (level() instanceof ServerLevel serverLevel) {
                    moveEntityToNearestAir(serverLevel, this, 2);
                }
                snapYawTo90(this);
            }
        }
    }

    public static void snapYawTo90(Entity entity) {
        float yaw = entity.getYRot();

        // -180 ～ 180 に正規化
        yaw = Mth.wrapDegrees(yaw);

        // 一番近い90度刻みにスナップ
        float snappedYaw = Math.round(yaw / 90.0f) * 90.0f;

        // 再度正規化（-180～180に戻す）
        snappedYaw = Mth.wrapDegrees(snappedYaw);

        // Entityへ反映
        entity.setYRot(snappedYaw);
        entity.setYBodyRot(snappedYaw);
        entity.setYHeadRot(snappedYaw);

        entity.yRotO = snappedYaw; // 補間用（ガクッを防ぐ）
    }

    public static boolean moveEntityToNearestAir(ServerLevel level, Entity entity, int maxRadius) {
        // すでに安全なら何もしない
        if (level.noCollision(entity)) {
            return false;
        }

        BlockPos origin = entity.blockPosition();
        AABB box = entity.getBoundingBox();

        // 距離が近い順に探索
        for (int r = 1; r <= maxRadius; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {

                        // 球っぽくする（不要なら消してOK）
                        if (dx * dx + dy * dy + dz * dz > r * r) continue;

                        BlockPos pos = origin.offset(dx, dy, dz);

                        // AABBを移動させて衝突チェック
                        double x = pos.getX() + 0.5;
                        double y = pos.getY();
                        double z = pos.getZ() + 0.5;

                        AABB movedBox = box.move(
                                x - entity.getX(),
                                y - entity.getY(),
                                z - entity.getZ()
                        );

                        if (level.noCollision(entity, movedBox)) {
                            // 移動！
                            entity.teleportTo(x, y, z);
                            entity.setDeltaMovement(Vec3.ZERO);
                            return true;
                        }
                    }
                }
            }
        }

        return false; // 見つからなかった
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && level() instanceof ServerLevel serverLevel) {
            if (breakTicks == 0) {
                this.triggerAnim("action_controller", "break");
                breakTicks = 1;

                moveEntityToNearestAir(serverLevel, this, 2);

                this.setOnGround(false);
                this.setNoGravity(false);


                Vec3 v = this.getDeltaMovement();
                if (v.y >= 0) {
                    this.setDeltaMovement(v.x, -0.08, v.z);
                }
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "action_controller", state -> PlayState.STOP)
                .triggerableAnim("break", RawAnimation.begin().thenPlayAndHold("animation.bossbox.break"))
        );
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public boolean ignoreExplosion() {
        return false;
    }

    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
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
    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void checkInsideBlocks() {

    }
}
