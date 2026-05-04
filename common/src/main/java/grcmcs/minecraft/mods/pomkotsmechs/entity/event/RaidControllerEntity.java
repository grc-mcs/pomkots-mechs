package grcmcs.minecraft.mods.pomkotsmechs.entity.event;

import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.EventDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.RaidDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.SpawnTarget;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.WaveDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.Pmc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.BGMState;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.ServerBGMTracker;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RaidControllerEntity extends LivingEntity {

    // -------------------------------------------------------------------------
    // 定数
    // -------------------------------------------------------------------------

    private static final int MAX_LIVING_TICK = 120000;

    // -------------------------------------------------------------------------
    // レイドタイプ
    // -------------------------------------------------------------------------

    public enum RaidType {
        DEFENSE("defense"),
        SURVIVE("survive"),
        SWEEP("sweep"),
        SWEEP_BOSS_BOX("sweep_boss_box"),
        ACTIVATE("activate"),
        UNKNOWN("");

        private final String id;

        RaidType(String id) { this.id = id; }

        public static RaidType of(String id) {
            if (id == null) return UNKNOWN;
            for (RaidType type : values()) {
                if (type.id.equals(id)) return type;
            }
            return UNKNOWN;
        }

        public String getId() {
            return id;
        }
    }

    // -------------------------------------------------------------------------
    // NBTキー定数
    // -------------------------------------------------------------------------

    private static final String NBT_START_TARGET_RAID_NAME     = "StartTargetRaidName";
    private static final String NBT_RAID_OWNER_UUID             = "RaidOwnerUUID";
    private static final String NBT_RAID_FINALIZE_CMD_SUCCESS   = "RaidFinalizeCommandSuccess";
    private static final String NBT_RAID_FINALIZE_CMD_FAIL      = "RaidFinalizeCommandFail";
    private static final String NBT_CURRENT_RAID_NAME           = "CurrentRaidName";
    private static final String NBT_RAID_TARGET_ENTITY_UUID     = "RaidTargetEntityUUID";
    private static final String NBT_CURRENT_WAVE_INDEX          = "CurrentWaveIndex";
    private static final String NBT_TICK_COUNTER                = "TickCounter";
    private static final String NBT_END_EVENT_WHEN_KILLED_ALL   = "EndEventWhenKilledAllMobs";
    private static final String NBT_SPAWNED_MOBS                = "SpawnedMobs";
    private static final String NBT_SPAWNED_MOBS_NUM            = "SpawnedMobsNum";

    // -------------------------------------------------------------------------
    // フィールド
    // -------------------------------------------------------------------------

    // 設定値（NBT保存対象）
    private String startTargetRaidName      = null;
    private UUID   raidOwnerUUID            = null;
    private String raidFinalizeCommandSuccess = null;
    private String raidFinalizeCommandFail    = null;

    // 実行状態（NBT保存対象）
    private String  currentRaidName        = null;
    private UUID    raidTargetEntityUUID   = null;
    private int     currentWaveIndex       = -1;
    private int     tickCounter            = -1;
    private boolean endEventWhenKilledAllMobs = false;
    private int     spawnedMobsNum         = -1;

    // 実行時のみ（NBT保存不要 or 復元）
    private RaidDefinition raidDefinition  = null;
    private LivingEntity   raidTargetEntity = null;
    private Entity         raidOwner       = null;
    private List<Entity>   spawnedMobs     = new ArrayList<>();
    private ListTag        spawnedMobsTag  = null;

    // BossBar
    private final ServerBossEvent timeBar;
    private final ServerBossEvent baseHpBar;
    private int prevRemaining = 0;

    // -------------------------------------------------------------------------
    // 静的ファクトリ・ユーティリティ
    // -------------------------------------------------------------------------

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MAX_HEALTH, 1000)
                .add(Attributes.ARMOR, 20)
                .add(Attributes.ARMOR_TOUGHNESS, 1);
    }

    public static @NotNull CompoundTag buildCompoundTag(
            String startTargetRaidName,
            UUID raidOwnerUUID,
            String raidFinalizeCommandSuccess,
            String raidFinalizeCommandFail
    ) {
        CompoundTag tag = new CompoundTag();
        tag.putString(nbt(NBT_START_TARGET_RAID_NAME), startTargetRaidName);
        if (raidOwnerUUID != null)
            tag.putUUID(nbt(NBT_RAID_OWNER_UUID), raidOwnerUUID);
        if (raidFinalizeCommandSuccess != null)
            tag.putString(nbt(NBT_RAID_FINALIZE_CMD_SUCCESS), raidFinalizeCommandSuccess);
        if (raidFinalizeCommandFail != null)
            tag.putString(nbt(NBT_RAID_FINALIZE_CMD_FAIL), raidFinalizeCommandFail);
        return tag;
    }

    public static List<BaseBossEntity> getInactiveBossesAroundPos(Level level, BlockPos pos) {
        AABB area = AABB.unitCubeFromLowerCorner(pos.getCenter()).inflate(40);
        return level.getEntitiesOfClass(BaseBossEntity.class, area)
                .stream()
                .filter(b -> b.getAiMode() == BaseBossEntity.AI_MODE_INACTIVE)
                .toList();
    }

    public static List<Player> getPlayersAroundPos(Level level, BlockPos pos) {
        AABB area = AABB.unitCubeFromLowerCorner(pos.getCenter()).inflate(100);
        return level.getEntitiesOfClass(Player.class, area);
    }

    public static Optional<SoundEvent> getSoundEvent(ResourceLocation id) {
        return BuiltInRegistries.SOUND_EVENT.getOptional(id);
    }

    // -------------------------------------------------------------------------
    // コンストラクタ
    // -------------------------------------------------------------------------

    public RaidControllerEntity(EntityType<? extends LivingEntity> entityType, Level world) {
        this(entityType, world, null);
    }

    public RaidControllerEntity(EntityType<? extends LivingEntity> entityType, Level world, BaseBossEntity parent) {
        super(entityType, world);
        this.noPhysics = true;
        this.setNoGravity(true);

        this.timeBar = new ServerBossEvent(
                Component.literal("Raid Time"),
                BossEvent.BossBarColor.BLUE,
                BossEvent.BossBarOverlay.PROGRESS
        );
        this.baseHpBar = new ServerBossEvent(
                Component.literal("Target HP"),
                BossEvent.BossBarColor.GREEN,
                BossEvent.BossBarOverlay.PROGRESS
        );

        timeBar.setProgress(1.0f);
        baseHpBar.setProgress(1.0f);
        setBossBarVisibility(false);
    }

    // -------------------------------------------------------------------------
    // NBT
    // -------------------------------------------------------------------------

    /** PomkotsMechs.nbtName() の短縮形 */
    private static String nbt(String key) {
        return PomkotsMechs.nbtName(key);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        // 起動時設定
        startTargetRaidName     = readString(tag, NBT_START_TARGET_RAID_NAME);
        raidOwnerUUID           = readUUID(tag, NBT_RAID_OWNER_UUID);
        raidFinalizeCommandSuccess = readString(tag, NBT_RAID_FINALIZE_CMD_SUCCESS);
        raidFinalizeCommandFail    = readString(tag, NBT_RAID_FINALIZE_CMD_FAIL);

        // 実行状態
        currentRaidName         = readString(tag, NBT_CURRENT_RAID_NAME);
        raidTargetEntityUUID    = readUUID(tag, NBT_RAID_TARGET_ENTITY_UUID);
        currentWaveIndex        = tag.contains(nbt(NBT_CURRENT_WAVE_INDEX))  ? tag.getInt(nbt(NBT_CURRENT_WAVE_INDEX))       : -1;
        tickCounter             = tag.contains(nbt(NBT_TICK_COUNTER))         ? tag.getInt(nbt(NBT_TICK_COUNTER))             : -1;
        spawnedMobsNum          = tag.contains(nbt(NBT_SPAWNED_MOBS_NUM))     ? tag.getInt(nbt(NBT_SPAWNED_MOBS_NUM))        : -1;
        endEventWhenKilledAllMobs = tag.contains(nbt(NBT_END_EVENT_WHEN_KILLED_ALL))
                && tag.getBoolean(nbt(NBT_END_EVENT_WHEN_KILLED_ALL));

        spawnedMobsTag = (ListTag) tag.get(nbt(NBT_SPAWNED_MOBS));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        // 起動時設定
        writeString(tag, NBT_START_TARGET_RAID_NAME, startTargetRaidName);
        writeUUID  (tag, NBT_RAID_OWNER_UUID, raidOwnerUUID);
        writeString(tag, NBT_RAID_FINALIZE_CMD_SUCCESS, raidFinalizeCommandSuccess);
        writeString(tag, NBT_RAID_FINALIZE_CMD_FAIL, raidFinalizeCommandFail);

        // 実行状態
        writeString(tag, NBT_CURRENT_RAID_NAME, currentRaidName);
        writeUUID  (tag, NBT_RAID_TARGET_ENTITY_UUID, raidTargetEntityUUID);
        writeIntIfSet(tag, NBT_CURRENT_WAVE_INDEX, currentWaveIndex);
        writeIntIfSet(tag, NBT_TICK_COUNTER, tickCounter);
        writeIntIfSet(tag, NBT_SPAWNED_MOBS_NUM, spawnedMobsNum);

        if (endEventWhenKilledAllMobs) {
            tag.putBoolean(nbt(NBT_END_EVENT_WHEN_KILLED_ALL), true);
        } else {
            tag.remove(nbt(NBT_END_EVENT_WHEN_KILLED_ALL));
        }

        ListTag list = new ListTag();
        for (Entity ent : spawnedMobs) {
            list.add(NbtUtils.createUUID(ent.getUUID()));
        }
        tag.put(nbt(NBT_SPAWNED_MOBS), list);
    }

    // NBTヘルパー
    private static String readString(CompoundTag tag, String key) {
        return tag.contains(nbt(key)) ? tag.getString(nbt(key)) : null;
    }

    private static UUID readUUID(CompoundTag tag, String key) {
        return tag.contains(nbt(key)) ? tag.getUUID(nbt(key)) : null;
    }

    private static void writeString(CompoundTag tag, String key, String value) {
        if (value != null) tag.putString(nbt(key), value);
        else               tag.remove(nbt(key));
    }

    private static void writeUUID(CompoundTag tag, String key, UUID value) {
        if (value != null) tag.putUUID(nbt(key), value);
        else               tag.remove(nbt(key));
    }

    private static void writeIntIfSet(CompoundTag tag, String key, int value) {
        if (value != -1) tag.putInt(nbt(key), value);
        else             tag.remove(nbt(key));
    }

    // -------------------------------------------------------------------------
    // Tick
    // -------------------------------------------------------------------------

    @Override
    public void tick() {
        if (this.firstTick && !this.level().isClientSide) {
            loadSpawnedMobs();
        }

        super.tick();

        if (this.level().isClientSide) return;

        if (this.tickCount > MAX_LIVING_TICK) {
            PomkotsMechs.LOGGER.info("MAX Tick Count exceeded: {}", this);
            this.discard();
            return;
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        spawnedMobs.removeIf(e -> e == null || !e.isAlive());

        // レイド開始処理
        if (startTargetRaidName != null) {
            if (!initializeRaid(serverLevel)) return;
        }

        // raidOwner解決
        raidOwner = (raidOwnerUUID != null) ? serverLevel.getEntity(raidOwnerUUID) : null;

        // レイド未開始
        if (currentRaidName == null) return;

        // 再起動後の復元
        if (raidTargetEntity == null) {
            raidTargetEntity = (LivingEntity) serverLevel.getEntity(raidTargetEntityUUID);
            raidDefinition   = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(currentRaidName);
        }

        if (raidDefinition == null || raidTargetEntity == null) return;

        tickRaid(serverLevel);
    }

    /** レイド初期化。失敗時はfalseを返しdiscardする */
    private boolean initializeRaid(ServerLevel serverLevel) {
        raidDefinition = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(startTargetRaidName);
        if (raidDefinition == null) {
            PomkotsMechs.LOGGER.error("Raid def not found: {}", startTargetRaidName);
            this.discard();
            return false;
        }

        currentRaidName  = startTargetRaidName;
        tickCounter      = 0;
        currentWaveIndex = 0;

        // 先にseenされていた場合のBGM補完
        for (ServerPlayer player : baseHpBar.getPlayers()) {
            startBGMforPlayer(player, raidDefinition);
        }

        setupRaidTarget(serverLevel);
        startTargetRaidName = null;
        return true;
    }

    /** レイドタイプに応じてターゲットを設定 */
    private void setupRaidTarget(ServerLevel serverLevel) {
        switch (RaidType.of(raidDefinition.type)) {
            case DEFENSE -> {
                RaidObjectiveEntity roe = PomkotsMechs.RAID_OBJECTIVE.get().create(serverLevel);
                if (roe != null) {
                    BlockPos pos = this.blockPosition();
                    roe.setPos(pos.getX(), pos.getY(), pos.getZ());
                    this.level().addFreshEntity(roe);
                    raidTargetEntity     = roe;
                    raidTargetEntityUUID = roe.getUUID();
                }
            }
            case SURVIVE -> {
                raidTargetEntity     = (LivingEntity) serverLevel.getEntity(raidOwnerUUID);
                raidTargetEntityUUID = raidOwnerUUID;
            }
            case SWEEP, SWEEP_BOSS_BOX, ACTIVATE -> {
                raidTargetEntity     = this;
                raidTargetEntityUUID = this.getUUID();

                if (RaidType.of(raidDefinition.type) == RaidType.ACTIVATE) {
                    List<BaseBossEntity> bosses = getInactiveBossesAroundPos(this.level(), this.blockPosition());
                    List<Player> players        = getPlayersAroundPos(this.level(), this.blockPosition());
                    for (BaseBossEntity boss : bosses) {
                        boss.boot();
                        players.forEach(p -> boss.addHateToEntity(p, 50));
                        spawnedMobs.add(boss);
                    }
                }
            }
        }
    }

    /** レイドのメインtick処理 */
    private void tickRaid(ServerLevel serverLevel) {
        WaveDefinition wave    = raidDefinition.waves.get(currentWaveIndex);
        RaidType       type    = RaidType.of(raidDefinition.type);

        if (raidTargetEntity instanceof RaidObjectiveEntity) {
            BlockPos pos = this.blockPosition();
            raidTargetEntity.setPos(pos.getX(), pos.getY(), pos.getZ());
        }

        if (tickCounter == 5) {
            showTitlesForPlayers(wave.title, wave.sub_title, baseHpBar.getPlayers());
        }

        tickCounter++;

        if (!baseHpBar.isVisible()) {
            setBossBarVisibility(true);
        }

        int[] remainMaxMobs = remainingSpawnTargets(raidDefinition, currentWaveIndex, tickCounter);
        updateBossBars(wave, remainMaxMobs);

        // レイドオーナー消失チェック
        if (raidOwner == null || !raidOwner.isAlive()) {
            endRaid(false, "レイドオーナーが消失");
            return;
        }

        // 即時終了条件チェック
        if (checkDefeat(type, remainMaxMobs)) return;

        // survive時はコントローラーをターゲットに追従
        if (type == RaidType.SURVIVE) {
            this.setPos(raidTargetEntity.position());
        }

        // 全Mob撃破による強制Wave終了
        if (endEventWhenKilledAllMobs && areAllMobsDefeated(remainMaxMobs)) {
            tickCounter = wave.duration_ticks;
        }

        // イベント発火
        fireEvent(wave);

        // Wave終了チェック
        checkWaveEnd(wave, type);
    }

    /** 敗北条件チェック。敗北時trueを返す */
    private boolean checkDefeat(RaidType type, int[] remainMaxMobs) {
        switch (type) {
            case DEFENSE, SURVIVE -> {
                if (raidTargetEntity == null || !raidTargetEntity.isAlive()) {
                    endRaid(false);
                    return true;
                }
            }
            case SWEEP, SWEEP_BOSS_BOX -> {
                if (remainMaxMobs == null) {
                    endRaid(false);
                    return true;
                }
                if (spawnedMobs.isEmpty() && remainMaxMobs[0] <= 0) {
                    if (isFinalWave(raidDefinition, currentWaveIndex)) {
                        endRaid(true);
                    } else {
                        proceedWave();
                    }
                    return true;
                }
            }
            case ACTIVATE -> {
                if (spawnedMobs.isEmpty()) {
                    endRaid(true);
                    return true;
                }
            }
        }
        return false;
    }

    private void proceedWave() {
        currentWaveIndex++;
        endEventWhenKilledAllMobs = false;
        tickCounter = 0;
    }

    /** 全Mobが撃破（またはPmc01の格納完了）されたか */
    private boolean areAllMobsDefeated(int[] remainMaxMobs) {
        if (spawnedMobs.isEmpty() && remainMaxMobs[0] <= 0) {
            return true;
        } else {
            for (Entity ent : spawnedMobs) {
                if (!(ent instanceof Pmc01Entity pmc01)) return false;
                if (!"".equals(pmc01.containerType))      return false;
            }
        }
        return true;
    }

    /** Wave終了条件チェック */
    private void checkWaveEnd(WaveDefinition wave, RaidType type) {
        if (tickCounter < wave.duration_ticks) return;

        switch (type) {
            case DEFENSE, SURVIVE -> {
                killAllMobs();
                proceedWave();
                if (currentWaveIndex >= raidDefinition.waves.size()) {
                    endRaid(true);
                }
            }
            case SWEEP, SWEEP_BOSS_BOX, ACTIVATE -> {
                killAllMobs();
                endRaid(false);
            }
        }
    }

    // -------------------------------------------------------------------------
    // BossBar
    // -------------------------------------------------------------------------

    private void setBossBarVisibility(boolean visible) {
        baseHpBar.setVisible(visible);
        timeBar.setVisible(visible);
    }

    private void updateBossBars(WaveDefinition wave, int[] remainingMobs) {
        if (raidTargetEntity == null) return;

        int remaining = wave.duration_ticks - tickCounter;
        if (remaining == prevRemaining) return;

        float timeProgress = (float) remaining / wave.duration_ticks;
        timeBar.setName(Component.literal(Utils.string2Component(wave.title).getString() + " | Time: " + (remaining / 20) + "sec"));
        timeBar.setProgress(Mth.clamp(timeProgress, 0f, 1f));

        RaidType type = RaidType.of(raidDefinition.type);
        if ((type == RaidType.SWEEP || type == RaidType.SWEEP_BOSS_BOX) && remainingMobs != null) {
            int total   = remainingMobs[0] + spawnedMobs.size();
            float prog  = total / (float) remainingMobs[1];
            baseHpBar.setName(Component.literal("Target Monsters: " + total + "/" + remainingMobs[1]));
            baseHpBar.setProgress(Mth.clamp(prog, 0f, 1f));
        } else if (type != RaidType.ACTIVATE) {
            float hp = raidTargetEntity.getHealth() / raidTargetEntity.getMaxHealth();
            baseHpBar.setName(Component.literal(
                    "Target HP: " + (int) raidTargetEntity.getHealth() + "/" + (int) raidTargetEntity.getMaxHealth()
            ));
            baseHpBar.setProgress(Mth.clamp(hp, 0f, 1f));
        }

        prevRemaining = remaining;
    }

    // -------------------------------------------------------------------------
    // Seen / BossBar プレイヤー管理
    // -------------------------------------------------------------------------

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        baseHpBar.addPlayer(player);
        timeBar.addPlayer(player);
        if (!firstTick) {
            startBGMforPlayer(player, raidDefinition);
        }
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        baseHpBar.removePlayer(player);
        timeBar.removePlayer(player);
        ServerBGMTracker.forceUnsetBGMState(player);
    }

    private void startBGMforPlayer(ServerPlayer player, RaidDefinition raidDef) {
        if (raidDef == null) return;
        BGMState state = "defense".equals(raidDef.type) ? BGMState.BATTLE_RAID : BGMState.BATTLE_BOSS;
        ServerBGMTracker.forceSetBGMState(player, state);
    }

    // -------------------------------------------------------------------------
    // イベント発火
    // -------------------------------------------------------------------------

    private void fireEvent(WaveDefinition wave) {
        if (wave.timeline == null) return;

        EventDefinition event = wave.timeline.get(tickCounter);
        if (event == null) return;

        switch (event.type) {
            case "spawn"        -> fireSpawnEvent(event);
            case "spawn_random" -> fireSpawnRandomEvent(event);
        }

        if (event.message != null) {
            sendMessage(event.message, baseHpBar.getPlayers());
        }

        endEventWhenKilledAllMobs = event.force_end_wave_when_killed_all;
    }

    private void fireSpawnEvent(EventDefinition event) {
        if (event.spawn_targets == null) return;

        for (SpawnTarget st : event.spawn_targets) {
            if (st.position == null) continue;

            var type = getEntityType(st.mob_type);
            if (type == null) continue;

            BlockPos pos = parseBlockPos(st.position);
            if (pos == null || this.level() == null) continue;

            Entity entity = type.get().create(this.level());
            if (entity == null) continue;

            BlockPos offset = this.blockPosition();
            entity.setPos(
                    offset.getX() + pos.getX(),
                    offset.getY() + pos.getY(),
                    offset.getZ() + pos.getZ()
            );

            applySnbt(entity, st.snbt);
            setupSpawnedMob(entity);
            spawnedMobs.add(entity);
            this.level().addFreshEntity(entity);
        }
    }

    private void fireSpawnRandomEvent(EventDefinition event) {
        if (event.spawn_targets == null) return;

        Random rand        = new Random();
        Level  level       = this.level();
        var    mobTypeList = getMobList(event);
        int    count       = event.amount;
        BlockPos center    = this.blockPosition();
        LivingEntity opener = getTargetCandidate();
        AABB spawnSpace = new AABB(0, 0, 0, 2, 2, 2);
        int maxHops = (event.range_x + event.range_z)/2;

        int spawned = 0;
        for (int i = 0; i < 100 && spawned < count; i++) {
//            int x = center.getX() + rand.nextInt(event.range_x) - event.range_x / 2;
//            int z = center.getZ() + rand.nextInt(event.range_z) - event.range_z / 2;
//            int y = findSpawnY(level, x, center.getY(), z, spawnSpace);
            var pos = findSpawnPos(maxHops, spawnSpace, (ServerLevel) level, center, random, event);

            if (pos.isPresent()) {
                Entity mob = getRandomMobType(rand, mobTypeList).create(level);
                if (mob == null) continue;

                mob.moveTo(pos.get().getX() + 0.5, pos.get().getY(), pos.get().getZ() + 0.5, rand.nextFloat() * 360F, 0);

                if (mob instanceof Mob m) {
                    if (opener != null) m.setTarget(opener);
                    if (mob instanceof BaseSmallMonsterEntity small) {
                        small.setPersistenceRequired();
                        small.setInEvent(true);
                    }
                }

                level.addFreshEntity(mob);
                spawnedMobs.add(mob);
                spawned++;
            }
        }
    }

    private Optional<BlockPos> findSpawnPos(int maxHops, AABB spawnSpace, ServerLevel level, BlockPos origin, RandomSource random, EventDefinition event) {
        for (int attempt = 0; attempt < 20; attempt++) {
            // 範囲内でランダムな座標を生成
            double angle  = random.nextDouble() * Math.PI * 2;
            double dist   = random.nextDouble() * maxHops;
            double dx     = Math.cos(angle) * dist;
            double dz     = Math.sin(angle) * dist;
            double dy = random.nextDouble() * 8;

            BlockPos candidate = origin.offset((int)dx, (int)dy, (int)dz);

            if (event.spawn_mobs_in_closed_area) {
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
            } else {
                return Optional.of(candidate);

            }
        }

        return Optional.of(origin);
    }

    /** スポーンしたエンティティにレイドタイプ別の設定を適用 */
    private void setupSpawnedMob(Entity entity) {
        RaidType type = RaidType.of(raidDefinition.type);

        switch (type) {
            case DEFENSE -> {
                if (entity instanceof Mob mob) mob.setTarget(raidTargetEntity);
                if (entity instanceof GenericPomkotsMonster p) { p.setInRaid(true); p.setInEvent(true); }
            }
            case SURVIVE, SWEEP, SWEEP_BOSS_BOX -> {
                if (entity instanceof Mob mob) {
                    if (entity instanceof GenericPomkotsMonster p) p.setInEvent(true);
                    if (((ServerLevel) this.level()).getEntity(raidOwnerUUID) instanceof LivingEntity le) {
                        mob.setTarget(le);
                    }
                }
            }
        }

        if (entity instanceof Pmc01Entity pmc01) {
            pmc01.raidControllerEntity     = this;
            pmc01.raidControllerEntityUUID = this.getUUID();
        } else if (entity instanceof Mob mob) {
            mob.setPersistenceRequired();
        }
    }

    private void applySnbt(Entity entity, String snbt) {
        if (snbt == null || !(entity instanceof LivingEntity le)) return;
        try {
            le.readAdditionalSaveData(TagParser.parseTag(snbt));
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("failed to parse snbt.", e);
        }
    }

    // -------------------------------------------------------------------------
    // レイド終了・リセット
    // -------------------------------------------------------------------------

    private void endRaid(boolean victory) {
        if (victory && raidDefinition != null
                && raidDefinition.advancement != null
                && !raidDefinition.advancement.isEmpty()
                && raidOwner instanceof Player p) {
            Utils.completeAdvancement(new ResourceLocation(raidDefinition.advancement), p);
        }

        endRaid(victory, "");
    }

    private void endRaid(boolean victory, String subTitle) {
        String title = victory ? "Mission Complete!!" : "Mission Failed...";
        showTitlesForPlayers(title, subTitle, baseHpBar.getPlayers());

        String finalizeCommand = victory ? raidFinalizeCommandSuccess : raidFinalizeCommandFail;
        if (this.level() instanceof ServerLevel serverLevel
                && raidOwnerUUID != null
                && finalizeCommand != null
                && serverLevel.getPlayerByUUID(raidOwnerUUID) instanceof ServerPlayer serverPlayer) {
            runCommandAsServer(finalizeCommand, serverPlayer, serverLevel);
        }

        resetRaid();
        this.discard();
    }

    private void resetRaid() {
//        raidDefinition  = null;

        if (raidTargetEntity instanceof RaidObjectiveEntity) {
            raidTargetEntity.discard();
        }
        raidTargetEntity          = null;
        raidOwner                 = null;
        startTargetRaidName       = null;
        raidOwnerUUID             = null;
        raidFinalizeCommandSuccess = null;
        raidFinalizeCommandFail    = null;
        currentRaidName           = null;
        raidTargetEntityUUID      = null;
        currentWaveIndex          = -1;
        tickCounter               = -1;
        endEventWhenKilledAllMobs = false;

        setBossBarVisibility(false);
    }

    private void killAllMobs() {
        if (this.raidDefinition != null && this.raidDefinition.type.equals(RaidType.ACTIVATE.getId())) {
            // NOP(Killしない)
        } else {
            for (Entity ent : spawnedMobs) {
                if (ent != null && ent.isAlive()) ent.kill();
            }
        }

        spawnedMobs.clear();
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide
                && (reason == RemovalReason.DISCARDED || reason == RemovalReason.KILLED)) {
            setBossBarVisibility(false);
            if (raidTargetEntity instanceof RaidObjectiveEntity && raidTargetEntity.isAlive()) {
                raidTargetEntity.discard();
            }
            killAllMobs();
        }
        super.remove(reason);
    }

    // -------------------------------------------------------------------------
    // ユーティリティ
    // -------------------------------------------------------------------------

    private void loadSpawnedMobs() {
        if (spawnedMobsTag == null) return;

        spawnedMobs.clear();
        for (Tag t : spawnedMobsTag) {
            UUID uuid = NbtUtils.loadUUID(t);
            Entity ent = ((ServerLevel) this.level()).getEntity(uuid);
            if (ent != null) spawnedMobs.add(ent);
        }
    }

    private LivingEntity getTargetCandidate() {
        if (raidOwnerUUID == null) return null;
        Entity res = ((ServerLevel) this.level()).getEntity(raidOwnerUUID);
        return (res instanceof LivingEntity le) ? le : null;
    }

    @SuppressWarnings("rawtypes")
    private EntityType[] getMobList(EventDefinition event) {
        List<EntityType<? extends Entity>> list = new ArrayList<>();
        for (SpawnTarget st : event.spawn_targets) {
            var s = Utils.getEntityType(st.mob_type);
            if (s != null) list.add(s.get());
        }
        return list.toArray(new EntityType[0]);
    }

    @SuppressWarnings("unchecked")
    private EntityType<? extends Entity> getRandomMobType(Random rand, EntityType<? extends Entity>[] mobList) {
        return mobList[rand.nextInt(mobList.length)];
    }

    private RegistrySupplier<EntityType<?>> getEntityType(String typeId) {
        ResourceLocation rl = new ResourceLocation(typeId);
        for (RegistrySupplier<EntityType<?>> entityType : PomkotsMechs.ENTITIES) {
            if (entityType.getId().equals(rl)) return entityType;
        }
        return null;
    }

    private boolean isFinalWave(RaidDefinition raidDef, int waveIndex) {
        return raidDef.waves == null || raidDef.waves.size() == waveIndex + 1;
    }

    private int[] remainingSpawnTargets(RaidDefinition raidDef, int waveIndex, int tick) {
        if (raidDef.waves == null || raidDef.waves.size() <= waveIndex) return null;

        WaveDefinition waveDef = raidDef.waves.get(waveIndex);
        if (waveDef.events == null) return new int[]{0, 0};

        int[] res = {0, 0};
        for (EventDefinition event : waveDef.events) {
            int count = "spawn".equals(event.type) ? event.spawn_targets.size()
                    : "spawn_random".equals(event.type) ? event.amount : 0;
            res[1] += count;
            if (event.trigger_tick >= tick) res[0] += count;
        }
        return res;
    }

    private int findSpawnY(Level level, int x, int startY, int z, AABB spaceSize) {
        int maxY  = startY + 50;
        int found = Integer.MIN_VALUE;

        for (int y = startY; y < maxY; y++) {
            AABB checkBox = spaceSize.move(x - spaceSize.minX, y - spaceSize.minY, z - spaceSize.minZ);
            if (level.noCollision(checkBox)) {
                found = y;
                break;
            }
        }

        return found != Integer.MIN_VALUE
                ? found
                : level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    private void runCommandAsOwner(String command, ServerPlayer player, ServerLevel serverLevel) {
        MinecraftServer server   = serverLevel.getServer();
        CommandSourceStack source = player.createCommandSourceStack()
                .withPermission(4);
        server.getCommands().performPrefixedCommand(source, command);
    }

    private void runCommandAsServer(String command, ServerPlayer player, ServerLevel serverLevel) {
        MinecraftServer server   = serverLevel.getServer();

//        if (player != null) {
//            command = replaceSelectorWithUUID(command, player);
//        }

        CommandSourceStack source = server.createCommandSourceStack()
                .withPermission(4)              // OPレベル最大
                .withSuppressedOutput()        // ログ汚したくないなら
                .withEntity(player)
                .withPosition(player.position())
                .withLevel(player.serverLevel());

        server.getCommands().performPrefixedCommand(source, command);
    }

    private String replaceSelectorWithUUID(String command, ServerPlayer player) {
        String selector = "@e[uuid=" + player.getUUID() + "]";

        return command
                .replace("@s", selector);
    }

    private void sendMessage(String message, Collection<ServerPlayer> players) {
        Component msg = Utils.string2Component(message);
        players.forEach(p -> p.sendSystemMessage(msg));
    }

    private void showTitlesForPlayers(String title, String subTitle, Collection<ServerPlayer> players) {
        ClientboundSetTitlesAnimationPacket timesPacket   = new ClientboundSetTitlesAnimationPacket(20, 60, 20);
        ClientboundSetTitleTextPacket       titlePacket   = new ClientboundSetTitleTextPacket(Utils.string2Component(title));
        ClientboundSetSubtitleTextPacket    subtitlePacket = new ClientboundSetSubtitleTextPacket(Utils.string2Component(subTitle));

        for (ServerPlayer player : players) {
            player.connection.send(timesPacket);
            player.connection.send(titlePacket);
            player.connection.send(subtitlePacket);
        }
    }

    public BlockPos parseBlockPos(String posString) {
        if (posString == null || posString.isEmpty()) return null;
        try {
            String[] parts = posString.trim().split("[,\\s]+");
            if (parts.length != 3) return null;
            return new BlockPos(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
            );
        } catch (Exception e) {
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Getter / Setter
    // -------------------------------------------------------------------------

    public void addSpawnedEntity(Entity ent)            { spawnedMobs.add(ent); }
    public int  getTickCounter()                         { return tickCounter; }
    public void setTickCounter(int v)                    { tickCounter = v; }
    public int  getCurrentWaveIndex()                    { return currentWaveIndex; }
    public void setCurrentWaveIndex(int v)               { currentWaveIndex = v; }

    // -------------------------------------------------------------------------
    // LivingEntity オーバーライド（ボイラープレート）
    // -------------------------------------------------------------------------

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return Utils.isSystemicDamage(source) && super.hurt(source, amount);
    }

    @Override public boolean         displayFireAnimation()                          { return false; }
    @Override public boolean         ignoreExplosion()                               { return true; }
    @Override public HumanoidArm     getMainArm()                                   { return null; }
    @Override public Iterable<ItemStack> getArmorSlots()                            { return NonNullList.withSize(4, ItemStack.EMPTY); }
    @Override public ItemStack       getItemBySlot(EquipmentSlot slot)              { return ItemStack.EMPTY; }
    @Override public void            setItemSlot(EquipmentSlot slot, ItemStack stack) { }
}