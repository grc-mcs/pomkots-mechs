package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BossActionController {
    public static class BossAction {
        public int maxCoolTick;
        public int maxActionTick;

        public int currentCoolTick = 0;
        public int currentActionTick = 0;

        private int currentLoopNum = 0;

        private boolean continueFlag = false;

        Consumer<BossAction> tickInAction;

        BossAction(int maxCoolTick, int maxActionTick, Consumer<BossAction> tickInAction) {
            this.currentCoolTick = this.maxCoolTick = maxCoolTick;
            this.maxActionTick = maxActionTick;
            this.tickInAction = tickInAction;
        }

        public void tick() {
            if (currentCoolTick > 0) {
                currentCoolTick--;

                return;
            }

            if (currentActionTick > 0) {
                this.tickInAction.accept(this);
                currentActionTick++;

            }

            if (currentActionTick == maxActionTick + 1) {
                if (consumeContinueFlag()) {
                    currentActionTick = 1;
                    currentLoopNum++;
                } else {
                    currentActionTick = 0;
                    currentCoolTick = maxCoolTick;
                }
            }
        }

        private boolean consumeContinueFlag() {
            var tmp = continueFlag;
            continueFlag = false;

            return tmp;
        }

        public void setContinue() {
            this.continueFlag = true;
        }

        public boolean isInAction() {
            return currentActionTick > 0;
        }

        public boolean isInCooltime() {
            return currentCoolTick > 0;
        }

        public boolean onStartOfAction() {
            return currentActionTick == 1;
        }

        public boolean onEndOfAction() {
            return currentActionTick == maxActionTick - 1;
        }

        public boolean isFirstLoop() {
            return currentLoopNum == 0;
        }

        public boolean tryAction() {
            if (!isInAction() && !isInCooltime()) {
                startAction();
                return true;
            } else {
                return false;
            }
        }

        public boolean canAction() {
            return !isInAction() && !isInCooltime();
        }

        private void startAction() {
            currentActionTick = 1;
            continueFlag = false;
            currentLoopNum = 0;
        }

        private void reset() {
            currentActionTick = 0;
            currentCoolTick = 0;
            continueFlag = false;
            currentLoopNum = 0;
        }

        public void stopAction() {
            currentActionTick = -1;
            currentCoolTick = maxCoolTick;
            continueFlag = false;
            currentLoopNum = 0;
        }
    }

    private final Map<String, BossAction> actionMap = new HashMap<>();

    public void registerAction(String name, BossAction action) {
        actionMap.put(name, action);
    }

    public BossAction getAction(String name) {
        return actionMap.get(name);
    }

    public void tick() {
        for(BossAction action: actionMap.values()) {
            action.tick();
        }
    }

    public void reset() {
        for(BossAction action: actionMap.values()) {
            action.reset();
        }
    }

    public boolean isInActionAll() {
        for(BossAction action: actionMap.values()) {
            if (action.isInAction()) {
                return true;
            }
        }
        return false;
    }
}
