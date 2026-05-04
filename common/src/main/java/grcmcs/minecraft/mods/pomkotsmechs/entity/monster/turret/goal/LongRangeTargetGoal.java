package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal.NearestEntityTargetGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

public class LongRangeTargetGoal extends NearestEntityTargetGoal<Player> {

    public LongRangeTargetGoal(Mob mob) {
        super(mob, Player.class, false, false);
    }

    @Override
//    public boolean canContinueToUse() {
//        LivingEntity target = this.mob.getTarget();
//        if (target instanceof Player player && player.isAlive() && !player.isSpectator()) {
//            // FollowRangeに関わらずターゲットを維持
//            return true;
//        }
//        return super.canContinueToUse();
//    }

    public boolean canContinueToUse() {
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity == null) {
            livingEntity = this.targetMob;
        }

        if (livingEntity == null) {
            return false;
        } else if (!this.mob.canAttack(livingEntity)) {
            return false;
        } else {
            Team team = this.mob.getTeam();
            Team team2 = livingEntity.getTeam();
            if (team != null && team2 == team) {
                return false;
            } else {
                double d = this.getFollowDistance();
                System.out.println(d);
                if (this.mob.distanceToSqr(livingEntity) > d * d) {
                    return false;
                } else {
                    this.mob.setTarget(livingEntity);
                    return true;
                }
            }
        }
    }
}
