package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.data;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitOperation;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitSkill;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.SkillEffect;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechFeatureType;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class CircuitSkillSnapshot {

    private static final String TAG_ID = "Id";
    private static final String TAG_NAME_KEY = "NameKey";
    private static final String TAG_FALLBACK_NAME = "FallbackName";
    private static final String TAG_EFFECTS = "Effects";

    private final ResourceLocation id;

    private final String nameKey;

    private final String fallbackName;

    private final List<SkillEffect> effects;

    public CircuitSkillSnapshot(
            ResourceLocation id,
            String nameKey,
            String fallbackName,
            List<SkillEffect> effects
    ) {
        this.id = id;
        this.nameKey = nameKey;
        this.fallbackName = fallbackName;
        this.effects = List.copyOf(effects);
    }

    public static CircuitSkillSnapshot fromSkill(CircuitSkill skill) {
        return new CircuitSkillSnapshot(
                skill.id(),
                skill.translationKey(),
                skill.fallbackName(),
                skill.effects()
        );
    }

    public ResourceLocation id() {
        return id;
    }

    public String nameKey() {
        return nameKey;
    }

    public String fallbackName() {
        return fallbackName;
    }

    public List<SkillEffect> effects() {
        return effects;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        tag.putString(TAG_ID, id.toString());
        tag.putString(TAG_NAME_KEY, nameKey);
        tag.putString(TAG_FALLBACK_NAME, fallbackName);

        ListTag effectsTag = new ListTag();

        for (SkillEffect effect : effects) {
            effectsTag.add(saveEffect(effect));
        }

        tag.put(TAG_EFFECTS, effectsTag);

        return tag;
    }

    public static CircuitSkillSnapshot load(CompoundTag tag) {
        ResourceLocation id =
                ResourceLocation.tryParse(
                        tag.getString(TAG_ID)
                );

        if (id == null) {
            return null;
        }

        String nameKey =
                tag.getString(TAG_NAME_KEY);

        String fallbackName =
                tag.getString(TAG_FALLBACK_NAME);

        List<SkillEffect> effects =
                new ArrayList<>();

        ListTag effectsTag =
                tag.getList(TAG_EFFECTS, Tag.TAG_COMPOUND);

        for (int i = 0; i < effectsTag.size(); i++) {
            SkillEffect effect =
                    loadEffect(effectsTag.getCompound(i));

            if (effect != null) {
                effects.add(effect);
            }
        }

        return new CircuitSkillSnapshot(
                id,
                nameKey,
                fallbackName,
                effects
        );
    }

    private static CompoundTag saveEffect(SkillEffect effect) {
        CompoundTag tag = new CompoundTag();

        tag.putString("Kind", effect.kind().name().toLowerCase());

        if (effect.isStatEffect()) {
            tag.putString("Stat", effect.stat().name().toLowerCase());
            tag.putString("Operation", effect.operation().name().toLowerCase());
            tag.putDouble("Value", effect.value());
            return tag;
        }

        if (effect.isFeatureEffect()) {
            tag.putString("Feature", effect.feature().name().toLowerCase());
            tag.putBoolean("Enabled", effect.enabled());
            return tag;
        }

        return tag;
    }

    private static SkillEffect loadEffect(CompoundTag tag) {
        if (tag.contains("Feature")) {
            MechFeatureType feature =
                    MechFeatureType.byName(
                            tag.getString("Feature"),
                            null
                    );

            if (feature == null) {
                return null;
            }

            boolean enabled =
                    !tag.contains("Enabled")
                            || tag.getBoolean("Enabled");

            return SkillEffect.feature(
                    feature,
                    enabled
            );
        }

        if (tag.contains("Stat")) {
            MechStatType stat =
                    MechStatType.byName(
                            tag.getString("Stat"),
                            null
                    );

            if (stat == null) {
                return null;
            }

            CircuitOperation operation =
                    CircuitOperation.byName(
                            tag.getString("Operation"),
                            CircuitOperation.ADD
                    );

            double value =
                    tag.getDouble("Value");

            return SkillEffect.stat(
                    stat,
                    operation,
                    value
            );
        }

        return null;
    }
}
