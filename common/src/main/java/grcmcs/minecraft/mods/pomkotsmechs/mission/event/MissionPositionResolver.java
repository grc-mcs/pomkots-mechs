package grcmcs.minecraft.mods.pomkotsmechs.mission.event;

import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionInstance;

import com.google.gson.JsonObject;
import grcmcs.minecraft.mods.pomkotsmechs.block.mission.MissionAnchorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class MissionPositionResolver {
    private MissionPositionResolver() {
    }

    public static ResolvedPosition resolve(
            MinecraftServer server,
            MissionInstance instance,
            ServerPlayer owner,
            JsonObject json
    ) {
        String type = GsonHelper.getAsString(json, "type", "anchor");
        return switch (type) {
            case "anchor" -> {
                ServerLevel level = server.getLevel(instance.anchor().dimension());
                if (level == null) throw new IllegalArgumentException("Mission anchor dimension is unavailable");
                Vec3 base = Vec3.atBottomCenterOf(instance.anchor().pos());
                yield new ResolvedPosition(level, base.add(number(json, "x"), number(json, "y"), number(json, "z")));
            }
            case "anchor_facing" -> {
                ServerLevel level = server.getLevel(instance.anchor().dimension());
                if (level == null) throw new IllegalArgumentException("Mission anchor dimension is unavailable");
                var state = level.getBlockState(instance.anchor().pos());
                if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    throw new ResolutionException(Component.translatable(
                            "mission.pomkotsmechs.error.anchor_facing_unavailable"));
                }
                var facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                Vec3 forward = new Vec3(facing.getStepX(), 0.0D, facing.getStepZ());
                Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
                Vec3 offset = right.scale(number(json, "right"))
                        .add(0.0D, number(json, "up"), 0.0D)
                        .add(forward.scale(number(json, "forward")));
                yield new ResolvedPosition(
                        level, Vec3.atBottomCenterOf(instance.anchor().pos()).add(offset));
            }
            case "owner" -> {
                requireOwner(owner);
                yield new ResolvedPosition(owner.serverLevel(), owner.position().add(
                        number(json, "x"), number(json, "y"), number(json, "z")));
            }
            case "owner_facing" -> {
                requireOwner(owner);
                double yaw = Math.toRadians(owner.getYRot());
                Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
                Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
                Vec3 offset = right.scale(number(json, "right"))
                        .add(0.0D, number(json, "up"), 0.0D)
                        .add(forward.scale(number(json, "forward")));
                yield new ResolvedPosition(owner.serverLevel(), owner.position().add(offset));
            }
            case "tracked_entity" -> {
                Entity entity = resolveTrackedEntity(server, instance, json);
                yield new ResolvedPosition((ServerLevel) entity.level(), entity.position().add(
                        number(json, "x"), number(json, "y"), number(json, "z")));
            }
            case "tracked_entity_facing" -> {
                Entity entity = resolveTrackedEntity(server, instance, json);
                double yaw = Math.toRadians(entity.getYRot());
                Vec3 forward = new Vec3(-Math.sin(yaw), 0.0D, Math.cos(yaw));
                Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
                Vec3 offset = right.scale(number(json, "right"))
                        .add(0.0D, number(json, "up"), 0.0D)
                        .add(forward.scale(number(json, "forward")));
                yield new ResolvedPosition((ServerLevel) entity.level(), entity.position().add(offset));
            }
            case "absolute" -> {
                ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "dimension"));
                ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, id));
                if (level == null) throw new IllegalArgumentException("Unknown dimension: " + id);
                yield new ResolvedPosition(level, new Vec3(number(json, "x"), number(json, "y"), number(json, "z")));
            }
            case "mission_anchor" -> resolveMissionAnchor(server, instance, json);
            default -> throw new IllegalArgumentException("Unknown mission position type: " + type);
        };
    }

    public static List<ResolvedPosition> resolveAllForSpawn(
            MinecraftServer server,
            MissionInstance instance,
            ServerPlayer owner,
            JsonObject json
    ) {
        if (!GsonHelper.getAsString(json, "type", "anchor").equals("mission_anchor")) {
            return List.of(resolve(server, instance, owner, json));
        }
        return resolveMissionAnchors(server, instance, json);
    }

    private static ResolvedPosition resolveMissionAnchor(
            MinecraftServer server, MissionInstance instance, JsonObject json
    ) {
        List<ResolvedPosition> matches = resolveMissionAnchors(server, instance, json);
        String anchorId = GsonHelper.getAsString(json, "id", "").trim();
        if (matches.size() > 1) {
            throw new ResolutionException(Component.translatable(
                    "mission.pomkotsmechs.error.mission_anchor_duplicate", anchorId));
        }
        return matches.get(0);
    }

    private static List<ResolvedPosition> resolveMissionAnchors(
            MinecraftServer server, MissionInstance instance, JsonObject json
    ) {
        String anchorId = GsonHelper.getAsString(json, "id", "").trim();
        if (anchorId.isEmpty()) {
            throw new ResolutionException(Component.translatable(
                    "mission.pomkotsmechs.error.mission_anchor_id_required"));
        }
        if (instance.forcedChunks().isEmpty()) {
            throw new ResolutionException(Component.translatable(
                    "mission.pomkotsmechs.error.mission_anchor_requires_loaded_areas", anchorId));
        }

        List<MissionAnchorBlockEntity> matches = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        for (MissionInstance.ForcedChunk forced : instance.forcedChunks()) {
            ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, forced.dimension()));
            if (level == null) continue;
            for (var blockEntity : level.getChunk(forced.x(), forced.z()).getBlockEntities().values()) {
                if (!(blockEntity instanceof MissionAnchorBlockEntity anchor)
                        || !anchor.anchorId().equals(anchorId)
                        || !visited.add(level.dimension().location() + "@" + anchor.getBlockPos().asLong())) continue;
                matches.add(anchor);
            }
        }
        if (matches.isEmpty()) {
            throw new ResolutionException(Component.translatable(
                    "mission.pomkotsmechs.error.mission_anchor_not_found", anchorId));
        }

        matches.sort(Comparator
                .comparing((MissionAnchorBlockEntity anchor) ->
                        anchor.getLevel().dimension().location().toString())
                .thenComparingLong(anchor -> anchor.getBlockPos().asLong()));
        List<ResolvedPosition> resolved = new ArrayList<>();
        for (MissionAnchorBlockEntity match : matches) {
            if (!(match.getLevel() instanceof ServerLevel level)) continue;
            Vec3 position = Vec3.atBottomCenterOf(match.getBlockPos()).add(
                    number(json, "x"), number(json, "y"), number(json, "z"));
            resolved.add(new ResolvedPosition(level, position));
        }
        return List.copyOf(resolved);
    }

    private static double number(JsonObject json, String key) {
        return GsonHelper.getAsDouble(json, key, 0.0D);
    }

    private static Entity resolveTrackedEntity(
            MinecraftServer server, MissionInstance instance, JsonObject json
    ) {
        String group = GsonHelper.getAsString(json, "group", "").trim();
        if (group.isEmpty()) {
            throw new ResolutionException(Component.literal(
                    "Tracked entity position requires a group"));
        }
        List<UUID> ids = instance.trackedEntityGroups().getOrDefault(group, Set.of())
                .stream().sorted().toList();
        for (UUID id : ids) {
            for (ServerLevel level : server.getAllLevels()) {
                Entity entity = level.getEntity(id);
                if (entity != null && !entity.isRemoved()) return entity;
            }
        }
        throw new ResolutionException(Component.literal(
                "Tracked entity group could not be resolved: " + group));
    }

    private static void requireOwner(ServerPlayer owner) {
        if (owner == null) throw new IllegalArgumentException("Mission owner is offline");
    }

    public record ResolvedPosition(ServerLevel level, Vec3 position) {
    }

    public static final class ResolutionException extends RuntimeException {
        private final Component playerMessage;

        public ResolutionException(Component playerMessage) {
            super(playerMessage.getString());
            this.playerMessage = playerMessage;
        }

        public Component playerMessage() {
            return playerMessage;
        }
    }
}
