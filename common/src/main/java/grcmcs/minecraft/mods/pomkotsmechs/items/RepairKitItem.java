package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class RepairKitItem extends Item {
    public static final int HEAL_AMOUNT = 50;

    public RepairKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand interactionHand) {
        Level level = player.level();

        if (level instanceof ServerLevel serverLevel && canRepair(target)) {
            float healed = Math.min(target.getMaxHealth(), target.getHealth() + HEAL_AMOUNT);
            target.setHealth(healed);
            healEffect(serverLevel, target);

            itemStack.shrink(1);
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    protected boolean canRepair(LivingEntity target) {
        return target.getMaxHealth() > target.getHealth() && target instanceof Pmvc01Entity;
    }

    public void healEffect(ServerLevel level, LivingEntity target) {
        // パーティクルを出す
        for (int i = 0; i < 10; i++) {
            double dx = target.getX() + (level.random.nextDouble() - 0.5) * target.getBbWidth();
            double dy = target.getY() + level.random.nextDouble() * target.getBbHeight();
            double dz = target.getZ() + (level.random.nextDouble() - 0.5) * target.getBbWidth();
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, dx, dy, dz, 1, 0.0, 0.1, 0.0, 0.1);
        }

        // SEを再生
        level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.2f);
    }
}
