package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action;

public class Action {
    public int maxCoolTime;
    public int maxChargeTime;
    public int maxFireTime;

    public int currentCoolTime = 0;
    public int currentChargeTime = 0;
    public int currentFireTime = 0;

    public boolean isOnEnd = false;

    public Action(int maxCoolTime, int maxChargeTime, int maxFireTime) {
        this.maxCoolTime = maxCoolTime;
        this.maxChargeTime = maxChargeTime;
        this.maxFireTime = maxFireTime;
    }

    public void tick() {
        isOnEnd = false;

        if (currentCoolTime > 0) {
            currentCoolTime--;
        }

        if (currentChargeTime > 0 && maxChargeTime != 0) {
            currentChargeTime++;

            if (currentChargeTime == maxChargeTime) {
                fireAction();
            }
        } else if (currentFireTime > 0){
            currentFireTime++;

            if (currentFireTime == maxFireTime) {
                currentFireTime = 0;
                isOnEnd = true;
            }
        }
    }

    public void fireAction() {
        currentChargeTime = 0;
        currentFireTime = 1;
    }

    public boolean isInAction() {
        return currentChargeTime > 0 || currentFireTime > 0;
    }

    public boolean isInFire() {
        return currentFireTime > 0;
    }

    public boolean isInCooltime() {
        return currentCoolTime > 0;
    }

    public boolean isCharging() {
        return currentChargeTime > 0;
    }

    public boolean isFiring() {
        return currentFireTime > 0;
    }

    public boolean isOnFire() {
        return currentFireTime == 1;
    }

    public boolean startAction() {
        if (maxCoolTime == 0 && isOnEnd) {
            // CoolTime 0の時はホールド可能にする
            currentChargeTime = 0;
            currentFireTime = 1;
            isOnEnd = false;

            return true;
        } else if (!isInAction() && !isInCooltime()) {
            startActionInternal();

            return true;
        } else {
            return false;
        }
    }

    public boolean isOnStart() {
        if (maxChargeTime > 0) {
            return currentChargeTime == 1;
        } else {
            return currentFireTime == 1;
        }
    }

    private void startActionInternal() {
        if (maxChargeTime > 0) {
            currentChargeTime = 1;
        } else {
            currentFireTime = 1;
        }

        currentCoolTime = maxCoolTime;
    }

    public void reset() {
        currentCoolTime = 0;
        currentChargeTime = 0;
        currentFireTime = 0;
    }

}