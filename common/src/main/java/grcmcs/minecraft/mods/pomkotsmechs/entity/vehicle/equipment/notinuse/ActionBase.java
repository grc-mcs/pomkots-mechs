package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.notinuse;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ActionBase {
    public int maxCoolTick;
    public int maxActionTick;

    public int currentCoolTick = 0;
    public int currentActionTick = 0;

    public boolean isOnEnd = false;
    PomkotsVehicleBase body;

    Map<Integer, ActionProcedure> procMap = new HashMap<>();

    public static class ActionProcedure {
        final Consumer<PomkotsVehicleBase> logicProcedure;
        final Function<AnimationState<PomkotsVehicleBase>, PlayState> animationProcedure;

        public ActionProcedure(Consumer<PomkotsVehicleBase> l, Function<AnimationState<PomkotsVehicleBase>, PlayState> a) {
            this.logicProcedure = l;
            this.animationProcedure = a;
        }
    }

    ActionBase(int maxCoolTick, int maxActionTick, PomkotsVehicleBase body) {
        this.maxCoolTick = maxCoolTick;
        this.maxActionTick = maxActionTick;
        this.body = body;
    }

    public void registerActionProcedure(int triggerTick, ActionProcedure proc) {
        procMap.put(triggerTick, proc);
    }

    public void tick() {
        if (currentCoolTick > 0) {
            currentCoolTick--;
        }

        if (currentActionTick > 0) {
            currentActionTick++;

            var p = procMap.get(currentActionTick);
            if (p != null && p.animationProcedure != null) {
                p.logicProcedure.accept(this.body);
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

    private void startAction() {
        currentActionTick = 1;
        currentCoolTick = maxCoolTick;

        procMap.get(1).logicProcedure.accept(this.body);
    }

    public PlayState tickAnimation(AnimationState<PomkotsVehicleBase> event) {
        if (currentActionTick > 0) {
            var p = procMap.get(currentActionTick);

            if (p == null || p.animationProcedure == null) {
                return null;
            } else {
                return p.animationProcedure.apply(event);
            }
        } else {
            return null;
        }
    }

    private void reset() {
        currentActionTick = 0;
        currentCoolTick = 0;
    }
}
