package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Emits a fixed number of electric sparks at random times over a duration. */
@Environment(EnvType.CLIENT)
public final class ClientElectricSparkEmitter {
    private static final List<ClientElectricSparkEmitter> ACTIVE_EMITTERS = new ArrayList<>();

    private final ClientLevel level;
    private final Vec3 center;
    private final double radius;
    private final int[] particlesPerTick;
    private int age;

    private ClientElectricSparkEmitter(ClientLevel level, Vec3 center, double radius, int count, int durationTicks) {
        this.level = level;
        this.center = center;
        this.radius = radius;
        this.particlesPerTick = new int[durationTicks];

        RandomSource random = level.getRandom();
        for (int i = 0; i < count; i++) {
            particlesPerTick[random.nextInt(durationTicks)]++;
        }
    }

    static void start(ClientLevel level, Vec3 center, double radius, int count, double durationSeconds) {
        if (radius < 0.0D || count <= 0) {
            return;
        }

        int durationTicks = Math.max(1, (int) Math.ceil(durationSeconds * 20.0D));
        ACTIVE_EMITTERS.add(new ClientElectricSparkEmitter(level, center, radius, count, durationTicks));
    }

    public static void tick(Minecraft minecraft) {
        ClientLevel currentLevel = minecraft.level;
        Iterator<ClientElectricSparkEmitter> iterator = ACTIVE_EMITTERS.iterator();

        while (iterator.hasNext()) {
            ClientElectricSparkEmitter emitter = iterator.next();
            if (currentLevel == null || emitter.level != currentLevel) {
                iterator.remove();
                continue;
            }

            int count = emitter.particlesPerTick[emitter.age++];
            if (count > 0) {
                ClientElectricSparkEffect.spawn(emitter.level, emitter.center, emitter.radius, count);
            }

            if (emitter.age >= emitter.particlesPerTick.length) {
                iterator.remove();
            }
        }
    }
}
