package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.goal.boss;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.BossActionController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.Pmb01Entity;

public class AttackBreakBossGoal extends AttackBossGoal {
    public AttackBreakBossGoal(BossActionController.BossAction action, GenericPomkotsMonster mob, float speedModifier) {
        super(action, mob, speedModifier);
    }

    @Override
    public boolean canUse() {
        boolean flag = ((Pmb01Entity)this.mob).consumeBreakFlag();
        return action.canAction() && flag;
    }
}
