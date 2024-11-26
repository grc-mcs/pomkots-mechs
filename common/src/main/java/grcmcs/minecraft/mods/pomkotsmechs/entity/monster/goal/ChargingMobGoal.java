package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.Pms01Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ChargingMobGoal extends Goal {
    protected final Pms01Entity mob;
    protected float speedModifier;
    protected int walkCount = 0;

    public ChargingMobGoal(Pms01Entity mob, float speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return true;
    }

    public boolean canContinueToUse() {
        return this.canUse();
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
        LivingEntity target = this.mob.getTarget();
        if (target != null) {
            if (walkCount > 0) {
                if (walkCount++ > 40) {
                    walkCount = 0;
                }
            } else {
                double distance = target.position().distanceTo(this.mob.position());

                if (mob.isAttacking()) {
                    // この処理はMOB側にあった方がいい気もしないでもない…
//                    updateHomingMovement();
                }
                // ターゲットが近接攻撃の距離にいるとき
                else if (isInShortAttackRange(distance)) {

                    long pattern = this.mob.tickCount % 3;

                    if (pattern == 0) {
                        this.mob.getNavigation().stop();
                        this.mob.tryAttack();

                    } else {
                        startRandomWalk(this.speedModifier);

                    }
                }
                // ターゲットが遠隔攻撃の距離にいるとき
                else if (isInLongAttackRange(distance)) {
                    long pattern = this.mob.tickCount % 3;

                    if (pattern == 0) {
                        this.mob.getNavigation().stop();
                        this.mob.tryAttack();

                    } else {
                        startWalk(target, this.speedModifier);

                    }
                }
                // それ以外はとにかくよってく
                else {
                    startWalk(target, this.speedModifier);
                }
            }
        }
    }

    protected void startRandomWalk(float speedModifier) {
        Vec3 rnd = DefaultRandomPos.getPos(this.mob, 10, 7);
        if (rnd != null) {
            this.mob.getNavigation().moveTo(rnd.x, rnd.y, rnd.z, this.speedModifier);
            this.walkCount = 1;

        }
    }
    protected void startWalk(LivingEntity target, float speedModifier) {
        this.mob.getNavigation().moveTo(target, this.speedModifier);
        this.walkCount = 1;
    }

    private static final int shortTo = 30;
    private static final int longTo = 100;

    protected boolean isInShortAttackRange(double distance) {
        return distance < shortTo;
    }

    protected boolean isInLongAttackRange(double distance) {
        return shortTo < distance && distance < longTo;
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
