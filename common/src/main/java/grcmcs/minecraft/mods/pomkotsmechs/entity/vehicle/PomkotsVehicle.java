package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle;

import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.entity.PomkotsControllable;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.LockTargets;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface PomkotsVehicle extends PomkotsControllable {
    public LockTargets getLockTargets();
    public Vec3 getClientSeatPos(Entity passenger);
    public boolean shouldRenderDefaultHud(String name);
    public boolean shouldLockMulti(DriverInput di);
    public boolean shouldLockWeak(DriverInput di);
    public boolean shouldLockStrong(DriverInput di);
}
