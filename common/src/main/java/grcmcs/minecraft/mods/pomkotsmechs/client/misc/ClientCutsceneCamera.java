package grcmcs.minecraft.mods.pomkotsmechs.client.misc;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public final class ClientCutsceneCamera {
    private static final int INITIAL_ROTATION_BLEND_TICKS = 5;
    private static CameraType previousCameraType;
    private static float initialYaw;
    private static float initialPitch;
    private static boolean initialRotationBlendPending;
    private static float transitionStartTime = Float.NaN;
    private static int directCameraEntityId = Integer.MIN_VALUE;
    private static float directInitialYaw;
    private static float directInitialPitch;
    private static float directTransitionStartTime = Float.NaN;
    private static float lastRenderedYaw;
    private static float lastRenderedPitch;
    private static boolean hasLastRenderedRotation;

    private ClientCutsceneCamera() {
    }

    public static void begin() {
        Minecraft minecraft = Minecraft.getInstance();
        if (previousCameraType == null) {
            previousCameraType = minecraft.options.getCameraType();
        }
        minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        // A rotation remembered before this point belongs to the previous
        // (usually third-person) camera and must not seed direct_approach.
        hasLastRenderedRotation = false;
        directCameraEntityId = Integer.MIN_VALUE;
        directTransitionStartTime = Float.NaN;
        if (minecraft.player != null) {
            // Capture the angle that vanilla first person will actually use on
            // this client. It can differ slightly from the server-side angle,
            // especially after a free-look third-person camera was active.
            initialYaw = minecraft.player.getYRot();
            initialPitch = minecraft.player.getXRot();
            initialRotationBlendPending = true;
            transitionStartTime = Float.NaN;
        }
    }

    public static void end(float finalYaw, float finalPitch) {
        if (previousCameraType == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.setYRot(finalYaw);
            minecraft.player.yRotO = finalYaw;
            minecraft.player.setXRot(finalPitch);
            minecraft.player.xRotO = finalPitch;
            minecraft.player.setYHeadRot(finalYaw);
            minecraft.player.yHeadRotO = finalYaw;
            minecraft.player.yBodyRot = finalYaw;
            minecraft.player.yBodyRotO = finalYaw;
        }
        minecraft.options.setCameraType(previousCameraType);
        previousCameraType = null;
        initialRotationBlendPending = false;
        transitionStartTime = Float.NaN;
        directCameraEntityId = Integer.MIN_VALUE;
        directTransitionStartTime = Float.NaN;
    }

    public static Rotation blendInitialRotation(
            float targetYaw,
            float targetPitch,
            float partialTick
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!initialRotationBlendPending || minecraft.player == null) {
            return new Rotation(targetYaw, targetPitch);
        }
        float currentTime = minecraft.player.tickCount + partialTick;
        if (Float.isNaN(transitionStartTime)) {
            // Start from the first frame that actually renders through the
            // CutsceneCamera, not from the earlier network-packet arrival.
            transitionStartTime = currentTime;
        }
        float elapsed = currentTime - transitionStartTime;
        float progress = Mth.clamp(elapsed / INITIAL_ROTATION_BLEND_TICKS, 0.0F, 1.0F);
        progress = progress * progress * (3.0F - 2.0F * progress);
        if (progress >= 1.0F) {
            initialRotationBlendPending = false;
            transitionStartTime = Float.NaN;
        }
        return new Rotation(
                Mth.rotLerp(progress, initialYaw, targetYaw),
                Mth.lerp(progress, initialPitch, targetPitch)
        );
    }

    public static Rotation blendDirectApproachRotation(
            int cameraEntityId,
            float targetYaw,
            float targetPitch,
            int rotationTicks,
            float partialTick
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) return new Rotation(targetYaw, targetPitch);
        float currentTime = minecraft.player.tickCount + partialTick;
        if (directCameraEntityId != cameraEntityId) {
            // Start from the last angle that was actually rendered before the
            // view entity changed. The raw first rotation of a newly synced
            // CutsceneCameraEntity can still be stale, especially on Forge.
            directCameraEntityId = cameraEntityId;
            directInitialYaw = hasLastRenderedRotation
                    ? lastRenderedYaw : minecraft.player.getYRot();
            directInitialPitch = hasLastRenderedRotation
                    ? lastRenderedPitch : minecraft.player.getXRot();
            directTransitionStartTime = currentTime;
            return new Rotation(directInitialYaw, directInitialPitch);
        }
        if (Float.isNaN(directTransitionStartTime)) {
            directTransitionStartTime = currentTime;
        }
        float elapsed = currentTime - directTransitionStartTime;
        float progress = Mth.clamp(elapsed / Math.max(1, rotationTicks), 0.0F, 1.0F);
        progress = progress * progress * (3.0F - 2.0F * progress);
        return new Rotation(
                Mth.rotLerp(progress, directInitialYaw, targetYaw),
                Mth.lerp(progress, directInitialPitch, targetPitch)
        );
    }

    public static void rememberRenderedRotation(float yaw, float pitch) {
        lastRenderedYaw = yaw;
        lastRenderedPitch = pitch;
        hasLastRenderedRotation = true;
    }

    public record Rotation(float yaw, float pitch) {
    }

}
