package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    // hurtメソッドに対してMixinを適用
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        // エンティティがプレイヤーかどうか確認
        if (livingEntity instanceof Player player) {
            // プレイヤーがロボットに乗っている場合
            if (isRidingRobot(player)) {
                cir.setReturnValue(false);  // ダメージを無効化
            }
        }
    }

    // プレイヤーがロボットに搭乗しているかを確認
    private boolean isRidingRobot(Player player) {
        Entity vehicle = player.getVehicle();
        return vehicle instanceof Pmv01Entity;  // MyRobotEntityはカスタムロボットのクラス
    }
}
