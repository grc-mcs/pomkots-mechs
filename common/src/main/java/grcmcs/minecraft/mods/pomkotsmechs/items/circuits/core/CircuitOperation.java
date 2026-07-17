package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core;

import java.util.Locale;

public enum CircuitOperation {

    ADD,
    MULTIPLY,
    OVERRIDE;

    public static CircuitOperation byName(
            String name,
            CircuitOperation fallback
    ) {
        if (name == null) {
            return fallback;
        }

        String normalized =
                name.toUpperCase(Locale.ROOT);

        for (CircuitOperation operation : values()) {
            if (operation.name().equals(normalized)) {
                return operation;
            }
        }

        return fallback;
    }
}
