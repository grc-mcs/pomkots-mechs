package grcmcs.minecraft.mods.pomkotsmechs.entity.misc;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundSource;

import java.util.Optional;
import java.util.UUID;

public final class MissionMarkerEntity extends Entity {
    public static final int ITEM_ARROW = 0;
    public static final int REACHED_AREA = 1;

    private static final EntityDataAccessor<Integer> MODE =
            SynchedEntityData.defineId(MissionMarkerEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> TARGET =
            SynchedEntityData.defineId(MissionMarkerEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Float> MIN_X =
            SynchedEntityData.defineId(MissionMarkerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MIN_Y =
            SynchedEntityData.defineId(MissionMarkerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MIN_Z =
            SynchedEntityData.defineId(MissionMarkerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_X =
            SynchedEntityData.defineId(MissionMarkerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_Y =
            SynchedEntityData.defineId(MissionMarkerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_Z =
            SynchedEntityData.defineId(MissionMarkerEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> CLOSING =
            SynchedEntityData.defineId(MissionMarkerEntity.class, EntityDataSerializers.BOOLEAN);
    private int closingTicks;

    public MissionMarkerEntity(EntityType<?> type, Level level) {
        super(type, level);
        noPhysics = true;
        noCulling = true;
    }

    public void configureItemArrow(UUID target) {
        entityData.set(MODE, ITEM_ARROW);
        entityData.set(TARGET, Optional.of(target));
    }

    public void configureReachedArea(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        entityData.set(MODE, REACHED_AREA);
        entityData.set(MIN_X, minX);
        entityData.set(MIN_Y, minY);
        entityData.set(MIN_Z, minZ);
        entityData.set(MAX_X, maxX);
        entityData.set(MAX_Y, maxY);
        entityData.set(MAX_Z, maxZ);
    }

    public int markerMode() { return entityData.get(MODE); }
    public float minX() { return entityData.get(MIN_X); }
    public float minY() { return entityData.get(MIN_Y); }
    public float minZ() { return entityData.get(MIN_Z); }
    public float maxX() { return entityData.get(MAX_X); }
    public float maxY() { return entityData.get(MAX_Y); }
    public float maxZ() { return entityData.get(MAX_Z); }
    public boolean isClosing() { return entityData.get(CLOSING); }
    public int closingTicks() { return closingTicks; }

    public void beginClosing() {
        if (markerMode() != REACHED_AREA || isClosing()) return;
        entityData.set(CLOSING, true);
        if (!level().isClientSide) {
            level().playSound(null, blockPosition(), PomkotsMechs.SE_PANEL_CLOSE.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && markerMode() == ITEM_ARROW) {
            Entity target = entityData.get(TARGET).map(id -> ((net.minecraft.server.level.ServerLevel) level()).getEntity(id)).orElse(null);
            if (target == null || target.isRemoved()) {
                discard();
            } else {
                setPos(target.getX(), target.getY(), target.getZ());
            }
        }
        if (isClosing() && ++closingTicks >= 12 && !level().isClientSide) discard();
    }

    @Override protected void defineSynchedData() {
        entityData.define(MODE, ITEM_ARROW);
        entityData.define(TARGET, Optional.empty());
        entityData.define(MIN_X, -0.5F);
        entityData.define(MIN_Y, 0.0F);
        entityData.define(MIN_Z, -0.5F);
        entityData.define(MAX_X, 0.5F);
        entityData.define(MAX_Y, 1.0F);
        entityData.define(MAX_Z, 0.5F);
        entityData.define(CLOSING, false);
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) { }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { }
    @Override public boolean isPickable() { return false; }
    @Override public boolean shouldRender(double cameraX, double cameraY, double cameraZ) { return true; }
    @Override public boolean shouldRenderAtSqrDistance(double distance) { return distance < 512.0D * 512.0D; }
}
