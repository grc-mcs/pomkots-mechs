package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core;

import java.util.Locale;

public enum CircuitCategory {

    OFFENCE,
    DEFENCE,
    ENERGY,
    MOBILITY,
    UTILITY;

    public static CircuitCategory byName(
            String name,
            CircuitCategory fallback
    ) {
        if (name == null) {
            return fallback;
        }

        String normalized =
                name.toUpperCase(Locale.ROOT);

        for (CircuitCategory category : values()) {
            if (category.name().equals(normalized)) {
                return category;
            }
        }

        return fallback;
    }
}