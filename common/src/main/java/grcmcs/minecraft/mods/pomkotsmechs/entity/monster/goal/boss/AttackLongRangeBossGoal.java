package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.goal.boss;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.BossActionController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;

public class AttackLongRangeBossGoal extends AttackBossGoal {
    public AttackLongRangeBossGoal(BossActionController.BossAction action, GenericPomkotsMonster mob, float speedModifier) {
        super(action, mob, speedModifier);
    }

    public AttackLongRangeBossGoal(BossActionController.BossAction action, GenericPomkotsMonster mob, float speedModifier, boolean adjustRotation) {
        super(action, mob, speedModifier, adjustRotation);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && this.isInLongAttackRange();
    }
}
