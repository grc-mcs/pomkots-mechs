package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public abstract class AbstractSkillRegistry<T extends SkillRegistryEntry> {

    private final Map<ResourceLocation, T> values = new LinkedHashMap<>();

    private boolean frozen = false;

    public T register(T value) {
        if (frozen) {
            throw new IllegalStateException("Registry is frozen.");
        }

        ResourceLocation id = value.id();

        if (values.containsKey(id)) {
            throw new IllegalStateException("Duplicate registry entry: " + id);
        }

        values.put(id, value);
        onRegister(value);

        return value;
    }

    protected void onRegister(T value) {
    }

    @Nullable
    public T get(ResourceLocation id) {
        return values.get(id);
    }

    public Collection<T> values() {
        return Collections.unmodifiableCollection(values.values());
    }

    public boolean contains(ResourceLocation id) {
        return values.containsKey(id);
    }

    public List<T> find(Predicate<T> predicate) {
        return values.values()
                .stream()
                .filter(predicate)
                .toList();
    }

    public void freeze() {
        this.frozen = true;
    }

    public void unfreezeForReload() {
        this.frozen = false;
    }

    public void clearForReload() {
        if (frozen) {
            throw new IllegalStateException("Cannot clear frozen registry.");
        }

        values.clear();
    }

    public boolean isFrozen() {
        return frozen;
    }
}