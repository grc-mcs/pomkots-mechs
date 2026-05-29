package grcmcs.minecraft.mods.pomkotsmechs.client.misc;

import net.minecraft.client.Minecraft;

public class ClientHudShake {

    public static float shake = 0f;

    public static void addShake(float amount) {

        shake = Math.max(shake, amount);
    }

    public static void tick(Minecraft mc) {

        shake *= 0.9f;

        if (shake < 0.05f) {
            shake = 0f;
        }
    }
}
