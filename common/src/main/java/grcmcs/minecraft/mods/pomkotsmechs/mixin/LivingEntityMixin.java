package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        if (livingEntity instanceof Player player) {
            if (Utils.isRidingPomkotsVehicle(player)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "shouldDropLoot", at = @At("HEAD"), cancellable = true)
    private void pomkotsmechs$suppressMissionMobLoot(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (pomkotsmechs$isMissionNonBossEnemy(entity)) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Forge inserts its living-drop hook in the final death-loot path, so the
     * shouldDropLoot guard alone is not sufficient in every execution path.
     * Cancelling here is a loader-neutral final guard; bosses remain excluded.
     */
    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"), cancellable = true)
    private void pomkotsmechs$suppressMissionMobDeathDrops(
            DamageSource damageSource,
            CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (pomkotsmechs$isMissionNonBossEnemy(entity)
                || entity instanceof BaseBossEntity boss && boss.suppressDeathDrops()) {
            ci.cancel();
        }
    }

    @Inject(method = "shouldDropExperience", at = @At("HEAD"), cancellable = true)
    private void pomkotsmechs$suppressMissionMobExperience(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (pomkotsmechs$isMissionNonBossEnemy(entity)) {
            cir.setReturnValue(false);
        }
    }

    /** Final guard for Forge's experience-drop hook. */
    @Inject(method = "dropExperience", at = @At("HEAD"), cancellable = true)
    private void pomkotsmechs$suppressMissionMobExperienceDrop(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (pomkotsmechs$isMissionNonBossEnemy(entity)) {
            ci.cancel();
        }
    }

    private static boolean pomkotsmechs$isMissionNonBossEnemy(LivingEntity entity) {
        return entity instanceof Enemy
                && !(entity instanceof BaseBossEntity)
                && entity.getTags().stream().anyMatch(tag -> tag.startsWith("pomkots_mission:"));
    }
}
