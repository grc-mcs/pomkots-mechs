package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GoalDice {
    protected BaseBossEntity mob;
    protected Map<Integer, Set<GoalDefinition>> modeGoalMap = new HashMap<>();
    protected Goal nextGoal = null;

    public GoalDice(BaseBossEntity mob) {
        this.mob = mob;

    }

    public void registerGoal(BaseBossGoal goal, int[] availableMode, float availableDistanceMin, float availableDistanceMax, int weight) {
        var def = new GoalDefinition(goal, availableDistanceMin, availableDistanceMax, weight);

        for (var mode: availableMode) {
            var set = modeGoalMap.computeIfAbsent(mode, k -> new HashSet<>());
            set.add(def);
        }
    }

    public void tick() {
        var target = mob.getTarget();

        if (target != null ) {
            var goalCandidates = modeGoalMap.get(mob.getAiMode());

            if (goalCandidates == null || goalCandidates.isEmpty()) {
                nextGoal = null;
            } else {
                nextGoal = getRandomWeighted(goalCandidates, target);
            }
        }
    }

    private Goal getRandomWeighted(Set<GoalDefinition> entries, Entity target) {
        var distance = mob.distanceToSqr(target);
        int totalWeight = 0;

        var cand2 = new HashSet<GoalDefinition>();
        for (var entry : entries) {
            if (entry.availableDistanceMin >= 0 && entry.availableDistanceMin > distance) {
                continue;
            } else if (entry.availableDistanceMax >=0 && entry.availableDistanceMax < distance) {
                continue;
            } else if (!entry.goal.canUseInternal()) {
                continue;
            }

            totalWeight += entry.weight;
            cand2.add(entry);
        }

        if (totalWeight == 0) {
            return null;
        }

        int r = mob.getRandom().nextInt(totalWeight); // 0 <= r < totalWeight
        int cumulative = 0;

        for (var entry : cand2) {
            cumulative += entry.weight;
            if (r < cumulative) {
                return entry.goal;
            }
        }

        throw new IllegalStateException("Should never reach here if weights are positive.");
    }

    public boolean canUse(Goal tgt) {
        return tgt == nextGoal && !mob.isStunning();
    }

    protected static class GoalDefinition {
        protected final BaseBossGoal goal;
        protected final float availableDistanceMin;
        protected final float availableDistanceMax;
        protected final int weight;

        public GoalDefinition(BaseBossGoal goal, float availableDistanceMin, float availableDistanceMax, int weight) {
            this.goal = goal;
            if (availableDistanceMin < 0) {
                this.availableDistanceMin = -1;
            } else {
                this.availableDistanceMin = availableDistanceMin * availableDistanceMin;
            }

            if (availableDistanceMax < 0) {
                this.availableDistanceMax = -1;
            } else {
                this.availableDistanceMax = availableDistanceMax * availableDistanceMax;
            }

            this.weight = weight;
        }
    }
}
