package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void onStartDestroyBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (minecraft != null && Utils.isRidingPomkotsMechs(minecraft.player)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    // ブロック右クリック
    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void onUseItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (minecraft != null && Utils.isRidingPomkotsMechs(minecraft.player)) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    // アイテム使用
    @Inject(method = "useItem", at = @At("HEAD"), cancellable = true)
    private void onUseItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (minecraft != null && Utils.isRidingPomkotsMechs(minecraft.player)) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    // エンティティ右クリック
    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteract(Player player, Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (minecraft != null && Utils.isRidingPomkotsMechs(minecraft.player)) {
            cir.setReturnValue(InteractionResult.FAIL);
            cir.cancel();
        }
    }

    // エンティティ攻撃
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(Player player, Entity target, CallbackInfo ci) {
        if (minecraft != null && Utils.isRidingPomkotsMechs(minecraft.player)) ci.cancel();
    }
}
