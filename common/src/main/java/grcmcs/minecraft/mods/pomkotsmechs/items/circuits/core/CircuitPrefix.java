package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.generator.WeightedSelector;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry.CircuitRegistries;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry.SkillRegistryEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class CircuitPrefix implements SkillRegistryEntry {

    private final ResourceLocation id;
    private final Component name;

    @Nullable
    private final CircuitCategory mainCategory;

    private final EnumMap<CircuitCategory, Integer> categoryWeights;

    public CircuitPrefix(
            ResourceLocation id,
            Component name,
            @Nullable CircuitCategory mainCategory,
            Map<CircuitCategory, Integer> categoryWeights
    ) {
        this.id = id;
        this.name = name;
        this.mainCategory = mainCategory;
        this.categoryWeights = new EnumMap<>(CircuitCategory.class);
        this.categoryWeights.putAll(categoryWeights);
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    public Component name() {
        return name;
    }

    @Nullable
    public CircuitCategory mainCategory() {
        return mainCategory;
    }

    public boolean isBalanced() {
        return mainCategory == null;
    }

    public boolean isMainCategory(CircuitCategory category) {
        return mainCategory != null && mainCategory == category;
    }

    public CircuitCategory rollCategory(RandomSource random) {
        CircuitCategory selected =
                WeightedSelector.select(
                        categoryWeights.keySet(),
                        category -> categoryWeights.getOrDefault(category, 0),
                        random
                );

        if (selected != null) {
            return selected;
        }

        return mainCategory != null
                ? mainCategory
                : CircuitCategory.UTILITY;
    }

    public static final CircuitPrefix BALANCED =
            register(
                    "balanced",
                    "Balanced",
                    null,
                    weights(
                            20, // OFFENCE
                            20, // DEFENCE
                            20, // ENERGY
                            20, // MOBILITY
                            0  // UTILITY
                    )
            );

    public static final CircuitPrefix OFFENCE =
            register(
                    "offence",
                    "Offence",
                    CircuitCategory.OFFENCE,
                    weights(
                            90, // OFFENCE
                            0, // DEFENCE
                            0, // ENERGY
                            10, // MOBILITY
                            0  // UTILITY
                    )
            );

    public static final CircuitPrefix DEFENCE =
            register(
                    "defence",
                    "Defence",
                    CircuitCategory.DEFENCE,
                    weights(
                            0, // OFFENCE
                            90, // DEFENCE
                            10, // ENERGY
                            0, // MOBILITY
                            0  // UTILITY
                    )
            );

    public static final CircuitPrefix ENERGY =
            register(
                    "energy",
                    "Energy",
                    CircuitCategory.ENERGY,
                    weights(
                            10, // OFFENCE
                            0, // DEFENCE
                            100, // ENERGY
                            0, // MOBILITY
                            0  // UTILITY
                    )
            );

    public static final CircuitPrefix MOBILITY =
            register(
                    "mobility",
                    "Mobility",
                    CircuitCategory.MOBILITY,
                    weights(
                            0, // OFFENCE
                            10, // DEFENCE
                            0, // ENERGY
                            100, // MOBILITY
                            0  // UTILITY
                    )
            );

    public static final CircuitPrefix UTILITY =
            register(
                    "utility",
                    "Utility",
                    CircuitCategory.UTILITY,
                    weights(
                            10, // OFFENCE
                            10, // DEFENCE
                            10, // ENERGY
                            10, // MOBILITY
                            90  // UTILITY
                    )
            );

    public static void init() {
        /*
         * static fieldの初期化を確実に走らせるための空メソッド。
         * Mod初期化時に ModCircuitPrefixes.init(); を呼ぶ。
         */
    }

    private static CircuitPrefix register(
            String path,
            String displayName,
            CircuitCategory mainCategory,
            Map<CircuitCategory, Integer> categoryWeights
    ) {
        ResourceLocation id =
                new ResourceLocation(
                        PomkotsMechs.MODID,
                        path
                );

        return CircuitRegistries.PREFIXES.register(
                new CircuitPrefix(
                        id,
                        Component.literal(displayName),
                        mainCategory,
                        categoryWeights
                )
        );
    }

    private static EnumMap<CircuitCategory, Integer> weights(
            int offence,
            int defence,
            int energy,
            int mobility,
            int utility
    ) {
        EnumMap<CircuitCategory, Integer> weights =
                new EnumMap<>(CircuitCategory.class);

        weights.put(CircuitCategory.OFFENCE, offence);
        weights.put(CircuitCategory.DEFENCE, defence);
        weights.put(CircuitCategory.ENERGY, energy);
        weights.put(CircuitCategory.MOBILITY, mobility);
        weights.put(CircuitCategory.UTILITY, utility);

        return weights;
    }

    public static List<CircuitPrefix> creativeOrder() {
        return List.of(
                BALANCED,
                OFFENCE,
                DEFENCE,
                ENERGY,
                MOBILITY,
                UTILITY
        );
    }
}
