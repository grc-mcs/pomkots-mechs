package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.cockpit.CockpitHudRendererManager;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void render(ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean bl, PoseStack poseStack, MultiBufferSource buffer, int light, int z, BakedModel bakedModel, CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;

        if (player != null && player.getVehicle() instanceof PomkotsVehicleBase vehicle && itemDisplayContext.firstPerson() && Utils.shouldRenderCockpit(vehicle)) {
            // 腕の描画位置を調整
            poseStack.pushPose();

            // そのままだと腕の位置に連動しちゃうので、一回リセット
            poseStack.setIdentity();

            // 位置とサイズを調整
            poseStack.translate(0.5, -1.8, 0.1);
            poseStack.scale(1, 1f, 1f);

            // 角度を調整（なんもせんとカメラにくっついてきちゃう）
            poseStack.mulPose(Axis.YP.rotationDegrees(180));

            poseStack.mulPose(Axis.XP.rotationDegrees(-player.getXRot()/2));

            // 実描画
            CockpitHudRendererManager.renderCockpit(poseStack, buffer, light, vehicle, player);

            poseStack.popPose();
        }
    }
}