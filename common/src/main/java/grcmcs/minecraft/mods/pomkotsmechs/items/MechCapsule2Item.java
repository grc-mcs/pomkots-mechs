package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class MechCapsule2Item extends Item {
    public MechCapsule2Item(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack ignoredStack,
            Player player,
            LivingEntity target,
            InteractionHand hand
    ) {
        if (!(target instanceof Pmvc01Entity mech)) {
            return InteractionResult.PASS;
        }

        ItemStack handStack = player.getItemInHand(hand);

        if (hasStoredMech(handStack)) {
            return InteractionResult.PASS;
        }

        if (!player.level().isClientSide) {
            CompoundTag storedMech = MechCapsuleData.capture(mech);

            /*
             * 保存済みにする1個。
             *
             * copy()した時点では元のスタック数を引き継ぐので、
             * 必ず1個に設定する。
             */
            ItemStack savedCloner = handStack.copy();
            savedCloner.setCount(1);

            savedCloner.getOrCreateTag().put(
                    MechCapsuleData.TAG_STORED_MECH,
                    storedMech.copy()
            );

            if (handStack.getCount() > 1) {
                /*
                 * 元の空クローナーから1個分を除いた残り。
                 */
                ItemStack remainingEmptyCloners = handStack.copy();
                remainingEmptyCloners.shrink(1);

                player.setItemInHand(hand, remainingEmptyCloners);

                if (!player.getInventory().add(savedCloner)) {
                    player.drop(savedCloner, false);
                }
            } else {
                /*
                 * もともと1個だけなら、そのまま保存済みへ置き換える。
                 */
                player.setItemInHand(hand, savedCloner);
            }

            target.discard();

            player.getInventory().setChanged();

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.containerMenu.broadcastChanges();
            }
        }

        return InteractionResult.sidedSuccess(
                player.level().isClientSide
        );
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);

        if (!hasStoredMech(stack)) {
            return InteractionResultHolder.pass(stack);
        }

        HitResult hitResult = player.pick(
                20.0D,
                0.0F,
                false
        );

        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            return InteractionResultHolder.pass(stack);
        }

        if (level.isClientSide) {
            return InteractionResultHolder.sidedSuccess(
                    stack,
                    true
            );
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResultHolder.fail(stack);
        }

        Vec3 hitPos = blockHitResult.getLocation();

        Vec3 spawnPos = hitPos.add(
                Vec3.atLowerCornerOf(
                        blockHitResult.getDirection().getNormal()
                ).scale(0.05D)
        );

        double dx = player.getX() - spawnPos.x;
        double dz = player.getZ() - spawnPos.z;

        float spawnYaw = (float) (
                Mth.atan2(dz, dx) * Mth.RAD_TO_DEG
        ) - 90.0F;

        CompoundTag storedMech = getStoredMech(stack);

        Optional<Entity> spawned = MechCapsuleData.spawn(
                serverLevel,
                storedMech,
                spawnPos,
                spawnYaw
        );

        if (spawned.isEmpty()) {
            /*
             * 生成失敗時はクローナーを消さない。
             */
            return InteractionResultHolder.fail(stack);
        }

        /*
         * 生成成功時だけ消費する。
         *
         * 保存時に必ず1個へ切り出しているので、
         * 通常はこの処理で手から完全に消える。
         */
        stack.shrink(1);

        player.getCooldowns().addCooldown(this, 10);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public Component getName(ItemStack stack) {
        Component baseName = super.getName(stack);

        if (!hasStoredMech(stack)) {
            return baseName;
        }

        return Component.empty()
                .append(baseName)
                .append(
                        Component.literal(
                                " (Saved)"
                        ).withStyle(ChatFormatting.GREEN)
                );
    }

    private void spawnStoredMech(
            ServerLevel level,
            ItemStack capsuleStack,
            Vec3 spawnPosition
    ) {
        if (!MechCapsule2Item.hasStoredMech(capsuleStack)) {
            return;
        }

        CompoundTag storedMech =
                MechCapsule2Item.getStoredMech(capsuleStack);

        MechCapsuleData.spawn(
                level,
                storedMech,
                spawnPosition,
                0
        ).ifPresent(spawned -> {
            // 生成成功後の演出など
            capsuleStack.shrink(1);
        });
    }

    public static boolean hasStoredMech(ItemStack stack) {
        CompoundTag root = stack.getTag();

        return root != null
                && root.contains(
                MechCapsuleData.TAG_STORED_MECH,
                CompoundTag.TAG_COMPOUND
        );
    }

    public static CompoundTag getStoredMech(ItemStack stack) {
        CompoundTag root = stack.getTag();

        if (root == null) {
            return new CompoundTag();
        }

        return root.getCompound(
                MechCapsuleData.TAG_STORED_MECH
        );
    }

    public static void clearStoredMech(ItemStack stack) {
        CompoundTag root = stack.getTag();

        if (root == null) {
            return;
        }

        root.remove(MechCapsuleData.TAG_STORED_MECH);

        if (root.isEmpty()) {
            stack.setTag(null);
        }
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
