package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossActionController;

import java.util.EnumSet;

public class SimpleBossAttackGoal extends BaseBossGoal {
    protected final BossActionController.BossAction action;
    private boolean adjustRotation = false;
    protected int maxRepeatNum = 0;
    protected int currentRepeatNum = 0;
    protected boolean rotateOnSamePlace = false;

    public SimpleBossAttackGoal(BossActionController.BossAction action, BaseBossEntity mob, boolean adjustRotation) {
        this(action, mob, adjustRotation, 1);
    }

    public SimpleBossAttackGoal(BossActionController.BossAction action, BaseBossEntity mob, boolean adjustRotation, int repeatNum) {
        this(action, mob, adjustRotation, repeatNum, false);
    }

    public SimpleBossAttackGoal(BossActionController.BossAction action, BaseBossEntity mob, boolean adjustRotation, boolean rotateOnSamePlace) {
        this(action, mob, adjustRotation, 1, rotateOnSamePlace);
    }

    public SimpleBossAttackGoal(BossActionController.BossAction action, BaseBossEntity mob, boolean adjustRotation, int repeatNum, boolean rotateOnSamePlace) {
        super(mob);
        this.action = action;
        this.adjustRotation = adjustRotation;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.maxRepeatNum = repeatNum;
        this.rotateOnSamePlace = rotateOnSamePlace;
    }

    @Override
    public boolean canUseInternal() {
        return !action.isInCooltime();
    }

    @Override
    public boolean canContinueToUse() {
        return action.isInAction();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        if (action.tryAction()) {
            if (!adjustRotation) {
                this.mob.getNavigation().stop();
            }
            this.currentRepeatNum = this.maxRepeatNum;
            this.mob.rotateToTarget(this.mob.getTarget());
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void tick() {
        if (this.adjustRotation && mob.getTarget() != null) {
            mob.rotateToTarget(mob.getTarget());
            if (rotateOnSamePlace) {
                mob.choiMove();
            }
        }

        if (action.onEndOfAction() && --currentRepeatNum > 0) {
            action.setContinue();
        }
    }
}
