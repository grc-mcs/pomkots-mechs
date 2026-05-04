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
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RaidControllerEntityBack extends LivingEntity {
    private static final int MAX_LIVING_TICK = 120000;

    private RaidDefinition raidDefinition;
    private LivingEntity raidTargetEntity;

    // NBT保存対象
    private String startTargetRaidName = null;
    private UUID raidOwnerUUID = null;
    private Entity raidOwner = null;
    private String raidFinalizeCommandSuccess = null;
    private String raidFinalizeCommandFail = null;

    private String currentRaidName = null;
    private UUID raidTargetEntityUUID = null;

    private int currentWaveIndex = -1;
    private int tickCounter = -1;
    private List<Entity> spawnedMobs = new ArrayList<>();
    private ListTag spawnedMobsTag;
    private int spawnedMobsNum = -1;
    private boolean endEventWhenKilledAllMobs = false;

    // BossBars
    private final ServerBossEvent timeBar;
    private final ServerBossEvent baseHpBar;

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MAX_HEALTH, 1000)
                .add(Attributes.ARMOR, 20)
                .add(Attributes.ARMOR_TOUGHNESS, 1);
    }

    public static @NotNull CompoundTag buildCompoundTag(String startTargetRaidName, UUID raidOwnerUUID, String raidFinalizeCommandSuccess, String raidFinalizeCommandFail) {
        CompoundTag tag = new CompoundTag();
        tag.putString(PomkotsMechs.nbtName("StartTargetRaidName"), startTargetRaidName);
        if (raidOwnerUUID != null) tag.putUUID(PomkotsMechs.nbtName("RaidOwnerUUID"), raidOwnerUUID);
        if (raidFinalizeCommandSuccess != null) tag.putString(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"), raidFinalizeCommandSuccess);
        if (raidFinalizeCommandFail != null) tag.putString(PomkotsMechs.nbtName("RaidFinalizeCommandFail"), raidFinalizeCommandFail);
        return tag;
    }

    public static List<BaseBossEntity> getInactiveBossesAroundPos(Level level, BlockPos bPos) {
        return level.getEntitiesOfClass(BaseBossEntity.class, new AABB(bPos.getX(), bPos.getY(), bPos.getZ(), bPos.getX() + 1, bPos.getY() + 1, bPos.getZ() + 1).inflate(40)).stream().filter(b->b.getAiMode() == BaseBossEntity.AI_MODE_INACTIVE).toList();
    }

    public static List<Player> getPlayersAroundPos(Level level, BlockPos bPos) {
        return level.getEntitiesOfClass(Player.class, new AABB(bPos.getX(), bPos.getY(), bPos.getZ(), bPos.getX() + 1, bPos.getY() + 1, bPos.getZ() + 1).inflate(100));
    }

    public RaidControllerEntityBack(EntityType<? extends LivingEntity> entityType, Level world) {
        this(entityType, world, null);
    }

    public RaidControllerEntityBack(EntityType<? extends LivingEntity> entityType, Level world, BaseBossEntity parent) {
        super(entityType, world);
        this.noPhysics = true;
        this.setNoGravity(true);

        this.raidDefinition = null;
        this.raidTargetEntity = null;
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

        this.initBossBars();
    }

    private void initBossBars() {
        this.timeBar.setProgress(1.0f);
        this.baseHpBar.setProgress(1.0f);

        this.setBossBarVisibility(false);
    }

    private void setBossBarVisibility(boolean bl) {
        this.baseHpBar.setVisible(bl);
        this.timeBar.setVisible(bl);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);

        // スタート時に設定する必要があるタグ
        if (tag.contains(PomkotsMechs.nbtName("StartTargetRaidName"))) {
            startTargetRaidName = tag.getString(PomkotsMechs.nbtName("StartTargetRaidName"));
        }

        if (tag.contains(PomkotsMechs.nbtName("RaidOwnerUUID"))) {
            raidOwnerUUID = tag.getUUID(PomkotsMechs.nbtName("RaidOwnerUUID"));
        }

        if (tag.contains(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"))) {
            raidFinalizeCommandSuccess = tag.getString(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"));
        }

        if (tag.contains(PomkotsMechs.nbtName("RaidFinalizeCommandFail"))) {
            raidFinalizeCommandFail = tag.getString(PomkotsMechs.nbtName("RaidFinalizeCommandFail"));
        }

        // 開始後に実行状況を保存しておくためのタグ（外から設定することはない）
        if (tag.contains(PomkotsMechs.nbtName("CurrentRaidName"))) {
            currentRaidName = tag.getString(PomkotsMechs.nbtName("CurrentRaidName"));
        }

        if (tag.contains(PomkotsMechs.nbtName("RaidTargetEntityUUID"))) {
            raidTargetEntityUUID = tag.getUUID(PomkotsMechs.nbtName("RaidTargetEntityUUID"));
        }

        if (tag.contains(PomkotsMechs.nbtName("CurrentWaveIndex"))) {
            currentWaveIndex = tag.getInt(PomkotsMechs.nbtName("CurrentWaveIndex"));
        }

        if (tag.contains(PomkotsMechs.nbtName("TickCounter"))) {
            tickCounter = tag.getInt(PomkotsMechs.nbtName("TickCounter"));
        }

        if (tag.contains(PomkotsMechs.nbtName("EndEventWhenKilledAllMobs"))) {
            endEventWhenKilledAllMobs = tag.getBoolean(PomkotsMechs.nbtName("EndEventWhenKilledAllMobs"));
        }

        if (tag.contains(PomkotsMechs.nbtName("SpawnedMobsNum"))) {
            spawnedMobsNum = tag.getInt(PomkotsMechs.nbtName("SpawnedMobsNum"));
        }

        this.spawnedMobsTag = (ListTag) tag.get(PomkotsMechs.nbtName("SpawnedMobs"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        // スタート時に設定する必要があるタグ
        if (this.startTargetRaidName != null) {
            tag.putString(PomkotsMechs.nbtName("StartTargetRaidName"), startTargetRaidName);
        } else {
            tag.remove(PomkotsMechs.nbtName("StartTargetRaidName"));
        }

        if (this.raidOwnerUUID != null) {
            tag.putUUID(PomkotsMechs.nbtName("RaidOwnerUUID"), raidOwnerUUID);
        } else {
            tag.remove(PomkotsMechs.nbtName("RaidOwnerUUID"));
        }

        if (this.raidFinalizeCommandSuccess != null) {
            tag.putString(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"), raidFinalizeCommandSuccess);
        } else {
            tag.remove(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"));
        }

        if (this.raidFinalizeCommandFail != null) {
            tag.putString(PomkotsMechs.nbtName("RaidFinalizeCommandFail"), raidFinalizeCommandFail);
        } else {
            tag.remove(PomkotsMechs.nbtName("RaidFinalizeCommandFail"));
        }

        // 開始後に実行状況を保存しておくためのタグ（外から設定することはない）
        if (this.currentRaidName != null) {
            tag.putString(PomkotsMechs.nbtName("CurrentRaidName"), currentRaidName);
        } else {
            tag.remove(PomkotsMechs.nbtName("CurrentRaidName"));
        }

        if (this.raidTargetEntityUUID != null) {
            tag.putUUID(PomkotsMechs.nbtName("RaidTargetEntityUUID"), raidTargetEntityUUID);
        } else {
            tag.remove(PomkotsMechs.nbtName("RaidTargetEntityUUID"));
        }

        if (this.currentWaveIndex != -1) {
            tag.putInt(PomkotsMechs.nbtName("CurrentWaveIndex"), currentWaveIndex);
        } else {
            tag.remove(PomkotsMechs.nbtName("CurrentWaveIndex"));
        }

        if (this.tickCounter != -1) {
            tag.putInt(PomkotsMechs.nbtName("TickCounter"), tickCounter);
        } else {
            tag.remove(PomkotsMechs.nbtName("TickCounter"));
        }

        if (this.endEventWhenKilledAllMobs) {
            tag.putBoolean(PomkotsMechs.nbtName("EndEventWhenKilledAllMobs"), endEventWhenKilledAllMobs);
        } else {
            tag.remove(PomkotsMechs.nbtName("EndEventWhenKilledAllMobs"));
        }

        ListTag list = new ListTag();
        for (Entity ent : spawnedMobs) {
            list.add(NbtUtils.createUUID(ent.getUUID()));
        }
        tag.put(PomkotsMechs.nbtName("SpawnedMobs"), list);


        if (this.spawnedMobsNum != -1) {
            tag.putInt(PomkotsMechs.nbtName("SpawnedMobsNum"), spawnedMobsNum);
        } else {
            tag.remove(PomkotsMechs.nbtName("SpawnedMobsNum"));
        }
    }

    private void loadSpawnedMobs() {
        if (spawnedMobsTag != null) {
            spawnedMobs.clear();

            if (spawnedMobsTag != null) {
                for (Tag t : spawnedMobsTag) {
                    UUID uuid = NbtUtils.loadUUID(t);
                    var ent = ((ServerLevel)this.level()).getEntity(uuid);

                    if (ent != null) {
                        spawnedMobs.add(ent);
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        if (this.firstTick && !this.level().isClientSide) {
            loadSpawnedMobs();
        }

        super.tick();

        if (this.level().isClientSide) {
            return;
        }

        if (this.tickCount > MAX_LIVING_TICK) {
            PomkotsMechs.LOGGER.info("MAX Tick Count exceeded:" + this);
            this.discard();
        }

        ServerLevel serverLevel = (ServerLevel) this.level();

        if (!this.spawnedMobs.isEmpty()) {
            this.spawnedMobs.removeIf(e -> e == null || !e.isAlive());
        }

        if (this.startTargetRaidName != null) {
            this.raidDefinition = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(this.startTargetRaidName);

            if (this.raidDefinition != null) {
                this.currentRaidName = this.startTargetRaidName;
                this.tickCounter = 0;
                this.currentWaveIndex = 0;

                // どうもエンティティのTick前にseenしちゃうケースがあるらしいので補完
                if (!baseHpBar.getPlayers().isEmpty()) {
                    for (ServerPlayer player: baseHpBar.getPlayers()) {
                        startBGMforPlayer(player, raidDefinition);
                    }
                }

                if ("defense".equals(this.raidDefinition.type)) {
                    RaidObjectiveEntity roe = PomkotsMechs.RAID_OBJECTIVE.get().create(serverLevel);
                    if (roe != null) {
                        var pos = this.blockPosition();
                        roe.setPos(pos.getX(), pos.getY(), pos.getZ());
                        this.level().addFreshEntity(roe);
                        this.raidTargetEntity = roe;
                        this.raidTargetEntityUUID = this.raidTargetEntity.getUUID();
                    }
                } else if ("survive".equals(this.raidDefinition.type)) {
                    this.raidTargetEntity = (LivingEntity) serverLevel.getEntity(this.raidOwnerUUID);
                    this.raidTargetEntityUUID = this.raidOwnerUUID;
                } else if ("sweep".equals(this.raidDefinition.type)) {
                    this.raidTargetEntity = this;
                    this.raidTargetEntityUUID = this.getUUID();
                } else if ("activate".equals(this.raidDefinition.type)) {
                    this.raidTargetEntity = this;
                    this.raidTargetEntityUUID = this.getUUID();

                    var bosses = getInactiveBossesAroundPos(this.level(), this.blockPosition());
                    var players = getPlayersAroundPos(this.level(), this.blockPosition());
                    for (var boss: bosses) {
                        boss.boot();
                        for (var player: players) {
                            boss.addHateToEntity(player, 50);
                        }
                        this.spawnedMobs.add(boss);
                    }
                }
            } else {
                PomkotsMechs.LOGGER.error("Raid def not found:" + startTargetRaidName);
                this.discard();
                return;
            }

            this.startTargetRaidName = null;
        }

        if (this.raidOwnerUUID == null) {
            this.raidOwner = null;
        } else {
            this.raidOwner = serverLevel.getEntity(this.raidOwnerUUID);
        }

        if (this.currentRaidName == null) {
            return;

        } else if (this.raidTargetEntity == null){
            // レイド中にサーバが停止されて再起動した場合
            this.raidTargetEntity = (LivingEntity) serverLevel.getEntity(this.raidTargetEntityUUID);
            this.raidDefinition = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(this.currentRaidName);
        }

        if (this.raidDefinition == null || this.raidTargetEntity == null) {
            return;
        }

        WaveDefinition wave = this.raidDefinition.waves.get(this.currentWaveIndex);
        if (this.tickCounter == 5) {
            this.showTitlesForPlayers(wave.title, wave.sub_title, this.baseHpBar.getPlayers());
        }

        this.tickCounter++;

        if (!this.baseHpBar.isVisible()) {
            this.setBossBarVisibility(true);
        }

        int[] remainMaxMobs = null;
        if ("sweep".equals(this.raidDefinition.type)) {
            remainMaxMobs = remainingSpawnTargets(this.raidDefinition, this.currentWaveIndex, this.tickCounter);
        }

        this.updateBossBars(wave, remainMaxMobs);

        if (raidOwner == null || !raidOwner.isAlive()) {
            this.endRaid(false, "レイドオーナーが消失");
            return;
        }

        if ("defense".equals(this.raidDefinition.type) || "survive".equals(this.raidDefinition.type)) {
            // 拠点が死亡していたら敗北
            if (this.raidTargetEntity == null || !this.raidTargetEntity.isAlive()) {
                this.endRaid(false);
                return;
            }
        } else if ("sweep".equals(this.raidDefinition.type)) {
            if (remainMaxMobs == null) {
                this.endRaid(false);
            } else if (spawnedMobs.isEmpty() && remainMaxMobs[0] <= 0) {
                if (isFinalWave(raidDefinition, currentWaveIndex)) {
                    this.endRaid(true);
                } else {
                    this.currentWaveIndex++;
                    this.tickCounter = 0;
                }
                return;
            }
        } else if ("activate".equals(this.raidDefinition.type)) {
            if (spawnedMobs.isEmpty()) {
                this.endRaid(true);
            }
        }

        if (raidDefinition != null && "survive".equals(raidDefinition.type)) {
            this.setPos(this.raidTargetEntity.position());
        }

        if (this.endEventWhenKilledAllMobs) {
            boolean end = true;
            for (var ent: spawnedMobs) {
                if (!(ent instanceof Pmc01Entity pmc01)) {
                    end = false;
                    break;
                } else if (!"".equals(pmc01.containerType)) {
                    end = false;
                    break;
                }
            }
            if (end) {
                this.tickCounter = wave.duration_ticks;
            }
        }

        // 定義されたイベントを発火
        this.fireEvent(wave);

        // Wave終了判定
        if (this.tickCounter >= wave.duration_ticks) {
            if ("defense".equals(this.raidDefinition.type) || "survive".equals(this.raidDefinition.type)) {
                this.currentWaveIndex++;
                this.tickCounter = 0;
                this.killAllMobs();

                if (this.currentWaveIndex >= this.raidDefinition.waves.size()) {
                    // 全Wave終了 → 勝利
                    this.endRaid(true);
                }
            } else if ("sweep".equals(this.raidDefinition.type) || "activate".equals(this.raidDefinition.type)) {
                this.killAllMobs();
                this.endRaid(false);
            }
        }
    }

    private boolean isFinalWave(RaidDefinition raidDef, int currentWaveIndex) {
        if (raidDef.waves == null) {
            return true;
        } else {
            return raidDef.waves.size() == currentWaveIndex + 1;
        }
    }

    private int[] remainingSpawnTargets(RaidDefinition raidDef, int currentWaveIndex, int tickCounter) {
        if (raidDef.waves == null || raidDef.waves.size() <= currentWaveIndex) {
            return null;
        } else {
            var eventDef = raidDef.waves.get(currentWaveIndex);

            int[] res = {0, 0};

            if (eventDef.events != null) {
                for (var event: eventDef.events) {
                    if (event.trigger_tick >= tickCounter) {
                        if ("spawn".equals(event.type)) {
                            res[0] += event.spawn_targets.size();
                        } else if ("spawn_random".equals(event.type)) {
                            res[0] += event.amount;
                        }
                    }

                    if ("spawn".equals(event.type)) {
                        res[1] += event.spawn_targets.size();
                    } else if ("spawn_random".equals(event.type)) {
                        res[1] += event.amount;
                    }
                }
            }

            return res;
        }
    }

    @Override
    public void remove(RemovalReason removalReason) {
        if (!level().isClientSide
                && (removalReason == RemovalReason.DISCARDED || removalReason == RemovalReason.KILLED)) {
            this.setBossBarVisibility(false);
            if (this.raidTargetEntity != null && this.raidTargetEntity.isAlive() && (raidTargetEntity instanceof RaidObjectiveEntity)) {
                raidTargetEntity.discard();
            }
            killAllMobs();
        }
        super.remove(removalReason);
    }

    private void killAllMobs() {
        if (this.level() instanceof ServerLevel sl) {
            for (Entity ent: spawnedMobs) {
                if (ent != null && ent.isAlive()) {
                    ent.kill();
                }
            }
            spawnedMobs.clear();
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        baseHpBar.addPlayer(player);
        timeBar.addPlayer(player);

        if (!firstTick) {
            startBGMforPlayer(player, this.raidDefinition);
        }
    }

    private void startBGMforPlayer(ServerPlayer player, RaidDefinition raidDefinition) {
        if (raidDefinition != null) {
            if ("defense".equals(raidDefinition.type)) {
                ServerBGMTracker.forceSetBGMState(player, BGMState.BATTLE_RAID);
            } else {
                ServerBGMTracker.forceSetBGMState(player, BGMState.BATTLE_BOSS);
            }
        }
    }

    public static Optional<SoundEvent> getSoundEvent(ResourceLocation id) {
        return BuiltInRegistries.SOUND_EVENT.getOptional(id);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        baseHpBar.removePlayer(player);
        timeBar.removePlayer(player);
        ServerBGMTracker.forceUnsetBGMState(player);
    }

    private void sendMessage(String message, Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            player.sendSystemMessage(Component.literal(message));
        }
    }

    private void showTitlesForPlayers(String title, String subTitle, Collection<ServerPlayer> players) {
        // フェードイン 20 ticks, 表示 60 ticks, フェードアウト 20 ticks
        ClientboundSetTitlesAnimationPacket timesPacket =
                new ClientboundSetTitlesAnimationPacket(20, 60, 20);

        // メインタイトル
        ClientboundSetTitleTextPacket titlePacket =
                new ClientboundSetTitleTextPacket(Component.literal(title));

        // サブタイトル
        ClientboundSetSubtitleTextPacket subtitlePacket =
                new ClientboundSetSubtitleTextPacket(Component.literal(subTitle));

        // 全プレイヤーに送信
        for (ServerPlayer player : players) {
            player.connection.send(timesPacket);
            player.connection.send(titlePacket);
            player.connection.send(subtitlePacket);
        }
    }

    private int prevRemaining = 0;
    private void updateBossBars(WaveDefinition wave, int[] remainingMobs) {
        if (raidTargetEntity == null) {
            return;
        }
        // 残り時間
        int remaining = wave.duration_ticks - tickCounter;

        if (remaining == prevRemaining) {
            return;
        }

        float timeProgress = (float) remaining / wave.duration_ticks;

        this.timeBar.setName(Component.literal(
                wave.title + " | Time: " + (remaining / 20) + "sec"
        ));
        this.timeBar.setProgress(Math.max(0f, Math.min(1f, timeProgress)));

        // 進捗設定
        if ("sweep".equals(this.raidDefinition.type) && remainingMobs != null) {
            float remainProgress = (remainingMobs[0] + spawnedMobs.size()) / (float)remainingMobs[1];
            this.baseHpBar.setName(Component.literal(
                    "Target Monsters: " + (remainingMobs[0] + spawnedMobs.size()) + "/" + remainingMobs[1]
            ));
            this.baseHpBar.setProgress(Math.max(0f, Math.min(1f, remainProgress)));
        } else if ("activate".equals(this.raidDefinition.type) && spawnedMobs != null) {
            //NOP
        } else {
            float healthProgress = raidTargetEntity.getHealth() / raidTargetEntity.getMaxHealth();
            this.baseHpBar.setName(Component.literal(
                    "Target HP: " + (int) raidTargetEntity.getHealth() + "/" + (int) raidTargetEntity.getMaxHealth()
            ));
            this.baseHpBar.setProgress(Math.max(0f, Math.min(1f, healthProgress)));
        }

        prevRemaining = remaining;
    }

    private void fireEvent(WaveDefinition wave) {
        if (wave.timeline != null) {
            var event = wave.timeline.get(tickCounter);
            if (event != null) {
                switch (event.type) {
                    case "spawn":
                        fireSpawnEvent(event);
                        break;
                    case "spawn_random":
                        fireSpawnRandomEvent(event);
                        break;
                    default:
                        // NOP
                }

                if (event.message != null) {
                    this.sendMessage(event.message, baseHpBar.getPlayers());
                }

                this.endEventWhenKilledAllMobs = event.force_end_wave_when_killed_all;
            }
        }
    }

    private void fireSpawnEvent(EventDefinition event) {
        if (event.spawn_targets != null) {
            for (SpawnTarget st: event.spawn_targets) {
                var type = getEntityType(st.mob_type);
                if (type != null && st.position != null) {
                    var pos = parseBlockPos(st.position);

                    if (pos != null && this.level() != null) {
                        Entity entity = type.get().create(this.level());
                        if (entity != null) {
                            var offset = this.blockPosition();
                            entity.setPos(
                                    offset.getX() + pos.getX(),
                                    offset.getY() + pos.getY(),
                                    offset.getZ() + pos.getZ()
                            );

                            if (st.snbt != null && entity instanceof LivingEntity le) {
                                try {
                                    le.readAdditionalSaveData(TagParser.parseTag(st.snbt));
                                } catch (Exception e) {
                                    PomkotsMechs.LOGGER.error("failed to parse snbt.", e);
                                }
                            }

                            if ("defense".equals(this.raidDefinition.type)) {
                                if (entity instanceof Mob mons) {
                                    mons.setTarget(raidTargetEntity);
                                }

                                if (entity instanceof GenericPomkotsMonster pomkots) {
                                    pomkots.setInRaid(true);
                                    pomkots.setInEvent(true);
                                }
                            } else if ("survive".equals(this.raidDefinition.type)
                                || "sweep".equals(this.raidDefinition.type)) {
                                if (entity instanceof Mob mons) {
                                    if (entity instanceof GenericPomkotsMonster pomkots) {
                                        pomkots.setInEvent(true);
                                    }
                                    if (((ServerLevel)this.level()).getEntity(this.raidOwnerUUID) instanceof LivingEntity le) {
                                        mons.setTarget(le);
                                    }
                                }
                            }

                            if (entity instanceof Pmc01Entity pmc01) {
//                                pmc01.raidControllerEntity = this;
                                pmc01.raidControllerEntityUUID = this.getUUID();

                            } else if (entity instanceof Mob mob){
                                mob.setPersistenceRequired();
                            }

                            spawnedMobs.add(entity);
                            this.level().addFreshEntity(entity);
                        }
                    }
                }
            }
        }
    }

    private void fireSpawnRandomEvent(EventDefinition event) {
        if (event.spawn_targets != null) {
            Random rand = new Random();
            var level = this.level();

            var mobTypeList = getMobList(event);
            var count = event.amount;
            var rangex = event.range_x;
            var rangez = event.range_z;

            var centerPos = this.blockPosition();
            var opener = getTargetCandidate();

            int spawned = 0;
            for (int i = 0; i < 100 && spawned < count; i++) {
                int x = centerPos.getX() + rand.nextInt(rangex) - rangex / 2;
                int z = centerPos.getZ() + rand.nextInt(rangez) - rangez / 2;

//                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                int y = findSpawnY(level, x, centerPos.getY(), z, new AABB(0,0,0,10,10,10));

                Entity mob = getRandomMobType(rand, mobTypeList).create(level);
                if (mob != null) {
                    mob.moveTo(x + 0.5, y, z + 0.5, rand.nextFloat() * 360F, 0);

                    if (mob instanceof Mob m) {
                        if (opener != null) {
                            m.setTarget(opener);
                        }
                        if (m instanceof BaseSmallMonsterEntity small){
                            small.setPersistenceRequired();
                            small.setInEvent(true);
                        }
                    }

                    level.addFreshEntity(mob);
                    spawned++;
                    spawnedMobs.add(mob);
                }
            }
        }
    }

    private int findSpawnY(Level level, int x, int startY, int z, AABB spaceSize) {
        int maxY = startY + 50;
        int res = Integer.MIN_VALUE;

        for (int y = startY; y < maxY; y++) {
            AABB checkBox = spaceSize.move(x - spaceSize.minX, y - spaceSize.minY, z - spaceSize.minZ);

            if (!level.noCollision(checkBox)) continue;

            res = y;
        }

        if (res == Integer.MIN_VALUE) {
            return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        } else {
            return res;
        }
    }

    private LivingEntity getTargetCandidate() {
        if (this.raidOwnerUUID == null) {
            return null;
        } else {
            var res = ((ServerLevel)(this.level())).getEntity(this.raidOwnerUUID);
            if (res instanceof LivingEntity r) {
                return r;
            } else {
                return null;
            }
        }
    }

    private EntityType[] getMobList(EventDefinition event) {
        List<EntityType<? extends Entity>> list = new ArrayList<>();
        for (var ent: event.spawn_targets) {
            var s = Utils.getEntityType(ent.mob_type);

            if (s != null) {
                list.add(s.get());
            }
        }

        return list.toArray(new EntityType[]{});
    }

    private EntityType<? extends Entity> getRandomMobType(Random rand, EntityType<? extends Entity>[] mobList) {
        int max = mobList.length;
        return mobList[(Math.abs(rand.nextInt()) % max)];
    }

    public void addSpawnedEntity(Entity ent) {
        spawnedMobs.add(ent);
    }

    public BlockPos parseBlockPos(String posString) {
        if (posString == null || posString.isEmpty()) {
            return null;
        }

        try {
            // カンマまたは空白で分割
            String[] parts = posString.trim().split("[,\\s]+");
            if (parts.length != 3) {
                return null;
            }

            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
            int z = Integer.parseInt(parts[2]);
            return new BlockPos(x, y, z);
        } catch (Exception e) {
            // 数字でない、フォーマット違いなどの場合
            return null;
        }
    }

    private RegistrySupplier<EntityType<?>> getEntityType(String typeId) {
        ResourceLocation rl = new ResourceLocation(typeId);

        RegistrySupplier<EntityType<?>> et = null;
        for (RegistrySupplier<EntityType<?>> entityType : PomkotsMechs.ENTITIES) {
            if (entityType.getId().equals(rl)) {
                et = entityType;
            }
        }

        return et;
    }

    private void endRaid(boolean victory) {
        this.endRaid(victory, "");
    }

    private void endRaid(boolean victory, String subTitle) {
        String finalizeCommand = null;
        if (victory) {
            this.showTitlesForPlayers("Mission Complete!!", subTitle, this.baseHpBar.getPlayers());
            finalizeCommand = this.raidFinalizeCommandSuccess;
        } else {
            this.showTitlesForPlayers("Mission Failed...", subTitle, this.baseHpBar.getPlayers());
            finalizeCommand = this.raidFinalizeCommandFail;
        }

        if (this.level() instanceof ServerLevel serverLevel && raidOwnerUUID != null && finalizeCommand != null) {
            if (serverLevel.getPlayerByUUID(raidOwnerUUID) instanceof ServerPlayer serverPlayer) {
                runCommandAsOwner(finalizeCommand, serverPlayer, serverLevel);
            }
        }

        resetRaid();

        this.discard();
    }

    private void runCommandAsOwner(String command, ServerPlayer serverPlayer, ServerLevel serverLevel) {
        MinecraftServer server = serverLevel.getServer();
        CommandSourceStack source = serverPlayer.createCommandSourceStack();

        server.getCommands().performPrefixedCommand(source, command);
    }

    private void resetRaid() {
        raidDefinition = null;

        if (raidTargetEntity != null && (raidTargetEntity instanceof RaidObjectiveEntity)) {
            raidTargetEntity.discard();
        }
        raidTargetEntity = null;
        startTargetRaidName = null;
        raidOwnerUUID = null;
        raidOwner = null;
        raidFinalizeCommandSuccess = null;
        raidFinalizeCommandFail = null;

        currentRaidName = null;
        raidTargetEntityUUID = null;
        currentWaveIndex = -1;
        tickCounter = -1;
        endEventWhenKilledAllMobs = false;

        this.setBossBarVisibility(false);
    }

    public int getTickCounter() {
        return tickCounter;
    }

    public void setTickCounter(int tickCounter) {
        this.tickCounter = tickCounter;
    }

    public int getCurrentWaveIndex() {
        return currentWaveIndex;
    }

    public void setCurrentWaveIndex(int currentWaveIndex) {
        this.currentWaveIndex = currentWaveIndex;
    }

    // そのほか

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (Utils.isSystemicDamage(source)) {
            return super.hurt(source, amount);
        } else {
            return false;
        }
    }

    @Override
    public boolean displayFireAnimation() {
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
    public boolean ignoreExplosion() {
        return true;
    }

    @Override
    public HumanoidArm getMainArm() {
        return null;
    }
}