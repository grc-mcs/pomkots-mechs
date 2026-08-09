package grcmcs.minecraft.mods.pomkotsmechs.mission.definition;

import grcmcs.minecraft.mods.pomkotsmechs.mission.definition.MissionDefinition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class MissionDefinitionRegistry {
    private static final Set<String> SUPPORTED_CONDITIONS = Set.of(
            "elapsed_time", "owner_dead", "owner_offline", "always", "all_of", "any_of", "not",
            "all_tracked_entities_dead", "tracked_entity_dead", "entity_dead", "player_reached", "player_has_item",
            "tracked_item_acquired", "spawn_sources_completed", "tracked_entity_health_below",
            "player_vehicle_health_below", "transport_ship_reached", "transport_ship_stuck"
    );
    private static final Set<String> SUPPORTED_EVENTS = Set.of(
            "noop", "radio", "spawn_entities", "spawn_piloted_mech", "activate_entities",
            "teleport_players", "drop_item", "command",
            "mission_presentation", "bgm", "mission_effect", "cutscene",
            "spawn_transport_ship", "transport_ship_control"
    );
    private static volatile Map<ResourceLocation, MissionDefinition> definitions = Map.of();

    private MissionDefinitionRegistry() {
    }

    public static Map<ResourceLocation, MissionDefinition> load(Path directory) throws IOException {
        Map<ResourceLocation, MissionDefinition> loaded = new LinkedHashMap<>();
        Files.createDirectories(directory);
        List<Path> files;
        try (var paths = Files.walk(directory)) {
            files = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
        }
        for (Path file : files) {
            ResourceLocation missionId = idFromFile(directory, file);
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                MissionDefinition definition = parse(missionId, json);
                if (loaded.put(missionId, definition) != null) {
                    throw new IllegalArgumentException("Duplicate mission id: " + missionId);
                }
            } catch (Exception e) {
                throw new IOException("Failed to load mission definition " + missionId + " from " + file, e);
            }
        }
        return Collections.unmodifiableMap(loaded);
    }

    public static void replace(Map<ResourceLocation, MissionDefinition> loaded) {
        definitions = Map.copyOf(loaded);
        PomkotsMechs.LOGGER.info("Loaded {} mission definitions", definitions.size());
    }

    public static Optional<MissionDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public static Map<ResourceLocation, MissionDefinition> all() {
        return definitions;
    }

    private static ResourceLocation idFromFile(Path directory, Path file) {
        Path relative = directory.toAbsolutePath().normalize().relativize(file.toAbsolutePath().normalize());
        String path = relative.toString().replace('\\', '/');
        if (path.startsWith("../") || !path.endsWith(".json")) {
            throw new IllegalArgumentException("Invalid mission path: " + file);
        }
        return new ResourceLocation(PomkotsMechs.MODID, path.substring(0, path.length() - 5));
    }

    private static MissionDefinition parse(ResourceLocation id, JsonObject json) {
        int version = GsonHelper.getAsInt(json, "version", 1);
        if (version != 1) throw new IllegalArgumentException("Unsupported mission version: " + version);

        Component displayName = component(json.get("display_name"), Component.literal(id.toString()));
        JsonObject settingsJson = json.has("settings")
                ? GsonHelper.getAsJsonObject(json, "settings") : new JsonObject();
        MissionDefinition.Settings settings = new MissionDefinition.Settings(
                GsonHelper.getAsBoolean(settingsJson, "allow_concurrent_instances", false),
                enumValue(settingsJson, "owner_disconnect", "fail", Set.of("fail", "suspend", "ignore")),
                enumValue(settingsJson, "participant_disconnect", "remove", Set.of("fail", "remove", "ignore")),
                enumValue(settingsJson, "owner_death", "fail", Set.of("fail", "ignore")),
                enumValue(settingsJson, "participant_death", "ignore", Set.of("fail", "remove", "ignore")),
                enumValue(settingsJson, "participant_mode", "owner", Set.of("owner", "team"))
        );

        JsonArray stagesJson = GsonHelper.getAsJsonArray(json, "stages");
        if (stagesJson.isEmpty()) throw new IllegalArgumentException("Mission has no stages: " + id);
        List<MissionDefinition.Stage> stages = new ArrayList<>();
        Set<String> stageIds = new HashSet<>();
        for (JsonElement element : stagesJson) {
            JsonObject stageJson = element.getAsJsonObject();
            String stageId = GsonHelper.getAsString(stageJson, "id");
            if (!stageIds.add(stageId)) throw new IllegalArgumentException("Duplicate stage id: " + stageId);
            stages.add(parseStage(id, stageId, stageJson));
        }

        return new MissionDefinition(
                id,
                version,
                displayName,
                settings,
                parseLoadedAreas(json),
                parseDynamicLoadedArea(json),
                stages,
                json.has("presentation") ? GsonHelper.getAsJsonObject(json, "presentation") : new JsonObject(),
                parseCommands(json, "on_success"),
                parseCommands(json, "on_failure")
        );
    }

    private static List<MissionDefinition.LoadedArea> parseLoadedAreas(JsonObject root) {
        if (!root.has("loaded_areas")) return List.of();
        List<MissionDefinition.LoadedArea> areas = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(root, "loaded_areas")) {
            JsonObject area = element.getAsJsonObject();
            areas.add(new MissionDefinition.LoadedArea(
                    GsonHelper.getAsJsonObject(area, "from"),
                    GsonHelper.getAsJsonObject(area, "to")));
        }
        return areas;
    }

    private static MissionDefinition.DynamicLoadedArea parseDynamicLoadedArea(JsonObject root) {
        if (!root.has("dynamic_loaded_area")) return null;
        JsonObject json = GsonHelper.getAsJsonObject(root, "dynamic_loaded_area");
        String followGroup = GsonHelper.getAsString(json, "follow_group");
        if (followGroup.isBlank()) {
            throw new IllegalArgumentException("dynamic_loaded_area follow_group must not be blank");
        }
        int radiusChunks = GsonHelper.getAsInt(json, "radius_chunks", 2);
        int forwardChunks = GsonHelper.getAsInt(json, "forward_chunks", 0);
        if (radiusChunks < 1 || radiusChunks > 16) {
            throw new IllegalArgumentException("dynamic_loaded_area radius_chunks must be between 1 and 16");
        }
        if (forwardChunks < 0 || forwardChunks > 8) {
            throw new IllegalArgumentException("dynamic_loaded_area forward_chunks must be between 0 and 8");
        }
        return new MissionDefinition.DynamicLoadedArea(followGroup, radiusChunks, forwardChunks);
    }

    private static MissionDefinition.Stage parseStage(ResourceLocation missionId, String stageId, JsonObject json) {
        if (!json.has("time_limit_ticks")) {
            throw new IllegalArgumentException("Stage has no time_limit_ticks: " + missionId + "/" + stageId);
        }
        int timeLimitTicks = GsonHelper.getAsInt(json, "time_limit_ticks");
        if (timeLimitTicks <= 0) {
            throw new IllegalArgumentException("Stage time_limit_ticks must be positive: " + missionId + "/" + stageId);
        }
        String timeLimitResult = enumValue(json, "time_limit_result", "failure", Set.of("success", "failure"));
        JsonObject hudProgress = json.has("hud_progress")
                ? GsonHelper.getAsJsonObject(json, "hud_progress") : new JsonObject();
        if (hudProgress.has("type")) {
            String progressType = GsonHelper.getAsString(hudProgress, "type");
            if (!progressType.equals("tracked_entity_health")) {
                throw new IllegalArgumentException("Unsupported hud_progress type: " + progressType);
            }
            if (!hudProgress.has("group")) {
                throw new IllegalArgumentException("tracked_entity_health requires group: " + missionId + "/" + stageId);
            }
        }
        List<MissionDefinition.Event> events = new ArrayList<>();
        Set<String> eventIds = new HashSet<>();
        if (json.has("events")) {
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "events")) {
                JsonObject eventJson = element.getAsJsonObject();
                String eventId = GsonHelper.getAsString(eventJson, "id");
                if (!eventIds.add(eventId)) {
                    throw new IllegalArgumentException("Duplicate event id in " + stageId + ": " + eventId);
                }
                int atTick = GsonHelper.getAsInt(eventJson, "at_tick");
                if (atTick < 0) throw new IllegalArgumentException("Negative event tick: " + eventId);
                ResourceLocation type = new ResourceLocation(GsonHelper.getAsString(eventJson, "type"));
                if (!type.getNamespace().equals(PomkotsMechs.MODID) || !SUPPORTED_EVENTS.contains(type.getPath())) {
                    throw new IllegalArgumentException("Unsupported mission event type: " + type);
                }
                events.add(new MissionDefinition.Event(eventId, atTick, type, eventJson.deepCopy()));
            }
        }
        List<MissionDefinition.TriggeredSequence> sequences = parseSequences(missionId, stageId, json);

        if (!json.has("success")) {
            throw new IllegalArgumentException("Stage has no success condition: " + missionId + "/" + stageId);
        }
        List<String> preservedTrackedGroups = new ArrayList<>();
        if (json.has("preserve_tracked_groups")) {
            Set<String> uniqueGroups = new HashSet<>();
            for (JsonElement element : GsonHelper.getAsJsonArray(json, "preserve_tracked_groups")) {
                String group = element.getAsString();
                if (group.isBlank()) {
                    throw new IllegalArgumentException(
                            "preserve_tracked_groups contains a blank group: " + missionId + "/" + stageId);
                }
                if (!uniqueGroups.add(group)) {
                    throw new IllegalArgumentException(
                            "Duplicate preserve_tracked_groups entry in " + stageId + ": " + group);
                }
                preservedTrackedGroups.add(group);
            }
        }
        int successGraceTicks = GsonHelper.getAsInt(json, "success_grace_ticks", -1);
        if (successGraceTicks < -1) {
            throw new IllegalArgumentException(
                    "success_grace_ticks must be zero or greater: " + missionId + "/" + stageId);
        }
        return new MissionDefinition.Stage(
                stageId,
                component(json.get("objective"), Component.empty()),
                hudProgress,
                json.has("presentation") ? GsonHelper.getAsJsonObject(json, "presentation") : new JsonObject(),
                GsonHelper.getAsBoolean(json, "cleanup_tracked_entities", false),
                preservedTrackedGroups,
                successGraceTicks,
                timeLimitTicks,
                timeLimitResult,
                events,
                sequences,
                parseCondition(GsonHelper.getAsJsonObject(json, "success")),
                json.has("failure") ? parseCondition(GsonHelper.getAsJsonObject(json, "failure")) : null
        );
    }

    private static List<MissionDefinition.TriggeredSequence> parseSequences(
            ResourceLocation missionId, String stageId, JsonObject stageJson
    ) {
        if (!stageJson.has("sequences")) return List.of();
        List<MissionDefinition.TriggeredSequence> sequences = new ArrayList<>();
        Set<String> sequenceIds = new HashSet<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(stageJson, "sequences")) {
            JsonObject sequenceJson = element.getAsJsonObject();
            String sequenceId = GsonHelper.getAsString(sequenceJson, "id");
            if (!sequenceIds.add(sequenceId)) {
                throw new IllegalArgumentException("Duplicate sequence id in " + stageId + ": " + sequenceId);
            }
            MissionDefinition.Condition trigger =
                    parseCondition(GsonHelper.getAsJsonObject(sequenceJson, "trigger"));
            JsonArray eventsJson = GsonHelper.getAsJsonArray(sequenceJson, "events");
            if (eventsJson.isEmpty()) {
                throw new IllegalArgumentException("Sequence has no events: " + missionId + "/" + sequenceId);
            }
            List<MissionDefinition.SequenceEvent> events = new ArrayList<>();
            Set<String> eventIds = new HashSet<>();
            for (JsonElement eventElement : eventsJson) {
                JsonObject eventJson = eventElement.getAsJsonObject();
                String eventId = GsonHelper.getAsString(eventJson, "id");
                if (!eventIds.add(eventId)) {
                    throw new IllegalArgumentException(
                            "Duplicate sequence event id in " + sequenceId + ": " + eventId);
                }
                int afterTicks = GsonHelper.getAsInt(eventJson, "after_ticks");
                if (afterTicks < 0) {
                    throw new IllegalArgumentException(
                            "Negative sequence event after_ticks: " + sequenceId + "/" + eventId);
                }
                ResourceLocation type = new ResourceLocation(GsonHelper.getAsString(eventJson, "type"));
                if (!type.getNamespace().equals(PomkotsMechs.MODID)
                        || !SUPPORTED_EVENTS.contains(type.getPath())) {
                    throw new IllegalArgumentException("Unsupported mission sequence event type: " + type);
                }
                events.add(new MissionDefinition.SequenceEvent(
                        eventId, afterTicks, type, eventJson.deepCopy()));
            }
            sequences.add(new MissionDefinition.TriggeredSequence(
                    sequenceId, trigger, events));
        }
        return sequences;
    }

    private static MissionDefinition.Condition parseCondition(JsonObject json) {
        ResourceLocation type = new ResourceLocation(GsonHelper.getAsString(json, "type"));
        if (!type.getNamespace().equals(PomkotsMechs.MODID) || !SUPPORTED_CONDITIONS.contains(type.getPath())) {
            throw new IllegalArgumentException("Unsupported mission condition: " + type);
        }
        List<MissionDefinition.Condition> children = new ArrayList<>();
        if (json.has("conditions")) {
            for (JsonElement child : GsonHelper.getAsJsonArray(json, "conditions")) {
                children.add(parseCondition(child.getAsJsonObject()));
            }
        } else if (json.has("condition")) {
            children.add(parseCondition(GsonHelper.getAsJsonObject(json, "condition")));
        }
        if ((type.getPath().equals("all_of") || type.getPath().equals("any_of")) && children.isEmpty()) {
            throw new IllegalArgumentException(type + " requires conditions");
        }
        if (type.getPath().equals("not") && children.size() != 1) {
            throw new IllegalArgumentException("not requires exactly one condition");
        }
        int ticks = GsonHelper.getAsInt(json, "ticks", 0);
        if (ticks < 0) throw new IllegalArgumentException("Negative condition ticks");
        if (type.getPath().equals("tracked_entity_health_below")
                || type.getPath().equals("player_vehicle_health_below")) {
            if (type.getPath().equals("tracked_entity_health_below") && !json.has("group")) {
                throw new IllegalArgumentException("tracked_entity_health_below requires group");
            }
            double ratio = GsonHelper.getAsDouble(json, "ratio");
            if (ratio < 0.0D || ratio > 1.0D) {
                throw new IllegalArgumentException("tracked_entity_health_below ratio must be between 0 and 1");
            }
            enumValue(json, "mode", "any", Set.of("any", "all"));
            if (type.getPath().equals("player_vehicle_health_below")) {
                enumValue(json, "target", "owner", Set.of("owner", "participants"));
            }
        }
        return new MissionDefinition.Condition(type, ticks, children, json);
    }

    private static List<String> parseCommands(JsonObject root, String key) {
        if (!root.has(key)) return List.of();
        JsonObject callback = GsonHelper.getAsJsonObject(root, key);
        if (!callback.has("commands")) return List.of();
        List<String> commands = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(callback, "commands")) {
            String command = element.getAsString().trim();
            if (!command.isEmpty()) commands.add(command);
        }
        return commands;
    }

    private static Component component(JsonElement element, Component fallback) {
        if (element == null) return fallback;
        Component parsed = Component.Serializer.fromJson(element);
        if (parsed == null) throw new IllegalArgumentException("Invalid component: " + element);
        return parsed;
    }

    private static String enumValue(JsonObject json, String key, String fallback, Set<String> allowed) {
        String value = GsonHelper.getAsString(json, key, fallback).toLowerCase(Locale.ROOT);
        if (!allowed.contains(value)) throw new IllegalArgumentException("Invalid " + key + ": " + value);
        return value;
    }
}
