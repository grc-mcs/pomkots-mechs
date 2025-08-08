package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.HoverUnitItem;
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
            this.renderWeapon("right", "hand", animatable.getRightArmWeapon(), poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        } else if ("weapon_right_arm".equals(bone.getName())) {
            this.renderWeapon("right", "arm", animatable.getRightArmWeapon(), poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        } else if ("weapon_left_hand".equals(bone.getName())) {
            this.renderWeapon("left", "hand", animatable.getLeftArmWeapon(), poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        } else if ("weapon_left_arm".equals(bone.getName())) {
            this.renderWeapon("left", "arm", animatable.getLeftArmWeapon(), poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        } else if ("weapon_right_shoulder".equals(bone.getName())) {
            this.renderWeapon("right", "shoulder", animatable.getRightShoulderWeapon(), poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        } else if ("weapon_left_shoulder".equals(bone.getName())) {
            this.renderWeapon("left", "shoulder", animatable.getLeftShoulderWeapon(), poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        } else if ("extension_attachment_spine".equals(bone.getName())) {
            this.renderExtensions(animatable, poseStack,  animatable,  bone,  renderType,  bufferSource,  buffer,  partialTick,  packedLight,  packedOverlay);
        }
    }

    private void renderWeapon(String side, String point, ItemStack item, PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (item.isEmpty()) {
            return;
        }

        if (item.getItem() instanceof BasePartsItem.Weapon w && point.equals(w.getWeaponAttachPoint())) {
            poseStack.pushPose();

            RenderUtils.translateAndRotateMatrixForBone(poseStack, bone);

            w.setParentEntity(animatable);
            w.setSide(side);
            Minecraft.getInstance().getItemRenderer().renderStatic(animatable, item,
                    ItemDisplayContext.NONE, false, poseStack, bufferSource, animatable.level(),
                    packedLight, packedOverlay, animatable.getId());
            w.setSide(null);
            w.setParentEntity(null);

            poseStack.popPose();
        }
    }

    private void renderExtensions(Pmvc01Entity animatabale, PoseStack poseStack, T animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        var ext1Stack = animatabale.getExtension1Weapon();
        var ext2Stack = animatabale.getExtension2Weapon();

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
            poseStack.pushPose();

            RenderUtils.translateAndRotateMatrixForBone(poseStack, bone);

            hover.setParentEntity(animatable);
            hover.setSide("right");
            Minecraft.getInstance().getItemRenderer().renderStatic(animatable, hoverStack,
                    ItemDisplayContext.NONE, false, poseStack, bufferSource, animatable.level(),
                    packedLight, packedOverlay, animatable.getId());
            hover.setSide(null);
            hover.setParentEntity(null);

            poseStack.popPose();
        }
    }
}
