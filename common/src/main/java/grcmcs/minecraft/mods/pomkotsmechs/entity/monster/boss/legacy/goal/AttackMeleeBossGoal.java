package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.BossActionController;

public class AttackMeleeBossGoal extends AttackBossGoal {
    public AttackMeleeBossGoal(BossActionController.BossAction action, GenericPomkotsMonster mob, float speedModifier) {
        super(action, mob, speedModifier);
    }

    @Override
    public boolean canUse() {
        return action.canAction() && this.mob.getRandom().nextFloat() < 0.7F && this.isInShortAttackRange();
    }
}
