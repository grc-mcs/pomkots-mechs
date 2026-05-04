package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.turret.Pmvt01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class KeycardItem extends Item {

    public static final String NBT_MECH_UUID = PomkotsMechs.nbtName("MechUUID");

    public KeycardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand interactionHand) {
        Level level = player.level();

        if (level instanceof ServerLevel serverLevel && target instanceof Pmvc01Entity mech) {
            if (isTargetMech(mech, itemStack, serverLevel)) {
                if (mech.isLocked()) {
                    player.sendSystemMessage(Utils.string2Component("{text.pomkotsmechs.messages.pmvc01.unlock}"));

                } else {
                    player.sendSystemMessage(Utils.string2Component("{text.pomkotsmechs.messages.pmvc01.lock}"));

                }
                mech.setLocked(!mech.isLocked());
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public boolean isTargetMech(Pmvc01Entity mech, ItemStack stack, Level level) {
        if (!stack.hasTag() || !stack.getTag().contains(NBT_MECH_UUID) || !(level instanceof ServerLevel sl)) {
            return false;
        } else {
            var uuid = stack.getTag().getUUID(NBT_MECH_UUID);
            return uuid.equals(mech.getUUID());
        }
    }

    public void setMech(ItemStack stack, Pmvc01Entity mech, Level level) {
        var tag = stack.getOrCreateTag();
        tag.putUUID(NBT_MECH_UUID, mech.getUUID());
        stack.setTag(tag);
    }
}
