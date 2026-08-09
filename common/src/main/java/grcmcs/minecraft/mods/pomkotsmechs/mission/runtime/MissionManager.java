package grcmcs.minecraft.mods.pomkotsmechs.mission.runtime;

import grcmcs.minecraft.mods.pomkotsmechs.mission.event.MissionConditionRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.mission.event.MissionEventRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.mission.event.MissionPositionResolver;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionInstance;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionResult;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionSavedData;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionStartResult;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionState;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.PendingMissionCallback;
import grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionChunkManager;
import grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionPresentationService;
import grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionTargetMarkerService;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.mission.definition.MissionDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.mission.definition.MissionDefinitionRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.block.PomkotsCubeBlockEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.MissionMarkerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.ServerBGMTracker;
import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class MissionManager {
    /**
     * 残り時間BossBarをクライアントへ同期する間隔（Tick）。
     * 1なら毎Tick、5なら0.25秒ごと、20なら1秒ごとに更新する。
     */
    private static final int TIMER_BAR_UPDATE_INTERVAL_TICKS = 1;
    private static final int ENTITY_DEATH_EFFECT_GRACE_TICKS = 20;

    private static final Map<UUID, MissionDefinition> RUNTIME_DEFINITIONS = new HashMap<>();
    private static final Map<UUID, ServerBossEvent> TIMER_BARS = new HashMap<>();

    private MissionManager() {
    }

    public static MissionStartResult start(
            MinecraftServer server,
            ServerPlayer owner,
            MissionDefinition definition,
            GlobalPos anchor,
            Collection<ServerPlayer> participants,
            List<String> successOverride,
            List<String> failureOverride
    ) {
        MissionSavedData data = MissionSavedData.get(server);
        List<String> successCommands = successOverride != null
                ? List.copyOf(successOverride) : definition.successCommands();
        List<String> failureCommands = failureOverride != null
                ? List.copyOf(failureOverride) : definition.failureCommands();

        String validationFailure = validateStart(data, definition, participants);
        if (validationFailure != null) {
            executeOrPendCallback(
                    server, data, owner, owner.getUUID(), owner.getGameProfile().getName(),
                    definition.id(), null, MissionResult.FAILURE, failureCommands
            );
            return MissionStartResult.failed(validationFailure);
        }

        UUID instanceId = UUID.randomUUID();
        Set<UUID> participantIds = new HashSet<>();
        participants.forEach(player -> participantIds.add(player.getUUID()));
        participantIds.add(owner.getUUID());
        MissionInstance instance = new MissionInstance(
                instanceId,
                definition.id(),
                definition.version(),
                owner.getUUID(),
                owner.getGameProfile().getName(),
                participantIds,
                anchor,
                successCommands,
                failureCommands,
                server.overworld().getGameTime(),
                definition.stages().get(0).id()
        );
        String chunkFailure = MissionChunkManager.acquire(server, owner, definition, instance);
        if (chunkFailure != null) {
            executeOrPendCallback(
                    server, data, owner, owner.getUUID(), owner.getGameProfile().getName(),
                    definition.id(), null, MissionResult.FAILURE, failureCommands
            );
            return MissionStartResult.failed(chunkFailure);
        }
        data.addInstance(instance);
        RUNTIME_DEFINITIONS.put(instanceId, definition);
        updateObjectiveHud(server, instance, definition, definition.stages().get(0));
        MissionTargetMarkerService.sync(server, instance);
        PomkotsMechs.LOGGER.info("Started mission {} instance {} for {}",
                definition.id(), instanceId, owner.getGameProfile().getName());
        return MissionStartResult.started(instanceId);
    }

    public static void tick(MinecraftServer server) {
        MissionSavedData data = MissionSavedData.get(server);
        List<MissionInstance> instances = new ArrayList<>(data.instances().values());
        for (MissionInstance instance : instances) {
            if (instance.state() != MissionState.RUNNING) continue;
            MissionDefinition definition = RUNTIME_DEFINITIONS.computeIfAbsent(
                    instance.instanceId(),
                    ignored -> MissionDefinitionRegistry.get(instance.missionId()).orElse(null)
            );
            if (definition == null || definition.version() != instance.definitionVersion()) {
                finish(server, instance.instanceId(), MissionResult.ABORTED, "Mission definition is missing or changed", null);
                continue;
            }
            tickInstance(server, data, instance, definition);
        }
    }

    public static void onServerStarted(MinecraftServer server) {
        MissionSavedData data = MissionSavedData.get(server);
        MissionChunkManager.rebuildReferences(data.instances().values());
        List<UUID> orphaned = new ArrayList<>(data.instances().keySet());
        for (UUID instanceId : orphaned) {
            MissionInstance instance = data.instances().get(instanceId);
            if (instance != null && instance.state() == MissionState.CLEANING_UP) {
                // 前回終了処理中に停止した場合も、callbackExecutedを尊重しつつ残りを完了する。
                instance.setState(MissionState.RUNNING);
                data.setDirty();
            }
            finish(server, instanceId, MissionResult.ABORTED, "Recovered orphaned mission after server restart", null);
        }
    }

    public static void onServerStopping(MinecraftServer server) {
        MissionSavedData data = MissionSavedData.get(server);
        List<UUID> active = new ArrayList<>(data.instances().keySet());
        for (UUID instanceId : active) {
            finish(server, instanceId, MissionResult.ABORTED, "Server stopping", null);
        }
    }

    public static void onPlayerJoin(ServerPlayer player) {
        MissionSavedData data = MissionSavedData.get(player.server);
        List<PendingMissionCallback> callbacks = data.pendingCallbacks().values().stream()
                .filter(callback -> callback.ownerId().equals(player.getUUID()))
                .toList();
        for (PendingMissionCallback callback : callbacks) {
            executeCommands(player.server, player, callback.missionId(), callback.instanceId(),
                    callback.result(), callback.commands());
            data.removePendingCallback(callback.callbackId());
        }
        for (MissionInstance instance : data.instances().values()) {
            if (instance.participantIds().contains(player.getUUID())) {
                MissionTargetMarkerService.sync(player, instance);
                break;
            }
        }
    }

    public static void onPlayerQuit(ServerPlayer player) {
        MissionSavedData data = MissionSavedData.get(player.server);
        List<MissionInstance> instances = new ArrayList<>(data.instances().values());
        for (MissionInstance instance : instances) {
            MissionDefinition definition = definitionFor(instance);
            if (instance.ownerId().equals(player.getUUID())) {
                if (definition == null || definition.settings().ownerDisconnect().equals("fail")) {
                    finish(player.server, instance.instanceId(), MissionResult.FAILURE, "Mission owner disconnected", player);
                }
            } else if (instance.participantIds().contains(player.getUUID()) && definition != null) {
                switch (definition.settings().participantDisconnect()) {
                    case "fail" -> finish(player.server, instance.instanceId(), MissionResult.FAILURE,
                            "A mission participant disconnected", player);
                    case "remove" -> {
                        instance.removeParticipant(player.getUUID());
                        ServerBossEvent timerBar = TIMER_BARS.get(instance.instanceId());
                        if (timerBar != null) timerBar.removePlayer(player);
                        data.setDirty();
                    }
                    default -> { }
                }
            }
        }
    }

    public static boolean finish(
            MinecraftServer server,
            UUID instanceId,
            MissionResult result,
            String reason,
            ServerPlayer callbackOwner
    ) {
        MissionSavedData data = MissionSavedData.get(server);
        MissionInstance instance = data.instances().get(instanceId);
        if (instance == null || instance.state() == MissionState.CLEANING_UP) return false;

        instance.setState(MissionState.CLEANING_UP);
        data.setDirty();
        notifyResult(server, instance, result, reason);

        MissionDefinition definition = definitionFor(instance);
        if (definition != null) {
            showConfiguredPresentation(server, instance, definition.presentation(),
                    result == MissionResult.SUCCESS ? "on_success" : "on_failure");
        }
        for (ServerPlayer participant : onlineParticipants(server, instance)) {
            MissionPresentationService.clearObjective(participant);
            ServerBGMTracker.forceUnsetBGMState(participant);
        }
        MissionTargetMarkerService.clear(onlineParticipants(server, instance));

        forceLoadCleanupChunks(server, instance);
        cleanupTrackedEntities(server, instance, true, result == MissionResult.SUCCESS, result, Set.of());
        if (result == MissionResult.SUCCESS) unlockAnchorCube(server, instance);
        MissionChunkManager.release(server, instance);

        List<String> commands = result == MissionResult.SUCCESS
                ? instance.successCommands() : instance.failureCommands();
        if (!instance.callbackExecuted()) {
            instance.setCallbackExecuted(true);
            data.setDirty();
            ServerPlayer owner = callbackOwner != null && callbackOwner.getUUID().equals(instance.ownerId())
                    ? callbackOwner : server.getPlayerList().getPlayer(instance.ownerId());
            executeOrPendCallback(
                    server, data, owner, instance.ownerId(), instance.ownerName(), instance.missionId(),
                    instance.instanceId(), result, commands
            );
        }

        data.removeInstance(instanceId);
        RUNTIME_DEFINITIONS.remove(instanceId);
        removeTimerBar(instanceId);
        PomkotsMechs.LOGGER.info("Finished mission {} instance {} with {}: {}",
                instance.missionId(), instanceId, result, reason);
        return true;
    }

    public static MissionInstance findByOwner(MinecraftServer server, UUID ownerId) {
        return MissionSavedData.get(server).instances().values().stream()
                .filter(instance -> instance.ownerId().equals(ownerId))
                .findFirst().orElse(null);
    }

    public static Collection<MissionInstance> all(MinecraftServer server) {
        return List.copyOf(MissionSavedData.get(server).instances().values());
    }

    public static int activeInstanceCount(MinecraftServer server) {
        return MissionSavedData.get(server).instances().size();
    }

    public static boolean discardOrphanedMissionEntity(Entity entity) {
        if (entity.level().isClientSide || entity.getServer() == null) {
            return false;
        }

        for (String tag : entity.getTags()) {
            if (!tag.startsWith("pomkots_mission:")) {
                continue;
            }

            UUID instanceId;
            try {
                instanceId = UUID.fromString(tag.substring("pomkots_mission:".length()));
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            MissionInstance instance =
                    MissionSavedData.get(entity.getServer()).instances().get(instanceId);
            if (instance == null || instance.state() != MissionState.RUNNING) {
                entity.discard();
                PomkotsMechs.LOGGER.info(
                        "Discarded orphaned mission entity {} for inactive instance {}",
                        entity.getUUID(), instanceId);
                return true;
            }
            return false;
        }
        return false;
    }

    public static void executeStartFailure(
            MinecraftServer server,
            ServerPlayer owner,
            MissionDefinition definition,
            List<String> failureOverride
    ) {
        List<String> commands = failureOverride != null
                ? List.copyOf(failureOverride) : definition.failureCommands();
        executeOrPendCallback(
                server,
                MissionSavedData.get(server),
                owner,
                owner.getUUID(),
                owner.getGameProfile().getName(),
                definition.id(),
                null,
                MissionResult.FAILURE,
                commands
        );
    }

    private static void tickInstance(
            MinecraftServer server,
            MissionSavedData data,
            MissionInstance instance,
            MissionDefinition definition
    ) {
        ServerPlayer owner = server.getPlayerList().getPlayer(instance.ownerId());
        if (owner == null && definition.settings().ownerDisconnect().equals("fail")) {
            finish(server, instance.instanceId(), MissionResult.FAILURE, "Mission owner is offline", null);
            return;
        }
        if (owner != null && !owner.isAlive() && definition.settings().ownerDeath().equals("fail")) {
            finish(server, instance.instanceId(), MissionResult.FAILURE, "Mission owner died", owner);
            return;
        }

        processParticipantDeaths(server, data, instance, definition);
        if (!data.instances().containsKey(instance.instanceId())) return;

        if (instance.stageIndex() < 0 || instance.stageIndex() >= definition.stages().size()) {
            finish(server, instance.instanceId(), MissionResult.ABORTED, "Invalid stage index", owner);
            return;
        }
        MissionDefinition.Stage stage = definition.stages().get(instance.stageIndex());
        if (!stage.id().equals(instance.stageId())) {
            finish(server, instance.instanceId(), MissionResult.ABORTED, "Stage definition changed", owner);
            return;
        }

        String dynamicChunkFailure = MissionChunkManager.updateDynamic(server, definition, instance);
        if (dynamicChunkFailure != null) {
            finish(server, instance.instanceId(), MissionResult.FAILURE, dynamicChunkFailure, owner);
            return;
        }

        if (instance.stageTick() % 5 == 0) updateObjectiveHud(server, instance, definition, stage);

        if (instance.stageSuccessPendingTick() >= 0) {
            try {
                if (stage.failure() != null
                        && evaluateFailureDuringSuccessGrace(server, instance, stage.failure())) {
                    showConfiguredPresentation(server, instance, stage.presentation(), "on_failure");
                    finish(server, instance.instanceId(), MissionResult.FAILURE,
                            "Stage failure condition met during success grace period", owner);
                    return;
                }
            } catch (MissionPositionResolver.ResolutionException e) {
                notifyParticipants(server, instance, e.playerMessage());
                finish(server, instance.instanceId(), MissionResult.FAILURE,
                        "Condition position could not be resolved", owner);
                return;
            }
            if (instance.stageTick() - instance.stageSuccessPendingTick() >= successGraceTicks(stage)) {
                completeStageSuccess(server, data, instance, definition, stage, owner);
            } else {
                advanceStageTick(server, data, instance);
            }
            return;
        }

        for (MissionDefinition.Event event : stage.events()) {
            if (event.atTick() == instance.stageTick() && !instance.firedEventIds().contains(event.id())) {
                try {
                    MissionEventRegistry.execute(new MissionEventRegistry.Context(server, data, instance, event, owner));
                    instance.firedEventIds().add(event.id());
                    data.setDirty();
                    MissionTargetMarkerService.sync(server, instance);
                } catch (MissionEventRegistry.ExpectedEventFailure e) {
                    PomkotsMechs.LOGGER.warn("Mission {} event {} could not be completed: {}",
                            instance.instanceId(), event.id(), e.getMessage());
                    notifyParticipants(server, instance, e.playerMessage());
                    finish(server, instance.instanceId(), MissionResult.FAILURE,
                            "Event could not be completed: " + event.id(), owner);
                    return;
                } catch (Exception e) {
                    PomkotsMechs.LOGGER.error("Mission {} event {} failed", instance.instanceId(), event.id(), e);
                    finish(server, instance.instanceId(), MissionResult.FAILURE,
                            "Event failed: " + event.id(), owner);
                    return;
                }
            }
        }

        try {
            processTriggeredSequences(server, data, instance, stage, owner);
        } catch (MissionEventRegistry.ExpectedEventFailure e) {
            PomkotsMechs.LOGGER.warn("Mission {} sequence event could not be completed: {}",
                    instance.instanceId(), e.getMessage());
            notifyParticipants(server, instance, e.playerMessage());
            finish(server, instance.instanceId(), MissionResult.FAILURE,
                    "Sequence event could not be completed", owner);
            return;
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Mission {} sequence event failed", instance.instanceId(), e);
            finish(server, instance.instanceId(), MissionResult.FAILURE,
                    "Sequence event failed", owner);
            return;
        }

        try {
            if (stage.failure() != null && evaluateCondition(server, instance, stage.failure())) {
                showConfiguredPresentation(server, instance, stage.presentation(), "on_failure");
                finish(server, instance.instanceId(), MissionResult.FAILURE, "Stage failure condition met", owner);
                return;
            }
            boolean stageSucceeded = evaluateCondition(server, instance, stage.success());
            if (!stageSucceeded && instance.stageTick() >= stage.timeLimitTicks()) {
                if (stage.timeLimitResult().equals("failure")) {
                    showConfiguredPresentation(server, instance, stage.presentation(), "on_failure");
                    finish(server, instance.instanceId(), MissionResult.FAILURE, "Stage time limit exceeded", owner);
                    return;
                }
                stageSucceeded = true;
            }
            if (stageSucceeded) {
                int graceTicks = successGraceTicks(stage);
                if (graceTicks > 0) {
                    instance.beginStageSuccessPending();
                    advanceStageTick(server, data, instance);
                } else {
                    completeStageSuccess(server, data, instance, definition, stage, owner);
                }
                return;
            }
        } catch (MissionPositionResolver.ResolutionException e) {
            PomkotsMechs.LOGGER.warn("Mission {} condition position could not be resolved: {}",
                    instance.instanceId(), e.getMessage());
            notifyParticipants(server, instance, e.playerMessage());
            finish(server, instance.instanceId(), MissionResult.FAILURE,
                    "Condition position could not be resolved", owner);
            return;
        }

        advanceStageTick(server, data, instance);
    }

    private static void processTriggeredSequences(
            MinecraftServer server,
            MissionSavedData data,
            MissionInstance instance,
            MissionDefinition.Stage stage,
            ServerPlayer owner
    ) throws Exception {
        for (MissionDefinition.TriggeredSequence sequence : stage.sequences()) {
            Integer startTick = instance.sequenceStartTicks().get(sequence.id());
            if (startTick == null
                    && MissionConditionRegistry.evaluate(server, instance, sequence.trigger())) {
                startTick = instance.stageTick();
                instance.sequenceStartTicks().put(sequence.id(), startTick);
                data.setDirty();
            }
            if (startTick == null) continue;

            int elapsed = instance.stageTick() - startTick;
            for (MissionDefinition.SequenceEvent sequenceEvent : sequence.events()) {
                String firedId = "sequence:" + sequence.id() + "/" + sequenceEvent.id();
                if (elapsed < sequenceEvent.afterTicks()
                        || instance.firedEventIds().contains(firedId)) continue;

                MissionDefinition.Event event = new MissionDefinition.Event(
                        firedId, instance.stageTick(), sequenceEvent.type(), sequenceEvent.data());
                MissionEventRegistry.execute(
                        new MissionEventRegistry.Context(server, data, instance, event, owner));
                instance.firedEventIds().add(firedId);
                data.setDirty();
                MissionTargetMarkerService.sync(server, instance);
            }
        }
    }

    private static void advanceStageTick(
            MinecraftServer server,
            MissionSavedData data,
            MissionInstance instance
    ) {
        instance.incrementStageTick();
        if (instance.stageTick() % MissionTargetMarkerService.SYNC_INTERVAL_TICKS == 0) {
            MissionTargetMarkerService.sync(server, instance);
        }
        data.setDirty();
    }

    private static int successGraceTicks(MissionDefinition.Stage stage) {
        if (stage.successGraceTicks() >= 0) return stage.successGraceTicks();
        return containsCondition(stage.success(), "all_tracked_entities_dead")
                ? ENTITY_DEATH_EFFECT_GRACE_TICKS : 0;
    }

    private static boolean containsCondition(MissionDefinition.Condition condition, String type) {
        if (condition == null) return false;
        if (condition.type().getPath().equals(type)) return true;
        return condition.children().stream().anyMatch(child -> containsCondition(child, type));
    }

    private static boolean evaluateFailureDuringSuccessGrace(
            MinecraftServer server,
            MissionInstance instance,
            MissionDefinition.Condition condition
    ) {
        String type = condition.type().getPath();
        if (type.equals("elapsed_time")) return false;
        if (type.equals("any_of")) {
            return condition.children().stream()
                    .anyMatch(child -> evaluateFailureDuringSuccessGrace(server, instance, child));
        }
        if (type.equals("all_of")) {
            return condition.children().stream()
                    .allMatch(child -> evaluateFailureDuringSuccessGrace(server, instance, child));
        }
        // 時間条件を反転した条件も、成功確定後の猶予中には再評価しない。
        if (type.equals("not") && containsCondition(condition, "elapsed_time")) return false;
        return evaluateCondition(server, instance, condition);
    }

    private static void completeStageSuccess(
            MinecraftServer server,
            MissionSavedData data,
            MissionInstance instance,
            MissionDefinition definition,
            MissionDefinition.Stage stage,
            ServerPlayer owner
    ) {
        showConfiguredPresentation(server, instance, stage.presentation(), "on_success");
        beginReachedMarkerClose(server, instance);
        int nextIndex = instance.stageIndex() + 1;
        if (nextIndex >= definition.stages().size()) {
            finish(server, instance.instanceId(), MissionResult.SUCCESS, "All stages completed", owner);
        } else {
            MissionDefinition.Stage next = definition.stages().get(nextIndex);
            Set<String> preservedGroups = Set.copyOf(stage.preservedTrackedGroups());
            cleanupTrackedEntities(server, instance, stage.cleanupTrackedEntities(), true, null,
                    preservedGroups);
            instance.proceedToStage(nextIndex, next.id(), preservedGroups);
            data.setDirty();
            updateObjectiveHud(server, instance, definition, next);
            MissionTargetMarkerService.sync(server, instance);
        }
    }

    private static void processParticipantDeaths(
            MinecraftServer server,
            MissionSavedData data,
            MissionInstance instance,
            MissionDefinition definition
    ) {
        for (UUID participantId : new HashSet<>(instance.participantIds())) {
            if (participantId.equals(instance.ownerId())) continue;
            ServerPlayer participant = server.getPlayerList().getPlayer(participantId);
            if (participant == null || participant.isAlive()) continue;
            switch (definition.settings().participantDeath()) {
                case "fail" -> {
                    finish(server, instance.instanceId(), MissionResult.FAILURE, "A mission participant died", null);
                    return;
                }
                case "remove" -> {
                    instance.removeParticipant(participantId);
                    MissionTargetMarkerService.clear(List.of(participant));
                    data.setDirty();
                }
                default -> { }
            }
        }
    }

    private static boolean evaluateCondition(
            MinecraftServer server,
            MissionInstance instance,
            MissionDefinition.Condition condition
    ) {
        return MissionConditionRegistry.evaluate(server, instance, condition);
    }

    public static void onEntityDeath(Entity entity) {
        if (entity.level().isClientSide || entity.getServer() == null) return;
        MissionSavedData data = MissionSavedData.get(entity.getServer());
        boolean changed = false;
        for (MissionInstance instance : data.instances().values()) {
            changed |= instance.markTrackedEntityDead(entity.getUUID());
        }
        if (changed) data.setDirty();
    }

    public static void onTrackedItemPickedUp(ItemEntity itemEntity) {
        if (itemEntity.level().isClientSide || itemEntity.getServer() == null) return;
        MissionSavedData data = MissionSavedData.get(itemEntity.getServer());
        boolean changed = false;
        for (MissionInstance instance : data.instances().values()) {
            changed |= instance.markTrackedItemAcquired(itemEntity.getUUID());
        }
        if (changed) data.setDirty();
    }

    private static String validateStart(
            MissionSavedData data,
            MissionDefinition definition,
            Collection<ServerPlayer> participants
    ) {
        if (!definition.settings().allowConcurrentInstances()) {
            boolean running = data.instances().values().stream()
                    .anyMatch(instance -> instance.missionId().equals(definition.id()));
            if (running) return "The same mission is already running";
        }
        Set<UUID> participantIds = new HashSet<>();
        participants.forEach(player -> participantIds.add(player.getUUID()));
        for (MissionInstance instance : data.instances().values()) {
            for (UUID participantId : participantIds) {
                if (instance.participantIds().contains(participantId)) {
                    return "A selected player is already participating in another mission";
                }
            }
        }
        return null;
    }

    private static MissionDefinition definitionFor(MissionInstance instance) {
        MissionDefinition runtime = RUNTIME_DEFINITIONS.get(instance.instanceId());
        return runtime != null ? runtime : MissionDefinitionRegistry.get(instance.missionId()).orElse(null);
    }

    private static void executeOrPendCallback(
            MinecraftServer server,
            MissionSavedData data,
            ServerPlayer owner,
            UUID ownerId,
            String ownerName,
            net.minecraft.resources.ResourceLocation missionId,
            UUID instanceId,
            MissionResult result,
            List<String> commands
    ) {
        if (commands.isEmpty()) return;
        if (owner != null) {
            executeCommands(server, owner, missionId, instanceId, result, commands);
        } else {
            PendingMissionCallback callback = new PendingMissionCallback(
                    UUID.randomUUID(), ownerId, ownerName, missionId, instanceId, result, commands
            );
            data.addPendingCallback(callback);
            PomkotsMechs.LOGGER.info("Deferred mission callback {} until owner {} logs in",
                    callback.callbackId(), ownerName);
        }
    }

    private static void executeCommands(
            MinecraftServer server,
            ServerPlayer owner,
            net.minecraft.resources.ResourceLocation missionId,
            UUID instanceId,
            MissionResult result,
            List<String> commands
    ) {
        CommandSourceStack source = owner.createCommandSourceStack()
                .withPermission(4)
                .withSuppressedOutput();
        for (String raw : commands) {
            String command = raw
                    .replace("${owner_uuid}", owner.getUUID().toString())
                    .replace("${owner_name}", owner.getGameProfile().getName())
                    .replace("${mission_id}", missionId.toString())
                    .replace("${instance_uuid}", instanceId == null ? "" : instanceId.toString())
                    .replace("${result}", result.name().toLowerCase());
            if (command.startsWith("/")) command = command.substring(1);
            server.getCommands().performPrefixedCommand(source, command);
        }
    }

    private static void notifyResult(
            MinecraftServer server,
            MissionInstance instance,
            MissionResult result,
            String reason
    ) {
        // Component message = Component.literal("Mission " + result.name());
        // if (reason != null && !reason.isBlank()) message = message.copy().append(": " + reason);
        // notifyParticipants(server, instance, message);
    }

    private static void notifyParticipants(MinecraftServer server, MissionInstance instance, Component message) {
        for (UUID participantId : instance.participantIds()) {
            ServerPlayer player = server.getPlayerList().getPlayer(participantId);
            if (player != null) player.sendSystemMessage(message);
        }
    }

    private static List<ServerPlayer> onlineParticipants(MinecraftServer server, MissionInstance instance) {
        List<ServerPlayer> players = new ArrayList<>();
        for (UUID id : instance.participantIds()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) players.add(player);
        }
        return players;
    }

    private static void updateObjectiveHud(MinecraftServer server, MissionInstance instance,
                                           MissionDefinition definition, MissionDefinition.Stage stage) {
        int remaining = Math.max(0, stage.timeLimitTicks() - instance.stageTick());
        Component objective = stage.objective();
        Component progress = objectiveProgress(server, instance, stage);
        for (ServerPlayer player : onlineParticipants(server, instance)) {
            MissionPresentationService.objective(player, definition.displayName(), Component.empty(), objective, progress, remaining);
        }
    }

    private static Component objectiveProgress(
            MinecraftServer server,
            MissionInstance instance,
            MissionDefinition.Stage stage
    ) {
        JsonObject configured = stage.hudProgress();
        if (configured.has("type")) {
            String type = GsonHelper.getAsString(configured, "type");
            if (type.equals("tracked_entity_health")) {
                String group = GsonHelper.getAsString(configured, "group");
                for (UUID id : instance.trackedEntityGroups().getOrDefault(group, Set.of())) {
                    Entity entity = findLoadedEntity(server, id);
                    if (entity instanceof net.minecraft.world.entity.LivingEntity living && living.isAlive()) {
                        int health = Mth.ceil(living.getHealth());
                        int maxHealth = Mth.ceil(living.getMaxHealth());
                        return Component.literal("HP  " + health + " / " + maxHealth);
                    }
                }
                return Component.literal("HP  -- / --");
            }
        }
        Component progress = objectiveProgress(instance, stage.success());
        return progress.getString().isEmpty() ? objectiveProgress(instance, stage.failure()) : progress;
    }

    private static Component objectiveProgress(MissionInstance instance, MissionDefinition.Condition condition) {
        if (condition == null) return Component.empty();
        String type = condition.type().getPath();
        if (type.equals("all_tracked_entities_dead")) {
            String group = GsonHelper.getAsString(condition.data(), "group", "");
            long remaining = instance.trackedEntityGroups().getOrDefault(group, Set.of()).stream()
                    .filter(id -> !instance.deadTrackedEntities().contains(id)).count();
            return Component.literal("TARGETS  " + remaining);
        }
        if (type.equals("tracked_item_acquired")) {
            String group = GsonHelper.getAsString(condition.data(), "group", "");
            return Component.literal(instance.acquiredItemGroups().contains(group) ? "ITEM  ACQUIRED" : "ITEM  PENDING");
        }
        if (type.equals("player_reached")) return Component.literal("DESTINATION");
        for (MissionDefinition.Condition child : condition.children()) {
            Component progress = objectiveProgress(instance, child);
            if (!progress.getString().isEmpty()) return progress;
        }
        return Component.empty();
    }

    private static Entity findLoadedEntity(MinecraftServer server, UUID id) {
        for (var level : server.getAllLevels()) {
            Entity entity = level.getEntity(id);
            if (entity != null) return entity;
        }
        return null;
    }

    private static void showConfiguredPresentation(MinecraftServer server, MissionInstance instance,
                                                   JsonObject root, String key) {
        if (root == null || !root.has(key)) return;
        JsonObject json = GsonHelper.getAsJsonObject(root, key);
        Component title = component(json, "title", Component.empty());
        Component message = component(json, "message", Component.empty());
        MissionPresentationService.show(onlineParticipants(server, instance),
                GsonHelper.getAsString(json, "style", key), title, message,
                GsonHelper.getAsInt(json, "duration_ticks", 60));
    }

    private static Component component(JsonObject json, String key, Component fallback) {
        if (!json.has(key)) return fallback;
        Component value = Component.Serializer.fromJson(json.get(key));
        return value == null ? fallback : value;
    }

    private static void updateTimerBar(
            MinecraftServer server,
            MissionInstance instance,
            MissionDefinition.Stage stage
    ) {
        int updateInterval = Math.max(1, TIMER_BAR_UPDATE_INTERVAL_TICKS);
        if (instance.stageTick() != 0 && instance.stageTick() % updateInterval != 0) {
            return;
        }

        int limit = stage.timeLimitTicks();

        int remaining = Math.max(0, limit - instance.stageTick());
        int remainingSeconds = (remaining + 19) / 20;
        ServerBossEvent bar = TIMER_BARS.computeIfAbsent(instance.instanceId(), ignored ->
                new ServerBossEvent(
                        Component.empty(),
                        BossEvent.BossBarColor.BLUE,
                        BossEvent.BossBarOverlay.PROGRESS
                )
        );
        bar.setName(Component.empty()
                .append(stage.objective())
                .append(Component.literal(" | Time: " + remainingSeconds + "s")));
        bar.setProgress(Mth.clamp(remaining / (float) limit, 0.0F, 1.0F));
        bar.setVisible(true);

        Set<ServerPlayer> expectedPlayers = new HashSet<>();
        for (UUID participantId : instance.participantIds()) {
            ServerPlayer player = server.getPlayerList().getPlayer(participantId);
            if (player != null) expectedPlayers.add(player);
        }
        for (ServerPlayer current : new ArrayList<>(bar.getPlayers())) {
            if (!expectedPlayers.contains(current)) bar.removePlayer(current);
        }
        expectedPlayers.forEach(player -> {
            if (!bar.getPlayers().contains(player)) bar.addPlayer(player);
        });
    }

    private static void removeTimerBar(UUID instanceId) {
        ServerBossEvent bar = TIMER_BARS.remove(instanceId);
        if (bar != null) {
            bar.setVisible(false);
            bar.removeAllPlayers();
        }
    }

    private static void beginReachedMarkerClose(MinecraftServer server, MissionInstance instance) {
        instance.trackedEntityGroups().forEach((group, ids) -> {
            if (!group.startsWith("__marker_reached_")) return;
            for (UUID id : ids) {
                for (var level : server.getAllLevels()) {
                    Entity entity = level.getEntity(id);
                    if (entity instanceof MissionMarkerEntity marker) marker.beginClosing();
                }
            }
        });
    }

    private static void cleanupTrackedEntities(
            MinecraftServer server,
            MissionInstance instance,
            boolean discardNonMarkers,
            boolean preserveClosingReachedMarkers,
            MissionResult result,
            Set<String> preservedGroups
    ) {
        Set<UUID> preservedEntityIds = new HashSet<>();
        for (String group : preservedGroups) {
            preservedEntityIds.addAll(instance.trackedEntityGroups().getOrDefault(group, Set.of()));
        }
        for (var entry : instance.trackedEntityCleanup().entrySet()) {
            UUID id = entry.getKey();
            String cleanupGroup = instance.trackedEntityCleanupGroups().get(id);
            if (preservedEntityIds.contains(id)
                    || (cleanupGroup != null && preservedGroups.contains(cleanupGroup))) continue;
            MissionInstance.TrackedEntityCleanup cleanup = entry.getValue();
            for (var level : server.getAllLevels()) {
                Entity entity = level.getEntity(id);
                if (entity == null) continue;
                if (entity instanceof MissionMarkerEntity marker) {
                    if (!preserveClosingReachedMarkers || !marker.isClosing()) marker.discard();
                    continue;
                }
                switch (cleanup.policy()) {
                    case DISCARD -> {
                        if (discardNonMarkers) entity.discard();
                    }
                    case KEEP -> {
                        if (result != null && entity instanceof grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster pomkots) {
                            pomkots.clearMissionDefenseTargetId();
                            pomkots.setInRaid(false);
                            pomkots.setTarget(null);
                        }
                    }
                    case RESTORE_ON_FAILURE -> {
                        if (result == MissionResult.FAILURE || result == MissionResult.ABORTED) {
                            restoreEntity(server, entity, cleanup.restoreData(), instance.instanceId());
                        }
                    }
                }
            }
        }
    }

    private static void restoreEntity(
            MinecraftServer server,
            Entity entity,
            net.minecraft.nbt.CompoundTag restoreData,
            UUID missionInstanceId
    ) {
        if (restoreData.isEmpty()) return;
        if (entity instanceof grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster pomkots) {
            pomkots.clearMissionDefenseTargetId();
            pomkots.setInRaid(false);
            pomkots.setTarget(null);
        }
        entity.removeTag("pomkots_mission:" + missionInstanceId);
        entity.getTags().stream()
                .filter(tag -> tag.startsWith("pomkots_mission_group:"))
                .toList()
                .forEach(entity::removeTag);

        if (restoreData.contains("PomkotsMissionBossRestore", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            net.minecraft.nbt.CompoundTag metadata = restoreData.getCompound("PomkotsMissionBossRestore");
            float health = entity instanceof LivingEntity living ? living.getHealth() : 0.0F;
            var dimension = net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    new net.minecraft.resources.ResourceLocation(metadata.getString("Dimension")));
            var targetLevel = server.getLevel(dimension);
            if (targetLevel == entity.level()) {
                entity.moveTo(
                        metadata.getDouble("X"), metadata.getDouble("Y"), metadata.getDouble("Z"),
                        metadata.getFloat("Yaw"), metadata.getFloat("Pitch"));
            } else {
                PomkotsMechs.LOGGER.warn(
                        "Mission boss {} moved to another dimension; preserving the same entity/UUID and restoring only its inactive state",
                        entity.getUUID());
            }
            if (entity instanceof grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity boss) {
                boss.clearAllHate();
                boss.setTarget(null);
                boss.setActivated(metadata.getBoolean("Activated"));
                boss.setAiMode(metadata.getInt("AiMode"));
                if (metadata.getBoolean("PreserveHealth")) boss.setHealth(health);
            }
            return;
        }

        net.minecraft.nbt.CompoundTag current = new net.minecraft.nbt.CompoundTag();
        entity.saveWithoutId(current);
        current.merge(restoreData.copy());
        entity.load(current);
    }

    private static void unlockAnchorCube(MinecraftServer server, MissionInstance instance) {
        var level = server.getLevel(instance.anchor().dimension());
        if (level == null) return;
        if (level.getBlockEntity(instance.anchor().pos()) instanceof PomkotsCubeBlockEntity cube) {
            cube.updateMode(PomkotsCubeBlockEntity.MODE_BLUE);
            cube.clearActiveMission();
        }
    }

    private static void forceLoadCleanupChunks(MinecraftServer server, MissionInstance instance) {
        for (MissionInstance.ForcedChunk chunk : instance.forcedChunks()) {
            var level = server.getLevel(net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION, chunk.dimension()));
            if (level != null) level.getChunk(chunk.x(), chunk.z());
        }
    }
}
