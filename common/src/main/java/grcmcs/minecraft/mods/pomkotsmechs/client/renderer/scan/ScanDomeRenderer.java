package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.scan;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public final class ScanDomeRenderer {

    private static final int LATITUDE_SEGMENTS = 24;
    private static final int LONGITUDE_SEGMENTS = 48;

    private ScanDomeRenderer() {
    }

    public static void render(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            float radius,
            float alphaMultiplier
    ) {
        if (radius <= 0.01F
                || alphaMultiplier <= 0.001F) {
            return;
        }

        VertexConsumer consumer =
                bufferSource.getBuffer(
                        ScanRenderTypes.SCAN_DOME
                );

        Matrix4f matrix = poseStack.last().pose();

        float red = 0.10F;
        float green = 1.00F;
        float blue = 0.30F;

        float baseAlpha = 0.40F;
        float alpha = baseAlpha * alphaMultiplier;

        for (int latitude = 0;
             latitude < LATITUDE_SEGMENTS;
             latitude++) {

            double theta1 =
                    -Math.PI / 2.0D
                            + Math.PI * latitude
                            / LATITUDE_SEGMENTS;

            double theta2 =
                    -Math.PI / 2.0D
                            + Math.PI * (latitude + 1)
                            / LATITUDE_SEGMENTS;

            for (int longitude = 0;
                 longitude < LONGITUDE_SEGMENTS;
                 longitude++) {

                double phi1 =
                        Math.PI * 2.0D * longitude
                                / LONGITUDE_SEGMENTS;

                double phi2 =
                        Math.PI * 2.0D * (longitude + 1)
                                / LONGITUDE_SEGMENTS;

                addSphereVertex(
                        consumer,
                        matrix,
                        radius,
                        theta1,
                        phi1,
                        red,
                        green,
                        blue,
                        alpha
                );

                addSphereVertex(
                        consumer,
                        matrix,
                        radius,
                        theta2,
                        phi1,
                        red,
                        green,
                        blue,
                        alpha
                );

                addSphereVertex(
                        consumer,
                        matrix,
                        radius,
                        theta2,
                        phi2,
                        red,
                        green,
                        blue,
                        alpha
                );

                addSphereVertex(
                        consumer,
                        matrix,
                        radius,
                        theta1,
                        phi2,
                        red,
                        green,
                        blue,
                        alpha
                );
            }
        }
    }

    private static void addSphereVertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            float radius,
            double latitude,
            double longitude,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        double cosLatitude = Math.cos(latitude);

        float x = (float) (
                radius
                        * cosLatitude
                        * Math.cos(longitude)
        );

        float y = (float) (
                radius
                        * Math.sin(latitude)
        );

        float z = (float) (
                radius
                        * cosLatitude
                        * Math.sin(longitude)
        );

        consumer.vertex(matrix, x, y, z)
                .color(red, green, blue, alpha)
                .endVertex();
    }
}