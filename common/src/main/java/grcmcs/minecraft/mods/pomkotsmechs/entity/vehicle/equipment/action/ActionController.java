package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action;

import java.util.HashMap;
import java.util.Map;

public class ActionController {
    public static enum ActionType {
        R_ARM_MAIN(),
        L_ARM_MAIN(),
        R_SHL_MAIN(),
        L_SHL_MAIN(),
        R_ARM_SUB(),
        L_ARM_SUB(),
        R_SHL_SUB(),
        L_SHL_SUB(),
        BASE();
    }

    private boolean isDash = false;

    private Map<Integer, Action> actionMap = new HashMap<>();

    private Map<ActionType, Action> actionTypeMap = new HashMap<>();

    public void registerAction(Integer key, Action action, ActionType type) {
        if (this.actionMap.containsKey(key)) {
            throw new IllegalStateException("Already registered. key:" + key);
        }

        this.actionMap.put(key, action);

        if (!ActionType.BASE.equals(type)) {
            this.actionTypeMap.put(type, action);
        }
    }

    public Action getAction(int key) {
        return this.actionMap.get(key);
    }

    public Action getActionFromType(ActionType type) {
        return this.actionTypeMap.get(type);
    }

    public void tick() {
        for (Action action: this.actionMap.values()) {
            action.tick();
        }
    }

    public void reset() {
        for (Action action: this.actionMap.values()) {
            action.reset();
        }
        setBoost(false);
    }

    public boolean isInActionAll() {
        for (Action action: this.actionMap.values()) {
            if (action.isInAction()) {
                return true;
            }
        }
        return false;
    }

    public boolean isBoost() {
        return isDash;
    }

    public void setBoost(boolean b) {
        this.isDash = b;
    }
}
