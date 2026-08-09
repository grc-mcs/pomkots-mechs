package grcmcs.minecraft.mods.pomkotsmechs.mission.runtime;

import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionState;
import grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionEntityCleanupPolicy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public final class MissionInstance {
    private final UUID instanceId;
    private final ResourceLocation missionId;
    private final int definitionVersion;
    private final UUID ownerId;
    private final String ownerName;
    private final Set<UUID> participantIds;
    private final GlobalPos anchor;
    private final List<String> successCommands;
    private final List<String> failureCommands;
    private final long startedGameTime;
    private MissionState state;
    private int stageIndex;
    private String stageId;
    private int stageTick;
    private int stageSuccessPendingTick;
    private final Set<String> firedEventIds;
    private final Map<String, Integer> sequenceStartTicks;
    private final Map<String, Set<UUID>> trackedEntityGroups;
    private final Set<String> spawnedGroups;
    private final Set<UUID> deadTrackedEntities;
    private final Set<String> acquiredItemGroups;
    private final List<ForcedChunk> forcedChunks;
    private final Map<UUID, TrackedEntityCleanup> trackedEntityCleanup;
    private final Map<UUID, String> trackedEntityCleanupGroups;
    private final Map<String, Set<UUID>> spawnSourceGroups;
    private final Set<UUID> completedSpawnSources;
    private boolean callbackExecuted;

    public MissionInstance(
            UUID instanceId,
            ResourceLocation missionId,
            int definitionVersion,
            UUID ownerId,
            String ownerName,
            Collection<UUID> participantIds,
            GlobalPos anchor,
            Collection<String> successCommands,
            Collection<String> failureCommands,
            long startedGameTime,
            String firstStageId
    ) {
        this.instanceId = instanceId;
        this.missionId = missionId;
        this.definitionVersion = definitionVersion;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.participantIds = new HashSet<>(participantIds);
        this.anchor = anchor;
        this.successCommands = List.copyOf(successCommands);
        this.failureCommands = List.copyOf(failureCommands);
        this.startedGameTime = startedGameTime;
        this.state = MissionState.RUNNING;
        this.stageIndex = 0;
        this.stageId = firstStageId;
        this.stageTick = 0;
        this.stageSuccessPendingTick = -1;
        this.firedEventIds = new HashSet<>();
        this.sequenceStartTicks = new HashMap<>();
        this.trackedEntityGroups = new HashMap<>();
        this.spawnedGroups = new HashSet<>();
        this.deadTrackedEntities = new HashSet<>();
        this.acquiredItemGroups = new HashSet<>();
        this.forcedChunks = new ArrayList<>();
        this.trackedEntityCleanup = new HashMap<>();
        this.trackedEntityCleanupGroups = new HashMap<>();
        this.spawnSourceGroups = new HashMap<>();
        this.completedSpawnSources = new HashSet<>();
    }

    public UUID instanceId() { return instanceId; }
    public ResourceLocation missionId() { return missionId; }
    public int definitionVersion() { return definitionVersion; }
    public UUID ownerId() { return ownerId; }
    public String ownerName() { return ownerName; }
    public Set<UUID> participantIds() { return participantIds; }
    public GlobalPos anchor() { return anchor; }
    public List<String> successCommands() { return successCommands; }
    public List<String> failureCommands() { return failureCommands; }
    public long startedGameTime() { return startedGameTime; }
    public MissionState state() { return state; }
    public int stageIndex() { return stageIndex; }
    public String stageId() { return stageId; }
    public int stageTick() { return stageTick; }
    public int stageSuccessPendingTick() { return stageSuccessPendingTick; }
    public Set<String> firedEventIds() { return firedEventIds; }
    public Map<String, Integer> sequenceStartTicks() { return sequenceStartTicks; }
    public boolean callbackExecuted() { return callbackExecuted; }
    public Map<String, Set<UUID>> trackedEntityGroups() { return trackedEntityGroups; }
    public Set<String> spawnedGroups() { return spawnedGroups; }
    public Set<UUID> deadTrackedEntities() { return deadTrackedEntities; }
    public Set<String> acquiredItemGroups() { return acquiredItemGroups; }
    public List<ForcedChunk> forcedChunks() { return forcedChunks; }
    public Map<UUID, TrackedEntityCleanup> trackedEntityCleanup() { return trackedEntityCleanup; }
    public Map<UUID, String> trackedEntityCleanupGroups() { return trackedEntityCleanupGroups; }
    public Map<String, Set<UUID>> spawnSourceGroups() { return spawnSourceGroups; }
    public Set<UUID> completedSpawnSources() { return completedSpawnSources; }

    public void setState(MissionState state) { this.state = state; }
    public void incrementStageTick() { stageTick++; }
    public void beginStageSuccessPending() {
        if (stageSuccessPendingTick < 0) stageSuccessPendingTick = stageTick;
    }
    public void setCallbackExecuted(boolean value) { callbackExecuted = value; }
    public void removeParticipant(UUID playerId) { participantIds.remove(playerId); }
    public void trackEntity(String group, UUID entityId) {
        trackEntity(group, entityId, MissionEntityCleanupPolicy.DISCARD, null);
    }
    public void trackEntity(String group, UUID entityId, MissionEntityCleanupPolicy policy, CompoundTag restoreData) {
        spawnedGroups.add(group);
        trackedEntityGroups.computeIfAbsent(group, ignored -> new HashSet<>()).add(entityId);
        trackEntityForCleanup(group, entityId, policy, restoreData);
    }
    public void trackEntityForCleanup(
            UUID entityId,
            MissionEntityCleanupPolicy policy,
            CompoundTag restoreData
    ) {
        trackEntityForCleanup(null, entityId, policy, restoreData);
    }
    public void trackEntityForCleanup(
            String group,
            UUID entityId,
            MissionEntityCleanupPolicy policy,
            CompoundTag restoreData
    ) {
        trackedEntityCleanup.put(entityId, new TrackedEntityCleanup(
                policy, restoreData == null ? new CompoundTag() : restoreData.copy()));
        if (group != null && !group.isBlank()) trackedEntityCleanupGroups.put(entityId, group);
    }
    public void trackSpawnSource(String group, UUID entityId) {
        spawnedGroups.add(group);
        spawnSourceGroups.computeIfAbsent(group, ignored -> new HashSet<>()).add(entityId);
    }
    public boolean markSpawnSourceCompleted(UUID entityId) {
        return completedSpawnSources.add(entityId);
    }
    public void markGroupSpawned(String group) { spawnedGroups.add(group); }
    public boolean markTrackedEntityDead(UUID entityId) {
        return trackedEntityGroups.values().stream().anyMatch(ids -> ids.contains(entityId))
                && deadTrackedEntities.add(entityId);
    }
    public boolean markTrackedItemAcquired(UUID entityId) {
        boolean changed = false;
        for (Map.Entry<String, Set<UUID>> entry : trackedEntityGroups.entrySet()) {
            if (entry.getValue().contains(entityId)) changed |= acquiredItemGroups.add(entry.getKey());
        }
        return changed;
    }
    public void addForcedChunk(ResourceLocation dimension, int x, int z, boolean originallyForced) {
        addForcedChunk(dimension, x, z, originallyForced, false);
    }
    public void addForcedChunk(
            ResourceLocation dimension, int x, int z, boolean originallyForced, boolean dynamic
    ) {
        forcedChunks.add(new ForcedChunk(dimension, x, z, originallyForced, dynamic));
    }

    public void proceedToStage(int index, String id, Set<String> preservedGroups) {
        Set<UUID> preservedEntityIds = new HashSet<>();
        for (String group : preservedGroups) {
            preservedEntityIds.addAll(trackedEntityGroups.getOrDefault(group, Set.of()));
        }
        Set<UUID> preservedSourceIds = new HashSet<>();
        for (String group : preservedGroups) {
            preservedSourceIds.addAll(spawnSourceGroups.getOrDefault(group, Set.of()));
        }

        stageIndex = index;
        stageId = id;
        stageTick = 0;
        stageSuccessPendingTick = -1;
        firedEventIds.clear();
        sequenceStartTicks.clear();
        trackedEntityGroups.keySet().retainAll(preservedGroups);
        spawnedGroups.retainAll(preservedGroups);
        spawnSourceGroups.keySet().retainAll(preservedGroups);
        completedSpawnSources.retainAll(preservedSourceIds);
        deadTrackedEntities.retainAll(preservedEntityIds);
        acquiredItemGroups.retainAll(preservedGroups);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("InstanceId", instanceId);
        tag.putString("MissionId", missionId.toString());
        tag.putInt("DefinitionVersion", definitionVersion);
        tag.putUUID("OwnerId", ownerId);
        tag.putString("OwnerName", ownerName);
        tag.putString("State", state.name());
        tag.putString("AnchorDimension", anchor.dimension().location().toString());
        tag.putLong("AnchorPos", anchor.pos().asLong());
        tag.putLong("StartedGameTime", startedGameTime);
        tag.putInt("StageIndex", stageIndex);
        tag.putString("StageId", stageId);
        tag.putInt("StageTick", stageTick);
        tag.putInt("StageSuccessPendingTick", stageSuccessPendingTick);
        tag.putBoolean("CallbackExecuted", callbackExecuted);

        ListTag participants = new ListTag();
        participantIds.forEach(id -> participants.add(NbtUtils.createUUID(id)));
        tag.put("Participants", participants);
        tag.put("SuccessCommands", saveStrings(successCommands));
        tag.put("FailureCommands", saveStrings(failureCommands));
        tag.put("FiredEvents", saveStrings(firedEventIds));
        CompoundTag sequenceStarts = new CompoundTag();
        sequenceStartTicks.forEach(sequenceStarts::putInt);
        tag.put("SequenceStartTicks", sequenceStarts);
        CompoundTag groups = new CompoundTag();
        trackedEntityGroups.forEach((group, ids) -> {
            ListTag list = new ListTag();
            ids.forEach(id -> list.add(NbtUtils.createUUID(id)));
            groups.put(group, list);
        });
        tag.put("TrackedEntityGroups", groups);
        tag.put("SpawnedGroups", saveStrings(spawnedGroups));
        ListTag dead = new ListTag();
        deadTrackedEntities.forEach(id -> dead.add(NbtUtils.createUUID(id)));
        tag.put("DeadTrackedEntities", dead);
        tag.put("AcquiredItemGroups", saveStrings(acquiredItemGroups));
        ListTag chunks = new ListTag();
        for (ForcedChunk chunk : forcedChunks) {
            CompoundTag chunkTag = new CompoundTag();
            chunkTag.putString("Dimension", chunk.dimension().toString());
            chunkTag.putInt("X", chunk.x());
            chunkTag.putInt("Z", chunk.z());
            chunkTag.putBoolean("OriginallyForced", chunk.originallyForced());
            chunkTag.putBoolean("Dynamic", chunk.dynamic());
            chunks.add(chunkTag);
        }
        tag.put("ForcedChunks", chunks);
        ListTag cleanupIds = new ListTag();
        trackedEntityCleanup.keySet().forEach(id -> cleanupIds.add(NbtUtils.createUUID(id)));
        tag.put("CleanupEntityIds", cleanupIds);
        ListTag cleanupEntries = new ListTag();
        trackedEntityCleanup.forEach((id, cleanup) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("EntityId", id);
            entry.putString("Policy", cleanup.policy().name());
            String cleanupGroup = trackedEntityCleanupGroups.get(id);
            if (cleanupGroup != null) entry.putString("Group", cleanupGroup);
            if (!cleanup.restoreData().isEmpty()) entry.put("RestoreData", cleanup.restoreData().copy());
            cleanupEntries.add(entry);
        });
        tag.put("TrackedEntityCleanup", cleanupEntries);
        CompoundTag sources = new CompoundTag();
        spawnSourceGroups.forEach((group, ids) -> {
            ListTag list = new ListTag();
            ids.forEach(id -> list.add(NbtUtils.createUUID(id)));
            sources.put(group, list);
        });
        tag.put("SpawnSourceGroups", sources);
        ListTag completedSources = new ListTag();
        completedSpawnSources.forEach(id -> completedSources.add(NbtUtils.createUUID(id)));
        tag.put("CompletedSpawnSources", completedSources);
        return tag;
    }

    public static MissionInstance load(CompoundTag tag) {
        ResourceKey<Level> dimension = ResourceKey.create(
                Registries.DIMENSION,
                new ResourceLocation(tag.getString("AnchorDimension"))
        );
        Set<UUID> participants = new HashSet<>();
        for (Tag element : tag.getList("Participants", Tag.TAG_INT_ARRAY)) {
            participants.add(NbtUtils.loadUUID(element));
        }
        MissionInstance instance = new MissionInstance(
                tag.getUUID("InstanceId"),
                new ResourceLocation(tag.getString("MissionId")),
                tag.getInt("DefinitionVersion"),
                tag.getUUID("OwnerId"),
                tag.getString("OwnerName"),
                participants,
                GlobalPos.of(dimension, BlockPos.of(tag.getLong("AnchorPos"))),
                loadStrings(tag.getList("SuccessCommands", Tag.TAG_STRING)),
                loadStrings(tag.getList("FailureCommands", Tag.TAG_STRING)),
                tag.getLong("StartedGameTime"),
                tag.getString("StageId")
        );
        instance.state = MissionState.valueOf(tag.getString("State"));
        instance.stageIndex = tag.getInt("StageIndex");
        instance.stageTick = tag.getInt("StageTick");
        instance.stageSuccessPendingTick = tag.contains("StageSuccessPendingTick")
                ? tag.getInt("StageSuccessPendingTick") : -1;
        instance.callbackExecuted = tag.getBoolean("CallbackExecuted");
        instance.firedEventIds.addAll(loadStrings(tag.getList("FiredEvents", Tag.TAG_STRING)));
        CompoundTag sequenceStarts = tag.getCompound("SequenceStartTicks");
        for (String sequenceId : sequenceStarts.getAllKeys()) {
            instance.sequenceStartTicks.put(sequenceId, sequenceStarts.getInt(sequenceId));
        }
        CompoundTag groups = tag.getCompound("TrackedEntityGroups");
        for (String group : groups.getAllKeys()) {
            Set<UUID> ids = new HashSet<>();
            for (Tag element : groups.getList(group, Tag.TAG_INT_ARRAY)) ids.add(NbtUtils.loadUUID(element));
            instance.trackedEntityGroups.put(group, ids);
        }
        instance.spawnedGroups.addAll(loadStrings(tag.getList("SpawnedGroups", Tag.TAG_STRING)));
        for (Tag element : tag.getList("DeadTrackedEntities", Tag.TAG_INT_ARRAY)) {
            instance.deadTrackedEntities.add(NbtUtils.loadUUID(element));
        }
        instance.acquiredItemGroups.addAll(loadStrings(tag.getList("AcquiredItemGroups", Tag.TAG_STRING)));
        for (Tag element : tag.getList("ForcedChunks", Tag.TAG_COMPOUND)) {
            CompoundTag chunk = (CompoundTag) element;
            instance.forcedChunks.add(new ForcedChunk(
                    new ResourceLocation(chunk.getString("Dimension")), chunk.getInt("X"), chunk.getInt("Z"),
                    chunk.getBoolean("OriginallyForced"), chunk.getBoolean("Dynamic")));
        }
        for (Tag element : tag.getList("TrackedEntityCleanup", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) element;
            UUID id = entry.getUUID("EntityId");
            instance.trackedEntityCleanup.put(id, new TrackedEntityCleanup(
                    MissionEntityCleanupPolicy.fromSerializedName(entry.getString("Policy")),
                    entry.contains("RestoreData", Tag.TAG_COMPOUND)
                            ? entry.getCompound("RestoreData").copy() : new CompoundTag()));
            if (entry.contains("Group", Tag.TAG_STRING)) {
                instance.trackedEntityCleanupGroups.put(id, entry.getString("Group"));
            }
        }
        // Phase A以前の保存データも確実に掃除対象へ移行する。
        for (Tag element : tag.getList("CleanupEntityIds", Tag.TAG_INT_ARRAY)) {
            instance.trackedEntityCleanup.putIfAbsent(NbtUtils.loadUUID(element),
                    new TrackedEntityCleanup(MissionEntityCleanupPolicy.DISCARD, new CompoundTag()));
        }
        instance.trackedEntityGroups.values().forEach(ids -> ids.forEach(id ->
                instance.trackedEntityCleanup.putIfAbsent(id,
                        new TrackedEntityCleanup(MissionEntityCleanupPolicy.DISCARD, new CompoundTag()))));
        instance.trackedEntityGroups.forEach((group, ids) -> ids.forEach(id ->
                instance.trackedEntityCleanupGroups.putIfAbsent(id, group)));
        CompoundTag sources = tag.getCompound("SpawnSourceGroups");
        for (String group : sources.getAllKeys()) {
            Set<UUID> ids = new HashSet<>();
            for (Tag element : sources.getList(group, Tag.TAG_INT_ARRAY)) ids.add(NbtUtils.loadUUID(element));
            instance.spawnSourceGroups.put(group, ids);
        }
        for (Tag element : tag.getList("CompletedSpawnSources", Tag.TAG_INT_ARRAY)) {
            instance.completedSpawnSources.add(NbtUtils.loadUUID(element));
        }
        return instance;
    }

    private static ListTag saveStrings(Collection<String> values) {
        ListTag list = new ListTag();
        values.forEach(value -> list.add(StringTag.valueOf(value)));
        return list;
    }

    private static List<String> loadStrings(ListTag list) {
        List<String> values = new ArrayList<>();
        for (Tag element : list) values.add(element.getAsString());
        return values;
    }

    public record ForcedChunk(
            ResourceLocation dimension, int x, int z, boolean originallyForced, boolean dynamic
    ) { }

    public record TrackedEntityCleanup(MissionEntityCleanupPolicy policy, CompoundTag restoreData) {
        public TrackedEntityCleanup {
            restoreData = restoreData.copy();
        }
    }
}
