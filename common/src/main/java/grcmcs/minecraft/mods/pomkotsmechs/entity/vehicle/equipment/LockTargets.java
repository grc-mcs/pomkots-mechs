package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment;

import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class LockTargets {
    private Entity lockTargetS = null;
    private Entity lockTargetH = null;
    private boolean multiLockComplete = false;
    private List<Entity> lockTargetM = new ArrayList<>();

    public Entity getLockTargetSoft() {
        return lockTargetS;
    }

    public void lockTargetSoft(Entity ent) {
        this.lockTargetS = ent;
    }

    public void unlockTargetSoft() {
        this.lockTargetS = null;
    }

    public Entity getLockTargetHard() {
        return lockTargetH;
    }

    public void lockTargetHard(Entity ent) {
        this.lockTargetH = ent;
    }

    public void unlockTargetHard() {
        this.lockTargetH = null;
    }

    public List<Entity> getLockTargetMulti() {
        return lockTargetM;
    }

    public void lockTargetMulti(Entity ent) {
        if (lockTargetM.size() < 6) {
            lockTargetM.add(ent);
        }
    }

    public void unlockTargetMulti() {
        multiLockComplete = true;
    }

    public void clearLockTargetsMulti() {
        lockTargetM.clear();
    }

    public boolean consumeMultiLockComplete() {
        if (this.multiLockComplete) {
            this.multiLockComplete = false;
            return true;
        } else {
            return false;
        }
    }

    public void clearLockTargets() {
        unlockTargetSoft();
        unlockTargetHard();

        if (!lockTargetM.isEmpty()) {
            lockTargetM.clear();
        }
    }
}
