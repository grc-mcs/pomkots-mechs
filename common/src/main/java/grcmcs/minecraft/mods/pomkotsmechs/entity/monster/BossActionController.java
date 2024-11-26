package grcmcs.minecraft.mods.pomkotsmechs.entity.monster;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BossActionController {
    public static class BossAction {
        public int maxCoolTick;
        public int maxActionTick;
        public int maxActionTickPerLoop;
        public int actualActionTick;
        public int actionLoopNum;

        public int currentCoolTick = 0;
        public int currentActionTick = 0;

        public boolean isOnEnd = false;

        private Consumer<Void> startAction;
        private Consumer<Void> actualAction;


        BossAction(int maxCoolTick, int maxActionTick, int actualActionTick, Consumer<Void> startAction, Consumer<Void> actualAction) {
            this(maxCoolTick, maxActionTick, actualActionTick, 1,  startAction, actualAction);
        }

        BossAction(int maxCoolTick, int maxActionTick, int actualActionTick, int actionLoopNum, Consumer<Void> startAction, Consumer<Void> actualAction) {
            this.maxCoolTick = maxCoolTick;
            this.maxActionTick = maxActionTick * actionLoopNum;
            this.maxActionTickPerLoop = maxActionTick;
            this.actionLoopNum = actionLoopNum;
            this.startAction = startAction;
            this.actualActionTick = actualActionTick;
            this.actualAction = actualAction;
        }

        public void tick() {
            if (currentCoolTick > 0) {
                currentCoolTick--;
            }

            if (currentActionTick > 0) {
                currentActionTick++;

                if (currentActionTick % maxActionTickPerLoop == actualActionTick) {
                    actualAction.accept(null);
                }

                if (currentActionTick == maxActionTick) {
                    currentActionTick = 0;
                    isOnEnd = true;
                }
            }
        }

        public boolean isInAction() {
            return currentActionTick > 0;
        }

        public boolean isInCooltime() {
            return currentCoolTick > 0;
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
            currentCoolTick = maxCoolTick;
            startAction.accept(null);
        }

        private void reset() {
            currentActionTick = 0;
            currentCoolTick = 0;
        }

    }

    private Map<String, BossAction> actionMap = new HashMap<>();

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
