package grcmcs.minecraft.mods.pomkotsmechs.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.MissionMarkerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.cutscene.CutsceneCameraEntity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaCameraEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class MissionTargetMarkerHud {
    public static final MissionTargetMarkerHud INSTANCE = new MissionTargetMarkerHud();
    private static final float HEAD_GAP = 2.0F;
    private static final int EDGE_MARGIN = 18;
    private static final float CORE_SIZE = 2.1F;
    private static final float POMKOTS_MONSTER_EXTRA_HEIGHT = 1.5F;
    private static final float POSITION_SMOOTHING = 0.28F;
    private static final Map<UUID, ScreenPoint> LAST_POSITIONS = new HashMap<>();

    private MissionTargetMarkerHud() { }

    public void render(GuiGraphics graphics, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.level == null || mc.player == null) return;

        Camera camera = mc.gameRenderer.getMainCamera();
        if (camera.getEntity() instanceof CutsceneCameraEntity
                || camera.getEntity() instanceof ArenaCameraEntity) return;
        Vec3 cameraPos = camera.getPosition();
        Vec3 forward = new Vec3(camera.getLookVector()).normalize();
        Vec3 up = new Vec3(camera.getUpVector()).normalize();
        Vec3 left = new Vec3(camera.getLeftVector()).normalize();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        double fov = mc.options.fov().get();
        double focal = height * 0.5D / Math.tan(Math.toRadians(fov) * 0.5D);

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 700.0F);
        Set<UUID> visibleMarkers = new HashSet<>();
        for (MissionTargetMarkerManager.Marker marker : MissionTargetMarkerManager.markers()) {
            if (!marker.dimension().equals(mc.level.dimension().location())) continue;
            Entity markerEntity = mc.level.getEntity(marker.entityId());
            if (markerEntity instanceof CutsceneCameraEntity) continue;
            visibleMarkers.add(marker.uuid());
            Vec3 target = markerPosition(mc, marker, partialTick).add(0.0D, markerHeight(mc, marker) + HEAD_GAP, 0.0D);
            Vec3 relative = target.subtract(cameraPos);
            double depth = relative.dot(forward);
            double side = relative.dot(left);
            double vertical = relative.dot(up);

            float desiredX;
            float desiredY;
            boolean edge = depth <= 0.05D;
            if (!edge) {
                desiredX = (float) (width * 0.5D - side * focal / depth);
                desiredY = (float) (height * 0.5D - vertical * focal / depth);
                edge = desiredX < EDGE_MARGIN || desiredX > width - EDGE_MARGIN
                        || desiredY < EDGE_MARGIN || desiredY > height - EDGE_MARGIN;
            } else {
                desiredX = width * 0.5F;
                desiredY = height * 0.5F;
            }

            if (edge) {
                ScreenPoint intersection = edgeIntersection(width, height, -side, -vertical,
                        LAST_POSITIONS.get(marker.uuid()));
                desiredX = intersection.x();
                desiredY = intersection.y();
            }
            ScreenPoint previous = LAST_POSITIONS.get(marker.uuid());
            ScreenPoint point = previous == null
                    ? new ScreenPoint(desiredX, desiredY)
                    : new ScreenPoint(Mth.lerp(POSITION_SMOOTHING, previous.x(), desiredX),
                                      Mth.lerp(POSITION_SMOOTHING, previous.y(), desiredY));
            LAST_POSITIONS.put(marker.uuid(), point);
            float rotation = edge
                    ? (float) Math.atan2(point.y() - height * 0.5F, point.x() - width * 0.5F)
                        - Mth.HALF_PI
                    : 0.0F;
            drawGlowTriangle(graphics.pose().last().pose(), point.x(), point.y(), rotation, marker.hostile());
        }
        LAST_POSITIONS.keySet().retainAll(visibleMarkers);
        graphics.pose().popPose();
    }

    private static ScreenPoint edgeIntersection(int width, int height, double dx, double dy,
                                                ScreenPoint previous) {
        double length = Math.sqrt(dx * dx + dy * dy);
        if (length < 1.0E-4D) {
            if (previous != null) {
                dx = previous.x() - width * 0.5D;
                dy = previous.y() - height * 0.5D;
                length = Math.sqrt(dx * dx + dy * dy);
            }
            if (length < 1.0E-4D) {
                dx = 1.0D;
                dy = 0.0D;
                length = 1.0D;
            }
        }
        dx /= length;
        dy /= length;
        double halfWidth = width * 0.5D - EDGE_MARGIN;
        double halfHeight = height * 0.5D - EDGE_MARGIN;
        double tx = Math.abs(dx) < 1.0E-4D ? Double.POSITIVE_INFINITY : halfWidth / Math.abs(dx);
        double ty = Math.abs(dy) < 1.0E-4D ? Double.POSITIVE_INFINITY : halfHeight / Math.abs(dy);
        double scale = Math.min(tx, ty);
        return new ScreenPoint((float) (width * 0.5D + dx * scale),
                (float) (height * 0.5D + dy * scale));
    }

    private static Vec3 markerPosition(Minecraft mc, MissionTargetMarkerManager.Marker marker, float partialTick) {
        Entity entity = mc.level.getEntity(marker.entityId());
        if (entity != null && entity.getUUID().equals(marker.uuid())) {
            Vec3 position = new Vec3(
                    Mth.lerp(partialTick, entity.xOld, entity.getX()),
                    Mth.lerp(partialTick, entity.yOld, entity.getY()),
                    Mth.lerp(partialTick, entity.zOld, entity.getZ()));
            if (entity instanceof MissionMarkerEntity missionMarker
                    && missionMarker.markerMode() == MissionMarkerEntity.REACHED_AREA) {
                return position.add(
                        (missionMarker.minX() + missionMarker.maxX()) * 0.5D,
                        0.0D,
                        (missionMarker.minZ() + missionMarker.maxZ()) * 0.5D);
            }
            return position;
        }
        return marker.position(partialTick);
    }

    private static float markerHeight(Minecraft mc, MissionTargetMarkerManager.Marker marker) {
        Entity entity = mc.level.getEntity(marker.entityId());
        if (entity != null && entity.getUUID().equals(marker.uuid())) {
            if (entity instanceof MissionMarkerEntity missionMarker) {
                return missionMarker.markerMode() == MissionMarkerEntity.REACHED_AREA
                        ? missionMarker.maxY() : 2.5F;
            }
            if (entity instanceof GenericPomkotsMonster) {
                return entity.getBbHeight() + POMKOTS_MONSTER_EXTRA_HEIGHT;
            }
            return entity.getBbHeight();
        }
        return marker.height();
    }

    private static void drawGlowTriangle(Matrix4f pose, float x, float y, float rotation, boolean hostile) {
        int r = hostile ? 210 : 0;
        int g = hostile ? 4 : 210;
        int b = hostile ? 2 : 105;
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        triangle(pose, x, y, CORE_SIZE * 2.2F, rotation, r, g, b, hostile ? 22 : 42);
        triangle(pose, x, y, CORE_SIZE * 1.55F, rotation, r, g, b, hostile ? 48 : 82);
        // 芯は通常のアルファ合成に戻し、明るい背景でも色が白く抜けないようにする。
        RenderSystem.defaultBlendFunc();
        triangle(pose, x, y, CORE_SIZE, rotation, r, g, b, 255);
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    private static void triangle(Matrix4f pose, float x, float y, float size, float rotation,
                                 int r, int g, int b, int a) {
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        vertex(buffer, pose, x, y, 0.0F, size * 0.5F, rotation, r, g, b, a);
        vertex(buffer, pose, x, y, -size, -size * 0.5F, rotation, r, g, b, a);
        vertex(buffer, pose, x, y, size, -size * 0.5F, rotation, r, g, b, a);
        Tesselator.getInstance().end();
    }

    private static void vertex(BufferBuilder buffer, Matrix4f pose, float centerX, float centerY,
                               float localX, float localY, float rotation,
                               int r, int g, int b, int a) {
        float sin = Mth.sin(rotation);
        float cos = Mth.cos(rotation);
        float x = centerX + localX * cos - localY * sin;
        float y = centerY + localX * sin + localY * cos;
        buffer.vertex(pose, x, y, 0.0F).color(r, g, b, a).endVertex();
    }

    private record ScreenPoint(float x, float y) { }
}
