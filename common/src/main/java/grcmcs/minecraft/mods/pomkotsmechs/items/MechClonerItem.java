package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
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

public class MechClonerItem extends Item {
    public MechClonerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand interactionHand) {
        Level level = player.level();

        if (level instanceof ServerLevel serverLevel && target instanceof Pmvc01Entity mech) {
            saveMechToCard(mech, ((ServerPlayer)player).getMainHandItem());
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        loadMechFromCard(stack, player, world);

        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
    }

    public static void saveMechToCard(Pmvc01Entity mech, ItemStack cardItem) {
        if (mech == null || cardItem.isEmpty()) return;

        // カードアイテムのNBTを準備
        CompoundTag cardTag = cardItem.getOrCreateTag();

        // MechのContainer内の全パーツをNBTとして保存
        ListTag itemList = new ListTag();

        for (int i = 0; i < mech.getContainerSize(); i++) {
            ItemStack stack = mech.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag stackTag = new CompoundTag();
                stack.save(stackTag);
                stackTag.putByte("Slot", (byte) i);
                itemList.add(stackTag);
            }
        }

        // Itemsとして保存
        cardTag.put("Items", itemList);
        cardTag.putString("StoredMechName", mech.getDisplayName().getString());
        cardTag.putUUID("StoredMechUUID", mech.getUUID());

        cardItem.setTag(cardTag);
        cardItem.save(cardTag);
    }

    public static void loadMechFromCard(ItemStack cardItem, Player player, Level level) {
        Pmvc01Entity mech = summonRobot(player, level);

        if (cardItem.isEmpty() || mech == null) return;

        CompoundTag cardTag = cardItem.getTag();

        if (cardTag == null || !cardTag.contains("Items", Tag.TAG_LIST)) return;

        ListTag itemList = cardTag.getList("Items", Tag.TAG_COMPOUND);

        // 一旦Mechのスロットをクリア
        for (int i = 0; i < mech.getContainerSize(); i++) {
            mech.setItem(i, ItemStack.EMPTY);
        }

        // カードのデータから再生成
        for (Tag base : itemList) {
            CompoundTag stackTag = (CompoundTag) base;
            int slot = stackTag.getByte("Slot") & 255;

            ItemStack stack = ItemStack.of(stackTag);

            if (!stack.isEmpty() && slot < mech.getContainerSize()) {
                mech.setItem(slot, stack);
            }
        }

        mech.setChanged();
    }

    private static Pmvc01Entity summonRobot(Player player, Level world) {
        Pmvc01Entity robot = new Pmvc01Entity(PomkotsMechs.PMVC01.get(), world);
        robot.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        world.addFreshEntity(robot);

        return robot;
    }
}
