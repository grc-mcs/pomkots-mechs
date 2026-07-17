package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class MechCapsuleData {

    private MechCapsuleData() {
    }

    public static final String TAG_STORED_MECH = "StoredMech";
    private static final String TAG_DATA_VERSION = "DataVersion";
    private static final String TAG_ENTITY_TYPE = "EntityType";
    private static final String TAG_ENTITY_DATA = "EntityData";

    private static final int CURRENT_DATA_VERSION = 1;

    /**
     * Mechをカプセル用NBTへ変換する。
     */
    public static CompoundTag capture(Pmvc01Entity mech) {
        CompoundTag entityData = new CompoundTag();

        /*
         * saveWithoutId() によってEntity本体・LivingEntity・
         * 独自addAdditionalSaveData()の内容をまとめて保存する。
         */
        mech.saveWithoutId(entityData);

        sanitizeCapturedEntityData(entityData);

        ResourceLocation entityTypeId =
                EntityType.getKey(mech.getType());

        CompoundTag storedMech = new CompoundTag();
        storedMech.putInt(TAG_DATA_VERSION, CURRENT_DATA_VERSION);
        storedMech.putString(TAG_ENTITY_TYPE, entityTypeId.toString());
        storedMech.put(TAG_ENTITY_DATA, entityData);

        return storedMech;
    }

    /**
     * 保存したMechを指定位置へ生成する。
     */
    public static Optional<Entity> spawn(
            ServerLevel level,
            CompoundTag storedMech,
            Vec3 spawnPosition,
            float spawnYaw
    ) {
        if (!storedMech.contains(TAG_ENTITY_TYPE, CompoundTag.TAG_STRING)
                || !storedMech.contains(TAG_ENTITY_DATA, CompoundTag.TAG_COMPOUND)) {
            return Optional.empty();
        }

        ResourceLocation entityTypeId =
                ResourceLocation.tryParse(
                        storedMech.getString(TAG_ENTITY_TYPE)
                );

        if (entityTypeId == null) {
            return Optional.empty();
        }

        Optional<EntityType<?>> entityTypeOptional =
                EntityType.byString(entityTypeId.toString());

        if (entityTypeOptional.isEmpty()) {
            return Optional.empty();
        }

        EntityType<?> entityType = entityTypeOptional.get();
        Entity entity = entityType.create(level);

        if (entity == null) {
            return Optional.empty();
        }

        /*
         * getCompound()で得たものを直接変更しないようcopy()する。
         * カプセル内の保存データを壊さず、何度でも複製できる。
         */
        CompoundTag entityData =
                storedMech.getCompound(TAG_ENTITY_DATA).copy();

        /*
         * 古いデータや外部編集によってUUIDなどが戻されていても、
         * 生成時に必ず除去する。
         */
        sanitizeBeforeSpawn(entityData);

        try {
            entity.load(entityData);
        } catch (RuntimeException exception) {
            entity.discard();
            return Optional.empty();
        }

        /*
         * load()後に位置を設定する。
         * NBT内に位置情報が残っていた場合でも、投擲地点を優先できる。
         */
        entity.moveTo(
                spawnPosition.x,
                spawnPosition.y,
                spawnPosition.z,
                spawnYaw,
                0.0F
        );

        entity.setDeltaMovement(Vec3.ZERO);
        entity.fallDistance = 0.0F;

        if (!level.addFreshEntity(entity)) {
            entity.discard();
            return Optional.empty();
        }

        return Optional.of(entity);
    }

    /**
     * 保存段階で、クローンへ引き継がない情報を除去する。
     */
    private static void sanitizeCapturedEntityData(CompoundTag tag) {
        removeIdentityData(tag);
        removeWorldStateData(tag);
        removeRelationshipData(tag);
    }

    /**
     * 生成直前にも再度サニタイズする。
     */
    private static void sanitizeBeforeSpawn(CompoundTag tag) {
        removeIdentityData(tag);
        removeWorldStateData(tag);
        removeRelationshipData(tag);
    }

    private static void removeIdentityData(CompoundTag tag) {
        /*
         * EntityのUUID。
         * 同じUUIDのEntityを複数生成してはいけない。
         */
        tag.remove("UUID");
    }

    private static void removeWorldStateData(CompoundTag tag) {
        tag.remove("Pos");
        tag.remove("Motion");
        tag.remove("Rotation");

        tag.remove("FallDistance");
        tag.remove("OnGround");
        tag.remove("PortalCooldown");

        /*
         * 必要に応じて残してもよいが、
         * カプセルから出た瞬間に燃えている等を避けたいなら除去。
         */
        tag.remove("Fire");
        tag.remove("Air");
    }

    private static void removeRelationshipData(CompoundTag tag) {
        /*
         * 元の乗員・同乗者まで複製しない。
         */
        tag.remove("Passengers");

        /*
         * リードの接続先も元個体のワールド状態なので除去。
         */
        tag.remove("Leash");
    }
}