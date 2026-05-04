package grcmcs.minecraft.mods.pomkotsmechs.block;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.Optional;
import java.util.UUID;

public class EntitySpawnerBlockEntity extends BlockEntity {

    // 設定項目
    private String targetEntityTypeID = "";
    private String nbt = "";
    private float healthModifier = 1.0F;
    private float attackModifier = 1.0F;
    private int spawnIntervalDays = 1;  // 召喚間隔（日数）
    private int maxDistance = 400;       // 最大許容距離
    private boolean activateBoss = false;

    // 状態管理
    private long lastSpawnDay = -1;     // 最終召喚日（-1=未召喚）
    private UUID spawnedEntityUUID = null;

    // キャッシュ
    private RegistrySupplier<EntityType<?>> cachedEntityType = null;
    private Entity cachedEntity = null;
    private boolean hasCheckedInitialSpawn = false;

    public EntitySpawnerBlockEntity(BlockPos pos, BlockState state) {
        super(PomkotsMechs.ENTITY_SPAWNER_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EntitySpawnerBlockEntity be) {
        // クライアント側は何もしない
        if (level.isClientSide) {
            return;
        }

        // マップ編集モード時はスキップ
        if (Utils.isMapEditingMode(level)) {
            return;
        }

        // エンティティタイプが未設定なら何もしない
        if (be.targetEntityTypeID.isEmpty()) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;

        // 初回召喚チェック（ロード直後に1回だけ）
        if (!be.hasCheckedInitialSpawn) {
            be.hasCheckedInitialSpawn = true;

            if (be.lastSpawnDay == -1) {
                // 未召喚状態なので即座に召喚
                be.trySpawn(serverLevel);
                return;
            }
        }

        // 定期チェック（10秒に1回程度で十分）
        if (level.getGameTime() % 200 != 0) {
            return;
        }

        // 召喚判定
        be.trySpawn(serverLevel);
    }

    /**
     * 召喚を試みる
     */
    private void trySpawn(ServerLevel level) {
        // エンティティタイプを取得（キャッシュ）
        if (cachedEntityType == null) {
            cachedEntityType = getEntityType(targetEntityTypeID);

            if (cachedEntityType == null) {
                PomkotsMechs.LOGGER.warn("Unknown entity type: {}", targetEntityTypeID);
                return;
            }
        }

        // 現在の日数を計算
        long currentDay = level.getDayTime() / 24000L;

        // 召喚間隔チェック
        if (lastSpawnDay >= 0 && currentDay < lastSpawnDay + spawnIntervalDays) {
            // まだ召喚間隔が経過していない
            return;
        }

        // 前回召喚したエンティティをチェック
        if (!isSpawnedEntityValid(level)) {
            // 前回のエンティティが無効なので新しく召喚
            spawnEntity(level, currentDay);
        }
    }

    /**
     * 前回召喚したエンティティが有効かチェック
     * @return true = まだ有効（召喚不要）, false = 無効（召喚可能）
     */
    private boolean isSpawnedEntityValid(ServerLevel level) {
        // UUIDがない場合は無効
        if (spawnedEntityUUID == null) {
            cachedEntity = null;
            return false;
        }

        // キャッシュが無効な場合は再取得
        if (cachedEntity == null || cachedEntity.isRemoved()) {
            cachedEntity = level.getEntity(spawnedEntityUUID);
        }

        // エンティティが存在しない
        if (cachedEntity == null) {
            spawnedEntityUUID = null;
            cachedEntity = null;
            return false;
        }

        // エンティティが死んでいる
        if (!cachedEntity.isAlive()) {
            spawnedEntityUUID = null;
            cachedEntity = null;
            return false;
        }

        // 距離チェック
        double distSqr = cachedEntity.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5
        );

        if (distSqr > maxDistance * maxDistance) {
            // 遠すぎる = もう管理しない
            spawnedEntityUUID = null;
            cachedEntity = null;
            return false;
        }

        // まだ有効
        return true;
    }

    /**
     * エンティティを召喚
     */
    private void spawnEntity(ServerLevel level, long currentDay) {
        Entity entity = cachedEntityType.get().create(level);

        if (entity == null) {
            PomkotsMechs.LOGGER.error("Failed to create entity: {}", targetEntityTypeID);
            return;
        }

        // 位置を設定
        entity.moveTo(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 1.0,
                worldPosition.getZ() + 0.5,
                level.random.nextFloat() * 360F,
                0
        );

        // ワールドに追加
        level.addFreshEntity(entity);

        // カスタム設定を適用
        if (entity instanceof GenericPomkotsMonster monster) {
            // NBTの適用
            if (nbt != null && !nbt.isEmpty()) {
                try {
                    CompoundTag nbtTag = TagParser.parseTag(nbt);
                    monster.readAdditionalSaveData(nbtTag);
                } catch (CommandSyntaxException e) {
                    PomkotsMechs.LOGGER.error("Failed to parse NBT: {}", nbt, e);
                }
            }

            // ステータス補正
            monster.setModifiers(healthModifier, attackModifier);

            // ボス固有設定
            if (entity instanceof BaseBossEntity boss) {
                if (!activateBoss) {
                    boss.setActivated(false);
                    boss.setAiMode(BaseBossEntity.AI_MODE_INACTIVE);
                }
            }
            // 小型モンスター固有設定
            else if (entity instanceof BaseSmallMonsterEntity small) {
                small.setPersistenceRequired();
            }
        }

        // 状態を更新
        spawnedEntityUUID = entity.getUUID();
        cachedEntity = entity;
        lastSpawnDay = currentDay;

        setChanged();

        PomkotsMechs.LOGGER.info("Spawned entity {} at {} (day: {})",
                targetEntityTypeID, worldPosition, currentDay);
    }

    /**
     * エンティティタイプを取得
     */
    private RegistrySupplier<EntityType<?>> getEntityType(String typeId) {
        try {
            ResourceLocation rl = new ResourceLocation(typeId);

            for (RegistrySupplier<EntityType<?>> entityType : PomkotsMechs.ENTITIES) {
                if (entityType.getId().equals(rl)) {
                    return entityType;
                }
            }
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Invalid entity type ID: {}", typeId, e);
        }

        return null;
    }

    // ==================== NBT読み書き ====================

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        targetEntityTypeID = tag.getString(PomkotsMechs.nbtName("TargetEntityTypeID"));
        nbt = tag.getString(PomkotsMechs.nbtName("EntityTag"));
        healthModifier = tag.getFloat(PomkotsMechs.nbtName("HealthModifier"));
        attackModifier = tag.getFloat(PomkotsMechs.nbtName("AttackModifier"));
        spawnIntervalDays = tag.getInt(PomkotsMechs.nbtName("SpawnIntervalDays"));
        maxDistance = tag.getInt(PomkotsMechs.nbtName("MaxDistance"));
        lastSpawnDay = tag.getLong(PomkotsMechs.nbtName("LastSpawnDay"));
        if (tag.contains(PomkotsMechs.nbtName("ActivateBoss"))) {
            activateBoss = tag.getBoolean(PomkotsMechs.nbtName("ActivateBoss"));
        } else {
            activateBoss = false;
        }

        if (tag.hasUUID(PomkotsMechs.nbtName("SpawnedEntityUUID"))) {
            spawnedEntityUUID = tag.getUUID(PomkotsMechs.nbtName("SpawnedEntityUUID"));
        }

        // デフォルト値の設定
        if (spawnIntervalDays <= 0) {
            spawnIntervalDays = 1;
        }
        if (maxDistance <= 0) {
            maxDistance = 400;
        }
        if (healthModifier == 0) {
            healthModifier = 1.0F;
        }
        if (attackModifier == 0) {
            attackModifier = 1.0F;
        }

        // キャッシュをクリア
        cachedEntityType = null;
        cachedEntity = null;
        hasCheckedInitialSpawn = false;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        tag.putString(PomkotsMechs.nbtName("TargetEntityTypeID"), targetEntityTypeID);
        tag.putString(PomkotsMechs.nbtName("EntityTag"), nbt);
        tag.putFloat(PomkotsMechs.nbtName("HealthModifier"), healthModifier);
        tag.putFloat(PomkotsMechs.nbtName("AttackModifier"), attackModifier);
        tag.putInt(PomkotsMechs.nbtName("SpawnIntervalDays"), spawnIntervalDays);
        tag.putInt(PomkotsMechs.nbtName("MaxDistance"), maxDistance);
        tag.putLong(PomkotsMechs.nbtName("LastSpawnDay"), lastSpawnDay);
        tag.putBoolean(PomkotsMechs.nbtName("ActivateBoss"), activateBoss);

        if (spawnedEntityUUID != null) {
            tag.putUUID(PomkotsMechs.nbtName("SpawnedEntityUUID"), spawnedEntityUUID);
        }
    }

    // ==================== Getter/Setter ====================

    public void setTargetEntityTypeID(String targetEntityTypeID) {
        this.targetEntityTypeID = targetEntityTypeID;
        this.cachedEntityType = null; // キャッシュをクリア
        setChanged();
    }

    public void setNbt(String nbt) {
        this.nbt = nbt;
        setChanged();
    }

    public void setHealthModifier(float healthModifier) {
        this.healthModifier = healthModifier;
        setChanged();
    }

    public void setAttackModifier(float attackModifier) {
        this.attackModifier = attackModifier;
        setChanged();
    }

    public void setSpawnIntervalDays(int spawnIntervalDays) {
        this.spawnIntervalDays = Math.max(1, spawnIntervalDays);
        setChanged();
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = Math.max(1, maxDistance);
        setChanged();
    }

    /**
     * 召喚状態をリセット（次回tick時に即座に召喚）
     */
    public void resetSpawnState() {
        lastSpawnDay = -1;
        spawnedEntityUUID = null;
        cachedEntity = null;
        hasCheckedInitialSpawn = false;
        setChanged();
    }

    // Getter群
    public String getTargetEntityTypeID() { return targetEntityTypeID; }
    public String getNbt() { return nbt; }
    public float getHealthModifier() { return healthModifier; }
    public float getAttackModifier() { return attackModifier; }
    public int getSpawnIntervalDays() { return spawnIntervalDays; }
    public int getMaxDistance() { return maxDistance; }
    public long getLastSpawnDay() { return lastSpawnDay; }

    public Entity getSpawnedEntity() {
        return cachedEntity;
    }
}

//public class EntitySpawnerBlockEntity extends BlockEntity {
//    private String targetEntityTypeID = "";
//    private RegistrySupplier<EntityType<?>> targetEntityType = null;
//    private UUID targetEntityUUID = null;
//    private Entity targetEntity = null;
//    private String nbt = "";
//    private float healthModifier = 1;
//    private float attackModifier = 1;
//    private int dayDuration = 1;
//    private long lastSpawnDay = 0;
//    private int maxDistance = 20;
//
//    public EntitySpawnerBlockEntity(BlockPos pos, BlockState state) {
//        super(PomkotsMechs.ENTITY_SPAWNER_BLOCK_ENTITY.get(), pos, state);
//    }
//
//    public static void tick(Level level, BlockPos pos, BlockState state, EntitySpawnerBlockEntity be) {
//        if (!"".equals(be.targetEntityTypeID)
//            && level.getDayTime() % 1000 == 0
//            && !Utils.isMapEditingMode(level)) {
//
//            ServerLevel serverLevel;
//
//            if (level instanceof ServerLevel sl) {
//                serverLevel = sl;
//            } else {
//                return;
//            }
//
//            if (!"".equals(be.targetEntityTypeID) && null == be.targetEntityType) {
//                be.targetEntityType = be.getEntityType(be.targetEntityTypeID);
//            }
//
//            if (be.targetEntityType == null) {
//                return;
//            }
//
//            long dayTime = level.getDayTime();
//            long currentDay = dayTime / 24000L; // 現在の「日」
//
//            // 同じ日ならスポーン済み
//            if (be.lastSpawnDay + be.dayDuration >= currentDay) return;
//
//            if (be.targetEntityUUID != null && be.targetEntity == null) {
//                be.targetEntity = serverLevel.getEntity(be.targetEntityUUID);
//                if (be.targetEntity == null) {
//                    be.targetEntityUUID = null;
//                    be.setChanged();
//                    return;
//                }
//            }
//
//            // 前回の個体がまだ存在するならスポーンしない
//            if (be.targetEntity != null && be.targetEntity.isAlive()) {
//                double distSqr = be.targetEntity.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
//                if (distSqr < be.maxDistance * be.maxDistance) {
//                    return;
//                } else {
//                    be.targetEntity = null;
//                    be.targetEntityUUID = null;
//                    be.setChanged();
//                }
//            }
//
//            // 実際のスポーン処理
//            Entity entity = be.targetEntityType.get().create(level);
//            if (entity != null) {
//                entity.moveTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
//                        level.random.nextFloat() * 360F, 0);
//
//                if (entity instanceof GenericPomkotsMonster mons) {
//                    if (be.nbt != null && !be.nbt.isEmpty()) {
//                        try {
//                            mons.readAdditionalSaveData(TagParser.parseTag(be.nbt));
//                        } catch (Exception e) {
//                            PomkotsMechs.LOGGER.error("failed to parse nbt", e);
//                        }
//                    }
//
//                    mons.setModifiers(be.healthModifier, be.attackModifier);
//
//                    if (entity instanceof BaseBossEntity boss) {
//                        boss.setActivated(false);
//                        boss.setAiMode(BaseBossEntity.AI_MODE_INACTIVE);
//                    } else if (entity instanceof BaseSmallMonsterEntity small) {
//                        small.setPersistence(true);
//                        small.setPersistenceRequired();
//                    }
//
//                }
//
//                level.addFreshEntity(entity);
//
//                be.targetEntity = entity;
//                be.targetEntityUUID = entity.getUUID();
//                be.lastSpawnDay = currentDay;
//
//                be.setChanged(); // NBT保存を促す
//            }
//        }
//    }
//
//    private RegistrySupplier<EntityType<?>> getEntityType(String typeId) {
//        ResourceLocation rl = new ResourceLocation(this.targetEntityTypeID);
//
//        RegistrySupplier<EntityType<?>> et = null;
//        for (RegistrySupplier<EntityType<?>> entityType : PomkotsMechs.ENTITIES) {
//            if (entityType.getId().equals(rl)) {
//                et = entityType;
//            }
//        }
//
//        return et;
//    }
//
//    @Override
//    public void load(CompoundTag tag) {
//        super.load(tag);
//
//        if (tag.contains(PomkotsMechs.nbtName("TargetEntityTypeID"))) {
//            this.targetEntityTypeID = tag.getString(PomkotsMechs.nbtName("TargetEntityTypeID"));
//        }
//
//        if (tag.contains(PomkotsMechs.nbtName("TargetEntityUUID"))) {
//            this.targetEntityUUID = tag.getUUID(PomkotsMechs.nbtName("TargetEntityUUID"));
//            if (level instanceof ServerLevel sl) {
//                var ent = sl.getEntity(this.targetEntityUUID);
//                if (ent != null) {
//                    this.targetEntity = ent;
//                } else {
//                    this.targetEntityUUID = null;
//                }
//            }
//        } else {
//            this.targetEntityUUID = null;
//        }
//
//        if (tag.contains(PomkotsMechs.nbtName("HealthModifier"))) {
//            this.healthModifier = tag.getFloat(PomkotsMechs.nbtName("HealthModifier"));
//        }
//
//        if (tag.contains(PomkotsMechs.nbtName("AttackModifier"))) {
//            this.attackModifier = tag.getFloat(PomkotsMechs.nbtName("AttackModifier"));
//        }
//
//        if (tag.contains(PomkotsMechs.nbtName("DayDuration"))) {
//            this.dayDuration = tag.getInt(PomkotsMechs.nbtName("DayDuration"));
//        }
//
//        if (tag.contains(PomkotsMechs.nbtName("LastSpawnDay"))) {
//            this.lastSpawnDay = tag.getLong(PomkotsMechs.nbtName("LastSpawnDay"));
//        }
//
//        if (tag.contains(PomkotsMechs.nbtName("MaxDistance"))) {
//            this.maxDistance = tag.getInt(PomkotsMechs.nbtName("MaxDistance"));
//        }
//
//        if (tag.contains(PomkotsMechs.nbtName("EntityTag"))) {
//            this.nbt = tag.getString(PomkotsMechs.nbtName("EntityTag"));
//        }
//    }
//
//    @Override
//    protected void saveAdditional(CompoundTag tag) {
//        super.saveAdditional(tag);
//        tag.putString(PomkotsMechs.nbtName("TargetEntityTypeID"), targetEntityTypeID);
//        if (targetEntityUUID != null) {
//            tag.putUUID(PomkotsMechs.nbtName("TargetEntityUUID"), targetEntityUUID);
//        } else {
//            tag.remove(PomkotsMechs.nbtName("TargetEntityUUID"));
//        }
//        tag.putFloat(PomkotsMechs.nbtName("HealthModifier"), healthModifier);
//        tag.putFloat(PomkotsMechs.nbtName("AttackModifier"), attackModifier);
//        tag.putInt(PomkotsMechs.nbtName("DayDuration"), dayDuration);
//        tag.putLong(PomkotsMechs.nbtName("LastSpawnDay"), lastSpawnDay);
//        tag.putInt(PomkotsMechs.nbtName("MaxDistance"), maxDistance);
//        tag.putString(PomkotsMechs.nbtName("EntityTag"), nbt);
//    }
//}