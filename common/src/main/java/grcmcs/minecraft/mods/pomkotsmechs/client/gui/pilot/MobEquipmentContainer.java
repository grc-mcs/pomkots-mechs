package grcmcs.minecraft.mods.pomkotsmechs.client.gui.pilot;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MobEquipmentContainer implements Container {

    private final Mob mob;

    private static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND,
            EquipmentSlot.OFFHAND
    };

    public MobEquipmentContainer(Mob mob) {
        this.mob = mob;
    }

    @Override
    public int getContainerSize() {
        return SLOTS.length;
    }

    @Override
    public boolean isEmpty() {

        for (EquipmentSlot slot : SLOTS) {

            if (!mob.getItemBySlot(slot).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        return mob.getItemBySlot(SLOTS[index]);
    }

    @Override
    public ItemStack removeItem(int index, int count) {

        ItemStack stack =
                mob.getItemBySlot(SLOTS[index]);

        ItemStack result =
                stack.split(count);

        mob.setItemSlot(
                SLOTS[index],
                stack
        );

        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {

        ItemStack stack =
                mob.getItemBySlot(SLOTS[index]);

        mob.setItemSlot(
                SLOTS[index],
                ItemStack.EMPTY
        );

        return stack;
    }

    @Override
    public void setItem(
            int index,
            ItemStack stack
    ) {
        mob.setItemSlot(
                SLOTS[index],
                stack
        );
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return mob.isAlive();
    }

    @Override
    public void clearContent() {

        for (EquipmentSlot slot : SLOTS) {
            mob.setItemSlot(
                    slot,
                    ItemStack.EMPTY
            );
        }
    }
}
