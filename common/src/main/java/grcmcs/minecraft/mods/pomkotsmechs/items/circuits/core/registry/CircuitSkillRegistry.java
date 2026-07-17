package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitCategory;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitSkill;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatType;

import java.util.List;

public final class CircuitSkillRegistry
        extends AbstractSkillRegistry<CircuitSkill> {

    public List<CircuitSkill> findByCategory(CircuitCategory category) {
        return find(skill -> skill.category() == category);
    }

    public List<CircuitSkill> findByTier(int tier) {
        return find(skill -> skill.tier() == tier);
    }

    public List<CircuitSkill> findByMinimumRarity(CircuitRarity rarity) {
        return find(skill -> skill.minimumRarity() == rarity);
    }

    public List<CircuitSkill> findByEffect(MechStatType statType) {
        return find(skill ->
                skill.effects()
                        .stream()
                        .anyMatch(effect -> effect.effect() == statType)
        );
    }
}