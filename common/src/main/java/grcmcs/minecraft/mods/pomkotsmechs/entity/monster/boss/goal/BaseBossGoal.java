package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class BaseBossGoal extends Goal {
    protected final GenericPomkotsMonster mob;
    protected float speedModifier;

    public BaseBossGoal(GenericPomkotsMonster mob, float speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
//        if (isTargetInAttackRange() || ((IkabossEntity)this.mob).actionController.isInActionAll()) {
//            this.mob.getNavigation().stop();
//        } else {
//            this.mob.getNavigation().moveTo(this.path, this.speedModifier);
//        }
//
//        this.mob.setAggressive(true);
//        this.ticksUntilNextPathRecalculation = 0;
//        this.ticksUntilNextAttack = 0;

//        logger.info("ml:started");
    }

    @Override
    public void stop() {
//        LivingEntity livingentity = this.mob.getTarget();
//        if (!EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(livingentity)) {
//            this.mob.setTarget((LivingEntity)null);
//        }
//
//        this.mob.setAggressive(false);
//        this.mob.getNavigation().stop();

//        logger.info("ml:end");
    }

    @Override
    public void tick() {
    }

    private static final int shortTo = 40;
    private static final int longTo = 200;

    protected boolean isInShortAttackRange() {
        var target = mob.getTarget();

        if (target == null) {
            return false;
        } else {
            double distance = target.position().distanceTo(this.mob.position());
            return distance < shortTo;
        }
    }

    protected boolean isInLongAttackRange() {
        var target = mob.getTarget();

        if (target == null) {
            return false;
        } else {
            double distance = target.position().distanceTo(this.mob.position());
            return shortTo < distance && distance < longTo;
        }
    }
}
