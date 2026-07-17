package grcmcs.minecraft.mods.pomkotsmechs.misc.scan;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class ScanUtils {
    public static boolean spawnPersonalScan(
            ServerLevel level,
            ServerPlayer player,
            Vec3 center
    ) {
        return spawnScan(
                level,
                player,
                center,
                ScanVisibility.OWNER,
                64.0F,
                60,
                100
        );
    }


    public static boolean spawnScan(
            ServerLevel level,
            ServerPlayer owner,
            Vec3 center,
            ScanVisibility visibility,
            float maxRadius,
            int expansionTicks,
            int highlightTicks
    ) {
        ScanPulseEntity pulse =
                PomkotsMechs.SCAN_PULSE.get().create(level);

        if (pulse == null) {
            return false;
        }

        pulse.moveTo(
                center.x,
                center.y,
                center.z,
                0.0F,
                0.0F
        );

        pulse.initialize(
                owner,
                visibility,
                maxRadius,
                expansionTicks,
                highlightTicks
        );

        return level.addFreshEntity(pulse);
    }
}
