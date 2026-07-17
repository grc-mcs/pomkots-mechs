package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry.SkillRegistryEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

import java.util.Objects;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public final class CircuitSkill implements SkillRegistryEntry {

    private final ResourceLocation id;

    private final Component name;

    private final String translationKey;

    private final String fallbackName;

    private final CircuitCategory category;

    private final int tier;

    private final CircuitRarity minimumRarity;

    private final int weight;

    private final List<SkillEffect> effects;

    public CircuitSkill(
            ResourceLocation id,
            Component name,
            String translationKey,
            String fallbackName,
            CircuitCategory category,
            int tier,
            CircuitRarity minimumRarity,
            int weight,
            List<SkillEffect> effects
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.translationKey = Objects.requireNonNull(translationKey, "translationKey");
        this.fallbackName = Objects.requireNonNull(fallbackName, "fallbackName");
        this.category = Objects.requireNonNull(category, "category");
        this.minimumRarity = Objects.requireNonNull(minimumRarity, "minimumRarity");
        this.effects = List.copyOf(Objects.requireNonNull(effects, "effects"));

        this.tier = Math.max(1, tier);
        this.weight = Math.max(0, weight);

        if (this.effects.isEmpty()) {
            throw new IllegalArgumentException("CircuitSkill must have at least one effect: " + id);
        }
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    public Component name() {
        return name;
    }

    public String translationKey() {
        return translationKey;
    }

    public String fallbackName() {
        return fallbackName;
    }

    public CircuitCategory category() {
        return category;
    }

    public int tier() {
        return tier;
    }

    public CircuitRarity minimumRarity() {
        return minimumRarity;
    }

    public int weight() {
        return weight;
    }

    public List<SkillEffect> effects() {
        return effects;
    }

    @Override
    public String toString() {
        return "CircuitSkill[" + id + "]";
    }
}
