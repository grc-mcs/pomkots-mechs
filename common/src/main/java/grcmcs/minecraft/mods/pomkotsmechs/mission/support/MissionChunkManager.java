package grcmcs.minecraft.mods.pomkotsmechs.mission.support;

import grcmcs.minecraft.mods.pomkotsmechs.mission.event.MissionPositionResolver;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionInstance;

import grcmcs.minecraft.mods.pomkotsmechs.config.PomkotsConfig;
import grcmcs.minecraft.mods.pomkotsmechs.mission.definition.MissionDefinition;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class MissionChunkManager {
    private static final Map<Key, State> REFERENCES = new HashMap<>();

    private MissionChunkManager() { }

    public static String acquire(MinecraftServer server, ServerPlayer owner,
                                 MissionDefinition definition, MissionInstance instance) {
        Set<Key> requested = new LinkedHashSet<>();
        for (MissionDefinition.LoadedArea area : definition.loadedAreas()) {
            var from = MissionPositionResolver.resolve(server, instance, owner, area.from());
            var to = MissionPositionResolver.resolve(server, instance, owner, area.to());
            if (from.level() != to.level()) return "Loaded area crosses dimensions";
            int minX = Math.min(((int) Math.floor(from.position().x)) >> 4, ((int) Math.floor(to.position().x)) >> 4);
            int maxX = Math.max(((int) Math.floor(from.position().x)) >> 4, ((int) Math.floor(to.position().x)) >> 4);
            int minZ = Math.min(((int) Math.floor(from.position().z)) >> 4, ((int) Math.floor(to.position().z)) >> 4);
            int maxZ = Math.max(((int) Math.floor(from.position().z)) >> 4, ((int) Math.floor(to.position().z)) >> 4);
            for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
                requested.add(new Key(from.level().dimension().location(), x, z));
            }
        }

        PomkotsConfig config = AutoConfig.getConfigHolder(PomkotsConfig.class).getConfig();
        if (requested.size() > Math.max(0, config.missionMaxForcedChunksPerInstance)) {
            return "Mission force chunk limit exceeded: " + requested.size();
        }
        long newUnique = requested.stream().filter(key -> !REFERENCES.containsKey(key)).count();
        if (REFERENCES.size() + newUnique > Math.max(0, config.missionMaxForcedChunksTotal)) {
            return "Server mission force chunk limit exceeded";
        }

        List<Key> acquired = new ArrayList<>();
        try {
            for (Key key : requested) {
                ServerLevel level = level(server, key.dimension());
                if (level == null) throw new IllegalArgumentException("Unknown loaded area dimension: " + key.dimension());
                State state = REFERENCES.get(key);
                boolean originallyForced = state != null ? state.originallyForced()
                        : level.getForcedChunks().contains(ChunkPos.asLong(key.x(), key.z()));
                if (state == null) {
                    level.setChunkForced(key.x(), key.z(), true);
                    REFERENCES.put(key, new State(1, originallyForced));
                } else {
                    REFERENCES.put(key, new State(state.references() + 1, state.originallyForced()));
                }
                instance.addForcedChunk(key.dimension(), key.x(), key.z(), originallyForced);
                acquired.add(key);
            }
            return null;
        } catch (Exception e) {
            releaseKeys(server, acquired);
            instance.forcedChunks().clear();
            return e.getMessage();
        }
    }

    public static void release(MinecraftServer server, MissionInstance instance) {
        List<Key> keys = instance.forcedChunks().stream()
                .map(chunk -> new Key(chunk.dimension(), chunk.x(), chunk.z())).toList();
        releaseKeys(server, keys);
        instance.forcedChunks().clear();
    }

    public static String updateDynamic(
            MinecraftServer server,
            MissionDefinition definition,
            MissionInstance instance
    ) {
        MissionDefinition.DynamicLoadedArea dynamic = definition.dynamicLoadedArea();
        if (dynamic == null) return null;

        Entity followed = findFollowedEntity(server, instance, dynamic.followGroup());
        if (followed == null || followed.isRemoved()) return null;

        ResourceLocation dimension = followed.level().dimension().location();
        ChunkPos center = followed.chunkPosition();
        Set<Key> requested = dynamicKeys(
                dimension, center.x, center.z, followed.getDeltaMovement().x,
                followed.getDeltaMovement().z, followed.getYRot(),
                dynamic.radiusChunks(), dynamic.forwardChunks());
        Set<Key> fixed = new LinkedHashSet<>();
        Set<Key> currentDynamic = new LinkedHashSet<>();
        for (MissionInstance.ForcedChunk chunk : instance.forcedChunks()) {
            Key key = new Key(chunk.dimension(), chunk.x(), chunk.z());
            if (chunk.dynamic()) currentDynamic.add(key);
            else fixed.add(key);
        }
        requested.removeAll(fixed);
        if (requested.equals(currentDynamic)) return null;

        Set<Key> finalInstanceKeys = new LinkedHashSet<>(fixed);
        finalInstanceKeys.addAll(requested);
        PomkotsConfig config = AutoConfig.getConfigHolder(PomkotsConfig.class).getConfig();
        if (finalInstanceKeys.size() > Math.max(0, config.missionMaxForcedChunksPerInstance)) {
            return "Mission dynamic force chunk limit exceeded: " + finalInstanceKeys.size();
        }

        Set<Key> additions = new LinkedHashSet<>(requested);
        additions.removeAll(currentDynamic);
        Set<Key> removals = new LinkedHashSet<>(currentDynamic);
        removals.removeAll(requested);
        long newUnique = additions.stream().filter(key -> !REFERENCES.containsKey(key)).count();
        long releasedUnique = removals.stream()
                .filter(key -> {
                    State state = REFERENCES.get(key);
                    return state != null && state.references() == 1;
                }).count();
        if (REFERENCES.size() - releasedUnique + newUnique
                > Math.max(0, config.missionMaxForcedChunksTotal)) {
            return "Server mission dynamic force chunk limit exceeded";
        }

        List<Key> acquired = new ArrayList<>();
        try {
            for (Key key : additions) {
                acquireKey(server, instance, key, true);
                acquired.add(key);
            }
        } catch (Exception e) {
            releaseKeys(server, acquired);
            instance.forcedChunks().removeIf(chunk -> acquired.contains(
                    new Key(chunk.dimension(), chunk.x(), chunk.z())));
            return e.getMessage();
        }

        releaseKeys(server, new ArrayList<>(removals));
        instance.forcedChunks().removeIf(chunk -> chunk.dynamic() && removals.contains(
                new Key(chunk.dimension(), chunk.x(), chunk.z())));
        return null;
    }

    public static boolean contains(MissionInstance instance, MissionPositionResolver.ResolvedPosition position) {
        if (instance.forcedChunks().isEmpty()) return true;
        int x = ((int) Math.floor(position.position().x)) >> 4;
        int z = ((int) Math.floor(position.position().z)) >> 4;
        ResourceLocation dimension = position.level().dimension().location();
        return instance.forcedChunks().stream().anyMatch(chunk ->
                chunk.dimension().equals(dimension) && chunk.x() == x && chunk.z() == z);
    }

    public static void rebuildReferences(Iterable<MissionInstance> instances) {
        REFERENCES.clear();
        for (MissionInstance instance : instances) for (MissionInstance.ForcedChunk chunk : instance.forcedChunks()) {
            Key key = new Key(chunk.dimension(), chunk.x(), chunk.z());
            State old = REFERENCES.get(key);
            REFERENCES.put(key, new State(old == null ? 1 : old.references() + 1,
                    old == null ? chunk.originallyForced() : old.originallyForced() && chunk.originallyForced()));
        }
    }

    private static void releaseKeys(MinecraftServer server, List<Key> keys) {
        for (Key key : keys) {
            State state = REFERENCES.get(key);
            if (state == null) continue;
            if (state.references() > 1) {
                REFERENCES.put(key, new State(state.references() - 1, state.originallyForced()));
            } else {
                REFERENCES.remove(key);
                ServerLevel level = level(server, key.dimension());
                if (level != null && !state.originallyForced()) level.setChunkForced(key.x(), key.z(), false);
            }
        }
    }

    private static void acquireKey(
            MinecraftServer server, MissionInstance instance, Key key, boolean dynamic
    ) {
        ServerLevel level = level(server, key.dimension());
        if (level == null) {
            throw new IllegalArgumentException("Unknown loaded area dimension: " + key.dimension());
        }
        State state = REFERENCES.get(key);
        boolean originallyForced = state != null ? state.originallyForced()
                : level.getForcedChunks().contains(ChunkPos.asLong(key.x(), key.z()));
        if (state == null) {
            level.setChunkForced(key.x(), key.z(), true);
            REFERENCES.put(key, new State(1, originallyForced));
        } else {
            REFERENCES.put(key, new State(state.references() + 1, state.originallyForced()));
        }
        instance.addForcedChunk(key.dimension(), key.x(), key.z(), originallyForced, dynamic);
    }

    private static Entity findFollowedEntity(
            MinecraftServer server, MissionInstance instance, String group
    ) {
        for (UUID id : instance.trackedEntityGroups().getOrDefault(group, Set.of())) {
            for (ServerLevel level : server.getAllLevels()) {
                Entity entity = level.getEntity(id);
                if (entity != null && !entity.isRemoved()) return entity;
            }
        }
        return null;
    }

    private static Set<Key> dynamicKeys(
            ResourceLocation dimension,
            int centerX,
            int centerZ,
            double velocityX,
            double velocityZ,
            float yaw,
            int radius,
            int forward
    ) {
        Set<Key> result = new LinkedHashSet<>();
        for (int x = centerX - radius; x <= centerX + radius; x++) {
            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                result.add(new Key(dimension, x, z));
            }
        }
        if (forward <= 0) return result;

        int directionX;
        int directionZ;
        if (velocityX * velocityX + velocityZ * velocityZ > 1.0E-4D) {
            if (Math.abs(velocityX) >= Math.abs(velocityZ)) {
                directionX = velocityX >= 0.0D ? 1 : -1;
                directionZ = 0;
            } else {
                directionX = 0;
                directionZ = velocityZ >= 0.0D ? 1 : -1;
            }
        } else {
            double radians = Math.toRadians(yaw);
            double lookX = -Math.sin(radians);
            double lookZ = Math.cos(radians);
            if (Math.abs(lookX) >= Math.abs(lookZ)) {
                directionX = lookX >= 0.0D ? 1 : -1;
                directionZ = 0;
            } else {
                directionX = 0;
                directionZ = lookZ >= 0.0D ? 1 : -1;
            }
        }
        for (int step = radius + 1; step <= radius + forward; step++) {
            for (int side = -radius; side <= radius; side++) {
                int x = centerX + directionX * step + directionZ * side;
                int z = centerZ + directionZ * step + directionX * side;
                result.add(new Key(dimension, x, z));
            }
        }
        return result;
    }

    private static ServerLevel level(MinecraftServer server, ResourceLocation dimension) {
        return server.getLevel(ResourceKey.create(Registries.DIMENSION, dimension));
    }

    private record Key(ResourceLocation dimension, int x, int z) { }
    private record State(int references, boolean originallyForced) { }
}
