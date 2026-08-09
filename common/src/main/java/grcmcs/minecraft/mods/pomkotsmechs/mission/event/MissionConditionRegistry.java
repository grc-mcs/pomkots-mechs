package grcmcs.minecraft.mods.pomkotsmechs.mission.event;

import grcmcs.minecraft.mods.pomkotsmechs.mission.event.MissionPositionResolver;
import grcmcs.minecraft.mods.pomkotsmechs.mission.event.MissionSpawnSource;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionInstance;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionSavedData;

import com.google.gson.JsonObject;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.mission.definition.MissionDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.MissionMarkerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.TransportShipEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class MissionConditionRegistry {
    private static final Map<ResourceLocation, Evaluator> EVALUATORS = new HashMap<>();

    static {
        register("elapsed_time", context -> context.instance().stageTick() >= context.condition().ticks());
        register("owner_dead", context -> context.owner() != null && !context.owner().isAlive());
        register("owner_offline", context -> context.owner() == null);
        register("always", context -> true);
        register("all_of", context -> context.condition().children().stream().allMatch(context::evaluate));
        register("any_of", context -> context.condition().children().stream().anyMatch(context::evaluate));
        register("not", context -> !context.evaluate(context.condition().children().get(0)));
        register("all_tracked_entities_dead", MissionConditionRegistry::allTrackedDead);
        register("tracked_entity_dead", MissionConditionRegistry::trackedEntityDead);
        register("entity_dead", MissionConditionRegistry::trackedEntityDead);
        register("player_reached", MissionConditionRegistry::playerReached);
        register("player_has_item", MissionConditionRegistry::playerHasItem);
        register("tracked_item_acquired", context -> context.instance().acquiredItemGroups()
                .contains(GsonHelper.getAsString(context.condition().data(), "group")));
        register("spawn_sources_completed", MissionConditionRegistry::spawnSourcesCompleted);
        register("tracked_entity_health_below", MissionConditionRegistry::trackedEntityHealthBelow);
        register("player_vehicle_health_below", MissionConditionRegistry::playerVehicleHealthBelow);
        register("transport_ship_reached", MissionConditionRegistry::transportShipReached);
        register("transport_ship_stuck", MissionConditionRegistry::transportShipStuck);
    }

    private MissionConditionRegistry() {
    }

    public static boolean evaluate(MinecraftServer server, MissionInstance instance, MissionDefinition.Condition condition) {
        Evaluator evaluator = EVALUATORS.get(condition.type());
        if (evaluator == null) return false;
        ServerPlayer owner = server.getPlayerList().getPlayer(instance.ownerId());
        return evaluator.evaluate(new Context(server, instance, condition, owner));
    }

    private static void register(String path, Evaluator evaluator) {
        EVALUATORS.put(PomkotsMechs.id(path), evaluator);
    }

    private static boolean allTrackedDead(Context context) {
        JsonObject data = context.condition().data();
        String group = GsonHelper.getAsString(data, "group");
        if (GsonHelper.getAsBoolean(data, "require_group_spawned", true)
                && !context.instance().spawnedGroups().contains(group)) return false;
        Set<UUID> ids = context.instance().trackedEntityGroups().getOrDefault(group, Set.of());
        if (ids.isEmpty()) return GsonHelper.getAsBoolean(data, "allow_empty", false);
        return ids.stream().allMatch(id -> isConfirmedDead(context, id));
    }

    private static boolean trackedEntityDead(Context context) {
        String group = GsonHelper.getAsString(context.condition().data(), "group");
        Set<UUID> ids = context.instance().trackedEntityGroups().getOrDefault(group, Set.of());
        return !ids.isEmpty() && ids.stream().anyMatch(id -> isConfirmedDead(context, id));
    }

    private static boolean trackedEntityHealthBelow(Context context) {
        JsonObject data = context.condition().data();
        String group = GsonHelper.getAsString(data, "group");
        if (GsonHelper.getAsBoolean(data, "require_group_spawned", true)
                && !context.instance().spawnedGroups().contains(group)) return false;
        Set<UUID> ids = context.instance().trackedEntityGroups().getOrDefault(group, Set.of());
        if (ids.isEmpty()) return false;
        double ratio = GsonHelper.getAsDouble(data, "ratio");
        boolean all = GsonHelper.getAsString(data, "mode", "any").equals("all");
        if (all) {
            return ids.stream().allMatch(id -> healthRatioAtOrBelow(context, id, ratio));
        }
        return ids.stream().anyMatch(id -> healthRatioAtOrBelow(context, id, ratio));
    }

    private static boolean healthRatioAtOrBelow(Context context, UUID id, double ratio) {
        Entity entity = findLoadedEntity(context.server(), id);
        if (!(entity instanceof net.minecraft.world.entity.LivingEntity living)) return false;
        if (living.getMaxHealth() <= 0.0F) return false;
        return living.getHealth() / living.getMaxHealth() <= ratio;
    }

    private static boolean playerVehicleHealthBelow(Context context) {
        JsonObject data = context.condition().data();
        List<ServerPlayer> players = context.players(
                GsonHelper.getAsString(data, "target", "owner"));
        if (players.isEmpty()) return false;
        double ratio = GsonHelper.getAsDouble(data, "ratio");
        boolean all = GsonHelper.getAsString(data, "mode", "any").equals("all");
        if (all) {
            return players.stream().allMatch(player -> vehicleHealthAtOrBelow(player, ratio));
        }
        return players.stream().anyMatch(player -> vehicleHealthAtOrBelow(player, ratio));
    }

    private static boolean vehicleHealthAtOrBelow(ServerPlayer player, double ratio) {
        if (!(player.getVehicle()
                instanceof grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase vehicle)) {
            return false;
        }
        if (vehicle.getMaxHealth() <= 0.0F || !vehicle.isAlive()) return false;
        return vehicle.getHealth() / vehicle.getMaxHealth() <= ratio;
    }

    private static boolean spawnSourcesCompleted(Context context) {
        JsonObject data = context.condition().data();
        String group = GsonHelper.getAsString(data, "group");
        if (GsonHelper.getAsBoolean(data, "require_group_spawned", true)
                && !context.instance().spawnedGroups().contains(group)) return false;
        Set<UUID> ids = context.instance().spawnSourceGroups().getOrDefault(group, Set.of());
        if (ids.isEmpty()) return GsonHelper.getAsBoolean(data, "allow_empty", true);
        for (UUID id : ids) {
            if (context.instance().completedSpawnSources().contains(id)) continue;
            Entity entity = findLoadedEntity(context.server(), id);
            // 死亡・削除済みの生成元は、今後Entityを生成しないため完了扱い。
            if (entity != null && (!entity.isAlive() || entity.isRemoved())) {
                markSpawnSourceCompleted(context, id);
                continue;
            }
            // nullはアンロード中の可能性があるため、完了とは断定しない。
            if (!(entity instanceof MissionSpawnSource source)) return false;
            if (source.hasPendingMissionSpawns()) return false;
            markSpawnSourceCompleted(context, id);
        }
        return true;
    }

    private static boolean transportShipReached(Context context) {
        JsonObject data = context.condition().data();
        String group = GsonHelper.getAsString(data, "group");
        String waypoint = GsonHelper.getAsString(data, "waypoint");
        Set<UUID> ids = context.instance().trackedEntityGroups().getOrDefault(group, Set.of());
        if (ids.isEmpty()) return false;
        boolean all = GsonHelper.getAsString(data, "mode", "all").equals("all");
        return all
                ? ids.stream().allMatch(id -> {
                    Entity entity = findLoadedEntity(context.server(), id);
                    return entity instanceof TransportShipEntity ship && ship.hasReached(waypoint);
                })
                : ids.stream().anyMatch(id -> {
                    Entity entity = findLoadedEntity(context.server(), id);
                    return entity instanceof TransportShipEntity ship && ship.hasReached(waypoint);
                });
    }

    private static boolean transportShipStuck(Context context) {
        JsonObject data = context.condition().data();
        String group = GsonHelper.getAsString(data, "group");
        int ticks = Math.max(1, GsonHelper.getAsInt(data, "ticks", 200));
        for (UUID id : context.instance().trackedEntityGroups().getOrDefault(group, Set.of())) {
            Entity entity = findLoadedEntity(context.server(), id);
            if (entity instanceof TransportShipEntity ship && ship.isRouteStuck(ticks)) return true;
        }
        return false;
    }

    private static void markSpawnSourceCompleted(Context context, UUID id) {
        if (context.instance().markSpawnSourceCompleted(id)) {
            MissionSavedData.get(context.server()).setDirty();
        }
    }

    private static boolean isConfirmedDead(Context context, UUID id) {
        if (context.instance().deadTrackedEntities().contains(id)) return true;
        Entity loaded = findLoadedEntity(context.server(), id);
        if (loaded != null && (loaded instanceof Pmvc01Entity mech && mech.isBroken()
                || !loaded.isAlive() || loaded.isRemoved())) {
            context.instance().markTrackedEntityDead(id);
            MissionSavedData.get(context.server()).setDirty();
            return true;
        }
        // nullはアンロード中の可能性があるため、生存として扱う。
        return false;
    }

    private static boolean playerReached(Context context) {
        JsonObject data = context.condition().data();
        MissionPositionResolver.ResolvedPosition target = MissionPositionResolver.resolve(
                context.server(), context.instance(), context.owner(), GsonHelper.getAsJsonObject(data, "position"));
        AABB area = reachedArea(data, target.position());
        ensureReachedAreaMarker(context, target, area);
        List<ServerPlayer> players = context.players(GsonHelper.getAsString(data, "target", "owner"));
        if (players.isEmpty()) return false;
        boolean all = GsonHelper.getAsString(data, "mode", "any").equals("all");
        return all ? players.stream().allMatch(player -> reached(player, target, area))
                : players.stream().anyMatch(player -> reached(player, target, area));
    }

    private static boolean reached(ServerPlayer player, MissionPositionResolver.ResolvedPosition target, AABB area) {
        return player.serverLevel() == target.level() && area.contains(player.position());
    }

    private static AABB reachedArea(JsonObject data, Vec3 origin) {
        if (data.has("aabb")) {
            JsonObject aabb = GsonHelper.getAsJsonObject(data, "aabb");
            JsonObject min = GsonHelper.getAsJsonObject(aabb, "min");
            JsonObject max = GsonHelper.getAsJsonObject(aabb, "max");
            return new AABB(
                    origin.x + number(min, "x"), origin.y + number(min, "y"), origin.z + number(min, "z"),
                    origin.x + number(max, "x"), origin.y + number(max, "y"), origin.z + number(max, "z")
            );
        }
        double radius = Math.max(0.0D, GsonHelper.getAsDouble(data, "radius", 1.0D));
        return new AABB(origin.x - radius, origin.y - radius, origin.z - radius,
                origin.x + radius, origin.y + radius, origin.z + radius);
    }

    private static void ensureReachedAreaMarker(Context context,
                                                MissionPositionResolver.ResolvedPosition target, AABB area) {
        String group = "__marker_reached_" + Integer.toUnsignedString(context.condition().data().toString().hashCode());
        if (context.instance().spawnedGroups().contains(group)) {
            for (UUID id : context.instance().trackedEntityGroups().getOrDefault(group, Set.of())) {
                Entity loaded = findLoadedEntity(context.server(), id);
                if (loaded instanceof MissionMarkerEntity) loaded.setPos(target.position());
            }
            return;
        }
        MissionMarkerEntity marker = new MissionMarkerEntity(PomkotsMechs.MISSION_MARKER.get(), target.level());
        Vec3 origin = target.position();
        marker.setPos(origin.x, origin.y, origin.z);
        marker.configureReachedArea(
                (float) (area.minX - origin.x), (float) (area.minY - origin.y), (float) (area.minZ - origin.z),
                (float) (area.maxX - origin.x), (float) (area.maxY - origin.y), (float) (area.maxZ - origin.z));
        marker.addTag("pomkots_mission_marker:" + context.instance().instanceId());
        context.instance().markGroupSpawned(group);
        if (target.level().addFreshEntity(marker)) context.instance().trackEntity(group, marker.getUUID());
        MissionSavedData.get(context.server()).setDirty();
    }

    private static double number(JsonObject json, String key) {
        return GsonHelper.getAsDouble(json, key, 0.0D);
    }

    private static boolean playerHasItem(Context context) {
        JsonObject data = context.condition().data();
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(data, "item"));
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
        if (item == null) return false;
        int count = Math.max(1, GsonHelper.getAsInt(data, "count", 1));
        List<ServerPlayer> players = context.players(GsonHelper.getAsString(data, "target", "owner"));
        if (players.isEmpty()) return false;
        boolean all = GsonHelper.getAsString(data, "mode", "any").equals("all");
        return all ? players.stream().allMatch(player -> countItem(player, item) >= count)
                : players.stream().anyMatch(player -> countItem(player, item) >= count);
    }

    private static int countItem(ServerPlayer player, Item item) {
        int count = 0;
        for (ItemStack stack : player.getInventory().items) if (stack.is(item)) count += stack.getCount();
        for (ItemStack stack : player.getInventory().armor) if (stack.is(item)) count += stack.getCount();
        for (ItemStack stack : player.getInventory().offhand) if (stack.is(item)) count += stack.getCount();
        return count;
    }

    private static Entity findLoadedEntity(MinecraftServer server, UUID id) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(id);
            if (entity != null) return entity;
        }
        return null;
    }

    @FunctionalInterface
    private interface Evaluator {
        boolean evaluate(Context context);
    }

    private record Context(
            MinecraftServer server,
            MissionInstance instance,
            MissionDefinition.Condition condition,
            ServerPlayer owner
    ) {
        boolean evaluate(MissionDefinition.Condition child) {
            return MissionConditionRegistry.evaluate(server, instance, child);
        }

        List<ServerPlayer> players(String target) {
            List<ServerPlayer> result = new ArrayList<>();
            if (target.toLowerCase(Locale.ROOT).equals("owner")) {
                if (owner != null) result.add(owner);
                return result;
            }
            for (UUID id : instance.participantIds()) {
                ServerPlayer player = server.getPlayerList().getPlayer(id);
                if (player != null) result.add(player);
            }
            return result;
        }
    }
}
