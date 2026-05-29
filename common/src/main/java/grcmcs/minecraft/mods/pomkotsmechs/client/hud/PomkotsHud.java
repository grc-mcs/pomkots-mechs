package grcmcs.minecraft.mods.pomkotsmechs.client.hud;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.event.events.client.ClientGuiEvent;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.TargetLocker;
import grcmcs.minecraft.mods.pomkotsmechs.client.misc.ClientHudShake;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.BasePartsItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.PomkotsCustomThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.turret.Pmvt01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PomkotsHud implements ClientGuiEvent.RenderHud {
    private static final int FG_COLOR = 0x990086C9;
    private static final int BG_COLOR = 0x55555555;

    private static final ResourceLocation CROSSHAIR_TEXTURE = PomkotsMechs.id("textures/crosshair/crosshair0.png");
    private static final ResourceLocation TARGET_LOCK_TEXTURE = PomkotsMechs.id("textures/crosshair/crosshair2.png");
    private static final ResourceLocation TARGET_LOCK_HARD_TEXTURE = PomkotsMechs.id("textures/crosshair/custom/target_hard.png");
    private static final ResourceLocation TARGET_LOCK_SOFT_TEXTURE = PomkotsMechs.id("textures/crosshair/custom/target_soft.png");
    private static final ResourceLocation TARGET_LOCK_SOFT_TEXTURE2 = PomkotsMechs.id("textures/crosshair/custom/target_icon.png");
    private static final ResourceLocation TARGET_LOCK_MULTI_TEXTURE = PomkotsMechs.id("textures/crosshair/custom/target_multi.png");
    private static final ResourceLocation REPAIR_KIT_ICON = PomkotsMechs.id("textures/crosshair/custom/repair_icon.png");

    protected Minecraft mc = Minecraft.getInstance();

    private static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    private float curHealth = 0;
    private float prevHealth = 0;

    private int curEN = 0;
    private int prevEN = 0;
    private int maxEN = 0;

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


    private int prevTick;

    public void renderHud(GuiGraphics guiGraphics, float tickDelta) {
        LocalPlayer pl = mc.player;

        if (pl != null) {
            if (pl.getVehicle() instanceof PomkotsVehicleBase mech) {
                if (prevTick != pl.tickCount) {
                    updateValues(mech);
                }

                renderLockOnMarks(mech, guiGraphics, tickDelta);

                if (mech instanceof Pmvc01Entity pmvc01) {
                    renderCustomMechHud(pmvc01, guiGraphics, tickDelta);
                } else if (mech instanceof Pmvt01Entity tr) {
                    renderTurretHud(tr, guiGraphics, tickDelta);
                } else {
                    if (mech.isMainMode()) {
                        renderHudBattle(mech, guiGraphics, tickDelta);
                    } else {
                        renderHudNormal(mech, guiGraphics, tickDelta);
                    }
                }

                prevTick = pl.tickCount;
            }
        }
    }

    public void renderLockOnMarks(PomkotsVehicleBase mech, GuiGraphics guiGraphics, float tickDelta) {
        TargetLocker locker = TargetLocker.getInstance();

        if (locker.getHardLockTarget() != null) {
            renderLockOnMark(mech, locker.getHardLockTarget(), TARGET_LOCK_HARD_TEXTURE, guiGraphics, tickDelta, true);
        } else if (locker.getSoftLockTarget() != null) {
            renderLockOnMark(mech, locker.getSoftLockTarget(), TARGET_LOCK_SOFT_TEXTURE, guiGraphics, tickDelta, true);
        }

        renderMultiLockOnMarks(mech, locker.targetMulti, guiGraphics, tickDelta);
        renderMultiLockOnMarks(mech, locker.targetMultiRA, guiGraphics, tickDelta);
        renderMultiLockOnMarks(mech, locker.targetMultiLA, guiGraphics, tickDelta);
        renderMultiLockOnMarks(mech, locker.targetMultiRS, guiGraphics, tickDelta);
        renderMultiLockOnMarks(mech, locker.targetMultiLS, guiGraphics, tickDelta);
    }

    private static boolean shouldRenderDistanceOnTarget() {
        TargetLocker locker = TargetLocker.getInstance();
        return locker.getSoftLockTarget() != null || locker.getHardLockTarget() != null;
    }

    private void renderMultiLockOnMarks(PomkotsVehicleBase mech,Map<Integer, Entity> multiLock, GuiGraphics guiGraphics, float tickDelta) {
        if (!multiLock.isEmpty()) {
            for (var entry: multiLock.entrySet()) {
                renderLockOnMark(mech, entry.getValue(), TARGET_LOCK_MULTI_TEXTURE, guiGraphics, tickDelta, false);
            }
        }
    }

    private void renderLockOnMark(PomkotsVehicleBase mech, Entity targetEntity, ResourceLocation texture, GuiGraphics guiGraphics, float tickDelta, boolean renderDistance) {
        Vector3f screenPos = projectEntityToScreen(targetEntity, tickDelta);
        if (screenPos == null) return;

        int screenX = (int) screenPos.x();
        int screenY = (int) screenPos.y();

        if (renderDistance && mech instanceof Pmvc01Entity pmvc01) {
            renderDistance(pmvc01, screenX, screenY, new EntityHitResult(targetEntity), guiGraphics, tickDelta, Minecraft.getInstance());
        }

        int size = 16;
        int sizeSub = 10;

        guiGraphics.blit(texture, screenX - size / 2, screenY - size / 2, 0, 0, size, size, size, size);
        guiGraphics.blit(TARGET_LOCK_SOFT_TEXTURE2, screenX - sizeSub / 2, screenY - sizeSub * 2, sizeSub, sizeSub, 0, 0, 15, 16, 15, 16);
    }

    private Vector3f projectEntityToScreen(Entity entity, float tickDelta) {
        Camera camera = mc.gameRenderer.getMainCamera();

        Vec3 entityPos = entity.position().add(0, entity.getBbHeight() / 2, 0);
        Vec3 camPos = camera.getPosition();
        Quaternionf cameraRotation = camera.rotation();

        double x = Mth.lerp(tickDelta, entity.xOld, entity.getX()) - camPos.x;
        double y = Mth.lerp(tickDelta, entity.yOld + entity.getBbHeight() / 2, entity.getY() + entity.getBbHeight() / 2) - camPos.y;
        double z = Mth.lerp(tickDelta, entity.zOld, entity.getZ()) - camPos.z;

        Vec3 relativePos = new Vec3(x, y, z);
//        Vec3 relativePos = entityPos.subtract(camPos);

        Quaternionf q = new Quaternionf();
        Vector3f transformed = new Vector3f((float) relativePos.x, (float) relativePos.y, (float) relativePos.z);
        transformed.rotate(cameraRotation.conjugate(q));

        if (transformed.z() < 0.1F) return null;

        Window window = mc.getWindow();

        float aRatio = (float)window.getGuiScaledWidth() / window.getGuiScaledHeight();
        float fov = (float) Math.toRadians(mc.options.fov().get());
        float halfFov = (float)Math.tan(fov/2.0F);

        float screenX = (window.getGuiScaledWidth() / 2F) * (1F - transformed.x() / (transformed.z() * halfFov * aRatio));
        float screenY = (window.getGuiScaledHeight() / 2F) * (1F - transformed.y() / (transformed.z() * halfFov));

        return new Vector3f(screenX, screenY, transformed.z());
    }

    // あまりにあんまりなのでいつかなおす
    private void updateValues(PomkotsVehicleBase vehicle) {
        prevHealth = curHealth;
        curHealth = vehicle.getHealth();

        prevEN = curEN;
        curEN = vehicle.getEnergy();
        maxEN = vehicle.getMaxEnergy();

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
            maxCTRightArm = rArm.maxCoolTime;

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

    private void renderHudNormal(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
        renderCrossHair(guiGraphics, tickDelta);
        renderHealthBar(protobot, guiGraphics, tickDelta);
        renderEnergyBar(protobot, guiGraphics, tickDelta);
        renderCooldowns(protobot, guiGraphics, tickDelta);
    }

    private void renderHudBattle(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
        renderCrossHair(guiGraphics, tickDelta);
        renderHealthBar(protobot, guiGraphics, tickDelta);
        renderEnergyBar(protobot, guiGraphics, tickDelta);
        renderCooldowns(protobot, guiGraphics, tickDelta);
    }

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
    private void renderEnergyBar(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
        // 画面の幅と高さを取得
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int maxFuel = maxEN; // 最大燃料

        int width = 25;
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
        guiGraphics.fill((int)(x + ((float)width - fuelWidth)/2), y, (int)(x + ((float)width - fuelWidth)/2 + fuelWidth) + 1, y + height, FG_COLOR);
    }

    private void renderCooldowns(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {

        int offx = mc.getWindow().getGuiScaledWidth()/2;
        int offy = mc.getWindow().getGuiScaledHeight()/2;

        renderCooldown(prevCTRightArm, curCTRightArm, maxCTRightArm, offx + 19, offy - 9, false, guiGraphics, tickDelta);
        renderCooldown(prevCTLeftArm, curCTLeftArm, maxCTLeftArm, offx - 21, offy - 9, true, guiGraphics, tickDelta);
        renderCooldown(prevCTRightShoulder, curCTRightShoulder, maxCTRightShoulder, offx + 19, offy + 1, false, guiGraphics, tickDelta);
        renderCooldown(prevCTLeftShoulder, curCTLeftShoulder, maxCTLeftShoulder, offx - 21, offy + 1, true, guiGraphics, tickDelta);
    }

    private void renderCooldown(int prevCT, int curCT, int maxCT, int x, int y, boolean reverse, GuiGraphics guiGraphics, float tickDelta) {
        int blockWidth = 2;
        int blockHeight = 8;

        float ctLerp = Mth.lerp(tickDelta, prevCT, curCT);
        int cooldownHeight = (int) (blockHeight * (((float)maxCT - ctLerp) / (float)maxCT));

        guiGraphics.fill(x, y, x + blockWidth, y + blockHeight, BG_COLOR);
        guiGraphics.fill(x, y + blockHeight - cooldownHeight, x + blockWidth, y + blockHeight, FG_COLOR);
    }


    private static final int FG_COLOR2 = 0xFFC7D4DE;
    private static final int BG_COLOR2 = 0x55555555;
    private static final int BG_ERR_COLOR2 = 0x55AA0000;
    private static final int FG_ERR_COLOR2 = 0xAAAA0000;

    private static final ResourceLocation CROSSHAIR_TEXTURE2 = PomkotsMechs.id("textures/crosshair/custom/crosshair.png");
    private static final ResourceLocation DONUT_CD_FORE = PomkotsMechs.id("textures/crosshair/custom/front_cooldown.png");
    private static final ResourceLocation DONUT_AM_FORE = PomkotsMechs.id("textures/crosshair/custom/front_ammo.png");

    protected void renderCustomMechHud(Pmvc01Entity mech, GuiGraphics guiGraphics, float tickDelta) {
        PoseStack pose = guiGraphics.pose();

        pose.pushPose();

        float shake = ClientHudShake.shake;
        if (shake > 0f) {
            float offsetX =
                    (Minecraft.getInstance().level.random.nextFloat() - 0.5f)
                            * shake;
            float offsetY =
                    (Minecraft.getInstance().level.random.nextFloat() - 0.5f)
                            * shake;
            pose.translate(offsetX, offsetY, 0);
        }

        renderCrossHair2(guiGraphics, tickDelta);
        renderPickedDistance(mech, guiGraphics, tickDelta);
        renderWeaponInformation(mech, guiGraphics, tickDelta);
        renderHealthBar2(mech, guiGraphics, tickDelta);
        renderFuelBar(mech, guiGraphics, tickDelta);
        renderEnergyBar2(mech, guiGraphics, tickDelta);
        renderRepairKitNum(mech, guiGraphics, tickDelta);
        renderMechHPWarnings(mech, guiGraphics, tickDelta);

        pose.popPose();
    }

    private static final int[] DIST_COLOR_GREEN   = {0xFF00FF88, 0xFF003322};
    private static final int[] DIST_COLOR_YELLOW  = {0xE6DA00, 0xFF5E5A00};
    private static final int[] DIST_COLOR_RED     = {0xFFFF4444, 0xFF330000};

    public static void renderPickedDistance(Pmvc01Entity mech, GuiGraphics guiGraphics, float partialTick) {
        if (shouldRenderDistanceOnTarget()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.options.hideGui) return;

        double maxDist = 150.0;

        Vec3 eyePos  = mc.player.getEyePosition(partialTick);
        Vec3 lookVec = mc.player.getViewVector(partialTick);
        Vec3 endPos  = eyePos.add(lookVec.scale(maxDist));

        BlockHitResult blockHit = mc.level.clip(new ClipContext(
                eyePos, endPos,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                mc.player
        ));
//
//        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
//                mc.player,
//                eyePos,
//                endPos,
//                new AABB(eyePos, endPos).inflate(1.0),
//                e -> !e.isSpectator() && e != mc.player,
//                maxDist * maxDist
//        );
//
//        HitResult hitResult = pickClosest(eyePos, blockHit, entityHit);

        int centerX = guiGraphics.guiWidth()  / 2;
        int centerY = guiGraphics.guiHeight() / 2;

        renderDistance(mech, centerX, centerY, blockHit, guiGraphics, partialTick, mc);
    }

    private static void renderDistance(Pmvc01Entity mech, int centerX, int centerY, HitResult hitResult, GuiGraphics guiGraphics, float partialTick, Minecraft mc) {
        String distText;
        int[] color;

        if (hitResult.getType() == HitResult.Type.MISS) {
            distText = "-- m";
            color = DIST_COLOR_GREEN;
        } else {
            Vec3 playerEye = mc.player.getEyePosition(partialTick);
            double dist    = playerEye.distanceTo(hitResult.getLocation());
            distText = String.format("%.1fm", dist);
            color = chooseColor(mech, hitResult.getType() == HitResult.Type.ENTITY, dist);
        }

        int lineStartX = centerX + 12;
        int lineEndX   = centerX + 40;
        int lineY      = centerY;

        // 横線
        guiGraphics.fill(lineStartX, lineY,     lineEndX,     lineY + 1, color[0]);
        // 縦線アクセント
        guiGraphics.fill(lineEndX,   lineY - 4, lineEndX + 1, lineY + 1, color[0]);

        // テキスト
        Font font  = mc.font;
        int textX  = lineEndX + 3;
        int textY  = lineY - font.lineHeight / 2;

        guiGraphics.drawString(font, distText, textX + 1, textY + 1, color[1], false);
        guiGraphics.drawString(font, distText, textX,     textY,     color[0],   false);
    }

    private static int[] chooseColor(Pmvc01Entity mech, boolean isEntity, double dist) {
        if (isEntity) {
            if (mech.getDriverInput().isWeaponRightHandPressed()) {
                return chooseColorFromWeapon(mech.getRightArmWeapon().getItem(), dist);
            } else if (mech.getDriverInput().isWeaponLeftHandPressed()) {
                return chooseColorFromWeapon(mech.getLeftArmWeapon().getItem(), dist);
            } else {
                return chooseColorFromWeapon(mech.getRightArmWeapon().getItem(), dist);
            }
        } else {
            return DIST_COLOR_GREEN;
        }
    }

    private static int[] chooseColorFromWeapon(Item item, double distance) {
        if (item instanceof BasePartsItem.Weapon weapon) {
            return switch (weapon.getCurrentRange((int)distance)) {
                case OPTIMAL -> DIST_COLOR_GREEN;
                case EFFECTIVE -> DIST_COLOR_YELLOW;
                case MAXIMUM, OUT -> DIST_COLOR_RED;
                default -> DIST_COLOR_GREEN;
            };
        } else {
            return DIST_COLOR_GREEN;
        }
    }

    private static HitResult pickClosest(Vec3 eyePos, BlockHitResult blockHit, EntityHitResult entityHit) {
        double blockDist  = blockHit  != null && blockHit.getType()  != HitResult.Type.MISS
                ? eyePos.distanceToSqr(blockHit.getLocation())  : Double.MAX_VALUE;
        double entityDist = entityHit != null && entityHit.getType() != HitResult.Type.MISS
                ? eyePos.distanceToSqr(entityHit.getLocation()) : Double.MAX_VALUE;

        if (blockDist <= entityDist) return blockHit;
        return entityHit;
    }

    protected void renderTurretHud(Pmvt01Entity mech, GuiGraphics guiGraphics, float tickDelta) {
        renderCrossHair2(guiGraphics, tickDelta);
        renderHealthBar2(mech, guiGraphics, tickDelta);
    }

    private void renderWeaponInformation(Pmvc01Entity mech, GuiGraphics guiGraphics, float tickDelta) {
        int offx = mc.getWindow().getGuiScaledWidth()/2;
        int offy = mc.getWindow().getGuiScaledHeight()/2;

        renderCooldown2(prevCTRightArm, curCTRightArm, maxCTRightArm, offx + 36 - 2, offy - 20, false, guiGraphics, tickDelta);
        renderBulletNum(mech.getAmmoManager(Pmvc01Entity.INV_WEAPON_RIGHT_HAND), offx + 36 + 3 - 2, offy - 20, guiGraphics);
        renderAmmoRight(mech.getAmmoManager(Pmvc01Entity.INV_WEAPON_RIGHT_HAND), offx + 36 + 12 - 2, offy - 18, guiGraphics);

        renderCooldown2(prevCTRightShoulder, curCTRightShoulder, maxCTRightShoulder, offx + 36 - 2, offy + 12, false, guiGraphics, tickDelta);
        renderBulletNum(mech.getAmmoManager(Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER), offx + 36 + 3 - 2, offy + 12,  guiGraphics);
        renderAmmoRight(mech.getAmmoManager(Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER), offx + 36 + 12 - 2, offy + 14, guiGraphics);

        renderCooldown2(prevCTLeftArm, curCTLeftArm, maxCTLeftArm, offx - 36, offy - 20, true, guiGraphics, tickDelta);
        renderBulletNum(mech.getAmmoManager(Pmvc01Entity.INV_WEAPON_LEFT_HAND), offx - 36 - 3, offy - 20, guiGraphics);
        renderAmmoLeft(mech.getAmmoManager(Pmvc01Entity.INV_WEAPON_LEFT_HAND), offx - 36, offy - 18, guiGraphics);

        renderCooldown2(prevCTLeftShoulder, curCTLeftShoulder, maxCTLeftShoulder, offx - 36, offy + 12, true, guiGraphics, tickDelta);
        renderBulletNum(mech.getAmmoManager(Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER), offx - 36 - 3, offy + 12,  guiGraphics);
        renderAmmoLeft(mech.getAmmoManager(Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER), offx - 36, offy + 14, guiGraphics);


    }

    private void renderCooldown2(int prevCT, int curCT, int maxCT, int x, int y, boolean reverse, GuiGraphics guiGraphics, float tickDelta) {
        int blockWidth = 2;
        int blockHeight = 12;

        float ctLerp = Mth.lerp(tickDelta, prevCT, curCT);
        int cooldownHeight = (int) (blockHeight * (((float)maxCT - ctLerp) / (float)maxCT));

        guiGraphics.fill(x, y, x + blockWidth, y + blockHeight, BG_COLOR);
        guiGraphics.fill(x, y + blockHeight - cooldownHeight, x + blockWidth, y + blockHeight, FG_COLOR2);
    }

    private void renderBulletNum(Pmvc01Entity.AmmoManager amm, int x, int y, GuiGraphics guiGraphics) {
        int bulletNum = amm.getBulletNum();
        int maxBulletNum = amm.getBulletNumPerMagazine();

        if (maxBulletNum > 0) {
            int blockWidth = 2;
            int blockHeight = 12;

            if (!amm.isReloading()) {
                int bulletNumHeight = (int) (blockHeight * ((bulletNum) / (float)maxBulletNum)); // 100を最大値とした割合

                if (bulletNum == 0) {
                    guiGraphics.fill(x, y, x + blockWidth, y + blockHeight, BG_ERR_COLOR2);
                } else {
                    guiGraphics.fill(x, y, x + blockWidth, y + blockHeight, BG_COLOR);
                }

                guiGraphics.fill(x, y + blockHeight - bulletNumHeight, x + blockWidth, y + blockHeight, FG_COLOR2);
            } else {
                int bulletNumHeight = (int) (blockHeight * ((amm.RELOAD_TICKS - amm.getReloadTicks()) / (float)amm.RELOAD_TICKS)); // 100を最大値とした割合

                guiGraphics.fill(x, y, x + blockWidth, y + blockHeight, BG_ERR_COLOR2);
                guiGraphics.fill(x, y + blockHeight - bulletNumHeight, x + blockWidth, y + blockHeight, FG_ERR_COLOR2);
            }
        }
    }

    private void renderAmmoLeft(Pmvc01Entity.AmmoManager amm, int x, int y, GuiGraphics gui) {
        if (amm.getBulletNumPerMagazine() != 0) {
            int bColor = 0xBCFFE1, mColor = FG_COLOR2;
            if (amm.getBulletNum() == 0) {
                bColor = 0xFF0000;
            }
            if (amm.getMagazineNum() == 0) {
                mColor = 0xFF0000;
            }
            gui.drawString(Minecraft.getInstance().font, String.format("%2s", amm.getMagazineNum()), (x - 20), y, mColor, false);
        }
    }

    private void renderAmmoRight(Pmvc01Entity.AmmoManager amm, int x, int y, GuiGraphics gui) {
        if (amm.getBulletNumPerMagazine() != 0) {
            int bColor = 0xBCFFE1, mColor = FG_COLOR2;
            if (amm.getBulletNum() == 0) {
                bColor = 0xFF0000;
            }
            if (amm.getMagazineNum() == 0) {
                mColor = 0xFF0000;
            }
            gui.drawString(Minecraft.getInstance().font, String.format("%-2s", amm.getMagazineNum()), x, y, mColor, false);
        }

    }

    private void renderCrossHair2(GuiGraphics guiGraphics, float tickDelta) {
        // テクスチャの幅と高さ（クロスヘアの画像サイズ）
        int textureWidth = 64;  // クロスヘアの幅
        int textureHeight = 64; // クロスヘアの高さ

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
        RenderSystem.setShaderTexture(0, CROSSHAIR_TEXTURE2);

        // `blit`メソッドでテクスチャを画面中央に描画
        guiGraphics.blit(CROSSHAIR_TEXTURE2, startX, startY, 0, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // R, G, B, A (Aが透明度)

    }

    private void renderHealthBar2(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
        // 画面の幅と高さを取得
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 体力バーの位置とサイズ
        int width = 120;
        int height = 3;

        // 画面中央の位置を計算
        int x = (screenWidth / 2) - (width / 2);
        int y = screenHeight - 13;

        if (protobot.shouldRenderDefaultHud("renderHotbar")) {
            y -= 20;
        }

        int maxHealth = (int) protobot.getMaxHealth();

        // 背景バー
        guiGraphics.fill(x, y, x + width, y + height, BG_COLOR);

        // 現在の体力の割合を計算
        int healthWidthCur = (int) (width * Mth.lerp(tickDelta, (float) curHealth / maxHealth, (float) curHealth / maxHealth));
        int healthWidthPrev = (int) (width * Mth.lerp(tickDelta, (float) prevHealth / maxHealth, (float) prevHealth / maxHealth));

        int healthWidth = Mth.lerpInt(tickDelta, healthWidthPrev, healthWidthCur);
        // 前景バー
        guiGraphics.fill(x, y, x + healthWidth, y + height, (healthWidth < 40) ? FG_ERR_COLOR2 : FG_COLOR2);
    }

    private void renderFuelBar(Pmvc01Entity protobot, GuiGraphics guiGraphics, float tickDelta) {
        // 画面の幅と高さを取得
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 体力バーの位置とサイズ
        int width = 120;
        int height = 3;

        // 画面中央の位置を計算
        int x = (screenWidth / 2) - (width / 2);
        int y = screenHeight - 8;

        if (protobot.shouldRenderDefaultHud("renderHotbar")) {
            y -= 20;
        }

        int maxFuel = protobot.getMaxFuel();


        float fuelWidth = (maxFuel == 0 ? 0: (float)protobot.getFuelNow()/maxFuel) * width;

        guiGraphics.fill(x, y, x + width, y + height, fuelWidth == 0 ? BG_ERR_COLOR2: BG_COLOR);
        guiGraphics.fill(x, y, x + (int)fuelWidth, y + height, (fuelWidth < 40) ? FG_ERR_COLOR2 : FG_COLOR2);
    }

    private void renderEnergyBar2(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
        // 画面の幅と高さを取得
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int maxFuel = maxEN; // 最大燃料

        int width = 35;
        int height = 2;

        // 画面中央の位置を計算
        int x = (screenWidth / 2) - (width / 2);
        int y = (screenHeight / 2) -40;

        // 燃料の割合を計算
        float enLerp = Mth.lerp(tickDelta, prevEN, curEN);
        float fuelWidth = width * (enLerp / maxFuel);

        if (protobot instanceof Pmvc01Entity mech) {
            // 背景バー
            guiGraphics.fill(x, y, x + width, y + height, mech.isOverHeat() ? BG_ERR_COLOR2: BG_COLOR);

            // 前景バー
            guiGraphics.fill((int)(x + ((float)width - fuelWidth)/2), y, (int)(x + ((float)width - fuelWidth)/2 + fuelWidth) + 1, y + height, mech.isOverHeat() ? FG_ERR_COLOR2: FG_COLOR2);

        } else {
            // 背景バー
            guiGraphics.fill(x, y, x + width, y + height, (fuelWidth < width * 0.2) ? BG_ERR_COLOR2: BG_COLOR);

            // 前景バー
            guiGraphics.fill((int)(x + ((float)width - fuelWidth)/2), y, (int)(x + ((float)width - fuelWidth)/2 + fuelWidth) + 1, y + height, FG_COLOR2);

        }
    }

    private void renderRepairKitNum(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
        if (!(protobot instanceof Pmvc01Entity pmvc01)) {
            return;
        }
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 体力バーの位置とサイズ
        int width = 120;

        // 画面中央の位置を計算
        int x = (screenWidth / 2) + (width / 2) + 8;
        int y = screenHeight - 13;

        if (protobot.shouldRenderDefaultHud("renderHotbar")) {
            y -= 20;
        }

        int size = 8;

        guiGraphics.blit(REPAIR_KIT_ICON, x, y, 0, 0, size, size, size, size);
        guiGraphics.drawString(Minecraft.getInstance().font, String.format("%2s", pmvc01.getRepairKitNum()), (x + 10), y, FG_COLOR2, false);

    }

    private void renderMechHPWarnings(PomkotsVehicleBase mech, GuiGraphics guiGraphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();

        float hpRatio = mech.getHealth() / mech.getMaxHealth();

        if (hpRatio <= 0.4f) {
            String text;
            int rgb;
            float speed;

            if (hpRatio <= 0.3f) {
                text = "DANGER: ARMOR LOW";
                rgb = 0xFF0000;
                speed = 0.4f;

            } else {
                text = "WARNING: ARMOR LOW";
                rgb = 0xFFFF00;
                speed = 0.15f;
            }

            float time = (mc.player.tickCount + tickDelta) * speed;

            float alpha = (Mth.sin(time) + 1.0f) * 0.5f;
            alpha = 0.2f + alpha * 0.8f;

            int a = (int)(alpha * 255.0f);
            int color = (a << 24) | rgb;

            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            // 画面中央の位置を計算
            int x = (screenWidth / 2) ;
            int y = (screenHeight / 2) + 40;

            Font font = mc.font;

            int textWidth = font.width(text);

            int xx = x - textWidth / 2;

            guiGraphics.drawString(
                    font,
                    text,
                    xx,
                    y,
                    color,
                    false
            );
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