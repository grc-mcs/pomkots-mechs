package grcmcs.minecraft.mods.pomkotsmechs.cutscene;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public final class CutsceneDefinitionRegistry {
    private static volatile Map<ResourceLocation, CutsceneDefinition> definitions = Map.of();
    private CutsceneDefinitionRegistry() { }

    public static Map<ResourceLocation, CutsceneDefinition> load(Path directory) throws IOException {
        Map<ResourceLocation, CutsceneDefinition> loaded = new LinkedHashMap<>();
        Files.createDirectories(directory);
        List<Path> files;
        try (var paths = Files.walk(directory)) {
            files = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
        }
        for (Path file : files) {
            ResourceLocation id = idFromFile(directory, file);
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (GsonHelper.getAsInt(json, "version", 1) != 1)
                    throw new IllegalArgumentException("Unsupported cutscene version");
                String type = GsonHelper.getAsString(json, "type");
                boolean startFade = GsonHelper.getAsBoolean(json, "start_fade", true);
                if (!type.equals("orbit") && !type.equals("linear_sessions")
                        && !type.equals("aerial_approach") && !type.equals("direct_approach")) {
                    throw new IllegalArgumentException("Unsupported cutscene type: " + type);
                }
                if (type.equals("orbit")) {
                    int duration = GsonHelper.getAsInt(json, "duration_ticks");
                    double radius = GsonHelper.getAsDouble(json, "radius");
                    double height = GsonHelper.getAsDouble(json, "height", 2.0D);
                    double speed = GsonHelper.getAsDouble(json, "angular_speed_degrees", 1.0D);
                    if (duration <= 0 || duration > 12000) throw new IllegalArgumentException("Invalid duration_ticks");
                    if (radius <= 0 || radius > 256) throw new IllegalArgumentException("Invalid radius");
                    if (speed <= 0 || speed > 45) throw new IllegalArgumentException("Invalid angular_speed_degrees");
                    loaded.put(id, new CutsceneDefinition(id, type, duration, radius, height, speed,
                            GsonHelper.getAsBoolean(json, "clockwise", false), List.of(), false, null,
                            null, startFade));
                } else if (type.equals("linear_sessions")) {
                    JsonArray sessionJson = GsonHelper.getAsJsonArray(json, "sessions");
                    if (sessionJson.isEmpty() || sessionJson.size() > 32) {
                        throw new IllegalArgumentException("sessions must contain 1-32 entries");
                    }
                    List<CutsceneDefinition.LinearSession> sessions = new ArrayList<>();
                    int totalDuration = 0;
                    for (var element : sessionJson) {
                        JsonObject session = element.getAsJsonObject();
                        int duration = GsonHelper.getAsInt(session, "duration_ticks");
                        if (duration <= 0 || duration > 12000) {
                            throw new IllegalArgumentException("Invalid session duration_ticks");
                        }
                        sessions.add(new CutsceneDefinition.LinearSession(
                                readVec3(GsonHelper.getAsJsonObject(session, "from")),
                                readVec3(GsonHelper.getAsJsonObject(session, "to")),
                                duration,
                                readLookMode(session)));
                        totalDuration += duration;
                    }
                    if (totalDuration > 12000) throw new IllegalArgumentException("Cutscene is too long");
                    loaded.put(id, new CutsceneDefinition(id, type, totalDuration, 0, 0, 0,
                            false, List.copyOf(sessions),
                            GsonHelper.getAsBoolean(json, "relative_to_target_yaw", false), null,
                            null, startFade));
                } else if (type.equals("aerial_approach")) {
                    double ascentHeight = GsonHelper.getAsDouble(json, "ascent_height");
                    int ascentTicks = GsonHelper.getAsInt(json, "ascent_duration_ticks");
                    int turnTicks = GsonHelper.getAsInt(json, "turn_duration_ticks");
                    int approachTicks = GsonHelper.getAsInt(json, "approach_duration_ticks");
                    double approachDistance = GsonHelper.getAsDouble(json, "approach_distance");
                    double targetHeight = GsonHelper.getAsDouble(json, "target_height", 12.0D);
                    if (ascentHeight <= 0.0D || ascentHeight > 256.0D) {
                        throw new IllegalArgumentException("Invalid ascent_height");
                    }
                    if (ascentTicks <= 0 || turnTicks <= 0 || approachTicks <= 0
                            || ascentTicks + turnTicks + approachTicks > 12000) {
                        throw new IllegalArgumentException("Invalid aerial_approach durations");
                    }
                    if (approachDistance < 0.0D || approachDistance > 512.0D) {
                        throw new IllegalArgumentException("Invalid approach_distance");
                    }
                    if (targetHeight < -128.0D || targetHeight > 256.0D) {
                        throw new IllegalArgumentException("Invalid target_height");
                    }
                    int duration = ascentTicks + turnTicks + approachTicks;
                    var aerial = new CutsceneDefinition.AerialApproach(
                            ascentHeight, ascentTicks, turnTicks, approachTicks,
                            approachDistance, targetHeight);
                    loaded.put(id, new CutsceneDefinition(id, type, duration, 0, 0, 0,
                            false, List.of(), false, aerial, null, startFade));
                } else {
                    int duration = GsonHelper.getAsInt(json, "duration_ticks");
                    int rotationDuration = GsonHelper.getAsInt(
                            json, "rotation_duration_ticks", Math.min(40, duration));
                    int movementDelay = GsonHelper.getAsInt(json, "movement_delay_ticks", 4);
                    double approachDistance = GsonHelper.getAsDouble(json, "approach_distance");
                    double targetHeight = GsonHelper.getAsDouble(json, "target_height", 2.0D);
                    if (duration <= 0 || duration > 12000) {
                        throw new IllegalArgumentException("Invalid duration_ticks");
                    }
                    if (rotationDuration <= 0 || rotationDuration > duration) {
                        throw new IllegalArgumentException("Invalid rotation_duration_ticks");
                    }
                    if (movementDelay < 0 || movementDelay >= duration) {
                        throw new IllegalArgumentException("Invalid movement_delay_ticks");
                    }
                    if (approachDistance < 0.0D || approachDistance > 512.0D) {
                        throw new IllegalArgumentException("Invalid approach_distance");
                    }
                    if (targetHeight < -128.0D || targetHeight > 256.0D) {
                        throw new IllegalArgumentException("Invalid target_height");
                    }
                    var direct = new CutsceneDefinition.DirectApproach(
                            duration, rotationDuration, movementDelay,
                            approachDistance, targetHeight);
                    loaded.put(id, new CutsceneDefinition(id, type, duration, 0, 0, 0,
                            false, List.of(), false, null, direct, startFade));
                }
            } catch (Exception e) {
                throw new IOException("Failed to load cutscene definition " + id + " from " + file, e);
            }
        }
        return Collections.unmodifiableMap(loaded);
    }

    public static void replace(Map<ResourceLocation, CutsceneDefinition> loaded) {
        definitions = Map.copyOf(loaded);
        PomkotsMechs.LOGGER.info("Loaded {} cutscene definitions", definitions.size());
    }

    public static Optional<CutsceneDefinition> get(ResourceLocation id) {
        return Optional.ofNullable(definitions.get(id));
    }

    public static Map<ResourceLocation, CutsceneDefinition> all() {
        return definitions;
    }

    private static ResourceLocation idFromFile(Path directory, Path file) {
        Path relative = directory.toAbsolutePath().normalize().relativize(file.toAbsolutePath().normalize());
        String path = relative.toString().replace('\\', '/');
        if (path.startsWith("../") || !path.endsWith(".json")) {
            throw new IllegalArgumentException("Invalid cutscene path: " + file);
        }
        return new ResourceLocation(PomkotsMechs.MODID, path.substring(0, path.length() - 5));
    }

    private static Vec3 readVec3(JsonObject json) {
        return new Vec3(
                GsonHelper.getAsDouble(json, "x"),
                GsonHelper.getAsDouble(json, "y"),
                GsonHelper.getAsDouble(json, "z"));
    }

    private static boolean readLookMode(JsonObject session) {
        String lookMode = GsonHelper.getAsString(session, "look_mode", "fixed");
        return switch (lookMode) {
            case "fixed" -> false;
            case "track" -> true;
            default -> throw new IllegalArgumentException("Unsupported look_mode: " + lookMode);
        };
    }
}
