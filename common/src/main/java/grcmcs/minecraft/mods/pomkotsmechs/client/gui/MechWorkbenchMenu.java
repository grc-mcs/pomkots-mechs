package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

import java.util.function.Predicate;

public class MechWorkbenchMenu extends AbstractContainerMenu {
    public static int MODE_VIEW = 0;
    public static int MODE_ASSEMBLE = 1;

    private DataSlot entityId = new SimpleDataSlot();
    private DataSlot textureColor = new SimpleDataSlot();
    private Pmvc01Entity mech;

    public MechWorkbenchMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(Pmvc01Entity.CONTAINER_SIZE), null, 0);
    }

    public MechWorkbenchMenu(int id, Inventory playerInventory, Container mechInventory, Pmvc01Entity mech, int mode) {
        super(PomkotsMechs.MECH_WORKBENCH_GUI.get(), id);

        entityId = this.addDataSlot(entityId);
        textureColor = this.addDataSlot(textureColor);

        int offsetX = 8; // 左寄せ調整（通常44→8）
        int offsetY = 106; // ⚠️ GUIを下にずらす (4行分)

        addSlotInternal(0, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Head, mode);
        addSlotInternal(1, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Body, mode);
        addSlotInternal(2, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Arm, mode);
        addSlotInternal(3, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Legs, mode);
        addSlotInternal(4, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Generator, mode);
        addSlotInternal(5, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Booster, mode);
        addSlotInternal(6, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.WeaponArm, mode);
        addSlotInternal(7, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.WeaponArm, mode);
        addSlotInternal(8, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.WeaponShoulder, mode);
        addSlotInternal(9, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.WeaponShoulder, mode);
        addSlotInternal(10, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Extension, mode);
        addSlotInternal(11, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Extension, mode);
        addSlotInternal(12, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Magazine, mode);
        addSlotInternal(13, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Magazine, mode);
        addSlotInternal(14, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Magazine, mode);
        addSlotInternal(15, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Magazine, mode);
        addSlotInternal(16, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Fuel, mode);
        addSlotInternal(17, offsetX, offsetY, mechInventory, item->false, mode);

        // --- プレイヤーインベントリ ---
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new MechCustomSlot(playerInventory, col + row * 9 + 9, offsetX + col * 18, offsetY + 68 + row * 18, mode));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new MechCustomSlot(playerInventory, i, offsetX + i * 18, offsetY + 126, mode));
        }

        this.mech = mech;
    }

    private void addSlotInternal(int index, int offsetX, int offsetY, Container mechInventory, Predicate<Item> pred, int mode) {
            var pos = getItemPosition(index, offsetX, offsetY);
            this.addSlot(new MechPartsCustomSlot(mechInventory, index, (int)pos.x, (int)pos.y, mode, pred));
    }

    protected static Vec2 getItemPosition(int slotNum, int offsetX, int offsetY) {
        int col = slotNum % 6;
        int row = slotNum / 6;

        return new Vec2(offsetX + col * 18, offsetY + row * 18);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }

    public int getEntityId() {
        return entityId.get();
    }

    public void setEntityId(int id) {
        this.setData(0, id);
    }

    public int getTextureColor() {
        return textureColor.get();
    }

    public void setTextureColor(int id) {
        this.setData(1, id);
    }

    private static class SimpleDataSlot extends DataSlot {
        private int val = 0;

        @Override
        public int get() {
            return val;
        }

        @Override
        public void set(int i) {
            this.val = i;
        }
    };

    private static class MechCustomSlot extends Slot {
        private final int mode;

        public MechCustomSlot(Container container, int index, int x, int y, int mode) {
            super(container, index, x, y);
            this.mode = mode;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (mode == 1) {
                return true;
            } else {
                return stack.getItem() instanceof BasePartsItem.Fuel || stack.getItem() instanceof BasePartsItem.Magazine;
            }
        }

//        @Override
//        public boolean mayPickup(Player player) {
//            if (mode == 1) {
//                return true;
//            }
//        }
    }

    public static class MechPartsCustomSlot extends MechCustomSlot {
        private final Predicate<Item> p;
        public MechPartsCustomSlot(Container container, int index, int x, int y, int mode, Predicate<Item> p) {
            super(container, index, x, y, mode);
            this.p = p;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            var item = stack.getItem();
            return p.test(item);
        }
    };
}
