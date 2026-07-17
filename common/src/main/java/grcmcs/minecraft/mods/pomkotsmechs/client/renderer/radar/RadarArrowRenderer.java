package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.radar;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.PomkotsRadarItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.RadarTarget;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.lang.ref.WeakReference;
import java.util.*;

public class RadarArrowRenderer {
    public static final RenderType WIREFRAME = RenderType.create(
            "radar_arrow_wireframe",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,  // LINESはNORMAL必須
            VertexFormat.Mode.LINES,
            131072, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(4.0))) // 線の太さ
                    .createCompositeState(false)
    );

    public static final RenderType TRIANGLE = RenderType.create(
            "radar_cone",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLES,
            131072, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setCullState(RenderStateShard.CULL)
                    .createCompositeState(false)
    );

    // 矢印の色（RGBA）
    private static final int COLOR_R = 0;
    private static final int COLOR_G = 80;
    private static final int COLOR_B = 255;
    private static final int COLOR_A = 128;

    // 矢印のベースサイズ
    private static final float ARROW_BASE_SIZE = 0.4f;

    // プレイヤーの何ブロック前に表示するか
    private static final float FORWARD_OFFSET = 3.0f;

    public static void onRenderWorld(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            Camera camera,
            float partialTick
    ) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        ItemStack stack = player.getMainHandItem();

        RadarTarget target = null;

        if (stack.getItem() instanceof PomkotsRadarItem) {
            target = PomkotsRadarItem.getActiveTarget(stack).orElse(null);
        } else if (stack.getItem() instanceof PomkotsDatapadItem) {
            target = PomkotsDatapadItem.getActiveTarget(stack).orElse(null);
        }

        if (target == null) return;

        Vec3 camPos    = camera.getPosition();
        Vec3 centerPos = player.getPosition(partialTick);
        Vec3 targetPos = resolveTargetPos(target, mc.level, partialTick);

        if (targetPos == null) {
            int blinkAlpha = (int)(getBlinkAlpha((ClientLevel) player.level(), partialTick) * 200);
            if (blinkAlpha < 10) return;

            Vec3 crossPos = centerPos.add(0, player.getBbHeight() + 1.0, 0);

            poseStack.pushPose();
            poseStack.translate(
                    crossPos.x - camPos.x,
                    crossPos.y - camPos.y,
                    crossPos.z - camPos.z
            );

            // カメラに向かせる（ビルボード）
            poseStack.mulPose(camera.rotation());

            poseStack.scale(0.5f, 0.5f, 0.5f);

            VertexConsumer vc = bufferSource.getBuffer(WIREFRAME);
            renderCross(vc, poseStack.last(), 255, 80, 80, blinkAlpha);

            poseStack.popPose();

        } else {
            float time      = (player.level().getGameTime() + partialTick);

            Vec3 diff      = targetPos.subtract(centerPos);
            double dist    = diff.length();

            if (dist < 1.0) return;

            Vec3 dir = diff.normalize();

            Vec3 playerCenter  = centerPos.add(0, player.getBbHeight() * 0.5, 0);
//            Vec3 playerCenter  = centerPos;

            float bobOffset = (float)(Math.sin(time / 10.0) * 0.5);
            Vec3 arrowWorldPos = playerCenter.add(dir.scale(4 + bobOffset));

            float scale = (float) Mth.clamp(dist / 30.0, 0.5, 2.0) * ARROW_BASE_SIZE;

            poseStack.pushPose();
            poseStack.translate(
                    arrowWorldPos.x - camPos.x,
                    arrowWorldPos.y - camPos.y,
                    arrowWorldPos.z - camPos.z
            );

            alignToDirection(poseStack, dir);

            float rotation = (float)(time / 20.0 * Math.PI); // 1秒で180°
            poseStack.mulPose(new Quaternionf().rotateZ(rotation));

            poseStack.scale(scale, scale, scale);

            VertexConsumer vc = bufferSource.getBuffer(TRIANGLE);
            renderCone(vc, poseStack.last(), COLOR_R, COLOR_G, COLOR_B, COLOR_A);

            poseStack.popPose();
        }

    }

    /**
     * poseStackをdirectionの方向に向ける
     * +Z方向が矢印の先端として扱う
     */
    private static void alignToDirection(PoseStack poseStack, Vec3 direction) {
        // +Z軸をdirectionに向けるクォータニオンを計算
        Vec3 from = new Vec3(0, 0, 1);
        Vec3 axis  = from.cross(direction);
        double axisLen = axis.length();

        if (axisLen < 1e-6) {
            // ほぼ同方向 or 真逆
            if (direction.z < 0) {
                poseStack.mulPose(new Quaternionf().rotateY((float) Math.PI));
            }
            return;
        }

        double angle = Math.acos(Mth.clamp(from.dot(direction), -1.0, 1.0));
        Vec3 normalizedAxis = axis.normalize();
        poseStack.mulPose(new Quaternionf().rotateAxis(
                (float) angle,
                (float) normalizedAxis.x,
                (float) normalizedAxis.y,
                (float) normalizedAxis.z
        ));
    }

    private static float getBlinkAlpha(ClientLevel level, float partialTick) {
        float time = (level.getGameTime() + partialTick) / 10.0f; // 速さ調整
        return (float)(Math.sin(time * Math.PI) * 0.5 + 0.5);    // 0.0〜1.0でサイン波
    }

    private static void renderCross(
            VertexConsumer vc,
            PoseStack.Pose pose,
            int r, int g, int b, int a
    ) {
        float s = 0.5f; // ✕のサイズ

        // ＼ライン
        line(vc, pose, -s,  s, 0,  s, -s, 0,  r, g, b, a);
        // ／ライン
        line(vc, pose,  s,  s, 0, -s, -s, 0,  r, g, b, a);
    }

    private static void line(
            VertexConsumer vc, PoseStack.Pose pose,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            int r, int g, int b, int a
    ) {
        float nx = x2 - x1, ny = y2 - y1, nz = z2 - z1;
        float len = (float) Math.sqrt(nx*nx + ny*ny + nz*nz);
        nx /= len; ny /= len; nz /= len;

        vc.vertex(pose.pose(), x1, y1, z1).color(r, g, b, a).normal(pose.normal(), nx, ny, nz).endVertex();
        vc.vertex(pose.pose(), x2, y2, z2).color(r, g, b, a).normal(pose.normal(), nx, ny, nz).endVertex();
    }

    private static void renderCone(
            VertexConsumer vc,
            PoseStack.Pose pose,
            int r, int g, int b, int a
    ) {
        float tipZ  = 2.5f;
        float baseR = 0.4f;

        float tp  = 0, ty = 0, tz = tipZ;
        float v1x  = (float)(-Math.sqrt(3) / 2 * baseR),  v1y  = -baseR * 0.5f;
        float v2x = (float)( Math.sqrt(3) / 2 * baseR),  v2y = -baseR * 0.5f;
        float v3x  = 0,                                   v3y  =  baseR;

        triShaded(vc, pose,  tp, ty, tz,  v3x, v3y, 0,  v1x, v1y, 0,  r, g, b, a, 0.5F);
        triShaded(vc, pose,  tp, ty, tz,  v2x, v2y, 0,  v3x, v3y, 0,  r, g, b, a, 0.7F);
        triShaded(vc, pose,  tp, ty, tz,  v1x, v1y, 0,  v2x, v2y, 0,  r, g, b, a, 0.9F);
        triShaded(vc, pose,  v1x, v1y, 0,  v3x, v3y, 0, v2x, v2y, 0, r, g, b, a, 0.3F);
    }

    private static void triShaded(
            VertexConsumer vc, PoseStack.Pose pose,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            int r, int g, int b, int a,
            float brightness
    ) {

        r = (int)(r * brightness);
        g = (int)(g * brightness);
        b = (int)(b * brightness);

        vc.vertex(pose.pose(), x1, y1, z1).color(r, g, b, a).endVertex();
        vc.vertex(pose.pose(), x2, y2, z2).color(r, g, b, a).endVertex();
        vc.vertex(pose.pose(), x3, y3, z3).color(r, g, b, a).endVertex();
    }

    private static final Map<UUID, WeakReference<Entity>> entityCache = new HashMap<>();
    private static final Set<UUID> notFoundCache = new HashSet<>(); // ← 見つからなかったUUID

    private static Vec3 resolveTargetPos(RadarTarget target, ClientLevel level, float partialTick) {
        if (target instanceof RadarTarget.CoordTarget c) {
            return Vec3.atCenterOf(c.pos());
        }

        if (!(target instanceof RadarTarget.EntityTarget e)) return null;

        // キャッシュから取得
        Entity cached = Optional.ofNullable(entityCache.get(e.uuid()))
                .map(WeakReference::get)
                .orElse(null);

        if (cached != null && cached.isAlive()) {
            return cached.getPosition(partialTick);
        }

        // 20tickに1回だけ検索（tickCountの剰余で間引き）
        if (notFoundCache.contains(e.uuid()) && level.getGameTime() % 100 != 0) {
            return null;
        }

        // 検索実行
        Entity found = level.getEntitiesOfClass(Entity.class,
                new AABB(level.getSharedSpawnPos()).inflate(999999),
                en -> en.getUUID().equals(e.uuid())
        ).stream().findFirst().orElse(null);

        if (found != null) {
            entityCache.put(e.uuid(), new WeakReference<>(found));
            notFoundCache.remove(e.uuid());
            return found.getPosition(partialTick);
        }

        entityCache.remove(e.uuid());
        notFoundCache.add(e.uuid());
        return null;
    }
}

