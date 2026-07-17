package grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.KeycardItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DataPadKeyCardMenu
        extends AbstractContainerMenu {

    private final Container keyCardContainer;
    private final ItemStack datapad;

    public DataPadKeyCardMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(containerId, playerInventory, (ItemStack) null);
    }
    public DataPadKeyCardMenu(
            int containerId,
            Inventory playerInventory,
            ItemStack datapad
    ) {
        super(
                PomkotsMechs.POMKOTS_DATAPAD_KEYCARD_GUI.get(),
                containerId
        );

        this.datapad = datapad;

        if (this.datapad != null) {
            this.keyCardContainer =
                    PomkotsDatapadItem
                            .createKeyCardContainer(
                                    datapad
                            );
        } else {
            this.keyCardContainer = new SimpleContainer(PomkotsDatapadItem.MAX_KEYCARD_SLOTS);
        }
        addDataPadInventory(this.keyCardContainer);
        addPlayerInventory(playerInventory);
    }

    private void addDataPadInventory(Container container) {
        int offsetX = 20 + 28 + 13;
        int offsetY = 40 + 30;

        int slot = 0;
        for (int row = 0; row < container.getContainerSize(); row++) {
            addSlot(
                new Slot(
                        container,
                        row,
                        offsetX,
                        offsetY + row * 18
                ) {

                    @Override
                    public boolean mayPlace(
                            ItemStack stack
                    ) {
                        return stack.getItem()
                                instanceof KeycardItem;
                    }
                }
            );
        }
    }

    private void addPlayerInventory(
            Inventory inventory
    ) {
        int offsetX = 20 + 28 + 164 + 28 + 28 + 2;
        int offsetY = 40 + 26;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                if (inventory.getItem(col + row * 9 + 9).is(PomkotsMechs.POMKOTS_DATAPAD_ITEM.get())) {
                    this.addSlot(new Slot(inventory, col + row * 9 + 9, offsetX + col * 18, offsetY + row * 18){
                        @Override
                        public boolean mayPickup(
                                Player player
                        ) {
                            return false;
                        }
                    });
                } else {
                    this.addSlot(new Slot(inventory, col + row * 9 + 9, offsetX + col * 18, offsetY + row * 18));
                }
            }
        }

        for (int i = 0; i < 9; i++) {
            if (inventory.getItem(i).is(PomkotsMechs.POMKOTS_DATAPAD_ITEM.get())) {
                this.addSlot(new Slot(inventory, i, offsetX + i * 18, offsetY + 3 * 18 + 4){
                    @Override
                    public boolean mayPickup(
                            Player player
                    ) {
                        return false;
                    }
                });
            } else {
                this.addSlot(new Slot(inventory, i, offsetX + i * 18, offsetY + 3 * 18 + 4));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int index
    ) {
        ItemStack result = ItemStack.EMPTY;

        Slot slot = slots.get(index);

        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index < 8) {
            if (!moveItemStackTo(
                    stack,
                    8,
                    slots.size(),
                    true
            )) {
                return ItemStack.EMPTY;
            }
        }
        else {
            if (!(stack.getItem()
                    instanceof KeycardItem)) {

                return ItemStack.EMPTY;
            }

            if (!moveItemStackTo(
                    stack,
                    0,
                    8,
                    false
            )) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        }
        else {
            slot.setChanged();
        }

        return result;
    }

    @Override
    public void removed(
            Player player
    ) {
        super.removed(player);

        if (!player.level().isClientSide()) {

            PomkotsDatapadItem
                    .saveKeyCardContainer(
                            datapad,
                            keyCardContainer
                    );
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
