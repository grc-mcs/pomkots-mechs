package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.CachedBoneFinder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BasePartsItemRenderer<T extends BasePartsItem> extends GeoItemRenderer<T> {
    public BasePartsItemRenderer(BasePartsItemModel model) {
        super(model);
    }

    protected void copy(CachedBoneFinder mech, BakedGeoModel parts, String boneName) {
        GeoBone m = mech.getBone(boneName);
        GeoBone p = parts.getBone(boneName).get();

        p.setPosX(m.getPosX());
        p.setPosY(m.getPosY());
        p.setPosZ(m.getPosZ());
        p.setRotX(m.getRotX());
        p.setRotY(m.getRotY());
        p.setRotZ(m.getRotZ());
    }

    public static class Head<T extends BasePartsItem> extends BasePartsItemRenderer<T> {
        public Head(BasePartsItemModel model) {
            super(model);
        }

        @Override
        public void preApplyRenderLayers(PoseStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            var parent = animatable.getCachedBoneFinder();
            if (parent != null) {
//                copy(parent, model, "root");
                copy(parent, model, "hips");
                copy(parent, model, "spine");
                copy(parent, model, "body");
                copy(parent, model, "body_rot");
            }

            super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }

        @Override
        public void actuallyRender(PoseStack poseStack, T animatable, BakedGeoModel model, RenderType renderType,
                                   MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick,
                                   int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
            var parentEntity = animatable.getParentEntity();
            if (parentEntity != null && parentEntity.getBodyParts().getItem() instanceof BasePartsItem.Body body && body.getNeckPos() != 0) {
                poseStack.pushPose();

                poseStack.translate(0, body.getNeckPos(), 0);

                super.actuallyRender(poseStack, animatable, model, renderType,
                        bufferSource, buffer, isReRender, partialTick,
                        packedLight, packedOverlay, red, green, blue, alpha);

                poseStack.popPose();
            } else {
                super.actuallyRender(poseStack, animatable, model, renderType,
                        bufferSource, buffer, isReRender, partialTick,
                        packedLight, packedOverlay, red, green, blue, alpha);
            }
        }
    }

    public static class Body<T extends BasePartsItem> extends BasePartsItemRenderer<T> {
        public Body(BasePartsItemModel model) {
            super(model);
        }

        @Override
        public void preApplyRenderLayers(PoseStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            var parent = animatable.getCachedBoneFinder();
            if (parent != null) {
                copy(parent, model, "root");
                copy(parent, model, "hips");
                copy(parent, model, "spine");
                copy(parent, model, "body");
                copy(parent, model, "body_rot");
            }

            var pe = animatable.getParentEntity();
            var showLid = pe != null && pe.getHeadParts().getItem() instanceof BasePartsItem.Head head && head.isFullCovered();
            var lid = model.getBone("body_lid");
            if (lid.isPresent()) {
                lid.get().setHidden(!showLid);
            }

            super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }
    }

    public static class Arm<T extends BasePartsItem> extends BasePartsItemRenderer<T> {
        public Arm(BasePartsItemModel model) {
            super(model);
        }

        @Override
        public void preApplyRenderLayers(PoseStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            var parent = animatable.getCachedBoneFinder();
            if (parent != null) {
//                copy(parent, model, "root");
                copy(parent, model, "hips");
                copy(parent, model, "spine");
                copy(parent, model, "body");
                copy(parent, model, "body_rot");
                copy(parent, model, "right_shoulder");
                copy(parent, model, "right_upper_arm");
                copy(parent, model, "right_lower_arm");
                copy(parent, model, "left_shoulder");
                copy(parent, model, "left_upper_arm");
                copy(parent, model, "left_lower_arm");
            }

            super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }
    }

    public static class Legs<T extends BasePartsItem> extends BasePartsItemRenderer<T> {
        public Legs(BasePartsItemModel model) {
            super(model);
        }

        @Override
        public void preApplyRenderLayers(PoseStack poseStack, T animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            var parent = animatable.getCachedBoneFinder();
            if (parent != null) {
                copy(parent, model, "root");
                copy(parent, model, "hips");
                copy(parent, model, "right_upper_leg");
                copy(parent, model, "right_lower_leg");
                copy(parent, model, "right_foot");
                copy(parent, model, "left_upper_leg");
                copy(parent, model, "left_lower_leg");
                copy(parent, model, "left_foot");
            }

            super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
        }
    }

    public static class Weapon<T extends BasePartsItem.Weapon> extends BasePartsItemRenderer<T> {
        public Weapon(BasePartsItemModel model) {
            super(model);
        }
    }

    public static class Generator<T extends BasePartsItem.Generator> extends BasePartsItemRenderer<T> {
        public Generator(BasePartsItemModel model) {
            super(model);
        }
    }

    public static class Booster<T extends BasePartsItem.Booster> extends BasePartsItemRenderer<T> {
        public Booster(BasePartsItemModel model) {
            super(model);
        }
    }

    public static class Extension<T extends BasePartsItem.Extension> extends BasePartsItemRenderer<T> {
        public Extension(BasePartsItemModel model) {
            super(model);
        }
    }

    public static class Magazine<T extends BasePartsItem.Magazine> extends BasePartsItemRenderer<T> {
        public Magazine(BasePartsItemModel model) {
            super(model);
        }
    }

    public static class Fuel<T extends BasePartsItem.Fuel> extends BasePartsItemRenderer<T> {
        public Fuel(BasePartsItemModel model) {
            super(model);
        }
    }
}
