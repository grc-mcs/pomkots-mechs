package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.PomkotsRadarItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class RadarTargetSelectMenu extends AbstractContainerMenu {

    private final ItemStack radarStack;

    public RadarTargetSelectMenu(int syncId, Inventory inv) {
        super(PomkotsMechs.POMKOTS_RADAR_GUI.get(), syncId);
        // メインハンドのレーダーを取得
        this.radarStack = inv.player.getMainHandItem().getItem() instanceof PomkotsRadarItem
                ? inv.player.getMainHandItem()
                : inv.player.getOffhandItem();
    }

    public ItemStack getRadarStack() { return radarStack; }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int i) { return ItemStack.EMPTY; }
}
