package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb99Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.Pmb01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
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
    public void onRender(T entity, float yaw, float tickDelta, PoseStack poseStack, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
        // エンティティが特定のエンティティに乗っている場合、座標を調整する
        if (entity.getVehicle() instanceof PomkotsVehicle robot) {
            Vec3 seatPos = robot.getClientSeatPos(entity);

            //@JOKE
            if (robot instanceof Pmvc01Entity pmvc && pmvc.getVehicle() instanceof Pmb99Entity pmb99) {
                poseStack.translate(seatPos.x() + pmb99.seatPos.x, seatPos.y() + pmb99.seatPos.y, seatPos.z() + pmb99.seatPos.z());
            }

            // エンティティの位置を座席位置に変更
            if (robot instanceof Pmvc01Entity pmvc && pmvc.getHeadParts().getItem() instanceof BasePartsItem.Head h && h.isFullCovered()) {
                poseStack.translate(seatPos.x(), seatPos.y() - 1, seatPos.z());
            } else {
                poseStack.translate(seatPos.x(), seatPos.y(), seatPos.z());
            }
        } else if (entity.getVehicle() instanceof Pmb01Entity robot) {
            Vec3 seatPos = robot.getClientSeatPos();

            // エンティティの位置を座席位置に変更
            poseStack.translate(seatPos.x(), seatPos.y(), seatPos.z());
        }
    }
}
