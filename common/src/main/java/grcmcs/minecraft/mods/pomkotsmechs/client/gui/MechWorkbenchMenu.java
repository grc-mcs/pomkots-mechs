package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.MechWorkbenchBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.RepairKitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
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
    public static short MODE_VIEW = 0;
    public static short MODE_ASSEMBLE = 1;

    private ShortDataSlot entityId = new ShortDataSlot();
    private ShortDataSlot textureColor = new ShortDataSlot();
    private ShortDataSlot menuMode = new ShortDataSlot();
    private final DataSlot inventoryRevision = DataSlot.standalone();

    private MechWorkbenchBlockEntity accessor = null;

    private Pmvc01Entity mech;

    public MechWorkbenchMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(Pmvc01Entity.CONTAINER_SIZE), null, 0, null);
    }

    public MechWorkbenchMenu(int id, Inventory playerInventory, Container mechInventory, Pmvc01Entity mech, int mode, MechWorkbenchBlockEntity consoleAccessor) {
        super(PomkotsMechs.MECH_WORKBENCH_GUI.get(), id);

        entityId = (ShortDataSlot)this.addDataSlot(entityId);
        textureColor = (ShortDataSlot)this.addDataSlot(textureColor);
        menuMode = (ShortDataSlot)this.addDataSlot(menuMode);
        this.addDataSlot(inventoryRevision);

        int offsetX = 136; // 左寄せ調整（通常44→8）
        int offsetY = 210; // ⚠️ GUIを下にずらす (4行分)

        addSlotInternal(0, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Head, mode, false);
        addSlotInternal(1, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Body, mode, false);
        addSlotInternal(2, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Arm, mode, false);
        addSlotInternal(3, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Legs, mode, false);
        addSlotInternal(4, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Generator, mode, false);
        addSlotInternal(5, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Booster, mode, false);
        addSlotInternal(6, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.WeaponArm, mode, false);
        addSlotInternal(7, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.WeaponArm, mode, false);
        addSlotInternal(8, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.WeaponShoulder, mode, false);
        addSlotInternal(9, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.WeaponShoulder, mode, false);
        addSlotInternal(10, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Extension, mode, false);
        addSlotInternal(11, offsetX, offsetY, mechInventory, item->item instanceof BasePartsItem.Extension, mode, false);

        int offsetAmmoX;
        int offsetAmmoY;

        if (true) {
            offsetAmmoX = 9;
            offsetAmmoY = 197;
        } else {
            offsetAmmoX = 310 + 2;
            offsetAmmoY = -23;
        }

        addSlotInternalAmmo(12, offsetAmmoX, offsetAmmoY, mechInventory, item->item instanceof BasePartsItem.Magazine, mode, true);
        addSlotInternalAmmo(13, offsetAmmoX, offsetAmmoY, mechInventory, item->item instanceof BasePartsItem.Magazine, mode, true);
        addSlotInternalAmmo(14, offsetAmmoX, offsetAmmoY, mechInventory, item->item instanceof BasePartsItem.Magazine, mode, true);
        addSlotInternalAmmo(15, offsetAmmoX, offsetAmmoY, mechInventory, item->item instanceof BasePartsItem.Magazine, mode, true);
        addSlotInternalAmmo(16, offsetAmmoX, offsetAmmoY, mechInventory, item->item instanceof BasePartsItem.Fuel, mode, true);
        addSlotInternalAmmo(17, offsetAmmoX, offsetAmmoY, mechInventory, item->item instanceof RepairKitItem, mode, false);

        offsetX = 310 + 2;
        offsetY = 35 + 18;

        // Mech汎用インベントリ
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(mechInventory, col + row * 9 + Pmvc01Entity.CONTAINER_GENERAL_ITEM_START_INDEX, offsetX + col * 18, offsetY + row * 18));
            }
        }

        offsetY = 135 + 18;

        // プレイヤーインベントリ
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, offsetX + col * 18, offsetY + row * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, offsetX + i * 18, offsetY + 3 * 18 + 4));
        }

        for (Slot slot : slots) {
            lastSlots.add(slot.getItem().copy());
        }

        this.mech = mech;
        this.accessor = consoleAccessor;
    }

    private void addSlotInternal(int index, int offsetX, int offsetY, Container mechInventory, Predicate<Item> pred, int mode, boolean isModifiableOnViewMode) {
            var pos = getItemPosition(index, offsetX, offsetY);
        this.addSlot(new ViewOnlySlot(mechInventory, index, (int)pos.x, (int)pos.y, this));
//        this.addSlot(new MechPartsCustomSlot(mechInventory, index, (int)pos.x, (int)pos.y, mode, isModifiableOnViewMode, false, pred));

    }

    protected static Vec2 getItemPosition(int slotNum, int offsetX, int offsetY) {
        int col = slotNum % 6;
        int row = slotNum / 6;

        return new Vec2(offsetX + col * 18, offsetY + row * 18);
    }

    private void addSlotInternalAmmo(int index, int offsetX, int offsetY, Container mechInventory, Predicate<Item> pred, int mode, boolean isModifiableOnViewMode) {
        var pos = getItemPositionAmmo(index, offsetX, offsetY);
        this.addSlot(new MechPartsCustomSlot(mechInventory, index, (int)pos.x, (int)pos.y, mode, isModifiableOnViewMode, true, pred));
    }

    protected static Vec2 getItemPositionAmmo(int slotNum, int offsetX, int offsetY) {
        int col = slotNum % 6;
        int row = slotNum / 6;

        return new Vec2(offsetX + col * 18, offsetY + row * 18);
    }


    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        result = stack.copy();

        int MECH_START = 18;
        int MECH_END = 54;

        int PLAYER_START = 54;
        int PLAYER_END = 90;

        // =========================
        // Mech → Player
        // =========================
        if (index >= MECH_START && index < MECH_END) {
            if (!this.moveItemStackTo(stack, PLAYER_START, PLAYER_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        // =========================
        // Player → Mech
        // =========================
        else if (index >= PLAYER_START && index < PLAYER_END) {
            if (!this.moveItemStackTo(stack, MECH_START, MECH_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        // パーツスロット
        else {
            if (!this.moveItemStackTo(stack, PLAYER_START, PLAYER_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        // =========================
        // 後処理
        // =========================
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
    }

    public short getEntityId() {
        return entityId.getShort();
    }

    public void setEntityId(int id) {
        this.setData(0, (short) (id & 0xFFFF));
    }

    public short getTextureColor() {
        return textureColor.getShort();
    }

    public void setTextureColor(int id) {
        this.setData(1, id);
    }

    public short getMode() {
        return menuMode.getShort();
    }

    public void setMode(short id) {
        this.setData(2, id);
    }

    public void startMechRepair(Pmvc01Entity mech) {
        if (this.accessor != null && mech != null) {
            this.accessor.startMechRepair(mech);
        }
    }

    public static class ShortDataSlot extends DataSlot {
        private short value;

        @Override
        public int get() {
            return value & 0xFFFF; // int に拡張
        }

        @Override
        public void set(int value) {
            this.value = (short) value;
        }

        public short getShort() {
            return value;
        }

        public void setShort(short value) {
            this.value = value;
        }
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
        protected final int mode;

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
    }

    public static class MechPartsCustomSlot extends MechCustomSlot {
        private final Predicate<Item> p;
        private final boolean isModifiableOnViewMode;
        private final boolean isActive;

        public MechPartsCustomSlot(Container container, int index, int x, int y, int mode, boolean isModifiableOnViewMode, boolean isActive, Predicate<Item> p) {
            super(container, index, x, y, mode);
            this.p = p;
            this.isModifiableOnViewMode = isModifiableOnViewMode;
            this.isActive = isActive;
        }

        @Override
        public boolean isActive() {
            return isActive;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (super.mayPlace(stack)) {
                var item = stack.getItem();
                return p.test(item);
            } else {
                return false;
            }
        }

        @Override
        public boolean mayPickup(Player player) {
            return isModifiableOnViewMode || mode == MODE_ASSEMBLE;
        }
    };

    public static class ViewOnlySlot extends Slot {
        MechWorkbenchMenu menu;
        public ViewOnlySlot(Container container, int i, int j, int k, MechWorkbenchMenu menu) {
            super(container, i, j, k);
            this.menu = menu;
        }

        @Override
        public boolean isActive() {
//            return menu.getMode() == MODE_VIEW;

            // @TODO ROAD UI
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    };

    private Runnable inventoryChangedListener;

    public void setInventoryChangedListener(Runnable listener) {
        this.inventoryChangedListener = listener;
    }

    private NonNullList<ItemStack> lastSlots =
            NonNullList.create();

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        boolean changed = false;

        for (int i = 0; i < slots.size(); i++) {

            ItemStack current = slots.get(i).getItem();
            ItemStack last = lastSlots.get(i);

            if (!ItemStack.matches(current, last)) {

                lastSlots.set(i, current.copy());

                changed = true;
            }
        }

        if (changed) {
            updateInventoryRevision();
        }

    }

    private static final int CMD_EQUIP = 1;
    private static final int CMD_UNEQUIP = 2;

    @Override
    public boolean clickMenuButton(
            Player player,
            int packed
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        int command =
                (packed >> 24) & 0xFF;

        int mechSlot =
                (packed >> 16) & 0xFF;

        int value =
                packed & 0xFFFF;

        switch (command) {
            case CMD_EQUIP -> {
                return equipParts(
                        mechSlot,
                        value
                );
            }

            case CMD_UNEQUIP -> {
                return unequipPart(
                        mechSlot
                );
            }
        }

        return false;
    }

    public boolean equipParts(int mechSlotIdx, int sourceSlotIdx) {
        if (sourceSlotIdx >= slots.size()) {
            return false;
        }

        if (!isVallidPartsIndex(mechSlotIdx)  || mechSlotIdx >= slots.size()) {
            return false;
        }

        Slot sourceSlot = getSlot(sourceSlotIdx);

        if (!sourceSlot.hasItem()) {
            return false;
        }

        ItemStack source = sourceSlot.getItem();

        // =====================================
        // Find mech slot
        // =====================================

        Slot mechInvSlot = getSlot(mechSlotIdx);
        ItemStack equipped = mechInvSlot.getItem();

        // =====================================
        // Swap
        // =====================================

        mechInvSlot.set(source.copy());
        sourceSlot.set(equipped);

        sourceSlot.setChanged();
        mechInvSlot.setChanged();

        broadcastChanges();

        return true;
    }

    public boolean unequipPart(
            int mechSlot
    ) {

        Slot slot = getSlot(mechSlot);

        if (!slot.hasItem()) {
            return false;
        }

        ItemStack equipped =
                slot.getItem();

        boolean moved =
                moveItemStackTo(
                        equipped,
                        Pmvc01Entity.CONTAINER_GENERAL_ITEM_START_INDEX,
                        slots.size(),
                        false
                );

        if (!moved) {
            return false;
        }

        if (equipped.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        }

        slot.setChanged();

        broadcastChanges();

        return true;
    }

    public void autoSupplyAll() {
        // 武器4箇所
        autoSupplyWeaponAmmo(
                Pmvc01Entity.INV_WEAPON_RIGHT_HAND
        );

        autoSupplyWeaponAmmo(
                Pmvc01Entity.INV_WEAPON_LEFT_HAND
        );

        autoSupplyWeaponAmmo(
                Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER
        );

        autoSupplyWeaponAmmo(
                Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER
        );

        // fuel

        autoSupplySimpleItem(
                Pmvc01Entity.INV_FUEL,
                stack -> stack.getItem() instanceof BasePartsItem.Fuel
        );

        // repair

        if (getMode() == MODE_ASSEMBLE) {
            autoSupplySimpleItem(
                    Pmvc01Entity.INV_REPAIR_KIT,
                    stack -> stack.getItem() instanceof RepairKitItem
            );
        }

        broadcastChanges();
    }

    private void autoSupplyWeaponAmmo(
            int weaponSlotIndex
    ) {

        Slot weaponSlot = getSlot(weaponSlotIndex);

        if (!weaponSlot.hasItem()) {
            return;
        }

        ItemStack weaponStack = weaponSlot.getItem();

        if (!(weaponStack.getItem()
                instanceof BasePartsItem.Weapon weapon)) {
            return;
        }

        int ammoSlotIndex =
                getAmmoSlotIdx(weaponSlotIndex);

        fillTargetSlot(
                ammoSlotIndex,
                stack -> {
                    if (!(stack.getItem()
                            instanceof BasePartsItem.Magazine mag)) {
                        return false;
                    }

                    return weapon.isMatchAmmo(mag);
                }
        );
    }

    private void autoSupplySimpleItem(
            int targetSlot,
            Predicate<ItemStack> matcher
    ) {
        fillTargetSlot(
                targetSlot,
                matcher
        );
    }

    private void fillTargetSlot(
            int targetSlotIndex,
            Predicate<ItemStack> matcher
    ) {

        Slot targetSlot = getSlot(targetSlotIndex);

        ItemStack target =
                targetSlot.getItem();

        // =====================================
        // target validity
        // =====================================

        boolean targetInvalid =
                !target.isEmpty()
                        && !matcher.test(target);

        // =====================================
        // find first valid stack
        // =====================================

        int firstValidSlot = -1;

        int maxStack = 64;

        for (int i = 0; i < slots.size(); i++) {
            if (i < Pmvc01Entity.CONTAINER_GENERAL_ITEM_START_INDEX) {
                continue;
            }

            if (i == targetSlotIndex) {
                continue;
            }

            Slot slot = getSlot(i);

            if (!slot.hasItem()) {
                continue;
            }

            ItemStack stack = slot.getItem();

            if (!matcher.test(stack)) {
                continue;
            }

            firstValidSlot = i;

            maxStack = stack.getMaxStackSize();

            break;
        }

        // =====================================
        // invalid target handling
        // =====================================

        if (targetInvalid) {
            // 適合弾薬なし
            if (firstValidSlot < 0) {
                return;
            }

            Slot sourceSlot =
                    getSlot(firstValidSlot);

            ItemStack source =
                    sourceSlot.getItem();

            // swap
            sourceSlot.set(target);

            targetSlot.set(source);

            sourceSlot.setChanged();
            targetSlot.setChanged();

            target = source;
        }

        // =====================================
        // no valid source
        // =====================================

        if (firstValidSlot < 0 && target.isEmpty()) {
            return;
        }

        // =====================================
        // max stack
        // =====================================

        if (!target.isEmpty()) {
            maxStack = target.getMaxStackSize();
        }

        // =====================================
        // current
        // =====================================

        int currentCount =
                target.isEmpty()
                        ? 0
                        : target.getCount();

        int needed =
                maxStack - currentCount;

        if (needed <= 0) {
            return;
        }

        // =====================================
        // fill
        // =====================================

        for (int i = 0; i < slots.size(); i++) {

            if (i < Pmvc01Entity.CONTAINER_GENERAL_ITEM_START_INDEX) {
                continue;
            }

            if (i == targetSlotIndex) {
                continue;
            }

            Slot sourceSlot = getSlot(i);

            if (!sourceSlot.hasItem()) {
                continue;
            }

            ItemStack source =
                    sourceSlot.getItem();

            if (!matcher.test(source)) {
                continue;
            }

            // =================================
            // first insert
            // =================================

            if (target.isEmpty()) {

                int move =
                        Math.min(
                                needed,
                                source.getCount()
                        );

                ItemStack moved =
                        source.copy();

                moved.setCount(move);

                targetSlot.set(moved);

                source.shrink(move);

                target = moved;

                maxStack =
                        moved.getMaxStackSize();

                needed =
                        maxStack - target.getCount();

            } else {

                // 型違い禁止

                if (!ItemStack.isSameItemSameTags(
                        target,
                        source
                )) {
                    continue;
                }

                int move =
                        Math.min(
                                needed,
                                source.getCount()
                        );

                target.grow(move);

                source.shrink(move);

                needed -= move;
            }

            // =================================
            // cleanup
            // =================================

            if (source.isEmpty()) {
                sourceSlot.set(ItemStack.EMPTY);
            }

            sourceSlot.setChanged();
            targetSlot.setChanged();

            if (needed <= 0) {
                break;
            }
        }
    }

    private void tryAutoLoadAmmo(int mechWeaponSlot) {
        Slot weaponSlot = getSlot(mechWeaponSlot);

        if (!weaponSlot.hasItem()) {
            return;
        } else if (!isVallidWeaponIndex(mechWeaponSlot)) {
            return;
        }

        ItemStack weaponStack = weaponSlot.getItem();

        if (!(weaponStack.getItem() instanceof BasePartsItem.Weapon weapon)) {
            return;
        }

        // =====================================
        // Find best ammo
        // =====================================

        int bestSlot = -1;
        int bestCount = -1;

        for (int i = 0; i < slots.size(); i++) {
            // Ammo slot 自身は除外
            if (i < Pmvc01Entity.CONTAINER_GENERAL_ITEM_START_INDEX ) {
                continue;
            }

            Slot slot = getSlot(i);

            if (!slot.hasItem()) {
                continue;
            }

            ItemStack stack = slot.getItem();
            if (!(stack.getItem() instanceof BasePartsItem.Magazine mag)) {
                continue;
            }

            if (!weapon.isMatchAmmo(mag)) {
                continue;
            }

            if (stack.getCount() > bestCount) {
                bestCount = stack.getCount();
                bestSlot = i;
            }
        }

        if (bestSlot < 0) {
            return;
        }

        // =====================================
        // Swap ammo
        // =====================================

        int ammoSlotIdx = getAmmoSlotIdx(mechWeaponSlot);

        Slot sourceAmmoSlot = getSlot(bestSlot);
        Slot mechAmmoSlot = getSlot(
                ammoSlotIdx
        );

        ItemStack sourceAmmo =
                sourceAmmoSlot.getItem();

        ItemStack equippedAmmo =
                mechAmmoSlot.getItem();

        mechAmmoSlot.set(sourceAmmo.copy());
        sourceAmmoSlot.set(equippedAmmo);

        sourceAmmoSlot.setChanged();
        mechAmmoSlot.setChanged();
    }

    private boolean isVallidPartsIndex(int index) {
        return index >= Pmvc01Entity.INV_PARTS_HEAD && index <= Pmvc01Entity.INV_WEAPON_EXT2;
    }

    private boolean isVallidWeaponIndex(int index) {
        return index >= Pmvc01Entity.INV_WEAPON_RIGHT_HAND && index <= Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER;
    }

    private int getAmmoSlotIdx(int weaponInvIndex) {
        return weaponInvIndex + 6;
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        System.out.println(container);
        updateInventoryRevision();
    }

    private void updateInventoryRevision() {
        inventoryRevision.set(
                inventoryRevision.get() + 1
        );
    }

    public int getInventoryRevision() {
        return inventoryRevision.get();
    }
}
