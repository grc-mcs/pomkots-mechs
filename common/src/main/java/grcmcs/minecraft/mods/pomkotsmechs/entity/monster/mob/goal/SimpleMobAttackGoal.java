package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumSet;

public class SimpleMobAttackGoal extends SmallMobGoalBase {
    public SimpleMobAttackGoal(BaseSmallMonsterEntity mob, float speedModifier) {
        super(mob, speedModifier);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse() {
        if (super.canUse()) {
            var target = mob.getTarget();
            return target != null && target.isAlive() && this.mob.getRandom().nextFloat() < 0.7F;
        } else {
            return false;
        }
    }

    public boolean canContinueToUse() {
        return this.mob.getRandom().nextFloat() < 0.7F;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            this.mob.tryAttack();
        }
    }
}
