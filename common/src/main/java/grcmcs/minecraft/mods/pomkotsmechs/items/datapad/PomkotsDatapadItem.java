package grcmcs.minecraft.mods.pomkotsmechs.items.datapad;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadMenu;
import grcmcs.minecraft.mods.pomkotsmechs.items.KeycardItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.RadarTarget;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PomkotsDatapadItem extends Item {

    private static final String TAG_RADAR_TARGETS = PomkotsMechs.nbtName("RadarTargets");
    private static final String TAG_RADAR_SELECTED_INDEX = PomkotsMechs.nbtName("RadarSelectedIndex");

    private static final String TAG_ARENA = PomkotsMechs.nbtName("Arena");

    private static final String TAG_ARENA_REQUESTS = "Requests";
    private static final String TAG_RETURN_LOCATION = "ReturnLocation";
    private static final String TAG_REQUEST_UUID = "RequestUuid";
    private static final String TAG_CHALLENGER_UUID = "ChallengerUuid";
    private static final String TAG_ARENA_ID = "ArenaId";
    private static final String TAG_REGISTERED_ARENAS = "RegisteredArenas";

    private static final String TAG_RETURN_DIMENSION = "Dimension";
    private static final String TAG_RETURN_X = "X";
    private static final String TAG_RETURN_Y = "Y";
    private static final String TAG_RETURN_Z = "Z";

    private static final String TAG_KEY_CARDS =
            PomkotsMechs.nbtName("KeyCards");

    private static final String TAG_KEY_CARD_ITEMS =
            "Items";

    public static final int MAX_KEYCARD_SLOTS = 8;

    public PomkotsDatapadItem(Properties properties) {
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

    //=============================================================
    // KeyCard関連
    //=============================================================

    private static CompoundTag getKeyCardTag(
            ItemStack datapad
    ) {
        CompoundTag root =
                datapad.getOrCreateTag();

        if (!root.contains(
                TAG_KEY_CARDS,
                Tag.TAG_COMPOUND
        )) {

            root.put(
                    TAG_KEY_CARDS,
                    new CompoundTag()
            );
        }

        return root.getCompound(
                TAG_KEY_CARDS
        );
    }


    public static int getKeyCardNum(
            ItemStack datapad
    ) {
        CompoundTag root =
                datapad.getTag();

        if (root == null
                || !root.contains(
                TAG_KEY_CARDS,
                Tag.TAG_COMPOUND
        )) {
            return 0;
        }

        CompoundTag keyCardTag =
                root.getCompound(
                        TAG_KEY_CARDS
                );

        ListTag list =
                keyCardTag.getList(
                        TAG_KEY_CARD_ITEMS,
                        Tag.TAG_COMPOUND
                );


        int num = 0;

        for (int i = 0;
             i < list.size()
                     && i < MAX_KEYCARD_SLOTS;
             i++) {

            if (ItemStack.of(
                    list.getCompound(i)
            ).getItem() instanceof KeycardItem) {
                num++;
            }
        }

        return num;
    }

    public static SimpleContainer createKeyCardContainer(
            ItemStack datapad
    ) {
        SimpleContainer container =
                new SimpleContainer(
                        MAX_KEYCARD_SLOTS
                );

        CompoundTag root =
                datapad.getTag();

        if (root == null
                || !root.contains(
                TAG_KEY_CARDS,
                Tag.TAG_COMPOUND
        )) {

            return container;
        }

        CompoundTag keyCardTag =
                root.getCompound(
                        TAG_KEY_CARDS
                );

        ListTag list =
                keyCardTag.getList(
                        TAG_KEY_CARD_ITEMS,
                        Tag.TAG_COMPOUND
                );

        for (int i = 0;
             i < list.size()
                     && i < MAX_KEYCARD_SLOTS;
             i++) {

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

        for (int i = 0;
             i < container.getContainerSize();
             i++) {

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

        CompoundTag keyCardTag =
                getKeyCardTag(
                        datapad
                );

        keyCardTag.put(
                TAG_KEY_CARD_ITEMS,
                list
        );
    }

    //=============================================================
    // Radar関連
    //=============================================================

    public static void addTarget(ItemStack stack, RadarTarget target) {
        var targets = getTargets(stack);


        for (int i = 0; i < targets.size(); i++) {
            RadarTarget t = targets.get(i);

            if (t.label().equals(target.label())) {
                setSelectedIndex(stack, i);
                return;
            }
        }

        targets.add(target);
        setTargets(stack, targets);
        setSelectedIndex(stack, targets.size() - 1);
    }

    public static List<RadarTarget> getTargets(ItemStack stack) {
        ListTag list = stack.getOrCreateTag().getList(TAG_RADAR_TARGETS, Tag.TAG_COMPOUND);
        List<RadarTarget> result = new ArrayList<>();

        for (Tag tag : list) {
            result.add(RadarTarget.fromNbt((CompoundTag) tag));
        }
        return result;
    }

    public static void setTargets(ItemStack stack, List<RadarTarget> targets) {
        ListTag list = new ListTag();
        for (RadarTarget t : targets) list.add(RadarTarget.toNbt(t));
        stack.getOrCreateTag().put(TAG_RADAR_TARGETS, list);
    }

    public static int getSelectedIndex(ItemStack stack) {
        return stack.getOrCreateTag().getInt(TAG_RADAR_SELECTED_INDEX);
    }

    public static void setSelectedIndex(ItemStack stack, int index) {
        stack.getOrCreateTag().putInt(TAG_RADAR_SELECTED_INDEX, index);
    }

    public static Optional<RadarTarget> getActiveTarget(ItemStack stack) {
        var targets = getTargets(stack);
        if (targets.isEmpty()) return Optional.empty();
        int idx = Mth.clamp(getSelectedIndex(stack), 0, targets.size() - 1);
        return Optional.of(targets.get(idx));
    }

    //=============================================================
    // Arena関連
    //=============================================================

    private static CompoundTag getArenaTag(
            ItemStack datapad
    ) {
        CompoundTag root =
                datapad.getOrCreateTag();

        if (!root.contains(
                TAG_ARENA,
                Tag.TAG_COMPOUND
        )) {

            root.put(
                    TAG_ARENA,
                    new CompoundTag()
            );
        }

        return root.getCompound(
                TAG_ARENA
        );
    }

    public record ArenaRequestInfo(
            UUID requestUuid,
            UUID challengerUuid,
            String arenaId
    ) {
    }

    public static void addArenaRequest(
            ItemStack datapad,
            UUID challengerUuid,
            UUID requestUuid,
            String arenaId
    ) {
        CompoundTag arenaTag =
                getArenaTag(datapad);

        ListTag requests =
                arenaTag.getList(
                        TAG_ARENA_REQUESTS,
                        Tag.TAG_COMPOUND
                );

        CompoundTag request =
                new CompoundTag();

        request.putUUID(
                TAG_CHALLENGER_UUID,
                challengerUuid
        );

        request.putUUID(
                TAG_REQUEST_UUID,
                requestUuid
        );

        request.putString(
                TAG_ARENA_ID,
                arenaId
        );

        requests.add(request);

        arenaTag.put(
                TAG_ARENA_REQUESTS,
                requests
        );
    }

    public static boolean hasArenaRequests(
            ItemStack datapad
    ) {
        return !getArenaRequests(datapad).isEmpty();
    }

    public static List<ArenaRequestInfo> getArenaRequests(
            ItemStack datapad
    ) {
        List<ArenaRequestInfo> result =
                new ArrayList<>();

        CompoundTag root =
                datapad.getTag();

        if (root == null
                || !root.contains(
                TAG_ARENA,
                Tag.TAG_COMPOUND
        )) {

            return result;
        }

        CompoundTag arenaTag =
                root.getCompound(
                        TAG_ARENA
                );

        ListTag requests =
                arenaTag.getList(
                        TAG_ARENA_REQUESTS,
                        Tag.TAG_COMPOUND
                );

        for (int i = 0; i < requests.size(); i++) {

            CompoundTag request =
                    requests.getCompound(i);

            result.add(
                    new ArenaRequestInfo(
                            request.getUUID(
                                    TAG_REQUEST_UUID
                            ),
                            request.getUUID(
                                    TAG_CHALLENGER_UUID
                            ),
                            request.getString(
                                    TAG_ARENA_ID
                            )
                    )
            );
        }

        return result;
    }

    public static void removeArenaRequest(
            ItemStack datapad,
            UUID requestUuid
    ) {
        CompoundTag arenaTag =
                getArenaTag(datapad);

        ListTag requests =
                arenaTag.getList(
                        TAG_ARENA_REQUESTS,
                        Tag.TAG_COMPOUND
                );

        for (int i = requests.size() - 1; i >= 0; i--) {
            CompoundTag request =
                    requests.getCompound(i);
            if (requestUuid.equals(
                    request.getUUID(
                            TAG_REQUEST_UUID
                    )
            )) {
                requests.remove(i);
            }
        }

        arenaTag.put(
                TAG_ARENA_REQUESTS,
                requests
        );
    }

    public static void clearArenaRequests(
            ItemStack datapad
    ) {
        CompoundTag root =
                datapad.getTag();

        if (root == null
                || !root.contains(
                TAG_ARENA
        )) {

            return;
        }

        root.getCompound(
                TAG_ARENA
        ).remove(
                TAG_ARENA_REQUESTS
        );
    }

    //=============================================================
    // Arena登録関連
    //=============================================================

    public static void addArenaRegistration(
            ItemStack datapad,
            String arenaId
    ) {
        CompoundTag arenaTag =
                getArenaTag(datapad);

        ListTag list =
                arenaTag.getList(
                        TAG_REGISTERED_ARENAS,
                        Tag.TAG_STRING
                );

        // 重複防止
        for (int i = 0; i < list.size(); i++) {
            if (arenaId.equals(
                    list.getString(i)
            )) {
                return;
            }
        }

        list.add(
                StringTag.valueOf(
                        arenaId
                )
        );

        arenaTag.put(
                TAG_REGISTERED_ARENAS,
                list
        );
    }

    public static void removeArenaRegistration(
            ItemStack datapad,
            String arenaId
    ) {
        CompoundTag arenaTag =
                getArenaTag(datapad);

        ListTag list =
                arenaTag.getList(
                        TAG_REGISTERED_ARENAS,
                        Tag.TAG_STRING
                );

        for (int i = list.size() - 1; i >= 0; i--) {

            if (arenaId.equals(
                    list.getString(i)
            )) {

                list.remove(i);
            }
        }

        arenaTag.put(
                TAG_REGISTERED_ARENAS,
                list
        );
    }

    public static boolean isArenaRegistered(
            ItemStack datapad,
            String arenaId
    ) {
        CompoundTag root =
                datapad.getTag();

        if (root == null
                || !root.contains(
                TAG_ARENA,
                Tag.TAG_COMPOUND
        )) {

            return false;
        }

        ListTag list =
                root.getCompound(
                        TAG_ARENA
                ).getList(
                        TAG_REGISTERED_ARENAS,
                        Tag.TAG_STRING
                );

        for (int i = 0; i < list.size(); i++) {

            if (arenaId.equals(
                    list.getString(i)
            )) {

                return true;
            }
        }

        return false;
    }

    public static List<String> getRegisteredArenaIds(
            ItemStack datapad
    ) {
        List<String> result =
                new ArrayList<>();

        CompoundTag root =
                datapad.getTag();

        if (root == null
                || !root.contains(
                TAG_ARENA,
                Tag.TAG_COMPOUND
        )) {

            return result;
        }

        ListTag list =
                root.getCompound(
                        TAG_ARENA
                ).getList(
                        TAG_REGISTERED_ARENAS,
                        Tag.TAG_STRING
                );

        for (int i = 0; i < list.size(); i++) {

            result.add(
                    list.getString(i)
            );
        }

        return result;
    }

    //=============================================================
    // Arenaの帰還Pos関連
    //=============================================================

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
        CompoundTag arenaTag =
                getArenaTag(datapad);

        CompoundTag returnTag =
                new CompoundTag();

        returnTag.putString(
                TAG_RETURN_DIMENSION,
                dimension.location().toString()
        );

        returnTag.putInt(
                TAG_RETURN_X,
                pos.getX()
        );

        returnTag.putInt(
                TAG_RETURN_Y,
                pos.getY()
        );

        returnTag.putInt(
                TAG_RETURN_Z,
                pos.getZ()
        );

        arenaTag.put(
                TAG_RETURN_LOCATION,
                returnTag
        );
    }

    @Nullable
    public static ReturnLocation getReturnLocation(
            ItemStack datapad
    ) {
        CompoundTag root =
                datapad.getTag();

        if (root == null
                || !root.contains(
                TAG_ARENA,
                Tag.TAG_COMPOUND
        )) {

            return null;
        }

        CompoundTag arenaTag =
                root.getCompound(
                        TAG_ARENA
                );

        if (!arenaTag.contains(
                TAG_RETURN_LOCATION,
                Tag.TAG_COMPOUND
        )) {

            return null;
        }

        CompoundTag returnTag =
                arenaTag.getCompound(
                        TAG_RETURN_LOCATION
                );

        ResourceKey<Level> dimension =
                ResourceKey.create(
                        Registries.DIMENSION,
                        new ResourceLocation(
                                returnTag.getString(
                                        TAG_RETURN_DIMENSION
                                )
                        )
                );

        return new ReturnLocation(
                dimension,
                new BlockPos(
                        returnTag.getInt(
                                TAG_RETURN_X
                        ),
                        returnTag.getInt(
                                TAG_RETURN_Y
                        ),
                        returnTag.getInt(
                                TAG_RETURN_Z
                        )
                )
        );
    }

    public static void clearReturnLocation(
            ItemStack datapad
    ) {
        CompoundTag root =
                datapad.getTag();

        if (root == null
                || !root.contains(
                TAG_ARENA
        )) {

            return;
        }

        root.getCompound(
                TAG_ARENA
        ).remove(
                TAG_RETURN_LOCATION
        );
    }

    public static boolean hasReturnLocation(
            ItemStack datapad
    ) {
        return getReturnLocation(
                datapad
        ) != null;
    }

    public static List<ItemStack> getPlayersDataPads(Player player) {
        List<ItemStack> res = new ArrayList<>();

        for (var ent: player.getInventory().items) {
            if (ent.getItem() instanceof PomkotsDatapadItem) {
                res.add(ent);
            }
        }

        return res;
    }
}
