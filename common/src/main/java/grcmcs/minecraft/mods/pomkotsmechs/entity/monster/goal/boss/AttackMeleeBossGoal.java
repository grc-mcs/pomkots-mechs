package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.goal.boss;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.BossActionController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;

public class AttackMeleeBossGoal extends AttackBossGoal {
    public AttackMeleeBossGoal(BossActionController.BossAction action, GenericPomkotsMonster mob, float speedModifier) {
        super(action, mob, speedModifier);
    }

    @Override
    public boolean canUse() {
        return action.canAction() && this.mob.getRandom().nextFloat() < 0.7F && this.isInShortAttackRange();
    }
}
