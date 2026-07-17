package grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena;

import grcmcs.minecraft.mods.pomkotsmechs.client.hud.PomkotsHud;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;

public class ArenaCameraClient {

    private static CameraType previousCameraType;

    public static void startOpeningCamera() {
        PomkotsHud.setActive(false);

        Minecraft mc = Minecraft.getInstance();

        previousCameraType =
                mc.options.getCameraType();

        mc.options.setCameraType(
                CameraType.FIRST_PERSON
        );
    }

    public static void endOpeningCamera() {
        PomkotsHud.setActive(true);

        Minecraft mc = Minecraft.getInstance();

        if (previousCameraType != null) {

            mc.options.setCameraType(
                    previousCameraType
            );

            previousCameraType = null;

        } else {
            mc.options.setCameraType(
                    CameraType.THIRD_PERSON_FRONT
            );
        }
    }
}