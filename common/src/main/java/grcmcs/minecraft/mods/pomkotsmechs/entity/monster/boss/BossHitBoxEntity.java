package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileBaseEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.NonNullList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BossHitBoxEntity extends LivingEntity {
    private static final EntityDataAccessor<Integer> PARENT_ID = SynchedEntityData.defineId(BossHitBoxEntity.class, EntityDataSerializers.INT);
    protected BaseBossEntity parentEntity;
    protected Consumer<Void> breakCallback;

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.MAX_HEALTH, 50);
    }

    public BossHitBoxEntity(EntityType<? extends LivingEntity> entityType, Level world) {
        this(entityType, world, null);
    }

    public BossHitBoxEntity(EntityType<? extends LivingEntity> entityType, Level world, BaseBossEntity parent) {
        super(entityType, world);
        this.parentEntity = parent;
        if (parent != null) {
            this.entityData.set(PARENT_ID, parent.getId());
        }
        this.noPhysics = true; // 当たり判定のみで、物理的な挙動はなし
        this.setNoGravity(true);
    }

    public void setBreakCallback(Consumer<Void> breakCallback) {
        this.breakCallback = breakCallback;
    }

    public Vec3 getRelativeParentPos() {
        return Vec3.ZERO;
    }

    public BaseBossEntity getParentEntity() {
        int parentId = this.entityData.get(PARENT_ID);
        if (parentEntity == null || parentEntity.isRemoved() || parentEntity.getId() != parentId) {
            var entity = this.level().getEntity(parentId);
            parentEntity = entity instanceof BaseBossEntity boss ? boss : null;
        }
        return parentEntity;
    }

    public void snapToParent() {
        BaseBossEntity parent = getParentEntity();
        if (parent == null) return;
        Vec3 position = parent.position().add(getRelativeParentPos());
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(position.x, position.y, position.z);
        this.xo = position.x;
        this.yo = position.y;
        this.zo = position.z;
    }

    public Vec3 getStableAimPosition(boolean aimFoot) {
        BaseBossEntity parent = getParentEntity();
        Vec3 base = parent != null ? parent.position().add(getRelativeParentPos()) : this.position();
        return aimFoot ? base : base.add(0, this.getBbHeight() * 0.5, 0);
    }

    public Vec3 getStableAimVelocity() {
        BaseBossEntity parent = getParentEntity();
        return parent != null ? parent.getDeltaMovement() : Vec3.ZERO;
    }

    @Override
    public void tick() {
        super.tick();

        snapToParent();

        BaseBossEntity parent = getParentEntity();
        if (isParentActive() && parent.getAiMode() == BaseBossEntity.AI_MODE_INACTIVE) {
            return;
        }

        if (!this.level().isClientSide) {
            // ボスのAABB（現在の位置からの範囲）
            AABB bossBoundingBox = this.getBoundingBox();

            // ボスの近くに存在するProjectileエンティティを取得
            List<PomkotsThrowableProjectile> projectiles = this.level().getEntitiesOfClass(
                    PomkotsThrowableProjectile.class, // Projectileエンティティのクラス
                    bossBoundingBox.inflate(1.0D), // 判定範囲を少し拡大
                    projectile -> !projectile.isRemoved() // 削除されていないエンティティのみ
            );

            List<PomkotsThrowableProjectile> hitProjectiles = new ArrayList<>();
            for (PomkotsThrowableProjectile projectile : projectiles) {
                if (isRelatedShooter(projectile)) {
                    continue;
                }
                if (bossBoundingBox.intersects(projectile.getBoundingBox())) {
                    projectile.onHitEntityPublic(this);
                }
            }
        }

        if (!this.level().isClientSide && !firstTick && !isParentActive()) {
            this.discard();
        }
    }

    public boolean isRelatedShooter(PomkotsThrowableProjectile projectile) {
        BaseBossEntity parent = getParentEntity();
        return parent != null && parent.equals(projectile.getShooter());
    }

    protected boolean isParentActive() {
        BaseBossEntity parent = getParentEntity();
        return parent != null && !parent.isDeadOrDying();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 当たった場合、親エンティティにダメージを伝える
        if (isParentActive()) {
            BaseBossEntity parent = getParentEntity();
            parent.invulnerableTime = 0;
            this.invulnerableTime = 0;

            return parent.hurtFromAdditionalHitBox(source, amount);

        } else if (Utils.isSystemicDamage(source)) {
            return super.hurt(source, amount);
        } else {
            return false;
        }
    }

    public void addStunPoint(int point) {
        if (isParentActive()) {
            getParentEntity().addStunPoint(point);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PARENT_ID, -1);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

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
