package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStatType;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public final class CircuitModifier {

    private final CircuitSkill skill;

    public CircuitModifier(CircuitSkill skill) {
        this.skill = Objects.requireNonNull(skill, "skill");
    }

    public CircuitSkill skill() {
        return skill;
    }

    public ResourceLocation skillId() {
        return skill.id();
    }

    public CircuitCategory category() {
        return skill.category();
    }

    public int tier() {
        return skill.tier();
    }

    public CircuitRarity minimumRarity() {
        return skill.minimumRarity();
    }

    public int weight() {
        return skill.weight();
    }

    public List<SkillEffect> effects() {
        return skill.effects();
    }

    public boolean hasEffect(MechStatType statType) {
        for (SkillEffect effect : skill.effects()) {
            if (effect.effect() == statType) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "CircuitModifier[" +
                "skill=" + skill.id() +
                ']';
    }
}
