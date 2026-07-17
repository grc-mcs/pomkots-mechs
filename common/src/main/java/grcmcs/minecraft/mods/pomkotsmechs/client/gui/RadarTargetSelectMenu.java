package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad.DataPadMenu;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.PomkotsRadarItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class RadarTargetSelectMenu extends AbstractContainerMenu {

    private ItemStack dataPadStack = null;
    private boolean openedFromDatapad = false;
    private boolean stillValid = true;

    public RadarTargetSelectMenu(int syncId, Inventory inv) {
        this(syncId, inv, null);
    }

    public RadarTargetSelectMenu(int syncId, Inventory inv, Player p) {
        this(syncId, inv, p, false);
    }

    public RadarTargetSelectMenu(int syncId, Inventory inv, Player p, boolean openedFromDatapad) {
        super(PomkotsMechs.POMKOTS_RADAR_GUI.get(), syncId);

        this.openedFromDatapad = openedFromDatapad;

        // メインハンドのレーダーを取得
        var mainItem = inv.player.getMainHandItem();
        var offItem = inv.player.getOffhandItem();
        var fromInv = PomkotsDatapadItem.getPlayersDataPads(inv.player);

        if (mainItem.getItem() instanceof PomkotsRadarItem || mainItem.getItem() instanceof PomkotsDatapadItem) {
            this.dataPadStack = mainItem;

        } else if (offItem.getItem() instanceof PomkotsRadarItem || offItem.getItem() instanceof PomkotsDatapadItem) {
            this.dataPadStack = offItem;

        } else if (!fromInv.isEmpty()) {
            this.dataPadStack = fromInv.get(0);

        } else {
            this.dataPadStack = ItemStack.EMPTY;

        }
    }

    public void setSelectedTarget(int idx) {
        ItemStack stack = getRadarStack();

        if (stack.getItem() instanceof PomkotsRadarItem) {
            PomkotsRadarItem.setSelectedIndex(stack, idx);
        } else if (stack.getItem() instanceof PomkotsDatapadItem) {
            PomkotsDatapadItem.setSelectedIndex(stack, idx);
        }

//        stillValid = false;
    }

    public ItemStack getRadarStack() { return dataPadStack; }

    @Override
    public boolean stillValid(Player player) { return stillValid; }

    @Override
    public ItemStack quickMoveStack(Player player, int i) { return ItemStack.EMPTY; }

    @Override
    public void removed(
            Player player
    ) {
        super.removed(player);
    }

    private boolean isOpenedFromDatapad() {
        return openedFromDatapad;
    }

    private ItemStack getDatapadItemStackForMenu() {
        return dataPadStack;
    }

    private void reopenDatapadMenu(Player player) {
        if (player instanceof ServerPlayer serverPlayer
                && isOpenedFromDatapad()
                && getDatapadItemStackForMenu() != null
                && getDatapadItemStackForMenu().getItem() instanceof PomkotsDatapadItem) {

            serverPlayer.server.execute(
                    () -> {
                        serverPlayer.openMenu(
                                new SimpleMenuProvider(
                                        (id, inv, p) ->
                                                new DataPadMenu(
                                                        id,
                                                        inv,
                                                        p,
                                                        getDatapadItemStackForMenu()
                                                ),
                                        Component.literal("Pomkots Datapad")
                                )
                        );
                    }
            );
        }
    }
}
