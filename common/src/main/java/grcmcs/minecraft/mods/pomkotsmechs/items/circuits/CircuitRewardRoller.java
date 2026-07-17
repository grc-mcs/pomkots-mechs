package grcmcs.minecraft.mods.pomkotsmechs.items.circuits;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitPrefix;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public final class CircuitRewardRoller {

    private CircuitRewardRoller() {
    }

    // 回路が報酬として出る確率
    private static final float CIRCUIT_DROP_CHANCE = 0.25f;

    // Prefix抽選重み
    public static final WeightedEntry<CircuitPrefix>[] PREFIX_WEIGHTS = new WeightedEntry[]{
            new WeightedEntry<>(CircuitPrefix.OFFENCE, 20),
            new WeightedEntry<>(CircuitPrefix.DEFENCE, 20),
            new WeightedEntry<>(CircuitPrefix.ENERGY, 20),
            new WeightedEntry<>(CircuitPrefix.MOBILITY, 20),
            new WeightedEntry<>(CircuitPrefix.BALANCED, 10),
    };

    public static final WeightedEntry<CircuitPrefix>[] PREFIX_WEIGHTS_ARENA = new WeightedEntry[]{
            new WeightedEntry<>(CircuitPrefix.OFFENCE, 50),
            new WeightedEntry<>(CircuitPrefix.MOBILITY, 50)
    };

    // Rarity抽選重み
    public static final WeightedEntry<CircuitRarity>[] RARITY_WEIGHTS = new WeightedEntry[]{
            new WeightedEntry<>(CircuitRarity.COMMON, 60),
            new WeightedEntry<>(CircuitRarity.RARE, 30),
            new WeightedEntry<>(CircuitRarity.EPIC, 10),
    };

    public static final WeightedEntry<CircuitRarity>[] RARITY_WEIGHTS_ARENA_SS_S = new WeightedEntry[]{
            new WeightedEntry<>(CircuitRarity.COMMON, 0),
            new WeightedEntry<>(CircuitRarity.RARE, 40),
            new WeightedEntry<>(CircuitRarity.EPIC, 60),
    };

    public static final WeightedEntry<CircuitRarity>[] RARITY_WEIGHTS_ARENA_A_B = new WeightedEntry[]{
            new WeightedEntry<>(CircuitRarity.COMMON, 50),
            new WeightedEntry<>(CircuitRarity.RARE, 40),
            new WeightedEntry<>(CircuitRarity.EPIC, 10),
    };

    public static final WeightedEntry<CircuitRarity>[] RARITY_WEIGHTS_ARENA_C_D = new WeightedEntry[]{
            new WeightedEntry<>(CircuitRarity.COMMON, 90),
            new WeightedEntry<>(CircuitRarity.RARE, 9),
            new WeightedEntry<>(CircuitRarity.EPIC, 1),
    };

    public static ItemStack roll(
            RandomSource random) {
        return roll(PREFIX_WEIGHTS, RARITY_WEIGHTS,CIRCUIT_DROP_CHANCE, random);
    }

    public static ItemStack roll(
            WeightedEntry<CircuitPrefix>[] weightPrefix,
            WeightedEntry<CircuitRarity>[] rarityPrefix,
            float dropChance,
            RandomSource random) {

        if (random.nextFloat() >= dropChance) {
            return ItemStack.EMPTY;
        }

        CircuitPrefix prefix =
                rollWeighted(random, weightPrefix);

        CircuitRarity rarity =
                rollWeighted(random, rarityPrefix);

        ItemStack stack = new ItemStack(PomkotsMechs.CIRCUIT_BASE.get());

        CircuitItemStackHelper.setUnidentified(stack, prefix, rarity);

        return stack;
    }

    private static <T> T rollWeighted(
            RandomSource random,
            WeightedEntry<T>[] entries
    ) {
        int totalWeight = 0;

        for (WeightedEntry<T> entry : entries) {
            totalWeight += entry.weight();
        }

        int roll =
                random.nextInt(totalWeight);

        int current = 0;

        for (WeightedEntry<T> entry : entries) {
            current += entry.weight();

            if (roll < current) {
                return entry.value();
            }
        }

        return entries[entries.length - 1].value();
    }

    public record WeightedEntry<T>(
            T value,
            int weight
    ) {
    }
}
