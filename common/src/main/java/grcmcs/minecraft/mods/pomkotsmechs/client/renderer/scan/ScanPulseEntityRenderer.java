package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.scan;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.misc.scan.ScanPulseEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class ScanPulseEntityRenderer
        extends EntityRenderer<ScanPulseEntity> {

    private static final ResourceLocation EMPTY_TEXTURE =
            new ResourceLocation(
                    "pomkotsmechs",
                    "textures/misc/empty.png"
            );

    public ScanPulseEntityRenderer(
            EntityRendererProvider.Context context
    ) {
        super(context);
    }

    @Override
    public void render(
            ScanPulseEntity pulse,
            float entityYaw,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight
    ) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer == null
                || !pulse.isVisibleTo(localPlayer)) {
            return;
        }

        float radius = pulse.getRadius(partialTick);
        float maxRadius = pulse.getMaxRadius();

        /*
         * 最大半径の80%を超えたらフェードアウト開始。
         *
         * radius == maxRadius * 0.8 → alphaMultiplier = 1
         * radius == maxRadius       → alphaMultiplier = 0
         */
        float fadeStartRadius = maxRadius * 0.8F;

        float alphaMultiplier;

        if (radius <= fadeStartRadius) {
            alphaMultiplier = 1.0F;
        } else {
            alphaMultiplier = 1.0F - Mth.clamp(
                    (radius - fadeStartRadius)
                            / (maxRadius - fadeStartRadius),
                    0.0F,
                    1.0F
            );
        }

        ScanDomeRenderer.render(
                poseStack,
                bufferSource,
                radius,
                alphaMultiplier
        );

        super.render(
                pulse,
                entityYaw,
                partialTick,
                poseStack,
                bufferSource,
                packedLight
        );
    }

    @Override
    public ResourceLocation getTextureLocation(
            ScanPulseEntity entity
    ) {
        return EMPTY_TEXTURE;
    }

    @Override
    public boolean shouldRender(
            ScanPulseEntity pulse,
            Frustum frustum,
            double cameraX,
            double cameraY,
            double cameraZ
    ) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer == null
                || !pulse.isVisibleTo(localPlayer)) {
            return false;
        }

        double radius = pulse.getMaxRadius();

        AABB domeBox = new AABB(
                pulse.getX() - radius,
                pulse.getY() - radius,
                pulse.getZ() - radius,
                pulse.getX() + radius,
                pulse.getY() + radius,
                pulse.getZ() + radius
        );

        return frustum.isVisible(domeBox);
    }
}