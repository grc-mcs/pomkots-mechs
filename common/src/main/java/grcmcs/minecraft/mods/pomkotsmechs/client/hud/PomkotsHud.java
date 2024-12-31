package grcmcs.minecraft.mods.pomkotsmechs.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.event.events.client.ClientGuiEvent;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PomkotsHud implements ClientGuiEvent.RenderHud {
    Minecraft mc = Minecraft.getInstance();

    private static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    private float curHealth = 0;
    private float prevHealth = 0;

    private int curEN = 0;
    private int prevEN = 0;

    private int curCTRightArm = 0;
    private int prevCTRightArm = 0;
    private int maxCTRightArm = 0;

    private int curCTLeftArm = 0;
    private int prevCTLeftArm = 0;
    private int maxCTLeftArm = 0;

    private int curCTRightShoulder = 0;
    private int prevCTRightShoulder = 0;
    private int maxCTRightShoulder = 0;

    private int curCTLeftShoulder = 0;
    private int prevCTLeftShoulder = 0;
    private int maxCTLeftShoulder = 0;

    private int offsetY = 0;

    public void renderHud(GuiGraphics guiGraphics, float tickDelta) {
        LocalPlayer pl = mc.player;

        if (pl != null) {
            if (pl.getVehicle() instanceof PomkotsVehicleBase protobot) {
                offsetY = mc.getWindow().getGuiScaledHeight() - 30;

                if (protobot.isMainMode()) {
                    renderHudBattle(protobot, guiGraphics, tickDelta);
                } else {
                    renderHudNormal(protobot, guiGraphics, tickDelta);
                }

            }
        }
    }

    private void renderHudNormal(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
        updateValues(protobot);
        renderCrossHair(guiGraphics, tickDelta);
        renderHealthBar(protobot, guiGraphics, tickDelta);
        renderFuelBar(protobot, guiGraphics, tickDelta);
        renderCooldowns(protobot, guiGraphics, tickDelta);
    }

    private static final int FG_COLOR = 0x990086C9;
    private static final int BG_COLOR = 0x55555555;

    private void renderHudBattle(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
        updateValues(protobot);
        renderCrossHair(guiGraphics, tickDelta);
        renderHealthBar(protobot, guiGraphics, tickDelta);
        renderFuelBar(protobot, guiGraphics, tickDelta);
        renderCooldowns(protobot, guiGraphics, tickDelta);
    }

    private static final ResourceLocation CROSSHAIR_TEXTURE = PomkotsMechs.id("textures/crosshair/crosshair0.png");

    private void renderCrossHair(GuiGraphics guiGraphics, float tickDelta) {
        // テクスチャの幅と高さ（クロスヘアの画像サイズ）
        int textureWidth = 32;  // クロスヘアの幅
        int textureHeight = 32; // クロスヘアの高さ

        // 画面の幅と高さを取得
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 画面中央の位置を計算
        int startX = (screenWidth / 2) - (textureWidth / 2);
        int startY = (screenHeight / 2) - (textureHeight / 2);

        // クロスヘアのテクスチャをバインド
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc(); // デフォルトのブレンド関数を設定

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.6f); // R, G, B, A (Aが透明度)

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, CROSSHAIR_TEXTURE);

        // `blit`メソッドでテクスチャを画面中央に描画
        guiGraphics.blit(CROSSHAIR_TEXTURE, startX, startY, 0, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // R, G, B, A (Aが透明度)

    }

    private void updateValues(PomkotsVehicleBase vehicle) {
        prevHealth = curHealth;
        curHealth = vehicle.getHealth();

        prevEN = curEN;
        curEN = vehicle.getEnergy();

        Action rArm;
        Action lArm;
        Action rShoulder;
        Action lShoulder;

        if (vehicle.isMainMode()) {
            rArm = vehicle.actionController.getActionFromType(ActionController.ActionType.R_ARM_MAIN);
            lArm = vehicle.actionController.getActionFromType(ActionController.ActionType.L_ARM_MAIN);
            rShoulder = vehicle.actionController.getActionFromType(ActionController.ActionType.R_SHL_MAIN);
            lShoulder = vehicle.actionController.getActionFromType(ActionController.ActionType.L_SHL_MAIN);
        } else {
            rArm = vehicle.actionController.getActionFromType(ActionController.ActionType.R_ARM_SUB);
            lArm = vehicle.actionController.getActionFromType(ActionController.ActionType.L_ARM_SUB);
            rShoulder = vehicle.actionController.getActionFromType(ActionController.ActionType.R_SHL_SUB);
            lShoulder = vehicle.actionController.getActionFromType(ActionController.ActionType.L_SHL_SUB);

        }

        if (rArm != null ) {
            prevCTRightArm = curCTRightArm;
            curCTRightArm = rArm.currentCoolTime;
            maxCTRightArm = rArm.maxChargeTime + 1;

        } else {
            prevCTRightArm = curCTRightArm = maxCTRightArm = 1;
        }

        if (lArm != null ) {
            prevCTLeftArm = curCTLeftArm;
            curCTLeftArm = lArm.currentCoolTime;
            maxCTLeftArm = lArm.maxCoolTime;

        } else {
            prevCTLeftArm = curCTLeftArm = maxCTLeftArm = 1;
        }

        if (rShoulder != null ) {
            prevCTRightShoulder = curCTRightShoulder;
            curCTRightShoulder = rShoulder.currentCoolTime;
            maxCTRightShoulder = rShoulder.maxCoolTime;

        } else {
            prevCTRightShoulder = curCTRightShoulder = maxCTRightShoulder = 1;
        }


        if (lShoulder != null ) {
            prevCTLeftShoulder = curCTLeftShoulder;
            curCTLeftShoulder = lShoulder.currentCoolTime;
            maxCTLeftShoulder = lShoulder.maxCoolTime;

        } else {
            prevCTLeftShoulder = curCTLeftShoulder = maxCTLeftShoulder = 1;
        }
    }

    // エンティティの体力バーを描画するメソッド
    private void renderHealthBar(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
        // 画面の幅と高さを取得
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 体力バーの位置とサイズ
        int width = 120;
        int height = 3;

        // 画面中央の位置を計算
        int x = (screenWidth / 2) - (width / 2);
        int y = screenHeight - 10;

        if (protobot.shouldRenderDefaultHud("renderHotbar")) {
            y -= 20;
        }

        int maxHealth = (int) protobot.getMaxHealth();

        // 背景バー
        guiGraphics.fill(x, y, x + width, y + height, BG_COLOR);

        // 現在の体力の割合を計算
        int healthWidthCur = (int) (width * Mth.lerp(tickDelta, (float) curHealth / maxHealth, (float) curHealth / maxHealth));
        int healthWidthPrev = (int) (width * Mth.lerp(tickDelta, (float) prevHealth / maxHealth, (float) prevHealth / maxHealth));

        // 前景バー
        guiGraphics.fill(x, y, x + Mth.lerpInt(tickDelta, healthWidthPrev, healthWidthCur), y + height, FG_COLOR);
    }

    // 燃料ゲージの描画
    private void renderFuelBar(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
        // 画面の幅と高さを取得
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int maxFuel = 100; // 最大燃料

        int width = 20;
        int height = 2;

        // 画面中央の位置を計算
        int x = (screenWidth / 2) - (width / 2);
        int y = (screenHeight / 2) + 19;

        // 背景バー
        guiGraphics.fill(x, y, x + width, y + height, BG_COLOR);

        // 燃料の割合を計算
        float enLerp = Mth.lerp(tickDelta, prevEN, curEN);
        float fuelWidth = width * (enLerp / maxFuel);

        // 前景バー
        guiGraphics.fill((int)(x + ((float)width - fuelWidth)/2), y, (int)(x + ((float)width - fuelWidth)/2 + fuelWidth), y + height, FG_COLOR);
    }

    private void renderCooldowns(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {

        int offx = mc.getWindow().getGuiScaledWidth()/2;
        int offy = mc.getWindow().getGuiScaledHeight()/2;

        renderCooldown(prevCTRightArm, curCTRightArm, maxCTRightArm, offx + 19, offy - 9, false, guiGraphics, tickDelta);
        renderCooldown(prevCTLeftArm, curCTLeftArm, maxCTLeftArm, offx - 21, offy - 9, true, guiGraphics, tickDelta);
        renderCooldown(prevCTRightShoulder, curCTRightShoulder, maxCTRightShoulder, offx + 19, offy + 1, false, guiGraphics, tickDelta);
        renderCooldown(prevCTLeftShoulder, curCTLeftShoulder, maxCTLeftShoulder, offx - 21, offy + 1, true, guiGraphics, tickDelta);

//
//        renderCooldown(prevCTGat, curCTGat, maxCTGat, offx + 17, offy - 3, false, guiGraphics, tickDelta);
//        renderCooldown(prevCTPile, curCTPile, maxCTPile, offx - 25, offy - 3, true, guiGraphics, tickDelta);
//        renderCooldown(prevCTMissile, curCTMissile, maxCTMissile, offx + 17, offy + 1, false, guiGraphics, tickDelta);
//        renderCooldown(prevCTGrenade, curCTGrenade, maxCTGrenade, offx - 25, offy + 1, true, guiGraphics, tickDelta);
    }

    private void renderCooldown(int prevCT, int curCT, int maxCT, int x, int y, boolean reverse, GuiGraphics guiGraphics, float tickDelta) {
        // クールダウンブロックのサイズと配置位置
        int blockWidth = 2; // クールダウンブロックの幅
        int blockHeight = 8; // クールダウンブロックの高さ


        // クールダウンが残っている場合、その割合に応じてゲージを描画
        float ctLerp = Mth.lerp(tickDelta, prevCT, curCT);
        int cooldownHeight = (int) (blockHeight * (((float)maxCT - ctLerp) / (float)maxCT)); // 100を最大値とした割合

        // 背景バー（黒）
        guiGraphics.fill(x, y, x + blockWidth, y + blockHeight, BG_COLOR);

        // クールダウンゲージ（青）
        guiGraphics.fill(x, y + blockHeight - cooldownHeight, x + blockWidth, y + blockHeight, FG_COLOR);

    }


    private void renderCooldown2(int prevCT, int curCT, int maxCT, int x, int y, boolean reverse, GuiGraphics guiGraphics, float tickDelta) {
        // クールダウンブロックのサイズと配置位置
        int blockWidth = 8; // クールダウンブロックの幅
        int blockHeight = 2; // クールダウンブロックの高さ


        // クールダウンが残っている場合、その割合に応じてゲージを描画
        float ctLerp = Mth.lerp(tickDelta, prevCT, curCT);
        int cooldownWidth = (int) (blockWidth * (((float)maxCT - ctLerp) / (float)maxCT)); // 100を最大値とした割合

        // 背景バー（黒）
        guiGraphics.fill(x, y, x + blockWidth, y + blockHeight, BG_COLOR);

        // クールダウンゲージ（青）
        guiGraphics.fill(x, y, x + cooldownWidth, y + blockHeight, FG_COLOR);

    }

    private void drawRectangleOutline(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        // 上辺
        guiGraphics.fill(x, y, x + width, y + 1, color);
        // 下辺
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color);
        // 左辺
        guiGraphics.fill(x, y, x + 1, y + height, color);
        // 右辺
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color);
    }
//
//    // 残弾数の表示
//    private void renderAmmoCount(MatrixStack matrixStack, PlayerEntity player) {
//        int ammoCount = getAmmoFromPlayerInventory(player);
//
//        // 残弾数をHUDに表示
//        String ammoText = "Ammo: " + ammoCount;
//        MinecraftClient.getInstance().textRenderer.draw(matrixStack, ammoText, 10, 80, 0xFFFFFF);
//    }
//
//    // プレイヤーのインベントリから弾薬数を取得するメソッド
//    private int getAmmoFromPlayerInventory(PlayerEntity player) {
//        // プレイヤーのインベントリから弾薬アイテムをカウント
//        // （弾薬のアイテムを特定するロジックをここに追加）
//        return 10; // 仮の値、実際はアイテムスタックから取得する
//    }

    private void renderHealthBars(GuiGraphics guiGraphics, float tickDelta) {
        PoseStack poseStack = guiGraphics.pose();
//        poseStack.pushPose();
        try {
            Camera camera = mc.gameRenderer.getMainCamera();

            // すべてのエンティティをループ
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof Monster) {
                    Monster monster = (Monster) entity;
                    // モンスターの体力バーをレンダリング
                    renderHealthBar(monster, guiGraphics, tickDelta);
                }
            }

        } finally {
//            poseStack.popPose();
        }
    }

    private void renderHealthBar(LivingEntity entity, GuiGraphics guiGraphics, float tickDelta) {
        Minecraft client = Minecraft.getInstance();
        Vec3 cameraPos = client.gameRenderer.getMainCamera().getPosition();
        double distance = entity.distanceToSqr(cameraPos);

        // 遠すぎる場合はヘルスバーを描画しない
        if (distance > 64 * 64) {
            return;
        }

        // モンスターの位置を取得
        double x = entity.xOld + (entity.getX() - entity.xOld) * tickDelta;
        double y = entity.yOld + (entity.getY() - entity.yOld) * tickDelta + entity.getBbHeight() + 0.5;
        double z = entity.zOld + (entity.getZ() - entity.zOld) * tickDelta;

        // カメラ方向へのオフセット
        Vec3 projectedPos = new Vec3(x, y, z).subtract(cameraPos);

        // 画面上の2D座標に変換
        Vector4f pos = new Vector4f((float) projectedPos.x, (float) projectedPos.y, (float) projectedPos.z, 1.0F);
        pos = client.gameRenderer.getProjectionMatrix(tickDelta).transform(pos);

        // ヘルスバーの描画位置を決定
        int screenX = (int)((client.getWindow().getGuiScaledWidth() / 2) + (pos.x() / pos.w() * client.getWindow().getGuiScaledWidth() / 2));
        int screenY = (int)((client.getWindow().getGuiScaledHeight() / 2) - (pos.y() / pos.w() * client.getWindow().getGuiScaledHeight() / 2));

        // ヘルスバーの大きさと色を設定
        int barWidth = 40;
        int barHeight = 6;
        int maxHealth = (int) entity.getMaxHealth();
        int currentHealth = (int) entity.getHealth();
        int healthBarWidth = (int) ((currentHealth / (float) maxHealth) * barWidth);

        // 背景の描画（グレー）
        guiGraphics.fill(screenX - barWidth / 2, screenY, screenX + barWidth / 2, screenY + barHeight, 0x66000000);

        // ヘルスバーの描画（緑）
        guiGraphics.fill(screenX - barWidth / 2, screenY, screenX - barWidth / 2 + healthBarWidth, screenY + barHeight, 0xFF00FF00);


        LOGGER.info("" + screenX + ":" + screenY);
    }

    private void renderHealthBar(Monster monster, Camera camera, GuiGraphics guiGraphics, float tickDelta) {
        Vec3 entityPos = getEntityRenderPosition(monster, tickDelta); // モンスターの位置を取得
        Vec3 cameraPos = camera.getPosition(); // カメラの位置を取得

        double x = entityPos.x - cameraPos.x;
        double y = entityPos.y - cameraPos.y + monster.getBbHeight() + 0.5; // モンスターの頭の上
        double z = entityPos.z - cameraPos.z;

        // エンティティの位置をスクリーン座標に変換
        int screenX = (int) (mc.getWindow().getGuiScaledWidth() / 2.0 + x * 100);
        int screenY = (int) (mc.getWindow().getGuiScaledHeight() / 2.0 - y * 100 - z * 50);

        // 体力バーの描画
        float health = monster.getHealth();
        float maxHealth = monster.getMaxHealth();
        float healthPercentage = health / maxHealth;

        int barWidth = 50;
        int barHeight = 5;

        // 赤い背景の描画


        fill(guiGraphics, screenX - barWidth / 2, screenY, screenX + barWidth / 2, screenY + barHeight, 0xFFFF0000);

        // 緑の体力部分の描画
        fill(guiGraphics, screenX - barWidth / 2, screenY, screenX - barWidth / 2 + (int)(barWidth * healthPercentage), screenY + barHeight, 0xFF00FF00);
    }

    private Vec3 getEntityRenderPosition(LivingEntity entity, float tickDelta) {
        double x = entity.xOld + (entity.getX() - entity.xOld) * tickDelta;
        double y = entity.yOld + (entity.getY() - entity.yOld) * tickDelta;
        double z = entity.zOld + (entity.getZ() - entity.zOld) * tickDelta;
        return new Vec3(x, y, z);
    }

    private void fill(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        // ヘルスバーの矩形を描画するためのコード
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        guiGraphics.fill(x1, y1, x2, y2, color);
        LOGGER.info("" + x1 + ":" + y1+ ":" + x2+ ":" + y2);

    }
}