package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.BuilderUnitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.HoverUnitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.SBUnitProtoTypeItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons.TenpouItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;

public class Pmvc01EntityPartsLayer <T extends Pmvc01Entity> extends GeoRenderLayer<T> {
    private final ItemRenderer itemRenderer;

    public Pmvc01EntityPartsLayer(GeoRenderer<T> entityRendererIn, ItemRenderer itemRenderer) {
        super(entityRendererIn);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void renderForBone(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if ("weapon_right_hand".equals(bone.getName())) {
            this.renderWeapon(BasePartsItem.AttachSide.RIGHT, BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_HAND, animatable.getRightArmWeapon(), poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        } else if ("weapon_right_arm".equals(bone.getName())) {
            this.renderWeapon(BasePartsItem.AttachSide.RIGHT, BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_ARM, animatable.getRightArmWeapon(), poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        } else if ("weapon_left_hand".equals(bone.getName())) {
            this.renderWeapon(BasePartsItem.AttachSide.LEFT, BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_HAND, animatable.getLeftArmWeapon(), poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        } else if ("weapon_left_arm".equals(bone.getName())) {
            this.renderWeapon(BasePartsItem.AttachSide.LEFT, BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_ARM, animatable.getLeftArmWeapon(), poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        } else if ("weapon_right_shoulder".equals(bone.getName())) {
            this.renderWeapon(BasePartsItem.AttachSide.RIGHT, BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_SHOULDER, animatable.getRightShoulderWeapon(), poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        } else if ("weapon_left_shoulder".equals(bone.getName())) {
            this.renderWeapon(BasePartsItem.AttachSide.LEFT, BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_SHOULDER, animatable.getLeftShoulderWeapon(), poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        } else if ("extension_attachment_spine".equals(bone.getName())) {
            this.renderExtensionsSpine(poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        } else if ("extension_attachment_backpack".equals(bone.getName())) {
            this.renderExtensionsBackpack(poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        }
    }

    private void renderWeapon(BasePartsItem.AttachSide side, BasePartsItem.WeaponInterface.WeaponAttachPoint point, ItemStack item, PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (item.isEmpty()) {
            return;
        }

        if (item.getItem() instanceof BasePartsItem.Weapon w && point.equals(w.getWeaponAttachPoint())) {
            poseStack.pushPose();

            RenderUtils.translateAndRotateMatrixForBone(poseStack, bone);

            w.setParentEntity(animatable);
            w.setSide(side);

            if (BasePartsItem.AttachSide.RIGHT == side && item.getItem() instanceof TenpouItem) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            }

            Minecraft.getInstance().getItemRenderer().renderStatic(animatable, item,
                    ItemDisplayContext.NONE, false, poseStack, bufferSource, animatable.level(),
                    packedLight, packedOverlay, animatable.getId());

            if (BasePartsItem.AttachSide.RIGHT == side && item.getItem() instanceof TenpouItem) {
                poseStack.popPose();
            }

            w.setSide(null);
            w.setParentEntity(null);

            poseStack.popPose();
        }
    }

    private void renderExtensionsSpine(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        var ext1Stack = animatable.getExtension1Weapon();
        var ext2Stack = animatable.getExtension2Weapon();

        HoverUnitItem hover = null;
        ItemStack hoverStack = null;

        if (!ext1Stack.isEmpty() && ext1Stack.getItem() instanceof HoverUnitItem hui) {
            hover = hui;
            hoverStack = ext1Stack;
        }

        if (!ext2Stack.isEmpty() && ext2Stack.getItem() instanceof HoverUnitItem hui) {
            hover = hui;
            hoverStack = ext2Stack;
        }

        if (hover != null) {
            renderExtension(hoverStack, hover, poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        }
    }

    private void renderExtensionsBackpack(PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        var ext1Stack = animatable.getExtension1Weapon();
        var ext2Stack = animatable.getExtension2Weapon();

        if (!ext1Stack.isEmpty() && (
                ext1Stack.getItem() instanceof SBUnitProtoTypeItem
                        || ext1Stack.getItem() instanceof BuilderUnitItem
        )) {
            renderExtension(ext1Stack, (BasePartsItem.Extension)ext1Stack.getItem(), poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        }

        if (!ext2Stack.isEmpty() && (
                ext2Stack.getItem() instanceof SBUnitProtoTypeItem
                        || ext2Stack.getItem() instanceof BuilderUnitItem
        )) {
            renderExtension(ext2Stack, (BasePartsItem.Extension)ext2Stack.getItem(), poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        }
    }

    private void renderExtension(ItemStack extItemStack, BasePartsItem.Extension extItem, PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        RenderUtils.translateAndRotateMatrixForBone(poseStack, bone);

        extItem.setParentEntity(animatable);
        extItem.setSide(BasePartsItem.AttachSide.RIGHT);
        Minecraft.getInstance().getItemRenderer().renderStatic(animatable, extItemStack,
                ItemDisplayContext.NONE, false, poseStack, bufferSource, animatable.level(),
                packedLight, packedOverlay, animatable.getId());
        extItem.setSide(null);
        extItem.setParentEntity(null);

        poseStack.popPose();
    }
}
