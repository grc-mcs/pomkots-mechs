package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class BotDriverRenderer <T extends LivingEntity, M extends EntityModel<T>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void onRender(T entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
        // エンティティが特定のエンティティに乗っている場合、座標を調整する
        if (entity.getVehicle() instanceof PomkotsVehicle robot) {
            Vec3 seatPos = robot.getClientSeatPos(entity);

            // エンティティの位置を座席位置に変更
            matrices.translate(seatPos.x(), seatPos.y(), seatPos.z());
        } else if (entity.getVehicle() instanceof Pmb01Entity robot) {
            Vec3 seatPos = robot.getClientSeatPos();

            // エンティティの位置を座席位置に変更
            matrices.translate(seatPos.x(), seatPos.y(), seatPos.z());
        }
    }
}
