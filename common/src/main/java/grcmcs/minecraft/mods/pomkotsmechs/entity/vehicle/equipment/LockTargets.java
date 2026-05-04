package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//TODO タゲロック回りまじでなんとかしないとやばい…
public class LockTargets {
    private Entity lockTargetS = null;
    private Entity lockTargetH = null;
    private boolean multiLockComplete = false;
    private List<Entity> lockTargetM = new ArrayList<>();
    private List<Entity> lockTargetMRA = new ArrayList<>();
    private List<Entity> lockTargetMLA = new ArrayList<>();
    private List<Entity> lockTargetMRS = new ArrayList<>();
    private List<Entity> lockTargetMLS = new ArrayList<>();

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

    public void clearLockTargetsMulti() {
        lockTargetM.clear();
    }

    public List<Entity> getLockTargetMulti(int slot) {
        if (slot == Pmvc01Entity.INV_WEAPON_RIGHT_HAND) {
            return lockTargetMRA;
        } else if (slot == Pmvc01Entity.INV_WEAPON_LEFT_HAND) {
            return lockTargetMLA;
        } else if (slot == Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER) {
            return lockTargetMRS;
        } else if (slot == Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER) {
            return lockTargetMLS;
        }
        return Collections.emptyList();
    }

    public void syncTargetMulti(int slotSrc, int slotDst) {
        var src = getLockTargetMulti(slotSrc);
        var dst = getLockTargetMulti(slotDst);

        dst.clear();
        dst.addAll(src);
    }

    public List<Entity> consumeTargetMulti(int slot) {
        var tgt = getLockTargetMulti(slot);

        if (tgt.isEmpty()) {
            return tgt;
        }

        var res = new ArrayList<>(tgt);
        tgt.clear();
        return res;
    }

    public Entity consumeTargetMultiSingle(int slot) {
        var tgt = getLockTargetMulti(slot);

        if (tgt.isEmpty()) {
            return null;
        }

        return tgt.remove(0);
    }

    public void lockTargetMulti(Entity ent, int slot, Pmvc01Entity mech) {
        if (slot == Pmvc01Entity.INV_WEAPON_RIGHT_HAND) {
            lockTargetMultiInternal(ent, lockTargetMRA, mech.getRightArmWeapon(), mech);
        } else if (slot == Pmvc01Entity.INV_WEAPON_LEFT_HAND) {
            lockTargetMultiInternal(ent, lockTargetMLA, mech.getLeftArmWeapon(), mech);
        } else if (slot == Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER) {
            lockTargetMultiInternal(ent, lockTargetMRS, mech.getRightShoulderWeapon(), mech);
        } else if (slot == Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER) {
            lockTargetMultiInternal(ent, lockTargetMLS, mech.getLeftShoulderWeapon(), mech);
        }
    }

    private void lockTargetMultiInternal(Entity ent, List<Entity> list, ItemStack stack, Pmvc01Entity mech) {
        // @JOKE
        if (list.size() < Pmvc01Entity.getMultiLockTargetNum(stack) || mech.isGattai()) {
            list.add(ent);
        }
    }

    public void clearLockTargetsMulti(int slot) {
        if (slot == Pmvc01Entity.INV_WEAPON_RIGHT_HAND) {
            lockTargetMRA.clear();
        } else if (slot == Pmvc01Entity.INV_WEAPON_LEFT_HAND) {
            lockTargetMLA.clear();
        } else if (slot == Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER) {
            lockTargetMRS.clear();
        } else if (slot == Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER) {
            lockTargetMLS.clear();
        }
    }

    public void unlockTargetMulti() {
        multiLockComplete = true;
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

        if (!lockTargetMRA.isEmpty()) {
            lockTargetMRA.clear();
        }
        if (!lockTargetMLA.isEmpty()) {
            lockTargetMLA.clear();
        }
        if (!lockTargetMRS.isEmpty()) {
            lockTargetMRS.clear();
        }
        if (!lockTargetMLS.isEmpty()) {
            lockTargetMLS.clear();
        }
    }
}
