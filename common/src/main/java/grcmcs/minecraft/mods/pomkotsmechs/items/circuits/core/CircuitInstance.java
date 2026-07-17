package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core;

import java.util.List;

public record CircuitInstance(
        CircuitPrefix prefix,
        CircuitRarity rarity,
        List<CircuitModifier> modifiers
) {
}