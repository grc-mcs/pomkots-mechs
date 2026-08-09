package grcmcs.minecraft.mods.pomkotsmechs.util;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ElectricSparkEffect {
    private ElectricSparkEffect() {
    }

    /**
     * Spawns sparks uniformly across the surface of a sphere.
     * Call this on the logical server to make the effect visible to nearby players.
     */
    public static void spawn(Level level, BlockPos center, double radius, int count) {
        spawn(level, Vec3.atCenterOf(center), radius, count);
    }

    /**
     * Spawns sparks uniformly across the surface of a sphere.
     * Call this on the logical server to make the effect visible to nearby players.
     */
    public static void spawn(Level level, Vec3 center, double radius, int count) {
        if (radius < 0.0D || count <= 0) {
            return;
        }

        RandomSource random = level.getRandom();
        for (int i = 0; i < count; i++) {
            double y = random.nextDouble() * 2.0D - 1.0D;
            double angle = random.nextDouble() * Mth.TWO_PI;
            double horizontal = Math.sqrt(Math.max(0.0D, 1.0D - y * y));
            Vec3 direction = new Vec3(
                    horizontal * Math.cos(angle),
                    y,
                    horizontal * Math.sin(angle)
            );

            Vec3 position = center.add(direction.scale(radius));
            double speed = 0.015D + random.nextDouble() * 0.025D;
            Vec3 velocity = direction.scale(speed);

            if (level instanceof ServerLevel serverLevel) {
                // A zero count sends exactly one particle and treats the offsets as velocity.
                serverLevel.sendParticles(
                        PomkotsMechs.ELECTRIC_SPARK.get(),
                        position.x, position.y, position.z,
                        0,
                        velocity.x, velocity.y, velocity.z,
                        1.0D
                );
            } else {
                level.addParticle(
                        PomkotsMechs.ELECTRIC_SPARK.get(),
                        position.x, position.y, position.z,
                        velocity.x, velocity.y, velocity.z
                );
            }
        }
    }
}
