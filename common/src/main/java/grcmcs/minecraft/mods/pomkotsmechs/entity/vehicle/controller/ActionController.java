package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.controller;

public class ActionController {
    public class Action {
        public int maxCoolTime;
        public int maxChargeTime;
        public int maxFireTime;

        public int currentCoolTime = 0;
        public int currentChargeTime = 0;
        public int currentFireTime = 0;

        public boolean isOnEnd = false;

        Action(int maxCoolTime, int maxChargeTime, int maxFireTime) {
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
                    currentChargeTime = 0;
                    currentFireTime = 1;
                }
            } else if (currentFireTime > 0){
                currentFireTime++;

                if (currentFireTime == maxFireTime) {
                    currentFireTime = 0;
                    isOnEnd = true;
                }
            }
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

        public boolean tryAction() {
            if (maxCoolTime == 0 && isOnEnd) {
                // CoolTime 0の時はホールド可能にする
                currentChargeTime = 0;
                currentFireTime = 1;
                isOnEnd = false;

                return true;
            } else if (!isInAction() && !isInCooltime()) {
                startAction();

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

        private void startAction() {
            if (maxChargeTime > 0) {
                currentChargeTime = 1;
            } else {
                currentFireTime = 1;
            }

            currentCoolTime = maxCoolTime;
        }

        private void reset() {
            currentCoolTime = 0;
            currentChargeTime = 0;
            currentFireTime = 0;
        }

    }

    private boolean isDash = false;
    public Action evasion = new Action(10, 0, 10);
    public Action jump = new Action(10, 6, 3);
    public Action hummer = new Action(20, 12, 8);
    public Action scop = new Action(20, 12, 8);
    public Action gatring = new Action(0, 7, 2);
    public Action pile = new Action(20, 10, 10);
    public Action grenade = new Action(60, 4, 10);
    public Action missile = new Action(60, 0, 20);
    public Action gigadrill = new Action(20, 0, 100);

    public void tick() {
        evasion.tick();
        jump.tick();
        hummer.tick();
        scop.tick();
        gatring.tick();
        pile.tick();
        grenade.tick();
        missile.tick();
        gigadrill.tick();
    }

    public void reset() {
        evasion.reset();
        jump.reset();
        hummer.reset();
        scop.reset();
        gatring.reset();
        pile.reset();
        grenade.reset();
        missile.reset();
        gigadrill.reset();
        setBoost(false);
    }

    public boolean isInActionAll() {
        return evasion.isInAction()
                || jump.isInAction()
                || hummer.isInAction()
                || scop.isInAction()
                || gatring.isInAction()
                || pile.isInAction()
                || grenade.isInAction()
                || gigadrill.isInAction();
    }

    public boolean isBoost() {
        return isDash;
    }

    public void setBoost(boolean b) {
        this.isDash = b;
    }
}
