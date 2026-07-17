package grcmcs.minecraft.mods.pomkotsmechs.mixin;
import grcmcs.minecraft.mods.pomkotsmechs.misc.scan.ClientScanManager;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityScanColorMixin {

    /**
     * スキャン対象のアウトライン色を、
     * このクライアント上だけ緑色へ差し替える。
     */
    @Inject(
            method = "getTeamColor()I",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pomkotsmechs$scanOutlineColor(
            CallbackInfoReturnable<Integer> cir
    ) {
        Entity self = (Entity) (Object) this;

        if (!self.level().isClientSide) {
            return;
        }

        if (ClientScanManager.isEntityHighlighted(self.getId())) {
            /*
             * RGB #40FF80
             */
            cir.setReturnValue(0x40FF80);
        }
    }
}
