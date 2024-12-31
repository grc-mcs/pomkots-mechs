package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.PlayerRideableJumping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void renderCrosshair(float f, GuiGraphics guiGraphics, CallbackInfo ci) {
        if (shouldCancel("renderHotbar")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (shouldCancel("renderCrosshair")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true)
    private void renderPlayerHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (shouldCancel("renderPlayerHealth")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderVehicleHealth", at = @At("HEAD"), cancellable = true)
    private void renderVehicleHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (shouldCancel("renderVehicleHealth")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderJumpMeter", at = @At("HEAD"), cancellable = true)
    private void renderJumpMeter(PlayerRideableJumping j, GuiGraphics guiGraphics, int i, CallbackInfo ci) {
        if (shouldCancel("renderJumpMeter")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void renderExperienceBar(GuiGraphics guiGraphics, int i, CallbackInfo ci) {
        if (shouldCancel("renderExperienceBar")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderSelectedItemName", at = @At("HEAD"), cancellable = true)
    private void renderSelectedItemName(GuiGraphics guiGraphics, CallbackInfo ci) {
        if (shouldCancel("renderSelectedItemName")) {
            ci.cancel();
        }
    }

    private boolean shouldCancel(String hudName) {
        Minecraft client = Minecraft.getInstance();

        var p = client.player;
        if (p != null && p.getVehicle() instanceof PomkotsVehicle v) {
            return !v.shouldRenderDefaultHud(hudName);
        }
        return false;
    }
}
