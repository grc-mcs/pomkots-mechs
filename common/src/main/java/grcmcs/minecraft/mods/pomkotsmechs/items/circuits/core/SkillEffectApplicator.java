package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechFeatures;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatType;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStats;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatus;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class SkillEffectApplicator {

    private SkillEffectApplicator() {
    }

    public static Accumulator createAccumulator() {
        return new Accumulator();
    }

    public static final class Accumulator {

        private final Map<MechStatType, AccumulatedStatEffect> statEffects =
                new EnumMap<>(MechStatType.class);

        private final List<SkillEffect> featureEffects =
                new ArrayList<>();

        public void add(CircuitInstance circuit) {
            for (CircuitModifier modifier : circuit.modifiers()) {
                add(modifier);
            }
        }

        public void add(CircuitModifier modifier) {
            add(modifier.skill());
        }

        public void add(CircuitSkill skill) {
            for (SkillEffect effect : skill.effects()) {
                add(effect);
            }
        }

        public void add(SkillEffect effect) {
            if (effect.isStatEffect()) {
                statEffects
                        .computeIfAbsent(
                                effect.stat(),
                                ignored -> new AccumulatedStatEffect()
                        )
                        .add(effect);
                return;
            }

            if (effect.isFeatureEffect()) {
                featureEffects.add(effect);
            }
        }

        public void applyTo(MechStatus status) {
            applyStats(status.stats());
            applyFeatures(status.features());
        }

        private void applyStats(MechStats stats) {
            for (Map.Entry<MechStatType, AccumulatedStatEffect> entry : statEffects.entrySet()) {
                MechStatType statType =
                        entry.getKey();

                double base =
                        stats.get(statType);

                double result =
                        entry.getValue().apply(base);

                stats.set(
                        statType,
                        result
                );
            }
        }

        private void applyFeatures(MechFeatures features) {
            for (SkillEffect effect : featureEffects) {
                applyFeature(
                        features,
                        effect
                );
            }
        }
    }

    private static final class AccumulatedStatEffect {

        private double addValue = 0.0;
        private double multiplyValue = 0.0;
        private Double overrideValue = null;

        public void add(SkillEffect effect) {
            switch (effect.operation()) {
                case ADD -> {
                    addValue += effect.value();
                }

                case MULTIPLY -> {
                    multiplyValue += effect.value();
                }

                case OVERRIDE -> {
                    overrideValue = effect.value();
                }
            }
        }

        public double apply(double base) {
            if (overrideValue != null) {
                return overrideValue;
            }

            double result =
                    base + addValue;

            result *= 1.0 + multiplyValue;

            return result;
        }
    }

    public static void applyFeature(
            MechFeatures features,
            SkillEffect effect
    ) {
        features.setEnabled(
                effect.feature(),
                effect.enabled()
        );
    }
}
