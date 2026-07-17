package grcmcs.minecraft.mods.pomkotsmechs.items.circuits;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitOperation;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.SkillEffect;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.data.CircuitSkillSnapshot;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechFeatureType;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.List;

public final class CircuitTooltipBuilder {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##");

    private CircuitTooltipBuilder() {
    }

    public static void appendSkillSnapshot(
            List<Component> tooltip,
            CircuitSkillSnapshot snapshot
    ) {
        tooltip.add(
                Component.literal(" - ")
                        .append(skillName(snapshot))
                        .withStyle(ChatFormatting.GRAY)
        );

        for (SkillEffect effect : snapshot.effects()) {
            tooltip.add(
                    Component.literal("    ")
                            .append(formatEffect(effect))
                            .withStyle(getChatFormatting(effect))
            );
        }
    }

    private static @NotNull ChatFormatting getChatFormatting(SkillEffect effect) {
        ChatFormatting color;

        if (effect.isFeatureEffect()) {
            color = ChatFormatting.AQUA;

        } else if (MechStatType.ENERGY_COST.equals(effect.stat())) {
            color = effect.value() <= 0
                    ? ChatFormatting.DARK_GREEN
                    : ChatFormatting.RED;
        } else {
            color = effect.value() >= 0
                    ? ChatFormatting.DARK_GREEN
                    : ChatFormatting.RED;
        }
        return color;
    }

    private static Component skillName(
            CircuitSkillSnapshot snapshot
    ) {
//        if (snapshot.nameKey() != null && !snapshot.nameKey().isBlank()) {
//            return Component.translatable(
//                    snapshot.nameKey()
//            );
//        }

        return Component.literal(
                snapshot.fallbackName()
        );
    }
    private static Component formatEffect(
            SkillEffect effect
    ) {
        if (effect.isFeatureEffect()) {
            return formatFeatureEffect(effect);
        }

        return formatStatEffect(effect);
    }

    private static Component formatStatEffect(
            SkillEffect effect
    ) {
        Component statName =
                statName(effect.stat());

        String valueText =
                formatValue(
                        effect.operation(),
                        effect.value()
                );

        return Component.literal(valueText + " ")
                .append(statName);
    }

    private static Component formatFeatureEffect(
            SkillEffect effect
    ) {
        Component featureName =
                featureName(effect.feature());

        if (effect.enabled()) {
            return Component.translatable(
                    "tooltip.pomkotsmechs.feature.enabled",
                    featureName
            );
        }

        return Component.translatable(
                "tooltip.pomkotsmechs.feature.disabled",
                featureName
        );
    }

    private static Component featureName(
            MechFeatureType featureType
    ) {
        return Component.translatable(
                "mech_feature.pomkotsmechs."
                        + featureType.name().toLowerCase()
        );
    }

    private static Component statName(
            MechStatType statType
    ) {
        return Component.translatable(
                "mech_stat.pomkotsmechs."
                        + statType.name().toLowerCase()
        );
    }

    private static String formatValue(
            CircuitOperation operation,
            double value
    ) {
        return switch (operation) {
            case ADD -> formatSignedNumber(value);
            case MULTIPLY -> formatSignedPercent(value);
            case OVERRIDE -> "= " + DECIMAL.format(value);
        };
    }

    private static String formatSignedNumber(double value) {
        if (value > 0) {
            return "+" + DECIMAL.format(value);
        }

        return DECIMAL.format(value);
    }

    private static String formatSignedPercent(double value) {
        double percent =
                value * 100.0;

        if (percent > 0) {
            return "+" + DECIMAL.format(percent) + "%";
        }

        return DECIMAL.format(percent) + "%";
    }
}
