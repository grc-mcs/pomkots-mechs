package grcmcs.minecraft.mods.pomkotsmechs.command;

import grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionParticipantResolver;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.CoreStoneBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.block.mission.MissionAnchorBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.config.definition.PomkotsDefinitionManager;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.PomkotsRadarItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.RadarTarget;
import grcmcs.minecraft.mods.pomkotsmechs.radio.RadioCommunicationService;
import grcmcs.minecraft.mods.pomkotsmechs.radio.RadioInterruptMode;
import grcmcs.minecraft.mods.pomkotsmechs.cutscene.CutsceneDefinitionRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.cutscene.CutsceneService;
import grcmcs.minecraft.mods.pomkotsmechs.mission.definition.MissionDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.mission.definition.MissionDefinitionRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionInstance;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionManager;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionResult;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionService;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionStartResult;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Collection;
import java.util.UUID;

public class PomkotsCommands {
    private static final int CORE_STONE_SEARCH_RADIUS = 100;
    private static final int MISSION_ANCHOR_SEARCH_RADIUS = 512;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("pomkots:raidstart")
                        .then(Commands.argument("raid_type", StringArgumentType.string())
                                .then(Commands.argument("command_success", StringArgumentType.string())
                                        .then(Commands.argument("command_fail", StringArgumentType.string())
                                        .executes(PomkotsCommands::executeRaid))))
        );

        dispatcher.register(
                Commands.literal("pomkots:register_radartarget")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("label", StringArgumentType.string())
                                        .then(Commands.literal("coord")
                                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                        .executes(ctx -> executeCoord(ctx))
                                                )
                                        )
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("pomkots:reset_boss_pos")
                        .then(Commands.argument("uuid", UuidArgument.uuid())
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> resetBossPosition(ctx))
                                )));

        dispatcher.register(
                Commands.literal("pomkots:radio")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("play")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("message_id", ResourceLocationArgument.id())
                                                .executes(ctx -> playRadio(ctx, RadioInterruptMode.QUEUE))
                                                .then(Commands.literal("queue")
                                                        .executes(ctx -> playRadio(ctx, RadioInterruptMode.QUEUE)))
                                                .then(Commands.literal("replace")
                                                        .executes(ctx -> playRadio(ctx, RadioInterruptMode.REPLACE))))))
                        .then(Commands.literal("stop")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(PomkotsCommands::stopRadio)))
        );

        dispatcher.register(
                Commands.literal("pomkots:cutscene")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("play_pos")
                                .then(Commands.argument("viewers", EntityArgument.players())
                                        .then(Commands.argument("cutscene_id", ResourceLocationArgument.id())
                                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                        .executes(PomkotsCommands::playCutsceneAtPos)))))
                        .then(Commands.literal("play_entity")
                                .then(Commands.argument("viewers", EntityArgument.players())
                                        .then(Commands.argument("cutscene_id", ResourceLocationArgument.id())
                                                .then(Commands.argument("target", EntityArgument.entity())
                                                        .executes(PomkotsCommands::playCutsceneAtEntity)))))
        );

        dispatcher.register(
                Commands.literal("pomkots:mission")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("start")
                                .then(Commands.argument("owner", EntityArgument.player())
                                        .then(Commands.argument("mission_id", ResourceLocationArgument.id())
                                                .executes(ctx -> startMission(ctx, null, null))
                                                .then(Commands.argument("success_command", StringArgumentType.string())
                                                        .then(Commands.argument("failure_command", StringArgumentType.string())
                                                                .executes(ctx -> startMission(
                                                                        ctx,
                                                                        StringArgumentType.getString(ctx, "success_command"),
                                                                StringArgumentType.getString(ctx, "failure_command")
                                                                )))))))
                        .then(Commands.literal("start_near_corestone")
                                .then(Commands.argument("owner", EntityArgument.player())
                                        .then(Commands.argument("mission_id", ResourceLocationArgument.id())
                                                .executes(ctx -> startMissionNearCoreStone(ctx, null, null))
                                                .then(Commands.argument("success_command", StringArgumentType.string())
                                                        .then(Commands.argument("failure_command", StringArgumentType.string())
                                                                .executes(ctx -> startMissionNearCoreStone(
                                                                        ctx,
                                                                        StringArgumentType.getString(ctx, "success_command"),
                                                                        StringArgumentType.getString(ctx, "failure_command")
                                                                )))))))
                        .then(Commands.literal("start_at")
                                .then(Commands.argument("owner", EntityArgument.player())
                                        .then(Commands.argument("mission_id", ResourceLocationArgument.id())
                                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                        .executes(ctx -> startMissionAt(ctx, null, null))
                                                        .then(Commands.argument("success_command", StringArgumentType.string())
                                                                .then(Commands.argument("failure_command", StringArgumentType.string())
                                                                        .executes(ctx -> startMissionAt(
                                                                                ctx,
                                                                                StringArgumentType.getString(ctx, "success_command"),
                                                                                StringArgumentType.getString(ctx, "failure_command")
                                                                        ))))))))
                        .then(Commands.literal("start_at_mission_anchor")
                                .then(Commands.argument("owner", EntityArgument.player())
                                        .then(Commands.argument("mission_id", ResourceLocationArgument.id())
                                                .then(Commands.argument("anchor_id", StringArgumentType.word())
                                                        .executes(ctx -> startMissionAtMissionAnchor(ctx, null, null))
                                                        .then(Commands.argument("success_command", StringArgumentType.string())
                                                                .then(Commands.argument("failure_command", StringArgumentType.string())
                                                                        .executes(ctx -> startMissionAtMissionAnchor(
                                                                                ctx,
                                                                                StringArgumentType.getString(ctx, "success_command"),
                                                                                StringArgumentType.getString(ctx, "failure_command")
                                                                        ))))))))
                        .then(Commands.literal("stop")
                                .then(Commands.argument("owner", EntityArgument.player())
                                        .executes(PomkotsCommands::stopMission)))
                        .then(Commands.literal("stop_instance")
                                .then(Commands.argument("instance_uuid", UuidArgument.uuid())
                                        .executes(PomkotsCommands::stopMissionInstance)))
                        .then(Commands.literal("list").executes(PomkotsCommands::listMissions))
                        .then(Commands.literal("reload").executes(PomkotsCommands::reloadMissionDefinitions))
                        .then(Commands.literal("debug")
                                .then(Commands.argument("instance_uuid", UuidArgument.uuid())
                                        .executes(PomkotsCommands::debugMission)))
        );
    }

    private static int playCutsceneAtPos(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var definition = CutsceneDefinitionRegistry.get(
                ResourceLocationArgument.getId(context, "cutscene_id")).orElse(null);
        if (definition == null) {
            context.getSource().sendFailure(Component.literal("Unknown cutscene"));
            return 0;
        }
        var pos = BlockPosArgument.getLoadedBlockPos(context, "pos").getCenter();
        int count = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(context, "viewers")) {
            if (player.serverLevel() == context.getSource().getLevel()
                    && CutsceneService.play(player, definition, pos, null)) count++;
        }
        return count;
    }

    private static int playCutsceneAtEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var definition = CutsceneDefinitionRegistry.get(
                ResourceLocationArgument.getId(context, "cutscene_id")).orElse(null);
        if (definition == null) {
            context.getSource().sendFailure(Component.literal("Unknown cutscene"));
            return 0;
        }
        Entity target = EntityArgument.getEntity(context, "target");
        int count = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(context, "viewers")) {
            if (player.level() == target.level()
                    && CutsceneService.play(player, definition, target.getBoundingBox().getCenter(), target)) count++;
        }
        return count;
    }

    private static int startMissionNearCoreStone(
            CommandContext<CommandSourceStack> ctx,
            String successOverride,
            String failureOverride
    ) {
        try {
            CommandSourceStack source = ctx.getSource();
            ServerPlayer owner = EntityArgument.getPlayer(ctx, "owner");
            ResourceLocation missionId = ResourceLocationArgument.getId(ctx, "mission_id");
            MissionDefinition definition = MissionDefinitionRegistry.get(missionId).orElse(null);
            if (definition == null) {
                source.sendFailure(Component.literal("Unknown mission: " + missionId));
                return 0;
            }

            CoreStoneBlockEntity coreStone = findFirstLoadedCoreStone(
                    source.getLevel(), BlockPos.containing(source.getPosition()), CORE_STONE_SEARCH_RADIUS);
            if (coreStone == null) {
                MissionManager.executeStartFailure(
                        source.getServer(), owner, definition,
                        failureOverride == null ? null : List.of(failureOverride));
                source.sendFailure(Component.literal(
                        "No loaded Core Stone was found within " + CORE_STONE_SEARCH_RADIUS + " blocks."));
                return 0;
            }

            BlockPos anchorPos = coreStone.getBlockPos();
            boolean missionActive = MissionManager.all(source.getServer()).stream()
                    .anyMatch(instance -> instance.anchor().dimension().equals(source.getLevel().dimension())
                            && instance.anchor().pos().equals(anchorPos));
            if (coreStone.isRaidActive() || missionActive) {
                MissionManager.executeStartFailure(
                        source.getServer(), owner, definition,
                        failureOverride == null ? null : List.of(failureOverride));
                source.sendFailure(Component.literal("The selected Core Stone is already in use."));
                return 0;
            }

            MissionStartResult result = MissionService.start(
                    owner,
                    definition,
                    net.minecraft.core.GlobalPos.of(source.getLevel().dimension(), anchorPos),
                    grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionParticipantResolver.resolve(owner, definition),
                    successOverride == null ? null : List.of(successOverride),
                    failureOverride == null ? null : List.of(failureOverride)
            );
            if (!result.success()) {
                source.sendFailure(Component.literal("Mission start failed: " + result.reason()));
                return 0;
            }
            return 1;
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to start mission near Core Stone", e);
            ctx.getSource().sendFailure(
                    Component.literal("Failed to start mission near Core Stone: " + e.getMessage()));
            return 0;
        }
    }

    private static int startMissionAt(
            CommandContext<CommandSourceStack> ctx,
            String successOverride,
            String failureOverride
    ) {
        try {
            BlockPos anchorPos = BlockPosArgument.getBlockPos(ctx, "pos");
            return startMissionAtResolvedPosition(
                    ctx, ctx.getSource().getLevel(), anchorPos,
                    successOverride, failureOverride, "absolute position");
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to start mission at absolute position", e);
            ctx.getSource().sendFailure(Component.literal(
                    "Failed to start mission at absolute position: " + e.getMessage()));
            return 0;
        }
    }

    private static int startMissionAtMissionAnchor(
            CommandContext<CommandSourceStack> ctx,
            String successOverride,
            String failureOverride
    ) {
        try {
            CommandSourceStack source = ctx.getSource();
            String anchorId = StringArgumentType.getString(ctx, "anchor_id").trim();
            BlockPos origin = BlockPos.containing(source.getPosition());
            MissionAnchorBlockEntity anchor = findNearestLoadedMissionAnchor(
                    source.getLevel(), origin, MISSION_ANCHOR_SEARCH_RADIUS, anchorId);
            if (anchor == null) {
                ServerPlayer owner = EntityArgument.getPlayer(ctx, "owner");
                MissionDefinition definition = MissionDefinitionRegistry.get(
                        ResourceLocationArgument.getId(ctx, "mission_id")).orElse(null);
                if (definition != null) {
                    MissionManager.executeStartFailure(
                            source.getServer(), owner, definition,
                            failureOverride == null ? null : List.of(failureOverride));
                }
                source.sendFailure(Component.literal(
                        "No loaded Mission Anchor with id '" + anchorId + "' was found within "
                                + MISSION_ANCHOR_SEARCH_RADIUS + " blocks."));
                return 0;
            }
            return startMissionAtResolvedPosition(
                    ctx, source.getLevel(), anchor.getBlockPos(),
                    successOverride, failureOverride, "Mission Anchor '" + anchorId + "'");
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to start mission at Mission Anchor", e);
            ctx.getSource().sendFailure(Component.literal(
                    "Failed to start mission at Mission Anchor: " + e.getMessage()));
            return 0;
        }
    }

    private static int startMissionAtResolvedPosition(
            CommandContext<CommandSourceStack> ctx,
            ServerLevel level,
            BlockPos anchorPos,
            String successOverride,
            String failureOverride,
            String description
    ) throws CommandSyntaxException {
        ServerPlayer owner = EntityArgument.getPlayer(ctx, "owner");
        ResourceLocation missionId = ResourceLocationArgument.getId(ctx, "mission_id");
        MissionDefinition definition = MissionDefinitionRegistry.get(missionId).orElse(null);
        if (definition == null) {
            ctx.getSource().sendFailure(Component.literal("Unknown mission: " + missionId));
            return 0;
        }
        MissionStartResult result = MissionService.start(
                owner,
                definition,
                net.minecraft.core.GlobalPos.of(level.dimension(), anchorPos),
                grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionParticipantResolver.resolve(owner, definition),
                successOverride == null ? null : List.of(successOverride),
                failureOverride == null ? null : List.of(failureOverride));
        if (!result.success()) {
            ctx.getSource().sendFailure(Component.literal("Mission start failed: " + result.reason()));
            return 0;
        }
        return 1;
    }

    private static MissionAnchorBlockEntity findNearestLoadedMissionAnchor(
            ServerLevel level,
            BlockPos origin,
            int radius,
            String anchorId
    ) {
        if (anchorId.isEmpty()) return null;
        int minChunkX = (origin.getX() - radius) >> 4;
        int maxChunkX = (origin.getX() + radius) >> 4;
        int minChunkZ = (origin.getZ() - radius) >> 4;
        int maxChunkZ = (origin.getZ() + radius) >> 4;
        double radiusSquared = (double) radius * radius;
        MissionAnchorBlockEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) continue;
                for (BlockEntity blockEntity : level.getChunk(chunkX, chunkZ).getBlockEntities().values()) {
                    if (!(blockEntity instanceof MissionAnchorBlockEntity anchor)
                            || !anchor.anchorId().equals(anchorId)) continue;
                    double distance = anchor.getBlockPos().distToCenterSqr(
                            origin.getX(), origin.getY(), origin.getZ());
                    if (distance <= radiusSquared && distance < nearestDistance) {
                        nearest = anchor;
                        nearestDistance = distance;
                    }
                }
            }
        }
        return nearest;
    }

    private static CoreStoneBlockEntity findFirstLoadedCoreStone(
            ServerLevel level,
            BlockPos origin,
            int radius
    ) {
        int minChunkX = (origin.getX() - radius) >> 4;
        int maxChunkX = (origin.getX() + radius) >> 4;
        int minChunkZ = (origin.getZ() - radius) >> 4;
        int maxChunkZ = (origin.getZ() + radius) >> 4;
        double radiusSquared = (double) radius * radius;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) continue;
                for (BlockEntity blockEntity : level.getChunk(chunkX, chunkZ).getBlockEntities().values()) {
                    if (blockEntity instanceof CoreStoneBlockEntity coreStone
                            && coreStone.getBlockPos().distToCenterSqr(
                                    origin.getX(), origin.getY(), origin.getZ()) <= radiusSquared) {
                        return coreStone;
                    }
                }
            }
        }
        return null;
    }

    private static int startMission(
            CommandContext<CommandSourceStack> ctx,
            String successOverride,
            String failureOverride
    ) {
        try {
            ServerPlayer owner = EntityArgument.getPlayer(ctx, "owner");
            ResourceLocation missionId = ResourceLocationArgument.getId(ctx, "mission_id");
            MissionDefinition definition = MissionDefinitionRegistry.get(missionId).orElse(null);
            if (definition == null) {
                ctx.getSource().sendFailure(Component.literal("Unknown mission: " + missionId));
                return 0;
            }
            MissionStartResult result = MissionService.start(
                    owner,
                    definition,
                    net.minecraft.core.GlobalPos.of(owner.serverLevel().dimension(), owner.blockPosition()),
                    grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionParticipantResolver.resolve(owner, definition),
                    successOverride == null ? null : List.of(successOverride),
                    failureOverride == null ? null : List.of(failureOverride)
            );
            if (!result.success()) {
                ctx.getSource().sendFailure(Component.literal("Mission start failed: " + result.reason()));
                return 0;
            }
            return 1;
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to start mission", e);
            ctx.getSource().sendFailure(Component.literal("Failed to start mission: " + e.getMessage()));
            return 0;
        }
    }

    private static int stopMission(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer owner = EntityArgument.getPlayer(ctx, "owner");
            MissionInstance instance = MissionManager.findByOwner(ctx.getSource().getServer(), owner.getUUID());
            if (instance == null) {
                ctx.getSource().sendFailure(Component.literal("The player does not own an active mission"));
                return 0;
            }
            MissionManager.finish(ctx.getSource().getServer(), instance.instanceId(), MissionResult.ABORTED,
                    "Stopped by command", owner);
            return 1;
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to stop mission", e);
            return 0;
        }
    }

    private static int stopMissionInstance(CommandContext<CommandSourceStack> ctx) {
        UUID instanceId = UuidArgument.getUuid(ctx, "instance_uuid");
        boolean stopped = MissionManager.finish(
                ctx.getSource().getServer(), instanceId, MissionResult.ABORTED, "Stopped by command", null
        );
        if (!stopped) {
            ctx.getSource().sendFailure(Component.literal("Mission instance not found: " + instanceId));
            return 0;
        }
        return 1;
    }

    private static int listMissions(CommandContext<CommandSourceStack> ctx) {
        Collection<MissionInstance> instances = MissionManager.all(ctx.getSource().getServer());
        ctx.getSource().sendSystemMessage(Component.literal("Active missions: " + instances.size()));
        if (instances.isEmpty()) {
            ctx.getSource().sendSystemMessage(Component.literal("No active mission instances."));
            return 1;
        }
        for (MissionInstance instance : instances) {
            ctx.getSource().sendSystemMessage(Component.literal(
                    instance.instanceId() + " | " + instance.missionId() + " | owner=" + instance.ownerName()
                            + " | stage=" + instance.stageId() + " | tick=" + instance.stageTick()
            ));
        }
        PomkotsMechs.LOGGER.info("Listed {} active mission instances for {}",
                instances.size(), ctx.getSource().getTextName());
        return instances.size();
    }

    private static int reloadMissionDefinitions(CommandContext<CommandSourceStack> ctx) {
        try {
            PomkotsDefinitionManager.ReloadResult result =
                    PomkotsDefinitionManager.reload(ctx.getSource().getServer());
            ctx.getSource().sendSuccess(
                    () -> Component.literal(
                            "Reloaded " + result.missions() + " missions, "
                                    + result.cutscenes() + " cutscenes, and "
                                    + result.radioMessages() + " radio messages from "
                                    + PomkotsDefinitionManager.root().toAbsolutePath()),
                    true);
            return 1;
        } catch (IllegalStateException e) {
            ctx.getSource().sendFailure(Component.literal(e.getMessage()));
            return 0;
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to reload external mission definitions", e);
            ctx.getSource().sendFailure(Component.literal(
                    "Definition reload failed; previous definitions remain active: " + e.getMessage()));
            return 0;
        }
    }

    private static int debugMission(CommandContext<CommandSourceStack> ctx) {
        UUID instanceId = UuidArgument.getUuid(ctx, "instance_uuid");
        MissionInstance instance = MissionManager.all(ctx.getSource().getServer()).stream()
                .filter(candidate -> candidate.instanceId().equals(instanceId))
                .findFirst().orElse(null);
        if (instance == null) {
            ctx.getSource().sendFailure(Component.literal("Mission instance not found: " + instanceId));
            return 0;
        }
        ctx.getSource().sendSystemMessage(Component.literal(
                "Mission " + instance.missionId()
                        + " state=" + instance.state()
                        + " owner=" + instance.ownerName()
                        + " participants=" + instance.participantIds().size()
                        + " stage=" + instance.stageId() + "[" + instance.stageIndex() + "]"
                        + " tick=" + instance.stageTick()
                        + " fired=" + instance.firedEventIds()
                        + " groups=" + instance.trackedEntityGroups()
                        + " dead=" + instance.deadTrackedEntities()
                        + " acquired=" + instance.acquiredItemGroups()
        ));
        return 1;
    }

    private static int playRadio(CommandContext<CommandSourceStack> ctx, RadioInterruptMode mode) {
        try {
            Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
            ResourceLocation id = ResourceLocationArgument.getId(ctx, "message_id");
            if (!RadioCommunicationService.play(players, id, mode)) {
                ctx.getSource().sendFailure(Component.literal("Unknown radio message: " + id));
                return 0;
            }
            ctx.getSource().sendSuccess(
                    () -> Component.literal("Playing radio message " + id + " for " + players.size() + " player(s)"),
                    true
            );
            return players.size();
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to play radio message", e);
            return 0;
        }
    }

    private static int stopRadio(CommandContext<CommandSourceStack> ctx) {
        try {
            Collection<ServerPlayer> players = EntityArgument.getPlayers(ctx, "targets");
            RadioCommunicationService.stop(players);
            ctx.getSource().sendSuccess(
                    () -> Component.literal("Stopped radio for " + players.size() + " player(s)"),
                    true
            );
            return players.size();
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to stop radio message", e);
            return 0;
        }
    }

    private static int resetBossPosition(CommandContext<CommandSourceStack> ctx) {
        try {
            CommandSourceStack source = ctx.getSource();

            UUID uuid = UuidArgument.getUuid(ctx, "uuid");
            BlockPos pos = BlockPosArgument.getLoadedBlockPos(ctx, "pos");

            ServerLevel level = source.getLevel();

            Entity entity = level.getEntity(uuid);

            if (!(entity instanceof BaseBossEntity boss)) {
                source.sendFailure(Component.literal("Entity not found"));
                return 0;
            }

            boss.teleportTo(
                    pos.getX() + 0.5,
                    pos.getY(),
                    pos.getZ() + 0.5
            );

            boss.setActivated(false);
            boss.setAiMode(BaseBossEntity.AI_MODE_INACTIVE);

            source.sendSuccess(() -> Component.literal("Boss moved!"), true);
            return 0;

        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Fail", e);
            return 0;
        }
    }

    private static int executeCoord(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player  = EntityArgument.getPlayer(ctx, "player");
            String label         = StringArgumentType.getString(ctx, "label");
            BlockPos pos         = BlockPosArgument.getBlockPos(ctx, "pos");
            ResourceLocation dim = player.level().dimension().location();

            // インベントリからPomkotsRadarItemを探す
            ItemStack radarStack = findRadarItem(player);
            if (radarStack == null) {
                ctx.getSource().sendFailure(Component.literal(
                        player.getName().getString() + " does not have a Pomkots Data Pad"));
                return 0;
            }

            PomkotsDatapadItem.addTarget(radarStack, new RadarTarget.CoordTarget(pos, label, dim));

            ctx.getSource().sendSuccess(() -> Component.literal(
                    "Registered radar target: " + label + " at " + pos), true);
            return 1;

        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Fail", e);
            return 0;
        }
    }

    private static ItemStack findRadarItem(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof PomkotsDatapadItem) return stack;
        }
        return null;
    }

    private static int executeRaid(CommandContext<CommandSourceStack> ctx) {
        String raid_type = StringArgumentType.getString(ctx, "raid_type");
        String command_success = StringArgumentType.getString(ctx, "command_success");
        String command_fail = StringArgumentType.getString(ctx, "command_fail");

        CommandSourceStack source = ctx.getSource();
        Level level = source.getLevel();

        var raidData = PomkotsDataPackManager.getInstance().getDataPack().getRaidData(raid_type);

        if (raidData == null) {
            source.sendFailure(Component.literal("Undefined raid: " + raid_type));

        } else if ("defense".equals(raidData.type)){
            BlockPos origin = BlockPos.containing(source.getPosition());

            int radius = 100;
            double closestDist = Double.MAX_VALUE;
            BlockPos closest = null;
            Block targetBlock = PomkotsMechs.CORE_STONE_BLOCK.get();

            for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius),
                    origin.offset(radius, radius, radius))) {
                if (level.getBlockState(pos).is(targetBlock)) {
                    double dist = pos.distToCenterSqr(origin.getX(), origin.getY(), origin.getZ());
                    if (dist < closestDist) {
                        closestDist = dist;
                        closest = pos.immutable();
                    }
                }
            }

            if (closest != null) {
                BlockEntity be = level.getBlockEntity(closest);
                if (be instanceof CoreStoneBlockEntity csbe) {
                    var p = source.getPlayer();
                    if (!csbe.isRaidActive() && p != null) {
                        csbe.startRaid(raid_type, p.getUUID(), command_success, command_fail);
                        csbe.setChanged();
                        source.sendSuccess(() ->
                                Component.literal("Raid Started"), true);
                    } else {
                        source.sendFailure(Component.literal("Another raid is active now."));
                    }
                } else {
                    source.sendFailure(Component.literal("Block has no BlockEntity."));
                }
            } else {
                source.sendFailure(Component.literal("No nearby block of type " + targetBlock + " found."));
            }
        } else if ("survive".equals(raidData.type)) {
            RaidControllerEntity rce = PomkotsMechs.RAID_CONTROLLER.get().create(level);
            if (rce != null) {
                BlockPos pos = BlockPos.containing(source.getPosition());
                rce.setPos(pos.getX(), pos.getY(), pos.getZ());
                CompoundTag tag = RaidControllerEntity.buildCompoundTag(
                        raid_type, source.getPlayer().getUUID(), command_success, command_fail
                );
                rce.readAdditionalSaveData(tag);
                level.addFreshEntity(rce);

                source.sendSuccess(() ->
                        Component.literal("Raid Started"), true);
            } else {
                source.sendFailure(Component.literal("Cant spawn raid controller"));
            }
        } else if ("sweep".equals(raidData.type)) {
            RaidControllerEntity rce = PomkotsMechs.RAID_CONTROLLER.get().create(level);
            if (rce != null) {
                BlockPos pos = BlockPos.containing(source.getPosition());
                rce.setPos(pos.getX(), pos.getY(), pos.getZ());
                CompoundTag tag = RaidControllerEntity.buildCompoundTag(
                        raid_type, source.getPlayer().getUUID(), command_success, command_fail
                );
                rce.readAdditionalSaveData(tag);
                level.addFreshEntity(rce);

                source.sendSuccess(() ->
                        Component.literal("Raid Started"), true);
            } else {
                source.sendFailure(Component.literal("Cant spawn raid controller"));
            }
        } else if ("activate".equals(raidData.type)) {
            RaidControllerEntity rce = PomkotsMechs.RAID_CONTROLLER.get().create(level);
            if (rce != null) {
                BlockPos pos = BlockPos.containing(source.getPosition());
                rce.setPos(pos.getX(), pos.getY(), pos.getZ());
                CompoundTag tag = RaidControllerEntity.buildCompoundTag(
                        raid_type, source.getPlayer().getUUID(), command_success, command_fail
                );
                rce.readAdditionalSaveData(tag);
                level.addFreshEntity(rce);

                source.sendSuccess(() ->
                        Component.literal("Raid Started"), true);
            } else {
                source.sendFailure(Component.literal("Cant spawn raid controller"));
            }
        }  else {
            source.sendFailure(Component.literal("Undefined raid type: " + raidData.type));
        }

        return 1;
    }

}
