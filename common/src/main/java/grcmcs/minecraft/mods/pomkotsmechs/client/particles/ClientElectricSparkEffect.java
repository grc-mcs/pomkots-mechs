package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import grcmcs.minecraft.mods.pomkotsmechs.util.ElectricSparkEffect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Client-only entry points for electric spark effects.
 * These methods do not send packets and are visible only to the local client.
 */
@Environment(EnvType.CLIENT)
public final class ClientElectricSparkEffect {
    private ClientElectricSparkEffect() {
    }

    public static void spawn(ClientLevel level, BlockPos center, double radius, int count) {
        ElectricSparkEffect.spawn(level, center, radius, count);
    }

    public static void spawn(ClientLevel level, Vec3 center, double radius, int count) {
        ElectricSparkEffect.spawn(level, center, radius, count);
    }

    public static void spawn(BlockPos center, double radius, int count) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            spawn(level, center, radius, count);
        }
    }

    public static void spawn(Vec3 center, double radius, int count) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            spawn(level, center, radius, count);
        }
    }

    /** Spawns the requested number of sparks at random times during the given number of seconds. */
    public static void spawnOverTime(ClientLevel level, BlockPos center, double radius, int count, double durationSeconds) {
        spawnOverTime(level, Vec3.atCenterOf(center), radius, count, durationSeconds);
    }

    /** Spawns the requested number of sparks at random times during the given number of seconds. */
    public static void spawnOverTime(ClientLevel level, Vec3 center, double radius, int count, double durationSeconds) {
        ClientElectricSparkEmitter.start(level, center, radius, count, durationSeconds);
    }

    public static void spawnOverTime(BlockPos center, double radius, int count, double durationSeconds) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            spawnOverTime(level, center, radius, count, durationSeconds);
        }
    }

    public static void spawnOverTime(Vec3 center, double radius, int count, double durationSeconds) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            spawnOverTime(level, center, radius, count, durationSeconds);
        }
    }
}
