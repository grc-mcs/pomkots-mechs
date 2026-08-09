package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.Pmv03EntityRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.client.misc.ClientScreenShake;
import grcmcs.minecraft.mods.pomkotsmechs.client.misc.ClientCutsceneCamera;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv03Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.cutscene.CutsceneCameraEntity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaCameraEntity;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Camera.class, priority = 100)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setPosition(Vec3 vec3);

    @Inject(method = "setup", at = @At("TAIL"), cancellable = true)
    private void adjustCameraPosition(BlockGetter blockGetter, Entity viewEntity, boolean thirdPerson, boolean inverse, float partialTick, CallbackInfo ci) {
        if (viewEntity instanceof CutsceneCameraEntity cutsceneCamera) {
            Camera camera = (Camera) (Object) this;
            if (cutsceneCamera.usesDirectApproachView()) {
                ClientCutsceneCamera.Rotation target =
                        pomkotsmechs$rotationTo(
                                camera, cutsceneCamera.getClientLookTarget(partialTick));
                ClientCutsceneCamera.Rotation rotation =
                        ClientCutsceneCamera.blendDirectApproachRotation(
                                cutsceneCamera.getId(),
                                target.yaw(), target.pitch(),
                                cutsceneCamera.getDirectRotationTicks(), partialTick);
                ((CameraAccessor) camera).invokeSetRotation(rotation.yaw(), rotation.pitch());
            } else if (cutsceneCamera.usesFixedView()) {
                ClientCutsceneCamera.Rotation rotation =
                        ClientCutsceneCamera.blendInitialRotation(
                                cutsceneCamera.getFixedViewYaw(partialTick),
                                cutsceneCamera.getFixedViewPitch(partialTick),
                                partialTick);
                ((CameraAccessor) camera).invokeSetRotation(
                        rotation.yaw(), rotation.pitch());
            } else {
                pomkotsmechs$lookAt(camera, cutsceneCamera.getClientLookTarget(partialTick));
            }
            return;
        }

        if (viewEntity instanceof ArenaCameraEntity arenaCamera) {
            Camera camera = (Camera) (Object) this;
            pomkotsmechs$lookAt(camera, arenaCamera.getClientLookTarget(partialTick));
            return;
        }

        if (viewEntity instanceof Player player) {
            Camera camera = (Camera) (Object) this;
            // プレイヤーが巨大ロボに乗っているかを判定
            if (player.getVehicle() instanceof Pmvc01Entity pmg && !thirdPerson) {
                Vec3 bonePos = pmg.getMainCameraPosition();

                // カメラ位置をボーン位置に設定
                setPosition(pmg.getPosition(partialTick).add(bonePos));

            } else if (player.getVehicle() instanceof Pmv03Entity pmg && !thirdPerson) {
                Vec3 bonePos = pmg.getMainCameraPosition();

                // カメラ位置をボーン位置に設定
                setPosition(pmg.getPosition(partialTick).add(bonePos));
            }
            ClientCutsceneCamera.rememberRenderedRotation(
                    camera.getYRot(), camera.getXRot());
        }

        if (thirdPerson) return;

        ClientScreenShake.Sample shake = ClientScreenShake.sample(partialTick);
        if (shake.active()) {
            Camera camera = (Camera) (Object) this;
            setPosition(camera.getPosition().add(shake.position()));
            ((CameraAccessor) camera).invokeSetRotation(
                    camera.getYRot() + shake.yaw(),
                    camera.getXRot() + shake.pitch());
        }
    }

    private static void pomkotsmechs$lookAt(Camera camera, Vec3 target) {
        ClientCutsceneCamera.Rotation rotation = pomkotsmechs$rotationTo(camera, target);
        ((CameraAccessor) camera).invokeSetRotation(rotation.yaw(), rotation.pitch());
    }

    private static ClientCutsceneCamera.Rotation pomkotsmechs$rotationTo(Camera camera, Vec3 target) {
        Vec3 position = camera.getPosition();
        double dx = target.x - position.x;
        double dy = target.y - position.y;
        double dz = target.z - position.z;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        float pitch = (float) (-(Mth.atan2(dy, horizontalDistance) * Mth.RAD_TO_DEG));
        return new ClientCutsceneCamera.Rotation(yaw, pitch);
    }
}
