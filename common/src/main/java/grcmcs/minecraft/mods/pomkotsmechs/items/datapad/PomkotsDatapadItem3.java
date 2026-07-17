package grcmcs.minecraft.mods.pomkotsmechs.items.datapad;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PomkotsDatapadItem3 extends Item {
    private static final String TAG_KEY_CARDS = PomkotsMechs.nbtName("KeyCards");

    private static final String TAG_ARENA_REQUEST_UUID = PomkotsMechs.nbtName("ArenaRequestUuid");
    private static final String TAG_ARENA_ID = PomkotsMechs.nbtName("ArenaId");

    private static final String TAG_RETURN_DIMENSION = PomkotsMechs.nbtName("ReturnDimension");

    private static final String TAG_RETURN_X = PomkotsMechs.nbtName("ReturnPosX");
    private static final String TAG_RETURN_Y = PomkotsMechs.nbtName("ReturnPosY");
    private static final String TAG_RETURN_Z = PomkotsMechs.nbtName("ReturnPosZ");


    public static final int MAX_KEYCARD_SLOTS = 8;

    public PomkotsDatapadItem3(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            player.openMenu(
                    new SimpleMenuProvider(
                            (id, inv, p) ->
                                    new DataPadMenu(
                                            id,
                                            inv,
                                            p,
                                            p.getItemInHand(hand)),
                            Component.literal("Pomkots Datapad")
                    )
            );
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    public static SimpleContainer createKeyCardContainer(
            ItemStack datapad
    ) {
        SimpleContainer container =
                new SimpleContainer(MAX_KEYCARD_SLOTS);

        CompoundTag tag =
                datapad.getTag();

        if (tag == null) {
            return container;
        }

        ListTag list =
                tag.getList(
                        TAG_KEY_CARDS,
                        Tag.TAG_COMPOUND
                );

        for (int i = 0; i < list.size() && i < MAX_KEYCARD_SLOTS; i++) {

            container.setItem(
                    i,
                    ItemStack.of(
                            list.getCompound(i)
                    )
            );
        }

        return container;
    }

    public static void saveKeyCardContainer(
            ItemStack datapad,
            Container container
    ) {
        ListTag list =
                new ListTag();

        for (int i = 0; i < container.getContainerSize(); i++) {

            ItemStack stack =
                    container.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            CompoundTag itemTag =
                    new CompoundTag();

            stack.save(itemTag);

            list.add(itemTag);
        }

        datapad.getOrCreateTag()
                .put(
                        TAG_KEY_CARDS,
                        list
                );
    }

    public static boolean isMatchApplied(ItemStack stack) {
        return true;
    }

    public static boolean isReturnPosEnabled(ItemStack stack) {
        return true;
    }

    public record ArenaRequestInfo(
            UUID requestUuid,
            String arenaId
    ) {
    }

    public static void setArenaRequest(
            ItemStack datapad,
            UUID requestUuid,
            String arenaId
    ) {
        CompoundTag tag =
                datapad.getOrCreateTag();

        tag.putUUID(
                TAG_ARENA_REQUEST_UUID,
                requestUuid
        );

        tag.putString(
                TAG_ARENA_ID,
                arenaId
        );
    }

    @Nullable
    public static ArenaRequestInfo getArenaRequest(
            ItemStack datapad
    ) {
        CompoundTag tag =
                datapad.getTag();

        if (tag == null
                || !tag.hasUUID(
                TAG_ARENA_REQUEST_UUID
        )
                || !tag.contains(
                TAG_ARENA_ID
        )) {

            return null;
        }

        return new ArenaRequestInfo(
                tag.getUUID(
                        TAG_ARENA_REQUEST_UUID
                ),
                tag.getString(
                        TAG_ARENA_ID
                )
        );
    }

    public static boolean hasArenaRequest(
            ItemStack datapad
    ) {
        return getArenaRequest(
                datapad
        ) != null;
    }

    public static void clearArenaRequest(
            ItemStack datapad
    ) {
        CompoundTag tag =
                datapad.getTag();

        if (tag == null) {
            return;
        }

        tag.remove(
                TAG_ARENA_REQUEST_UUID
        );

        tag.remove(
                TAG_ARENA_ID
        );
    }

    public record ReturnLocation(
            ResourceKey<Level> dimension,
            BlockPos pos
    ) {
    }

    public static void setReturnLocation(
            ItemStack datapad,
            ResourceKey<Level> dimension,
            BlockPos pos
    ) {
        CompoundTag tag =
                datapad.getOrCreateTag();

        tag.putString(
                TAG_RETURN_DIMENSION,
                dimension.location().toString()
        );

        tag.putInt(
                TAG_RETURN_X,
                pos.getX()
        );

        tag.putInt(
                TAG_RETURN_Y,
                pos.getY()
        );

        tag.putInt(
                TAG_RETURN_Z,
                pos.getZ()
        );
    }

    @Nullable
    public static ReturnLocation getReturnLocation(
            ItemStack datapad
    ) {
        CompoundTag tag =
                datapad.getTag();

        if (tag == null
                || !tag.contains(TAG_RETURN_DIMENSION)) {
            return null;
        }

        ResourceLocation dimensionId =
                new ResourceLocation(
                        tag.getString(
                                TAG_RETURN_DIMENSION
                        )
                );

        ResourceKey<Level> dimension =
                ResourceKey.create(
                        Registries.DIMENSION,
                        dimensionId
                );

        BlockPos pos =
                new BlockPos(
                        tag.getInt(TAG_RETURN_X),
                        tag.getInt(TAG_RETURN_Y),
                        tag.getInt(TAG_RETURN_Z)
                );

        return new ReturnLocation(
                dimension,
                pos
        );
    }

    public static boolean hasReturnLocation(
            ItemStack datapad
    ) {
        CompoundTag tag =
                datapad.getTag();

        return tag != null
                && tag.contains(
                TAG_RETURN_DIMENSION
        );
    }

    public static void clearReturnLocation(
            ItemStack datapad
    ) {
        CompoundTag tag =
                datapad.getTag();

        if (tag == null) {
            return;
        }

        tag.remove(TAG_RETURN_DIMENSION);

        tag.remove(TAG_RETURN_X);
        tag.remove(TAG_RETURN_Y);
        tag.remove(TAG_RETURN_Z);
    }
}
