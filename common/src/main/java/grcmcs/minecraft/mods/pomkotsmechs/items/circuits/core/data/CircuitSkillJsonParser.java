package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.data;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitCategory;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitOperation;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitSkill;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.SkillEffect;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechFeatureType;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public final class CircuitSkillJsonParser {

    private CircuitSkillJsonParser() {
    }

    public static CircuitSkill parse(JsonObject json) {
        ResourceLocation id =
                ResourceLocation.tryParse(
                        GsonHelper.getAsString(json, "id")
                );

        if (id == null) {
            throw new IllegalArgumentException("Invalid or missing circuit skill id.");
        }

        String defaultTranslationKey =
                "circuit_skill."
                        + id.getNamespace()
                        + "."
                        + id.getPath().replace('/', '.');

        String translationKey =
                GsonHelper.getAsString(
                        json,
                        "translation_key",
                        defaultTranslationKey
                );

        String fallbackName =
                GsonHelper.getAsString(
                        json,
                        "display_name",
                        id.getPath()
                );

        Component displayName =
                parseDisplayName(
                        json,
                        translationKey,
                        fallbackName
                );

        CircuitCategory category =
                CircuitCategory.byName(
                        GsonHelper.getAsString(json, "category", "utility"),
                        CircuitCategory.UTILITY
                );

        int tier =
                GsonHelper.getAsInt(json, "tier", 1);

        CircuitRarity minimumRarity =
                CircuitRarity.byName(
                        GsonHelper.getAsString(json, "minimum_rarity", "common"),
                        CircuitRarity.COMMON
                );

        int weight =
                GsonHelper.getAsInt(json, "weight", 10);

        List<SkillEffect> effects =
                parseEffects(id, json);

        return new CircuitSkill(
                id,
                displayName,
                translationKey,
                fallbackName,
                category,
                tier,
                minimumRarity,
                weight,
                effects
        );
    }

    private static Component parseDisplayName(
            JsonObject json,
            String translationKey,
            String fallbackName
    ) {
        if (json.has("name")) {
            Component component =
                    Component.Serializer.fromJson(json.get("name"));

            if (component != null) {
                return component;
            }
        }

        if (json.has("translation_key")) {
            return Component.translatable(translationKey);
        }

        if (json.has("display_name")) {
            return Component.literal(fallbackName);
        }

        return Component.translatable(translationKey);
    }
    private static List<SkillEffect> parseEffects(
            ResourceLocation skillId,
            JsonObject json
    ) {
        JsonArray array =
                GsonHelper.getAsJsonArray(json, "effects");

        List<SkillEffect> effects =
                new ArrayList<>();

        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException(
                        "Effect entry must be object in skill: " + skillId
                );
            }

            JsonObject effectJson =
                    element.getAsJsonObject();

            if (effectJson.has("stat")) {
                effects.add(
                        parseStatEffect(
                                skillId,
                                effectJson
                        )
                );
                continue;
            }

            if (effectJson.has("feature")) {
                effects.add(
                        parseFeatureEffect(
                                skillId,
                                effectJson
                        )
                );
                continue;
            }

            throw new IllegalArgumentException(
                    "Effect must have either 'stat' or 'feature' in skill: " + skillId
            );
        }

        if (effects.isEmpty()) {
            throw new IllegalArgumentException(
                    "Skill has no effects: " + skillId
            );
        }

        return effects;
    }

    private static SkillEffect parseStatEffect(
            ResourceLocation skillId,
            JsonObject effectJson
    ) {
        MechStatType stat =
                MechStatType.byName(
                        GsonHelper.getAsString(effectJson, "stat"),
                        null
                );

        if (stat == null) {
            throw new IllegalArgumentException(
                    "Unknown or missing stat in skill: " + skillId
            );
        }

        CircuitOperation operation =
                CircuitOperation.byName(
                        GsonHelper.getAsString(effectJson, "operation", "add"),
                        CircuitOperation.ADD
                );

        double value =
                GsonHelper.getAsDouble(effectJson, "value");

        return SkillEffect.stat(
                stat,
                operation,
                value
        );
    }

    private static SkillEffect parseFeatureEffect(
            ResourceLocation skillId,
            JsonObject effectJson
    ) {
        MechFeatureType feature =
                MechFeatureType.byName(
                        GsonHelper.getAsString(effectJson, "feature"),
                        null
                );

        if (feature == null) {
            throw new IllegalArgumentException(
                    "Unknown or missing feature in skill: " + skillId
            );
        }

        boolean enabled =
                GsonHelper.getAsBoolean(
                        effectJson,
                        "enabled",
                        true
                );

        return SkillEffect.feature(
                feature,
                enabled
        );
    }
}
