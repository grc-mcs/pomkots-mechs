package grcmcs.minecraft.mods.pomkotsmechs.compat;

import dev.architectury.platform.Platform;

public final class ThirdPersonCompat {

    private ThirdPersonCompat() {}

    public static boolean isLoaded() {
        return Platform.isModLoaded(
                "leawind_third_person"
        );
    }

    public static void enable() {

        if (!isLoaded()) {
            return;
        }


    }

    public static void disable() {

        if (!isLoaded()) {
            return;
        }

        // API呼び出し
    }
}
