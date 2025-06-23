package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossActionController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb06Entity;

public class LaunchMobGoal extends SimpleBossAttackGoal {
    private Pmb06Entity mobA = null;

    public LaunchMobGoal(BossActionController.BossAction action, Pmb06Entity mob, boolean adjustRotation) {
        super(action, mob, adjustRotation, 1);
        this.mobA = mob;
    }

    public boolean canUseInternal() {
        return super.canUseInternal() && mobA.spawnedMonsters.isEmpty();
    }
}
