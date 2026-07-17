package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechFeatureType;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatType;

import java.util.Objects;

public final class SkillEffect {

    private final SkillEffectKind kind;

    /**
     * kind == STAT のとき使用。
     */
    private final MechStatType stat;

    /**
     * kind == FEATURE のとき使用。
     */
    private final MechFeatureType feature;

    /**
     * kind == STAT のとき使用。
     */
    private final CircuitOperation operation;

    /**
     * STATなら数値。
     * FEATUREなら 1.0 = enabled, 0.0 = disabled として扱う。
     */
    private final double value;

    private SkillEffect(
            SkillEffectKind kind,
            MechStatType stat,
            MechFeatureType feature,
            CircuitOperation operation,
            double value
    ) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.stat = stat;
        this.feature = feature;
        this.operation = operation;
        this.value = value;
    }

    public static SkillEffect stat(
            MechStatType stat,
            CircuitOperation operation,
            double value
    ) {
        return new SkillEffect(
                SkillEffectKind.STAT,
                Objects.requireNonNull(stat, "stat"),
                null,
                Objects.requireNonNull(operation, "operation"),
                value
        );
    }

    public static SkillEffect feature(
            MechFeatureType feature,
            boolean enabled
    ) {
        return new SkillEffect(
                SkillEffectKind.FEATURE,
                null,
                Objects.requireNonNull(feature, "feature"),
                null,
                enabled ? 1.0 : 0.0
        );
    }

    public SkillEffectKind kind() {
        return kind;
    }

    /**
     * 既存コード互換用。
     * STAT Effect以外で呼ぶとnull。
     */
    public MechStatType effect() {
        return stat;
    }

    public MechStatType stat() {
        return stat;
    }

    public MechFeatureType feature() {
        return feature;
    }

    public CircuitOperation operation() {
        return operation;
    }

    public double value() {
        return value;
    }

    public boolean enabled() {
        return value > 0.0;
    }

    public boolean isStatEffect() {
        return kind == SkillEffectKind.STAT;
    }

    public boolean isFeatureEffect() {
        return kind == SkillEffectKind.FEATURE;
    }
}
