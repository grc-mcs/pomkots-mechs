package grcmcs.minecraft.mods.pomkotsmechs.client.gui.pilot;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PilotMenuProvider implements MenuProvider {

    private final Mob mob;

    public PilotMenuProvider(Mob mob) {
        this.mob = mob;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Pilot Config");
    }

    @Override
    public AbstractContainerMenu createMenu(
            int containerId,
            Inventory inventory,
            Player player
    ) {
        return new PilotMenu(
                containerId,
                inventory,
                mob
        );
    }
}
