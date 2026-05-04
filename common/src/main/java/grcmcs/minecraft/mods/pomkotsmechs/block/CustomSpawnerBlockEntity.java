package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import grcmcs.minecraft.mods.pomkotsmechs.util.WeightedList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class CustomSpawnerBlockEntity extends BlockEntity {

    // 設定値
    private int  maxMobs       = 4;
    private int  spawnInterval = 600;
    private int  maxHops       = 10;
    private AABB spawnSpace    = new AABB(0, 0, 0, 2, 2, 2);

    // MobタイプのWeightedList
    private WeightedList<ResourceLocation> mobTypes = new WeightedList<>();

    // 揮発記憶
    private final Map<UUID, Entity> spawnedMobs = new HashMap<>();

    public CustomSpawnerBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(PomkotsMechs.CUSTOM_SPAWNER_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.putInt(PomkotsMechs.nbtName("MaxMobs"),       maxMobs);
        tag.putInt(PomkotsMechs.nbtName("SpawnInterval"), spawnInterval);
        tag.putInt(PomkotsMechs.nbtName("MaxHops"),       maxHops);
        tag.putDouble(PomkotsMechs.nbtName("SpawnSpaceX"), spawnSpace.maxX - spawnSpace.minX);
        tag.putDouble(PomkotsMechs.nbtName("SpawnSpaceY"), spawnSpace.maxY - spawnSpace.minY);
        tag.putDouble(PomkotsMechs.nbtName("SpawnSpaceZ"), spawnSpace.maxZ - spawnSpace.minZ);

        ListTag mobList = new ListTag();
        // WeightedListからエントリを取り出してNBT化
        for (WeightedList.Entry<ResourceLocation> entry : mobTypes.getEntries()) {
            CompoundTag mobTag = new CompoundTag();
            mobTag.putString("type",   entry.instance().toString());
            mobTag.putInt   ("weight", entry.weight());
            mobList.add(mobTag);
        }
        tag.put(PomkotsMechs.nbtName("MobTypes"), mobList);
    }

    @Override
    public void load(CompoundTag tag) {
        maxMobs       = tag.getInt(PomkotsMechs.nbtName("MaxMobs"));
        spawnInterval = tag.getInt(PomkotsMechs.nbtName("SpawnInterval"));
        maxHops       = tag.getInt(PomkotsMechs.nbtName("MaxHops"));

        double sx = tag.getDouble(PomkotsMechs.nbtName("SpawnSpaceX"));
        double sy = tag.getDouble(PomkotsMechs.nbtName("SpawnSpaceY"));
        double sz = tag.getDouble(PomkotsMechs.nbtName("SpawnSpaceZ"));
        spawnSpace = new AABB(0, 0, 0, sx, sy, sz);

        mobTypes = new WeightedList<>();
        ListTag mobList = tag.getList(PomkotsMechs.nbtName("MobTypes"), Tag.TAG_COMPOUND);
        for (Tag t : mobList) {
            CompoundTag mobTag = (CompoundTag) t;
            ResourceLocation rl     = new ResourceLocation(mobTag.getString("type"));
            int              weight = mobTag.getInt("weight");
            mobTypes.addObject(weight, rl);
        }
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    public static void tick(Level level, BlockPos pos, BlockState state, CustomSpawnerBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (Utils.isMapEditingMode(level)) {
            return;
        }

        // 死亡したMobを除去
        be.spawnedMobs.entrySet().removeIf(e ->
                e.getValue() == null || !e.getValue().isAlive()
        );

        // 最大数チェック
        if (be.spawnedMobs.size() >= be.maxMobs) return;

        // インターバルチェック
        if (level.getGameTime() % be.spawnInterval != 0) return;

        // MobTypeが空なら何もしない
        if (be.mobTypes.isEmpty()) return;

        // スポーン位置探索
        be.findSpawnPos(serverLevel, pos, RandomSource.create()).ifPresent(spawnPos -> {
            // WeightedListからランダムに選択
            ResourceLocation mobType = be.mobTypes.getRandomObject(serverLevel.getRandom());

            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE
                    .getOptional(mobType)
                    .orElse(null);
            if (entityType == null) {
                PomkotsMechs.LOGGER.error("Unknown entity type: {}", mobType);
                return;
            }

            Entity mob = entityType.create(serverLevel);
            if (mob == null) return;

            mob.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
            serverLevel.addFreshEntity(mob);
            be.spawnedMobs.put(mob.getUUID(), mob);
        });
    }

    private Optional<BlockPos> findSpawnPos(ServerLevel level, BlockPos origin, RandomSource random) {
        for (int attempt = 0; attempt < 20; attempt++) {
            // 範囲内でランダムな座標を生成
            double angle  = random.nextDouble() * Math.PI * 2;
            double dist   = random.nextDouble() * maxHops;
            double dx     = Math.cos(angle) * dist;
            double dz     = Math.sin(angle) * dist;
            double dy = random.nextDouble() * 8;

            BlockPos candidate = origin.offset((int)dx, (int)dy, (int)dz);

            // スポーナーより下はスキップ
            if (candidate.getY() < origin.getY()) continue;

            // candidateからoriginにレイキャスト
            BlockHitResult hit = level.isBlockInLine(new ClipBlockStateContext(
                    Vec3.atCenterOf(candidate),
                    Vec3.atCenterOf(origin),
                    blockState -> !blockState.isAir() && !blockState.equals(level.getBlockState(origin))
            ));

            // レイがスポーナーに到達していれば空気で繋がっている
            if (hit.getType().equals(HitResult.Type.MISS)) {
                // スポーン可能な空間チェック
                AABB checkBox = spawnSpace.move(
                        candidate.getX() - spawnSpace.minX,
                        candidate.getY() - spawnSpace.minY,
                        candidate.getZ() - spawnSpace.minZ
                );
                if (level.noCollision(checkBox)) {
                    return Optional.of(candidate);
                }
            }
        }

        return Optional.of(origin);
    }
}
