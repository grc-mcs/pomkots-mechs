package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.goal.boss;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public class WalkBossGoal extends BaseBossGoal {
    protected static final int MAX_WALK_COUNT = 40;
    protected int walkCount = 0;

    public WalkBossGoal(GenericPomkotsMonster mob, float speedModifier) {
        super(mob, speedModifier);
    }

    @Override
    public boolean canContinueToUse() {
        return walkCount < MAX_WALK_COUNT;
    }

    @Override
    public void start() {
        walkCount = 0;

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
                walkCount++;
            } else {
                // ターゲットが近接攻撃の距離にいるとき
                if (isInShortAttackRange()) {
                    startRandomWalk(this.speedModifier);
                }
                // ターゲットが遠隔攻撃の距離にいるとき
                else if (isInLongAttackRange()) {
                    startWalk(target, this.speedModifier);
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
}
