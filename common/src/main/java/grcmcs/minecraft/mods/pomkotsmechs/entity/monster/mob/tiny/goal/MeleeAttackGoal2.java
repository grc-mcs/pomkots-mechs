package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;


public class MeleeAttackGoal2 extends MeleeAttackGoal {
    public MeleeAttackGoal2(PathfinderMob pathfinderMob, double d, boolean bl) {
        super(pathfinderMob, d, bl);
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity livingEntity, double d) {
        if (this.mob instanceof BaseSmallMonsterEntity small) {
            if (d <= this.getAttackReachSqr(livingEntity)) {
                small.tryAttack();
            }
        } else {
            super.checkAndPerformAttack(livingEntity, d);
        }
    }

    @Override
    protected double getAttackReachSqr(LivingEntity livingEntity) {
        return (double)(this.mob.getBbWidth() * 3.0F * this.mob.getBbWidth() * 3.0F + livingEntity.getBbWidth());
    }
}
