package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FlavorTextItem extends Item {
    public FlavorTextItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Component base = super.getName(stack);

        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(PomkotsMechs.nbtName("FlavorTextNo"))) {
            String no = tag.getString(PomkotsMechs.nbtName("FlavorTextNo"));
            return base.copy().append(": " + no);
        }

        return base;
    }
}
