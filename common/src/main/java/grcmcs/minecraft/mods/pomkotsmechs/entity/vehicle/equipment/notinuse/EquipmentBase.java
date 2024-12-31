package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.notinuse;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;

public abstract class EquipmentBase {
    Action action = null;

    public boolean launchWeapon() {
        return action.startAction();
    }

}
