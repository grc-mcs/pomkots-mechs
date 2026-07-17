package grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.KeycardItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaFighterData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaMechData;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ArenaReceptionistMenu
        extends AbstractContainerMenu {

    public static final int MODE_RECEPTION = 0;
    public static final int MODE_DATAPAD = 1;

    public static final int BTN_ID_ACCEPT_MATCH = 0;
    public static final int BTN_ID_DECLINE_MATCH = 1;
    public static final int BTN_ID_RETURN_FROM_MATCH = 2;

    private final SimpleContainer dataPadContainerForRegistration = new SimpleContainer(1);

    private String arenaId = "";
    private int mode = MODE_RECEPTION;
    private ItemStack dataPadStack = ItemStack.EMPTY;
    private List<ArenaRankingEntry> rankings =
            List.of();

    /**
     * openExtendedMenu用
     */
    public ArenaReceptionistMenu(
            int containerId,
            Inventory playerInventory,
            FriendlyByteBuf buf
    ) {
        this(containerId, playerInventory, getData(buf));
    }

    private static ArenaMenuData.Entry getData(FriendlyByteBuf buf) {
        ArenaMenuData.Entry data = null;
        if (buf != null) {
            data = ArenaMenuData.read(buf);
        }

        return data;
    }

    /**
     * MenuType用
     */
    public ArenaReceptionistMenu(
            int containerId,
            Inventory playerInventory,
            ArenaMenuData.Entry extendedData
    ) {
        super(
                PomkotsMechs.ARENA_RECEPTIONIST.get(),
                containerId
        );
        addSlots(playerInventory);

        if (extendedData != null) {
            this.arenaId = extendedData.getArenaId();
            this.mode = extendedData.getMode();
            this.dataPadStack = extendedData.getMechDataPadStack();
            this.rankings = extendedData.getRankings();
        }
    }

    private void addSlots(Inventory playerInventory) {
        addSlot(
                new ToggleableSlot(
                        dataPadContainerForRegistration,
                        0,
                        40,
                        59 + 105
                ) {
                    @Override
                    public boolean mayPlace(
                            ItemStack stack
                    ) {
                        return stack.is(
                                PomkotsMechs.POMKOTS_DATAPAD_ITEM.get()
                        );
                    }
                }
        );

        addPlayerInventory(playerInventory);
    }

    private void addPlayerInventory(
            Inventory inventory
    ) {

        int offsetX = 310 + 2;
        int offsetY = 35 + 18;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new ToggleableSlot(inventory, col + row * 9 + 9, offsetX + col * 18, offsetY + row * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new ToggleableSlot(inventory, i, offsetX + i * 18, offsetY + 3 * 18 + 4));
        }
    }

    public String getArenaId() {
        return arenaId;
    }

    public List<ArenaRankingEntry> getRankings() {
        return rankings;
    }

    public ItemStack getDataPadStack() {
        return dataPadStack;
    }

    public int getMode() {
        return mode;
    }

    @Override
    public boolean stillValid(
            Player player
    ) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int slot
    ) {
        return ItemStack.EMPTY;
    }

    public static class ToggleableSlot extends Slot {
        private boolean active = true;

        public ToggleableSlot(
                Container container,
                int slot,
                int x,
                int y
        ) {
            super(container, slot, x, y);
        }

        @Override
        public boolean isActive() {
            return active;
        }

        public void setActive(
                boolean active
        ) {
            this.active = active;
        }
    }

    public void savePlayerProfile(Player player, String name, String comment, String arenaId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        var dataPadItemStack = dataPadContainerForRegistration.getItem(0);

        var registeredInfo = ArenaManager.getFighter(serverPlayer.server, arenaId, serverPlayer.getUUID());

        if (registeredInfo == null) {
            ArenaFighterData data = new ArenaFighterData();

            // 新規作成
            if (name.isEmpty()) {
                name = player.getName().getString();

            }

            if (comment.isEmpty()) {
                comment = "NO COMMENT";

            }

            if (!(dataPadItemStack.getItem() instanceof PomkotsDatapadItem)) {
                sendMessage("Set your Pomkots Data Pad to the slot", player);
                return;
            }

            data.setType(ArenaFighterData.FighterType.PLAYER);
            data.setRating(0);
            data.setFighterId(player.getUUID());
            data.setDisplayName(name);
            data.setComment(comment);

            var mech = getMechDataFromDataPad(dataPadItemStack, player);

            if (mech != null) {
                data.setMechData(mech);
                System.out.println(mech.getMechName());
            } else {
                sendMessage("No mech data is registered in your Data Pad", player);
                return;
            }

            PomkotsDatapadItem.addArenaRegistration(dataPadItemStack, arenaId);
            backDataPad(player);

            ArenaManager.registerFighter(serverPlayer.server, arenaId, data);

            Utils.completeAdvancement(PomkotsMechs.id("pomkots_mechs/arena_register"), serverPlayer);

        } else {
            // 更新
            boolean changed = false;

            if (!name.isEmpty()) {
                registeredInfo.setDisplayName(name);
                changed = true;
            }

            if (!comment.isEmpty()) {
                registeredInfo.setComment(comment);
                changed = true;
            }

            if (dataPadItemStack.getItem() instanceof PomkotsDatapadItem) {
                var mech = getMechDataFromDataPad(dataPadItemStack, player);
                if (mech != null) {
                    registeredInfo.setMechData(mech);
                    changed = true;
                }

                backDataPad(player);
            }

            if (changed) {
                ArenaManager.update(serverPlayer.server, arenaId);
            }
        }
    }

    private void backDataPad(Player player) {
        ItemStack card = dataPadContainerForRegistration.getItem(0);
        dataPadContainerForRegistration.setItem(0, ItemStack.EMPTY);

        if (!card.isEmpty() && !player.getInventory().add(card)) {
            player.drop(
                    card,
                    false
            );
        }
    }

    private ArenaMechData getMechDataFromDataPad(ItemStack dataPadItemStack, Player player) {
        ArenaMechData res = null;

        for (var card: PomkotsDatapadItem.createKeyCardContainer(dataPadItemStack).items) {
            if (card.getItem() instanceof KeycardItem) {
                res = getMechDataFromKeyCard(card, player);
                break;
            }
        }

        return res;
    }

    private ArenaMechData getMechDataFromKeyCard(ItemStack cardItemStack, Player player) {
        CompoundTag tag = cardItemStack.getTag();

        if (tag == null || !tag.hasUUID(KeycardItem.NBT_MECH_UUID)) {
            sendMessage("Invalid Key Card", player);
            return null;
        }

        UUID mechUuid = tag.getUUID(KeycardItem.NBT_MECH_UUID);
        Entity entity = ((ServerLevel)player.level()).getEntity(mechUuid);

        if (!(entity instanceof Pmvc01Entity mech)) {
            sendMessage("Bring mech near by", player);
            return null;
        }

        return ArenaMechData.fromMechInstance(mech);
    }

    public void removePlayerProfile(Player player, String arenaId) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        var dataPadItemStack = dataPadContainerForRegistration.getItem(0);
        if (dataPadItemStack.getItem() instanceof PomkotsDatapadItem) {
            PomkotsDatapadItem.removeArenaRegistration(dataPadItemStack, arenaId);
            backDataPad(player);

            ArenaManager.removeFighter(serverPlayer.server, arenaId, serverPlayer.getUUID());

        } else {
            sendMessage("Set your Data Pad to remove", player);

        }
    }

    private void sendMessage(String msg, Player player) {
        player.sendSystemMessage(
                Component.literal(
                        msg
                )
        );
    }

    public void setRankings(List<ArenaRankingEntry> rankings) {
        this.rankings = rankings;
    }

    @Override
    public void removed(
            Player player
    ) {
        backDataPad(player);
        super.removed(player);
    }


    @Override
    public boolean clickMenuButton(
            Player player,
            int id
    ) {
        if (player instanceof ServerPlayer serverPlayer) {
            switch (id) {
                case BTN_ID_ACCEPT_MATCH -> {
                    if (dataPadStack == null) {
                        return true;
                    }

                    if (serverPlayer.getVehicle() instanceof Pmvc01Entity mech) {
                        for (var req: PomkotsDatapadItem.getArenaRequests(dataPadStack)) {
                            var match = ArenaManager.getActiveMatch(arenaId, req.requestUuid(), serverPlayer.getServer());
                            if (match != null) {
                                if (match.getTeleportPointPos() != null) {
                                    PomkotsDatapadItem.setReturnLocation(dataPadStack, serverPlayer.level().dimension(), mech.blockPosition());
                                    PomkotsDatapadItem.clearArenaRequests(dataPadStack);

                                    var pos = match.getTeleportPointPos();
                                    mech.teleportTo(
                                            pos.getX(),
                                            pos.getY(),
                                            pos.getZ()
                                    );


                                    break;
                                }
                            }
                        }
                    } else {
                        serverPlayer.sendSystemMessage(Component.literal("Ride a mech before teleport"));
                    }
                }
                case BTN_ID_DECLINE_MATCH -> {
                    if (dataPadStack == null) {
                        return true;
                    }

                    for (var req: PomkotsDatapadItem.getArenaRequests(dataPadStack)) {
                        var match = ArenaManager.getActiveMatch(arenaId, req.requestUuid(), serverPlayer.getServer());
                        if (match != null) {
                            ArenaManager.cancelMatch("Declined from opponent", arenaId, match.getMatchId(), serverPlayer.server);
                        }
                    }
                }
                case BTN_ID_RETURN_FROM_MATCH -> {
                    if (serverPlayer.getVehicle() instanceof Pmvc01Entity mech) {
                        PomkotsDatapadItem.ReturnLocation location =
                                PomkotsDatapadItem.getReturnLocation(
                                        dataPadStack
                                );
                        if (location != null) {
                            ServerLevel targetLevel =
                                    serverPlayer.server.getLevel(
                                            location.dimension()
                                    );

                            if (targetLevel != null) {
                                PomkotsDatapadItem.clearReturnLocation(dataPadStack);

                                mech.teleportTo(
                                        targetLevel,
                                        location.pos().getX() + 0.5,
                                        location.pos().getY(),
                                        location.pos().getZ() + 0.5,
                                        Set.of(),
                                        player.getYRot(),
                                        player.getXRot()
                                );
                            }
                        }
                    } else {
                        serverPlayer.sendSystemMessage(Component.literal("Ride a mech before teleport"));
                    }
                }
            }
        }

        return true;
    }
}