package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.items.pilot.PilotRoleItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class PilotDisposition {
    private PilotDisposition() {
    }

    public static boolean isHostilePilot(Entity entity) {
        return entity instanceof LivingEntity living
                && living.getOffhandItem().getItem() instanceof PilotRoleItem.PlotRoleRaider;
    }

    public static boolean hasHostilePilot(Entity entity) {
        return entity instanceof PomkotsVehicleBase vehicle
                && isHostilePilot(vehicle.getDrivingPassenger());
    }
}
