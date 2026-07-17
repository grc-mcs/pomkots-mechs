package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BluePrintItem extends Item {
    public BluePrintItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        var correspondingItem = Utils.getPartsClassFromBluePrint(itemStack);
        if (correspondingItem != null) {
            correspondingItem.appendHoverText(new ItemStack(correspondingItem), level, list, tooltipFlag);
        }
    }
}
