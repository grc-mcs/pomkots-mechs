package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;

/**
 * Lightweight, non-physical damage hitbox that follows a small monster by entity id.
 * It performs no projectile scans; vanilla/custom ray hits are forwarded to its parent.
 */
public class SmallMobHitBoxEntity extends Entity {
    private static final EntityDataAccessor<Integer> PARENT_ID = SynchedEntityData.defineId(SmallMobHitBoxEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> HITBOX_WIDTH = SynchedEntityData.defineId(SmallMobHitBoxEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> HITBOX_HEIGHT = SynchedEntityData.defineId(SmallMobHitBoxEntity.class, EntityDataSerializers.FLOAT);

    private BaseSmallMonsterEntity parentMob;

    public SmallMobHitBoxEntity(EntityType<? extends SmallMobHitBoxEntity> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
        setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();

        BaseSmallMonsterEntity parent = getParentMob();
        if (parent == null || parent.isRemoved() || parent.isDeadOrDying()) {
            if (!level().isClientSide) {
                discard();
            }
            return;
        }

        setOldPosAndRot();
        setPos(parent.getX(), parent.getY(), parent.getZ());
        setYRot(parent.getYRot());
        if (!level().isClientSide && parent.getDamageHitBox() != this) {
            discard();
        }
    }

    public void attachTo(BaseSmallMonsterEntity parent, float width, float height) {
        parentMob = parent;
        entityData.set(PARENT_ID, parent.getId());
        entityData.set(HITBOX_WIDTH, width);
        entityData.set(HITBOX_HEIGHT, height);
        refreshDimensions();
        setPos(parent.position());
    }

    public BaseSmallMonsterEntity getParentMob() {
        if (parentMob == null || parentMob.isRemoved()) {
            Entity entity = level().getEntity(entityData.get(PARENT_ID));
            parentMob = entity instanceof BaseSmallMonsterEntity parent ? parent : null;
        }
        return parentMob;
    }

    public boolean isAttachedTo(Entity entity) {
        return getParentMob() == entity;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return EntityDimensions.fixed(entityData.get(HITBOX_WIDTH), entityData.get(HITBOX_HEIGHT));
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (HITBOX_WIDTH.equals(key) || HITBOX_HEIGHT.equals(key)) {
            refreshDimensions();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        BaseSmallMonsterEntity parent = getParentMob();
        if (parent == null || parent.isDeadOrDying()) {
            return false;
        }

        parent.invulnerableTime = 0;
        invulnerableTime = 0;
        return parent.hurt(source, amount);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(PARENT_ID, -1);
        entityData.define(HITBOX_WIDTH, 0.1F);
        entityData.define(HITBOX_HEIGHT, 0.1F);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
