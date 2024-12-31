package grcmcs.minecraft.mods.pomkotsmechs.util;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.PomkotsControllable;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class Utils {
    protected static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    public static boolean isRidingPomkotsControllable(Entity entity) {
        return entity != null && entity.getVehicle() instanceof PomkotsControllable;
    }

    public static boolean isRidingPomkotsVehicle(Entity entity) {
        return entity != null && entity.getVehicle() instanceof PomkotsVehicle;
    }

    public static void logInfo(String message) {
        LOGGER.info(message);
    }

    public static void logError(String message, Throwable t) {
        LOGGER.error(message, t);
    }


    private static Vec3 createEvasionVelocityFromLocalVelocity(Entity ent) {
        Vec3 localVelocity = ent.getDeltaMovement().yRot((float) Math.toRadians((1.0) * ent.getYRot()));
        var forward = localVelocity.z;
        var sideways = localVelocity.x;

        Vec3 vel;
        if (forward == 0 && sideways == 0) {
            vel = new Vec3(0, 0, 1);
        } else if (Math.abs(sideways) >= 0.1) {
            vel = new Vec3(sideways, 0, 0).normalize();
        } else  {
            vel = new Vec3(sideways, 0, forward).normalize();
        }

        return vel;
    }

    // TODO くそかっこ悪いのでなんとかしたい…。半径指定したら計算して出してくれるようにする
    public static final ArrayList<Vec3i> circlePosRad9 = new ArrayList<Vec3i>();
    static {
        int rad = 5;
        for (int i = 1; i <= 9; i++) {
            circlePosRad9.add(new Vec3i(i - rad, 0, 0));}
        for (int i = 1; i <= 9; i++) {
            circlePosRad9.add(new Vec3i(i - rad, 0, 1));}
        for (int i = 1; i <= 9; i++) {
            circlePosRad9.add(new Vec3i(i - rad, 0, -1));}
        for (int i = 1; i <= 7; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 1, 0, 2));}
        for (int i = 1; i <= 7; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 1, 0, -2));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 2, 0, 3));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 2, 0, -3));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 3, 0, 4));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad9.add(new Vec3i(i - rad + 3, 0, -4));}
    }

    public static final ArrayList<Vec3i> circlePosRad7 = new ArrayList<Vec3i>();
    static {
        int rad = 5;
        for (int i = 1; i <= 7; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 1, 0, -1));}
        for (int i = 1; i <= 7; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 1, 0, 0));}
        for (int i = 1; i <= 7; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 1, 0, 1));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 2, 0, 2));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 2, 0, -2));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 3, 0, 3));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad7.add(new Vec3i(i - rad + 3, 0, -3));}
    }

    public static final ArrayList<Vec3i> circlePosRad5 = new ArrayList<Vec3i>();
    static {
        int rad = 5;
        for (int i = 1; i <= 5; i++) {
            circlePosRad5.add(new Vec3i(i - rad + 2, 0, 1));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad5.add(new Vec3i(i - rad + 2, 0, 0));}
        for (int i = 1; i <= 5; i++) {
            circlePosRad5.add(new Vec3i(i - rad + 2, 0, -1));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad5.add(new Vec3i(i - rad + 3, 0, 2));}
        for (int i = 1; i <= 3; i++) {
            circlePosRad5.add(new Vec3i(i - rad + 3, 0, -2));}
    }
}
