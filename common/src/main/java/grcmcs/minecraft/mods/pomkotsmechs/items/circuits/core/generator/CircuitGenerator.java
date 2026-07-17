package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.generator;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.*;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry.CircuitRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Prefix + Rarity から、具体的なSkill構成を生成するクラス。
 *
 * 仕様:
 * - 1個目はPrefixのメインカテゴリから抽選する
 * - フォールバックもPrefixのメインカテゴリから抽選する
 * - 2個目以降でPrefixと違うカテゴリを引いた場合、1段低いRarityとして抽選する
 * - Balanced Prefixの場合はカテゴリ違いペナルティなし
 */
public final class CircuitGenerator {

    private CircuitGenerator() {
    }

    public static CircuitInstance generate(
            RandomSource random,
            CircuitPrefix prefix,
            CircuitRarity rarity
    ) {
        int targetModifierCount =
                rarity.rollModifierCount(random);

        List<CircuitModifier> modifiers =
                new ArrayList<>();

        Set<ResourceLocation> selectedSkillIds =
                new HashSet<>();

        int attempts = 0;
        int maxAttempts =
                Math.max(16, targetModifierCount * 10);

        while (modifiers.size() < targetModifierCount && attempts++ < maxAttempts) {

            int modifierIndex =
                    modifiers.size();

            CircuitCategory category =
                    rollCategoryForModifier(
                            random,
                            prefix,
                            modifierIndex
                    );

            CircuitRarity effectiveRarity =
                    resolveEffectiveRarity(
                            prefix,
                            rarity,
                            category,
                            modifierIndex
                    );

            List<CircuitSkill> candidates =
                    findCandidates(
                            category,
                            effectiveRarity,
                            selectedSkillIds
                    );

            /*
             * フォールバック:
             * 可能ならPrefix本来のカテゴリから抽選する。
             * Balancedの場合はカテゴリ固定せず、指定Rarityの全候補から抽選する。
             */
            if (candidates.isEmpty()) {
                candidates =
                        findFallbackCandidates(
                                prefix,
                                rarity,
                                selectedSkillIds
                        );
            }

            if (candidates.isEmpty()) {
                break;
            }

            CircuitSkill selected =
                    WeightedSelector.select(
                            candidates,
                            CircuitSkill::weight,
                            random
                    );

            if (selected == null) {
                break;
            }

            selectedSkillIds.add(
                    selected.id()
            );

            modifiers.add(
                    new CircuitModifier(selected)
            );
        }

        return new CircuitInstance(
                prefix,
                rarity,
                List.copyOf(modifiers)
        );
    }

    /**
     * 1個目はPrefixのメインカテゴリ固定。
     * Balancedの場合、または2個目以降は通常通りPrefixの重みでカテゴリ抽選。
     */
    private static CircuitCategory rollCategoryForModifier(
            RandomSource random,
            CircuitPrefix prefix,
            int modifierIndex
    ) {
        if (modifierIndex == 0 && !prefix.isBalanced()) {
            return prefix.mainCategory();
        }

        return prefix.rollCategory(random);
    }

    /**
     * 2個目以降でPrefix本来のカテゴリと違うカテゴリを引いた場合、
     * Rarityを1段下げる。
     *
     * Balancedはペナルティなし。
     */
    private static CircuitRarity resolveEffectiveRarity(
            CircuitPrefix prefix,
            CircuitRarity baseRarity,
            CircuitCategory rolledCategory,
            int modifierIndex
    ) {
        if (prefix.isBalanced()) {
            return baseRarity.lower();
        }

        if (modifierIndex == 0) {
            return baseRarity;
        }

        if (prefix.isMainCategory(rolledCategory)) {
            return baseRarity;
        }

        return baseRarity.lower();
    }

    private static List<CircuitSkill> findCandidates(
            CircuitCategory category,
            CircuitRarity rarity,
            Set<ResourceLocation> selectedSkillIds
    ) {
        return CircuitRegistries.SKILLS.find(skill ->
                skill.category() == category
                        && isAvailableForRarity(skill, rarity)
                        && !selectedSkillIds.contains(skill.id())
        );
    }

    private static List<CircuitSkill> findFallbackCandidates(
            CircuitPrefix prefix,
            CircuitRarity rarity,
            Set<ResourceLocation> selectedSkillIds
    ) {
        if (prefix.isBalanced()) {
            return findFallbackAnyCategory(
                    rarity,
                    selectedSkillIds
            );
        }

        return findCandidates(
                prefix.mainCategory(),
                rarity,
                selectedSkillIds
        );
    }

    private static List<CircuitSkill> findFallbackAnyCategory(
            CircuitRarity rarity,
            Set<ResourceLocation> selectedSkillIds
    ) {
        return CircuitRegistries.SKILLS.find(skill ->
                isAvailableForRarity(skill, rarity)
                        && !selectedSkillIds.contains(skill.id())
        );
    }

    private static boolean isAvailableForRarity(
            CircuitSkill skill,
            CircuitRarity rarity
    ) {
        return skill.minimumRarity().ordinal() <= rarity.ordinal()
                && skill.tier() <= rarity.maxSkillTier();
    }
}