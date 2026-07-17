package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader.MechTraderEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader.TraderInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MechTraderMenu extends AbstractContainerMenu {
    private final MechTraderEntity trader;

    private MechWorkbenchMenu.ShortDataSlot entityId = new MechWorkbenchMenu.ShortDataSlot();

    public MechTraderMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                null
        );
    }

    public MechTraderMenu(
            int containerId,
            Inventory playerInventory,
            MechTraderEntity trader
    ) {
        super(
                PomkotsMechs.MECH_TRADER_GUI.get(),
                containerId
        );

        this.trader = trader;

        addTraderSlots(trader);
        addPlayerInventory(playerInventory);

        entityId = (MechWorkbenchMenu.ShortDataSlot)this.addDataSlot(entityId);
    }

    public short getEntityId() {
        return entityId.getShort();
    }

    public void setEntityId(int id) {
        this.setData(0, (short) (id & 0xFFFF));
    }

    private void addTraderSlots(MechTraderEntity trader) {
        Container traderInv;

        if (trader == null) {
            traderInv = new TraderInventory();
        } else {
            traderInv = trader.getInventory();
        }

        int offsetX = 30;
        int offsetY = 35 + 18;

        for (MechTraderEntity.TraderCategory category
                : MechTraderEntity.TraderCategory.values()) {

            int col = category.ordinal();

            for (int row = 0;
                 row < TraderInventory.OFFERS_PER_CATEGORY;
                 row++) {

                int offerSlot =
                        TraderInventory.getOfferSlot(
                                category,
                                row
                        );

                int priceSlot =
                        TraderInventory.getPriceSlot(
                                category,
                                row
                        );

                int y = offsetY + row * 22;

                int offerX = offsetX + col * 65;
                int priceX = offerX + 30;

                addSlot(
                        new Slot(
                                traderInv,
                                offerSlot,
                                offerX,
                                y
                        ) {
                            @Override
                            public boolean mayPlace(ItemStack stack) {
                                return false;
                            }

                            @Override
                            public boolean mayPickup(Player player) {
                                return false;
                            }

                            @Override
                            public boolean isActive() {
                                return !getItem().isEmpty();
                            }
                        }
                );

                addSlot(
                        new Slot(
                                traderInv,
                                priceSlot,
                                priceX,
                                y
                        ) {
                            @Override
                            public boolean mayPlace(ItemStack stack) {
                                return false;
                            }

                            @Override
                            public boolean mayPickup(Player player) {
                                return false;
                            }

                            @Override
                            public boolean isActive() {
                                return !getItem().isEmpty();
                            }
                        }
                );
            }
        }
    }

    private void addPlayerInventory(
            Inventory inventory
    ) {

        int offsetX = 310 + 2;
        int offsetY = 35 + 18;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, offsetX + col * 18, offsetY + row * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(inventory, i, offsetX + i * 18, offsetY + 3 * 18 + 4));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return trader.isAlive()
                && trader.distanceTo(player) < 8
                && !trader.isLeaving();
    }

    @Override
    public boolean clickMenuButton(
            Player player,
            int id
    ) {
        if (player instanceof ServerPlayer sp) {
            buy(sp, id);
        }

        return true;
    }

    public void buy(ServerPlayer player, int slot) {
        if (player.level().isClientSide() || trader == null) {
            return;
        }

        trader.buy(
                player,
                slot
        );
    }
}