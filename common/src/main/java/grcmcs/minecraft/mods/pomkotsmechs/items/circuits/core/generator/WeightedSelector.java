package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.generator;

import net.minecraft.util.RandomSource;

import java.util.Collection;
import java.util.Objects;
import java.util.function.ToIntFunction;

public final class WeightedSelector {

    private WeightedSelector() {
    }

    public static <T> T select(
            Collection<T> values,
            ToIntFunction<T> weightGetter,
            RandomSource random
    ) {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(weightGetter, "weightGetter");
        Objects.requireNonNull(random, "random");

        int totalWeight = 0;

        for (T value : values) {
            int weight = Math.max(0, weightGetter.applyAsInt(value));
            totalWeight += weight;
        }

        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);

        for (T value : values) {
            int weight = Math.max(0, weightGetter.applyAsInt(value));

            if (weight <= 0) {
                continue;
            }

            if (roll < weight) {
                return value;
            }

            roll -= weight;
        }

        return null;
    }
}