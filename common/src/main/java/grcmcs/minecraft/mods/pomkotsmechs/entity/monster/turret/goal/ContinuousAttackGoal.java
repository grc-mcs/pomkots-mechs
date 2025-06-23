package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.BaseTurretEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ContinuousAttackGoal extends Goal {
    protected final BaseTurretEntity mob;
    protected float speedModifier;
    protected int currentCoolTime = 0;
    protected int maxCoolTime = 20;
    protected int maxActionTime = 20;
    protected int currentActionTime = 0;
    protected int interval = 10;

    public ContinuousAttackGoal(BaseTurretEntity mob, float speedModifier, int maxActionTime, int maxCoolTime, int interval) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.currentCoolTime = 0;
        this.maxCoolTime = maxCoolTime;
        this.maxActionTime = maxActionTime;
        this.interval = interval;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (currentCoolTime > 0) {
            currentCoolTime--;
        }

        return currentCoolTime <= 0 && mob.getTarget() != null;
    }

    public boolean canContinueToUse() {
        return currentActionTime > 0;
    }

    @Override
    public void start() {
        this.currentCoolTime = this.maxCoolTime;
        this.currentActionTime = this.maxActionTime;
        mob.startActionAnimation();
    }

    @Override
    public void stop() {
        mob.stopActionAnimation();
    }

    @Override
    public void tick() {
        if (currentActionTime > 0) {
            currentActionTime--;
        }

        LivingEntity target = this.mob.getTarget();
        if (target != null && currentActionTime > 0 && currentActionTime % interval == 0) {
            mob.tryAttack();
        }
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
