package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.goal.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.BossActionController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttackBossGoal extends BaseBossGoal {
    protected final BossActionController.BossAction action;
    private boolean adjustRotation = false;
    public static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    public AttackBossGoal(BossActionController.BossAction action, GenericPomkotsMonster mob, float speedModifier) {
        this(action, mob, speedModifier, false);
    }

    public AttackBossGoal(BossActionController.BossAction action, GenericPomkotsMonster mob, float speedModifier, boolean adjustRotation) {
        super(mob, speedModifier);
        this.action = action;
        this.adjustRotation = adjustRotation;
    }

    @Override
    public boolean canUse() {
        return action.canAction() && this.mob.getRandom().nextFloat() < 0.6F;
    }

    @Override
    public boolean canContinueToUse() {
        return action.isInAction();
    }

    @Override
    public void start() {
        if (action.tryAction()) {
            if (!adjustRotation) {
                this.mob.getNavigation().stop();
            }
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
        }
    }
}
