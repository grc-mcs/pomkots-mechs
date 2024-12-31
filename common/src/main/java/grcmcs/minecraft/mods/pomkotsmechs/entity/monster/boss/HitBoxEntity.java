package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.mixin.ProjectileMixin;
import net.minecraft.core.NonNullList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;
import java.util.function.Consumer;

public class HitBoxEntity extends LivingEntity {
    protected final LivingEntity parentEntity;
    protected Consumer<Void> breakCallback;

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.MAX_HEALTH, 50);
    }

    public HitBoxEntity(EntityType<? extends LivingEntity> entityType, Level world) {
        this(entityType, world, null);
    }

    public HitBoxEntity(EntityType<? extends LivingEntity> entityType, Level world, LivingEntity parent) {
        super(entityType, world);
        this.parentEntity = parent;
        this.noPhysics = true; // 当たり判定のみで、物理的な挙動はなし
        this.setNoGravity(true);
    }

    public void setBreakCallback(Consumer<Void> breakCallback) {
        this.breakCallback = breakCallback;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // ボスのAABB（現在の位置からの範囲）
            AABB bossBoundingBox = this.getBoundingBox();

            // ボスの近くに存在するProjectileエンティティを取得
            List<PomkotsThrowableProjectile> projectiles = this.level().getEntitiesOfClass(
                    PomkotsThrowableProjectile.class, // Projectileエンティティのクラス
                    bossBoundingBox.inflate(1.0D), // 判定範囲を少し拡大
                    projectile -> !projectile.isRemoved() // 削除されていないエンティティのみ
            );

            for (PomkotsThrowableProjectile projectile : projectiles) {
                if (bossBoundingBox.intersects(projectile.getBoundingBox())) {
                    projectile.onHitEntityPublic(this);
                }
            }
        }

        if (!this.level().isClientSide && !firstTick && !isParentActive()) {
            this.kill();
        }
    }

    protected boolean isParentActive() {
        return parentEntity != null && !parentEntity.isDeadOrDying();
    }

    private float damageCount = 39;

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 当たった場合、親エンティティにダメージを伝える
        if (isParentActive()) {
            if (damageCount < 0) {
                return parentEntity.hurt(source, amount);
            } else {
                if (source.is(DamageTypes.PLAYER_ATTACK)) {
                    damageCount -= amount;

                    if (damageCount < 0 && breakCallback != null) {
                        breakCallback.accept(null);
                    }
                }

                return parentEntity.hurt(source, amount * 0.5F);
            }

        } else {
            return super.hurt(source, amount);
        }
    }
//
//    @Override
//    public boolean isInvisible() {
//        // エンティティを常に不可視に設定
//        return true;
//    }
//
//    @Override
//    public boolean shouldRenderAtSqrDistance(double distance) {
//        // どの距離でも表示しない
//        return false;
//    }

    @Override
    public boolean displayFireAnimation() {
        // 火がついていても表示しない
        return false;
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
    public HumanoidArm getMainArm() {
        return null;
    }
}
