package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;

public class ActionController2 extends ActionController {
    private PomkotsVehicleBase mech;

    public ActionController2(PomkotsVehicleBase mech) {
        this.mech = mech;
    }

    public boolean isBoost() {
        return mech.isBoost();
    }

    public void setBoost(boolean b) {
        if (!mech.level().isClientSide) mech.setBoost(b);
    }
}
