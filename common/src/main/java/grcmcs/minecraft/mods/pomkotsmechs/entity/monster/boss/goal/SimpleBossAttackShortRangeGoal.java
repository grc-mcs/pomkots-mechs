package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossActionController;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;

public class SimpleBossAttackShortRangeGoal extends SimpleBossAttackGoal {

    protected int distance;

    public SimpleBossAttackShortRangeGoal(BossActionController.BossAction action, BaseBossEntity mob, boolean adjustRotation, int distance) {
        super(action, mob, adjustRotation, 1);
        this.distance = distance;
    }

    @Override
    public boolean canUse() {
        if (mob.getTarget() == null) {
            return super.canUse();
        } else {
            return super.canUse() && Utils.isWithinHorizontalDistance(mob, mob.getTarget(), distance);
        }
    }
}
