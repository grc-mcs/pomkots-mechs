package grcmcs.minecraft.mods.pomkotsmechs.radio;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class RadioMessageRegistry {
    private static volatile Map<ResourceLocation, RadioMessage> messages = Map.of();

    private RadioMessageRegistry() {
    }

    public static Map<ResourceLocation, RadioMessage> load(Path directory) throws IOException {
        Map<ResourceLocation, RadioMessage> loaded = new LinkedHashMap<>();
        Files.createDirectories(directory);
        List<Path> files;
        try (var paths = Files.walk(directory)) {
            files = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .toList();
        }
        for (Path file : files) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                    JsonArray definitions = GsonHelper.getAsJsonArray(root, "messages");
                    for (JsonElement element : definitions) {
                        RadioMessage message = parseMessage(element.getAsJsonObject());
                        if (loaded.put(message.id(), message) != null) {
                            throw new IllegalArgumentException("Duplicate radio message id: " + message.id());
                        }
                    }
            } catch (Exception e) {
                throw new IOException("Failed to load radio messages from " + file, e);
            }
        }
        return Collections.unmodifiableMap(loaded);
    }

    public static void replace(Map<ResourceLocation, RadioMessage> loaded) {
        messages = Map.copyOf(loaded);
        PomkotsMechs.LOGGER.info("Loaded {} radio messages", messages.size());
    }

    public static Optional<RadioMessage> get(ResourceLocation id) {
        return Optional.ofNullable(messages.get(id));
    }

    public static Map<ResourceLocation, RadioMessage> all() {
        return messages;
    }

    private static RadioMessage parseMessage(JsonObject json) {
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "id"));
        Component speaker = parseComponent(json.get("speaker"), "speaker", id);
        RadioPortrait portrait = parsePortrait(GsonHelper.getAsJsonObject(json, "portrait"));
        JsonArray textArray = GsonHelper.getAsJsonArray(json, "texts");

        if (textArray.isEmpty() || textArray.size() > RadioMessage.MAX_TEXTS) {
            throw new IllegalArgumentException("Radio message " + id + " has invalid text count: " + textArray.size());
        }

        List<RadioText> texts = new java.util.ArrayList<>(textArray.size());
        for (JsonElement element : textArray) {
            JsonObject textJson = element.getAsJsonObject();
            Component text = parseComponent(textJson.get("text"), "text", id);
            int duration = GsonHelper.getAsInt(textJson, "duration_ticks", RadioText.DEFAULT_DURATION);
            duration = Mth.clamp(duration, RadioText.MIN_DURATION, RadioText.MAX_DURATION);
            texts.add(new RadioText(text, duration));
        }

        return new RadioMessage(id, speaker, portrait, texts);
    }

    private static Component parseComponent(JsonElement element, String field, ResourceLocation id) {
        if (element == null) {
            throw new IllegalArgumentException("Radio message " + id + " is missing " + field);
        }
        Component component = Component.Serializer.fromJson(element);
        if (component == null) {
            throw new IllegalArgumentException("Radio message " + id + " has invalid " + field);
        }
        return component;
    }

    private static RadioPortrait parsePortrait(JsonObject json) {
        RadioPortraitType type = RadioPortraitType.valueOf(
                GsonHelper.getAsString(json, "type", "texture").toUpperCase(Locale.ROOT));
        String textureValue = GsonHelper.getAsString(
                json, "texture", "pomkotsmechs:textures/entity/pms01.png");
        String entityValue = GsonHelper.getAsString(json, "entity", "minecraft:pig");
        if (type == RadioPortraitType.ENTITY_MODEL && !json.has("entity")) {
            throw new IllegalArgumentException("entity_model portrait requires entity");
        }
        String playerModel = GsonHelper.getAsString(json, "model", "wide").toLowerCase(Locale.ROOT);
        if (type == RadioPortraitType.PLAYER_MODEL
                && !playerModel.equals("wide")
                && !playerModel.equals("slim")) {
            throw new IllegalArgumentException("player_model portrait model must be wide or slim");
        }
        return new RadioPortrait(
                type,
                new ResourceLocation(textureValue),
                new ResourceLocation(entityValue),
                GsonHelper.getAsFloat(json, "yaw", 0.0F),
                GsonHelper.getAsFloat(json, "pitch", 0.0F),
                GsonHelper.getAsFloat(json, "scale", 1.0F),
                GsonHelper.getAsFloat(json, "offset_x", 0.0F),
                GsonHelper.getAsFloat(json, "offset_y", 0.0F),
                GsonHelper.getAsBoolean(json, "bust_up", false),
                playerModel.equals("slim")
        );
    }
}
