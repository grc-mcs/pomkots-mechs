package grcmcs.minecraft.mods.pomkotsmechs.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.radio.RadioPortraitType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import com.mojang.math.Axis;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RadioCommunicationHud {
    public static final RadioCommunicationHud INSTANCE = new RadioCommunicationHud();
    private static final int MAX_WIDTH = 420;
    private static final int PANEL_HEIGHT = 84;
    private static final int PORTRAIT_SIZE = 64;
    private static final int BOTTOM_MARGIN = 10;

    // -------------------------------------------------------------------------
    // 表示色の調整パラメーター
    // RGBは0xRRGGBB、不透明度は0（完全透明）～255（完全不透明）で指定する。
    // -------------------------------------------------------------------------
    private static final int PANEL_BACKGROUND_RGB = 0x08151D;
    private static final int PANEL_BACKGROUND_ALPHA = 190;

    private static final int PANEL_BORDER_RGB = 0xEAF4FF;
    private static final int PANEL_BORDER_ALPHA = 0;

    private static final int PORTRAIT_BACKGROUND_RGB = 0x05090C;
    private static final int PORTRAIT_BACKGROUND_ALPHA = 130;

    private static final int OPENING_EDGE_RGB = 0xEAF4FF;
    private static final int OPENING_EDGE_ALPHA = 255;

    private PlayerModel<LivingEntity> playerModel;
    private PlayerModel<LivingEntity> slimPlayerModel;
    private final Map<net.minecraft.resources.ResourceLocation, LivingEntity> portraitEntities = new HashMap<>();

    private RadioCommunicationHud() {
    }

    public void renderHud(GuiGraphics graphics, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.player == null) return;
        RadioCommunicationManager.Snapshot snapshot = RadioCommunicationManager.snapshot(partialTick);
        if (snapshot == null || snapshot.flicker()) return;

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 1000.0F);
        RenderSystem.disableDepthTest();

        int panelWidth = Math.min(MAX_WIDTH, graphics.guiWidth() - 24);
        int x = (graphics.guiWidth() - panelWidth) / 2;
        int y = graphics.guiHeight() - BOTTOM_MARGIN - PANEL_HEIGHT;
        float openProgress = smoothStep(snapshot.panelAlpha());
        int openHeight = Math.max(1, Math.round(PANEL_HEIGHT * openProgress));
        int clipTop = y + (PANEL_HEIGHT - openHeight) / 2;
        int clipBottom = clipTop + openHeight;
        int backgroundAlpha = scaledAlpha(PANEL_BACKGROUND_ALPHA, snapshot.panelAlpha());
        int borderAlpha = scaledAlpha(PANEL_BORDER_ALPHA, snapshot.panelAlpha());

        graphics.enableScissor(x, clipTop, x + panelWidth, clipBottom);
        graphics.fill(x, y, x + panelWidth, y + PANEL_HEIGHT,
                backgroundAlpha << 24 | PANEL_BACKGROUND_RGB);
        graphics.fill(x, y, x + panelWidth, y + 1, borderAlpha << 24 | PANEL_BORDER_RGB);
        graphics.fill(x, y + PANEL_HEIGHT - 1, x + panelWidth, y + PANEL_HEIGHT,
                borderAlpha << 24 | PANEL_BORDER_RGB);
        graphics.fill(x, y + 1, x + 1, y + PANEL_HEIGHT - 1,
                borderAlpha << 24 | PANEL_BORDER_RGB);
        graphics.fill(x + panelWidth - 1, y + 1, x + panelWidth, y + PANEL_HEIGHT - 1,
                borderAlpha << 24 | PANEL_BORDER_RGB);

        int portraitX = x + 10;
        int portraitY = y + 10;
        drawPortrait(graphics, snapshot, portraitX, portraitY, partialTick);
        int textX = portraitX + PORTRAIT_SIZE + 10;
        drawText(graphics, snapshot, textX, portraitY, Math.max(1, x + panelWidth - 10 - textX));
        graphics.disableScissor();

        int edgeAlpha = scaledAlpha(OPENING_EDGE_ALPHA, 1.0F - openProgress);
        graphics.fill(x, clipTop, x + panelWidth, Math.min(clipTop + 1, clipBottom),
                edgeAlpha << 24 | OPENING_EDGE_RGB);
        if (clipBottom - 1 > clipTop) {
            graphics.fill(x, clipBottom - 1, x + panelWidth, clipBottom,
                    edgeAlpha << 24 | OPENING_EDGE_RGB);
        }

        RenderSystem.enableDepthTest();
        graphics.pose().popPose();
    }

    private void drawPortrait(
            GuiGraphics graphics,
            RadioCommunicationManager.Snapshot snapshot,
            int x,
            int y,
            float partialTick
    ) {
        int backgroundAlpha = scaledAlpha(PORTRAIT_BACKGROUND_ALPHA, snapshot.panelAlpha());
        graphics.fill(x, y, x + PORTRAIT_SIZE, y + PORTRAIT_SIZE,
                backgroundAlpha << 24 | PORTRAIT_BACKGROUND_RGB);

        // Keep enlarged player/GeckoLib models inside the 64 x 64 portrait.
        // GuiGraphics maintains a scissor stack, so this is nested inside the
        // radio panel's existing opening-animation scissor.
        graphics.enableScissor(x, y, x + PORTRAIT_SIZE, y + PORTRAIT_SIZE);
        try {
            if (snapshot.message().portrait().type() == RadioPortraitType.PLAYER_MODEL) {
                drawPlayerModelPortrait(graphics, snapshot, x, y);
                return;
            }
            if (snapshot.message().portrait().type() == RadioPortraitType.ENTITY_MODEL) {
                drawEntityModelPortrait(graphics, snapshot, x, y, partialTick);
                return;
            }

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, snapshot.panelAlpha());
            graphics.blit(snapshot.message().portrait().texture(), x, y, 0.0F, 0.0F,
                    PORTRAIT_SIZE, PORTRAIT_SIZE, PORTRAIT_SIZE, PORTRAIT_SIZE);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
        } finally {
            graphics.disableScissor();
        }
    }

    private void drawPlayerModelPortrait(
            GuiGraphics graphics,
            RadioCommunicationManager.Snapshot snapshot,
            int x,
            int y
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        PlayerModel<LivingEntity> model = getPlayerModel(
                minecraft, snapshot.message().portrait().slimPlayerModel());
        float alpha = Mth.clamp(snapshot.panelAlpha(), 0.0F, 1.0F);
        float modelScale = 36.0F * Math.max(0.1F, snapshot.message().portrait().scale());

        resetPlayerModel(model);
        model.head.xRot = snapshot.message().portrait().pitch() * Mth.DEG_TO_RAD;
        model.hat.copyFrom(model.head);
        model.leftArm.zRot = -0.08F;
        model.rightArm.zRot = 0.08F;
        model.leftLeg.visible = false;
        model.rightLeg.visible = false;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x + PORTRAIT_SIZE / 2.0F, y + PORTRAIT_SIZE / 2.0F, 100.0F);
        pose.scale(modelScale, modelScale, -modelScale);
        pose.mulPose(Axis.YP.rotationDegrees(snapshot.message().portrait().yaw()));

        Lighting.setupForEntityInInventory();
        RenderSystem.setShaderLights(
                new Vector3f(-0.35F, -0.45F, 1.0F).normalize(),
                new Vector3f(0.45F, 0.15F, 0.75F).normalize()
        );
        RenderSystem.enableDepthTest();
        VertexConsumer consumer = graphics.bufferSource().getBuffer(
                RenderType.entityTranslucent(snapshot.message().portrait().texture())
        );
        model.renderToBuffer(
                pose,
                consumer,
                15728880,
                OverlayTexture.NO_OVERLAY,
                1.0F,
                1.0F,
                1.0F,
                alpha
        );
        graphics.flush();
        RenderSystem.disableDepthTest();
        Lighting.setupFor3DItems();
        pose.popPose();
    }

    private void drawEntityModelPortrait(
            GuiGraphics graphics,
            RadioCommunicationManager.Snapshot snapshot,
            int x,
            int y,
            float partialTick
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;

        var portrait = snapshot.message().portrait();
        LivingEntity entity = portraitEntities.get(portrait.entityType());
        if (entity == null || entity.level() != minecraft.level) {
            Entity created = BuiltInRegistries.ENTITY_TYPE.getOptional(portrait.entityType())
                    .map(type -> type.create(minecraft.level))
                    .orElse(null);
            if (!(created instanceof LivingEntity living)) return;
            entity = living;
            portraitEntities.put(portrait.entityType(), entity);
        }

        // The preview entity is never added to the level, so advance its animation
        // clock from the real client player.
        entity.tickCount = minecraft.player.tickCount;
        float framingScale = portrait.bustUp() ? 1.5F : 1.0F;
        float scale = 36.0F * framingScale * Math.max(0.01F, portrait.scale());
        int renderX = Math.round(x + PORTRAIT_SIZE / 2.0F + portrait.offsetX());
        float baseY = portrait.bustUp()
                ? y + PORTRAIT_SIZE + 36.0F
                : y + PORTRAIT_SIZE - 3.0F;
        int renderY = Math.round(baseY + portrait.offsetY());
        renderEntityPortrait(
                graphics,
                renderX,
                renderY,
                scale,
                portrait.yaw(),
                portrait.pitch(),
                partialTick,
                entity
        );
    }

    private static void renderEntityPortrait(
            GuiGraphics graphics,
            int x,
            int y,
            float scale,
            float yaw,
            float pitch,
            float partialTick,
            LivingEntity entity
    ) {
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 100.0F);
        pose.mulPoseMatrix(new Matrix4f().scaling(scale, scale, -scale));
        Quaternionf rotation = new Quaternionf()
                .rotateZ((float) Math.PI)
                .rotateX(pitch * Mth.DEG_TO_RAD);
        pose.mulPose(rotation);

        float oldBodyYaw = entity.yBodyRot;
        float oldBodyYawO = entity.yBodyRotO;
        float oldYaw = entity.getYRot();
        float oldYawO = entity.yRotO;
        float oldPitch = entity.getXRot();
        float oldPitchO = entity.xRotO;
        float oldHeadYaw = entity.yHeadRot;
        float oldHeadYawO = entity.yHeadRotO;
        float portraitYaw = 180.0F + yaw;
        entity.yBodyRot = portraitYaw;
        entity.yBodyRotO = portraitYaw;
        entity.setYRot(portraitYaw);
        entity.yRotO = portraitYaw;
        entity.setXRot(-pitch);
        entity.xRotO = -pitch;
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();

        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        dispatcher.setRenderShadow(false);
        RenderSystem.runAsFancy(() -> dispatcher.render(
                entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTick,
                pose, graphics.bufferSource(), 15728880));
        graphics.flush();
        dispatcher.setRenderShadow(true);
        Lighting.setupFor3DItems();

        entity.yBodyRot = oldBodyYaw;
        entity.yBodyRotO = oldBodyYawO;
        entity.setYRot(oldYaw);
        entity.yRotO = oldYawO;
        entity.setXRot(oldPitch);
        entity.xRotO = oldPitchO;
        entity.yHeadRot = oldHeadYaw;
        entity.yHeadRotO = oldHeadYawO;
        pose.popPose();
    }

    private PlayerModel<LivingEntity> getPlayerModel(Minecraft minecraft, boolean slim) {
        if (slim) {
            if (slimPlayerModel == null) {
                slimPlayerModel = new PlayerModel<>(
                        minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM), true);
            }
            return slimPlayerModel;
        }
        if (playerModel == null) {
            playerModel = new PlayerModel<>(
                    minecraft.getEntityModels().bakeLayer(ModelLayers.PLAYER), false);
        }
        return playerModel;
    }

    private static void resetPlayerModel(PlayerModel<LivingEntity> model) {
        ModelPart[] parts = {
                model.head, model.hat, model.body,
                model.leftArm, model.rightArm, model.leftLeg, model.rightLeg,
                model.jacket, model.leftSleeve, model.rightSleeve,
                model.leftPants, model.rightPants
        };
        for (ModelPart part : parts) {
            part.resetPose();
        }
        model.setAllVisible(true);
        model.crouching = false;
        model.young = false;
        model.attackTime = 0.0F;
    }

    private void drawText(GuiGraphics graphics, RadioCommunicationManager.Snapshot snapshot,
                          int x, int y, int width) {
        Font font = Minecraft.getInstance().font;
        int speakerAlpha = Mth.clamp(Math.round(snapshot.panelAlpha() * 255.0F), 0, 255);
        int textAlpha = Mth.clamp(Math.round(snapshot.textAlpha() * 255.0F), 0, 255);
        if (speakerAlpha < 4 || textAlpha < 4) return;

        graphics.drawString(font, snapshot.message().speaker(), x, y,
                speakerAlpha << 24 | 0x55DDF5, false);
        List<FormattedCharSequence> lines = font.split(snapshot.text().text(), width);
        for (int i = 0; i < lines.size() && i < 5; i++) {
            graphics.drawString(font, lines.get(i), x, y + 16 + i * 10,
                    textAlpha << 24 | 0xFFFFFF, false);
        }
    }

    private static float smoothStep(float progress) {
        float clamped = Mth.clamp(progress, 0.0F, 1.0F);
        return clamped * clamped * (3.0F - 2.0F * clamped);
    }

    private static int scaledAlpha(int alpha, float multiplier) {
        return Mth.clamp(Math.round(alpha * Mth.clamp(multiplier, 0.0F, 1.0F)), 0, 255);
    }
}
