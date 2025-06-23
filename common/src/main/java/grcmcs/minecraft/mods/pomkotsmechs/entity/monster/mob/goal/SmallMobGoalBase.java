package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import net.minecraft.world.entity.ai.goal.Goal;

abstract public class SmallMobGoalBase extends Goal {
    protected BaseSmallMonsterEntity mob;
    protected float speedModifier;

    public SmallMobGoalBase(BaseSmallMonsterEntity mob, float speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
    }

    @Override
    public boolean canUse() {
        return !mob.isClosed();
    }
}
