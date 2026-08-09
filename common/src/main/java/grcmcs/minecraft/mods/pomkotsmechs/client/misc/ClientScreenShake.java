package grcmcs.minecraft.mods.pomkotsmechs.client.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public final class ClientScreenShake {
    private static float intensity;
    private static float frequency;
    private static int remainingTicks;
    private static int totalTicks;

    private ClientScreenShake() {
    }

    public static void start(float newIntensity, int durationTicks, float newFrequency) {
        if (newIntensity <= 0.0F || durationTicks <= 0) return;
        if (remainingTicks > 0 && intensity > newIntensity) return;
        intensity = Math.min(newIntensity, 20.0F);
        frequency = Math.max(0.05F, Math.min(newFrequency, 10.0F));
        remainingTicks = durationTicks;
        totalTicks = durationTicks;
    }

    public static void tick(Minecraft minecraft) {
        if (remainingTicks > 0) remainingTicks--;
        if (remainingTicks <= 0) {
            intensity = 0.0F;
            remainingTicks = 0;
            totalTicks = 0;
        }
    }

    public static Sample sample(float partialTick) {
        if (remainingTicks <= 0 || totalTicks <= 0) return Sample.NONE;
        double time = (totalTicks - remainingTicks + partialTick) * frequency;
        double envelope = Math.min(1.0D, remainingTicks / Math.max(1.0D, totalTicks * 0.25D));
        double strength = intensity * envelope;
        Vec3 position = new Vec3(
                Math.sin(time * 2.17D) * strength * 0.015D,
                Math.sin(time * 2.83D + 1.7D) * strength * 0.010D,
                Math.sin(time * 1.73D + 3.1D) * strength * 0.015D
        );
        float yaw = (float) (Math.sin(time * 2.41D + 0.6D) * strength * 0.35D);
        float pitch = (float) (Math.sin(time * 2.97D + 2.0D) * strength * 0.28D);
        return new Sample(position, yaw, pitch);
    }

    public record Sample(Vec3 position, float yaw, float pitch) {
        private static final Sample NONE = new Sample(Vec3.ZERO, 0.0F, 0.0F);

        public boolean active() {
            return position != Vec3.ZERO || yaw != 0.0F || pitch != 0.0F;
        }
    }
}
