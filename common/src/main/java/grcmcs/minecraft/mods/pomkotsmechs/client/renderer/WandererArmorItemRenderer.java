package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.PomkotsArmorItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.WandererArmorItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class WandererArmorItemRenderer extends GeoArmorRenderer<WandererArmorItem> {
    public WandererArmorItemRenderer() {
        super(new DefaultedItemGeoModel<>(new ResourceLocation(PomkotsMechs.MODID, "wandererarmor")));
        // Using DefaultedItemGeoModel like this puts our 'location' as item/armor/example armor in the assets folders.
    }

    @Override
    public void preRender(PoseStack poseStack, WandererArmorItem animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        if (this.crouching) {
            var body = this.getBodyBone();
            if (body != null) {
                body.setRotX(body.getRotX() - 0.2F);
                body.setPosZ(body.getPosZ() + 2F);
            }
        }
    }
}
