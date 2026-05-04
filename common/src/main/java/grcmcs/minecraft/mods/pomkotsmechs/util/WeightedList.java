package grcmcs.minecraft.mods.pomkotsmechs.util;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeightedList<T> {
    private final List<Entry<T>> entries = new ArrayList<>();
    private int totalWeight = 0;

    public void addObject(int weight, T instance) {
        entries.add(new Entry<>(weight, instance));
        totalWeight += weight;
    }

    public T getRandomObject(RandomSource random) {
        int roll = random.nextInt(totalWeight);
        int current = 0;

        for (Entry<T> entry : entries) {
            current += entry.weight;
            if (roll < current) {
                return entry.instance;
            }
        }

        return entries.get(entries.size() - 1).instance;
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public List<Entry<T>> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public record Entry<T>(int weight, T instance) {}
}

