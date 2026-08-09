package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void pomkotsmechs$hideUnderwaterProjectileFromAbove(
            E entity,
            double x,
            double y,
            double z,
            float yRot,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            CallbackInfo ci) {
        if (!(entity instanceof PomkotsThrowableProjectile) || entity.level() == null) {
            return;
        }

        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        BlockPos cameraPos = BlockPos.containing(camera.getPosition());
        if (entity.level().getFluidState(cameraPos).is(FluidTags.WATER)) {
            return;
        }

        double projectileX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double projectileY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double projectileZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        BlockPos projectilePos = BlockPos.containing(projectileX, projectileY, projectileZ);
        FluidState fluid = entity.level().getFluidState(projectilePos);
        if (!fluid.is(FluidTags.WATER)) {
            return;
        }

        double waterSurfaceY = projectilePos.getY() + fluid.getHeight(entity.level(), projectilePos);
        if (projectileY < waterSurfaceY - 0.02D) {
            ci.cancel();
        }
    }
}
