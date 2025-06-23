package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.BossActionController;

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
