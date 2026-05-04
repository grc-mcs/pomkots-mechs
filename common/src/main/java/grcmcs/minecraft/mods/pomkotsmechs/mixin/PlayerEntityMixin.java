package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerEntityMixin {
//    @Inject(method = "tick", at = @At("HEAD"))
//    private void forceDisableSprinting(CallbackInfo ci) {
//        Player self = (Player) (Object) this;
//        ItemStack mainHand = self.getMainHandItem();
//
//        // 特定アイテムを持っている間は毎tick強制でスプリント解除
//        if (mainHand.getItem() == PomkotsMechs.CARTON.get()) {
//            if (self.isSprinting()) {
//                self.setSprinting(false);
//            }
//        }
//    }
}
