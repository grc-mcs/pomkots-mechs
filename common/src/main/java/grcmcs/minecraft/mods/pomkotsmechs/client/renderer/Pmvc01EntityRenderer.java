package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmvc01EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb99Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import java.util.function.Predicate;

public class Pmvc01EntityRenderer extends GeoEntityRenderer<Pmvc01Entity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    ItemRenderer itemRenderer;

    public Pmvc01EntityRenderer(EntityRendererProvider.Context renderManager) {
        this(renderManager, new Pmvc01EntityModel());
    }

    public Pmvc01EntityRenderer(EntityRendererProvider.Context renderManager, Pmvc01EntityModel model) {
        super(renderManager, model);
        addRenderLayer(new Pmvc01EntityPartsLayer<>(this, renderManager.getItemRenderer()));
        this.itemRenderer = renderManager.getItemRenderer();
    }

    @Override
    public void actuallyRender(PoseStack poseStack, Pmvc01Entity animatable, BakedGeoModel model, RenderType renderType,
                               MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
                               int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        //@JOKE
        if (animatable.getVehicle() instanceof Pmb99Entity pmb99) {
            poseStack.pushPose();
            poseStack.translate(pmb99.seatPos.x, pmb99.seatPos.y, pmb99.seatPos.z);
        }

        super.actuallyRender(poseStack, animatable, model, renderType,
                bufferSource, buffer, isReRender, partialTick,
        packedLight, packedOverlay, red, green, blue, alpha);

        this.renderParts(poseStack, animatable, model, renderType,
                bufferSource, buffer, isReRender, partialTick,
                packedLight, packedOverlay, red, green, blue, alpha);

        if (animatable.getDrivingPassenger() != null) {
            animatable.setClientSeatPos(getSeatPosition(model));
            animatable.setMainCameraPosition(getMainCameraPosition(model, animatable, partialTick, Pmvc01Entity.DEFAULT_SCALE));

        }

        RenderUtils.renderAdditionalHud2(animatable, poseStack, bufferSource, this.entityRenderDispatcher, 1);

        //@JOKE
        if (animatable.getVehicle() instanceof Pmb99Entity pmb99) {
            poseStack.popPose();
        }
    }

    public void renderParts(PoseStack poseStack, Pmvc01Entity animatable, BakedGeoModel bakedModel, RenderType renderType,
                            MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
                            int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

        poseStack.pushPose();
        float yaw = lerpYaw(partialTick, animatable.yBodyRotO, animatable.yBodyRot) + 180;
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw)); // 反転を考慮

        renderPart(animatable.getHeadParts(), (item) -> item instanceof BasePartsItem.Head, poseStack, animatable, bakedModel, renderType, bufferSource, packedLight, packedOverlay);
        renderPart(animatable.getBodyParts(), (item) -> item instanceof BasePartsItem.Body, poseStack, animatable, bakedModel, renderType, bufferSource, packedLight, packedOverlay);
        renderPart(animatable.getArmParts(), (item) -> item instanceof BasePartsItem.Arm, poseStack, animatable, bakedModel, renderType, bufferSource, packedLight, packedOverlay);
        renderPart(animatable.getLegsParts(), (item) -> item instanceof BasePartsItem.Legs, poseStack, animatable, bakedModel, renderType, bufferSource, packedLight, packedOverlay);

        poseStack.popPose();

    }

    private float lerpYaw(float partialTick, float prevYaw, float currentYaw) {
        float delta = Mth.wrapDegrees(currentYaw - prevYaw); // 角度差を -180～180° に正規化
        return prevYaw + partialTick * delta; // 補間
    }

    public void renderPart(ItemStack itemStack, Predicate<Item> isTarget, PoseStack poseStack, Pmvc01Entity animatable, BakedGeoModel bakedModel, RenderType renderType,
                           MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderPart(itemStack, isTarget, null, poseStack, animatable, bakedModel, renderType, bufferSource, packedLight, packedOverlay);
    }

    public void renderPart(ItemStack itemStack, Predicate<Item> isTarget, BasePartsItem.AttachSide side, PoseStack poseStack, Pmvc01Entity animatable, BakedGeoModel bakedModel, RenderType renderType,
                           MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!itemStack.isEmpty()) {
            Item i = itemStack.getItem();

            if (isTarget.test(i)) {
                var ti = (BasePartsItem)i;
                if (ti instanceof BasePartsItem.Weapon w) {
                    w.setSide(side);
                }

                ti.setParentEntity(animatable);
                ti.setCachedBoneFinder(animatable.getBoneFinder());
                this.itemRenderer.renderStatic(animatable, itemStack, ItemDisplayContext.NONE, false, poseStack, bufferSource, null, packedLight, packedOverlay, packedLight);
                ti.setCachedBoneFinder(null);
                ti.setParentEntity(null);

                if (ti instanceof BasePartsItem.Weapon w) {
                    w.setSide(null);
                }
            }
        }
    }

    private Vec3 getSeatPosition(BakedGeoModel model) {
        if (model != null) {
            GeoBone vcSeat = model.getBone("head").get();
            GeoBone vcRoot = model.getBone("root").get();

            Vector3d seatPos = vcSeat.getLocalPosition();
            Vector3d rootPos = vcRoot.getLocalPosition();

            return new Vec3(
                    seatPos.x * Pmvc01Entity.DEFAULT_SCALE - rootPos.x * Pmvc01Entity.DEFAULT_SCALE,
                    seatPos.y * Pmvc01Entity.DEFAULT_SCALE - rootPos.y * Pmvc01Entity.DEFAULT_SCALE - 4.2F,
                    seatPos.z * Pmvc01Entity.DEFAULT_SCALE - rootPos.z * Pmvc01Entity.DEFAULT_SCALE);
        } else {
            return Vec3.ZERO;
        }
    }

    private Vec3 getMainCameraPosition(BakedGeoModel model, Pmvc01Entity entity, float partialTick, float scale) {
        if (model != null) {
            GeoBone vcSeat = null;

            if (Utils.shouldRenderCockpit(entity)) {
                vcSeat = model.getBone("eyecam").get();
            } else {
                vcSeat = model.getBone("maincam").get();
            }

            Vector3d seatPos = vcSeat.getLocalPosition();

            return new Vec3(
                    seatPos.x,
                    seatPos.y * scale,
                    seatPos.z
            );

        } else {
            return Vec3.ZERO;
        }
    }

    @Override
    public void preApplyRenderLayers(PoseStack poseStack, Pmvc01Entity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        this.scaleHeight = Pmvc01Entity.DEFAULT_SCALE;
        this.scaleWidth = Pmvc01Entity.DEFAULT_SCALE;
        animatable.setupBoneFinder(model);
        setWeaponsVisibility(model, animatable, animatable.isMainMode());
//        setBoosterVisibilitty(model, animatable);
//        rotateBooster(animatable, 0, 0);
        super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }

    private void rotateBooster(Pmvc01Entity entity, long uniqueId, float partialTick) {
        CoreGeoBone flame = this.getGeoModel().getBone("fire_bpc_super_boost").orElse(null);

        if (flame == null) return;

        if (!entity.isSuperBoost()) {
            flame.setRotY(0);
            flame.setRotX(0);
            return;
        }

        float bodyYaw = entity.getYRot();   // degrees
        float bodyPitch = entity.getXRot(); // degrees

        // ---- ② エンティティの速度ベクトルを取得 ----
        Vec3 vel = entity.getDeltaMovement();
        if (vel.lengthSqr() < 1e-6) return; // 静止中は回転不要

        // ---- ③ 進行方向をワールド空間で角度化 ----
        float velocityYaw = (float)(Mth.atan2(vel.z, vel.x) * (180.0 / Math.PI)) - 90f;
        float velocityPitch = (float)(-Mth.atan2(vel.y, Math.sqrt(vel.x * vel.x + vel.z * vel.z)) * (180.0 / Math.PI));

        // ---- ④ ロボの向きとの差分をローカル座標に変換 ----
        float localYaw = velocityYaw - bodyYaw;
        float localPitch = velocityPitch - bodyPitch;

        // ---- ⑤ 進行方向「と逆」に炎を向けたい場合 ----
        localYaw += 180f;
        localPitch *= -1f;

        // ---- ⑥ 滑らかに補間 ----
        float currentYaw = flame.getRotY() * Mth.RAD_TO_DEG;
        float currentPitch = flame.getRotX() * Mth.RAD_TO_DEG;

        float smoothYaw = Mth.lerp(0.2f, currentYaw, localYaw);
        float smoothPitch = Mth.lerp(0.2f, currentPitch, localPitch);

        // ---- ⑦ GeckoLib にセット（ラジアンで）----
        flame.setRotY(smoothYaw * Mth.DEG_TO_RAD);
        flame.setRotX(smoothPitch * Mth.DEG_TO_RAD);
    }

    private void rotateBooster3(Pmvc01Entity entity, long uniqueId, float partialTick) {

        CoreGeoBone flame = this.getGeoModel().getBone("fire_bpcr_quick").orElse(null);

        if (flame == null) return;

        if (!entity.getActionEvasion().isInAction()) {
            flame.setRotY(0);
            flame.setRotX(0);
            return;
        }

        Vec3 motion = entity.getDeltaMovement();

        // --- 進行方向ベクトルから目標角度算出 ---
        float targetYaw = (float) Math.toDegrees(Math.atan2(-motion.x, motion.z));
        float targetPitch = (float) Math.toDegrees(Math.atan2(motion.y, Math.sqrt(motion.x * motion.x + motion.z * motion.z)));

        // --- 現在角度を取得 ---
        float currentYaw = (float) Math.toDegrees(flame.getRotY());
        float currentPitch = (float) Math.toDegrees(flame.getRotX());

        // --- 角度差をwrap（-180〜180の範囲に収める） ---
        float deltaYaw = Mth.wrapDegrees(targetYaw - currentYaw);
        float deltaPitch = Mth.wrapDegrees(targetPitch - currentPitch);

        // --- 1tickあたりの最大回転量 ---
        float maxTurnPerTick = 5f; // ←ここを調整（小さいほど滑らか）

        // --- 上限を付けてスムーズに補間 ---
        deltaYaw = Mth.clamp(deltaYaw, -maxTurnPerTick, maxTurnPerTick);
        deltaPitch = Mth.clamp(deltaPitch, -maxTurnPerTick, maxTurnPerTick);

        float newYaw = Mth.wrapDegrees(currentYaw + deltaYaw * 0.5f);   // ←0.5で補間率調整
        float newPitch = Mth.wrapDegrees(currentPitch + deltaPitch * 0.5f);

        // --- GeckoLibはラジアン指定 ---
        flame.setRotY((float) Math.toRadians(newYaw));
        flame.setRotX((float) Math.toRadians(newPitch));
    }
    private void rotateBooster2(Pmvc01Entity entity, long uniqueId, float partialTick) {
        if (!entity.getActionEvasion().isInAction()) {
            return;
        }

        // 炎ボーン（Blockbench側で作ったボーン名）
        CoreGeoBone flame1 = this.getGeoModel().getBone("fire_bpcr_quick").orElse(null);
        if (flame1 == null) return;

        CoreGeoBone flame2 = this.getGeoModel().getBone("fire_bpcl_quick").orElse(null);
        if (flame2 == null) return;

        // 進行方向ベクトルを計算
        double dx = entity.getDeltaMovement().x;
        double dy = entity.getDeltaMovement().y;
        double dz = entity.getDeltaMovement().z;

        // 静止時は処理しない
//        if (dx * dx + dy * dy + dz * dz < 1e-4) return;

        // 進行方向のYaw, Pitchを計算
        float yaw = (float)(Mth.atan2(-dx, dz) * (180F / Math.PI));
        float pitch = (float)(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180F / Math.PI));

        // 進行方向の逆向きに炎を向けたい場合は反転
        yaw += 180F;
        pitch = -pitch;

        yaw = Mth.wrapDegrees(yaw);
        pitch = Mth.wrapDegrees(pitch);

        // GeckoLibはラジアン指定なので変換
        flame1.setRotX(pitch * Mth.DEG_TO_RAD);
        flame1.setRotY(yaw * Mth.DEG_TO_RAD);

        flame2.setRotX(pitch * Mth.DEG_TO_RAD);
        flame2.setRotY(yaw * Mth.DEG_TO_RAD);
    }

    private void setBoosterVisibilitty(BakedGeoModel model, Pmvc01Entity ent) {
        var vel = ent.getDeltaMovement();
        vel = vel.yRot((float) Math.toRadians(ent.getYRot()));
        var hasHorizontalBoost = !ent.noHorizontalBoost();

        if (vel.x > 1 && hasHorizontalBoost) {
            model.getBone("fire_rsb").get().setHidden(false);
            model.getBone("fire_lsb").get().setHidden(true);
        } else if (vel.x < -1 && hasHorizontalBoost) {
            model.getBone("fire_rsb").get().setHidden(true);
            model.getBone("fire_lsb").get().setHidden(false);
        } else {
            model.getBone("fire_rsb").get().setHidden(true);
            model.getBone("fire_lsb").get().setHidden(true);
        }

        if (vel.z > 1 && hasHorizontalBoost) {
            model.getBone("fire_bpcr").get().setHidden(false);
            model.getBone("fire_bpcl").get().setHidden(false);
            model.getBone("fire_rl").get().setHidden(false);
            model.getBone("fire_ll").get().setHidden(false);

        } else {
            model.getBone("fire_bpcr").get().setHidden(true);
            model.getBone("fire_bpcl").get().setHidden(true);
            model.getBone("fire_rl").get().setHidden(true);
            model.getBone("fire_ll").get().setHidden(true);

        }

        if (ent.getDriverInput() != null && ent.getDriverInput().isJumpPressed() && ent.isNoGravity() && !ent.isOverHeat()) {
            model.getBone("fire_bpcr").get().setHidden(false);
            model.getBone("fire_bpcl").get().setHidden(false);

        } else {
            model.getBone("fire_bpcr").get().setHidden(true);
            model.getBone("fire_bpcl").get().setHidden(true);

        }
    }

    private void setWeaponsVisibility(BakedGeoModel model,Pmvc01Entity ent, boolean b) {

        model.getBone("left_shoulder_hunger").get().setHidden(animatable.getLeftShoulderWeapon().isEmpty());
        model.getBone("right_shoulder_hunger").get().setHidden(animatable.getRightShoulderWeapon().isEmpty());
    }

    @Override
    public boolean shouldShowName(Pmvc01Entity entity) {
        return false;
    }
}
