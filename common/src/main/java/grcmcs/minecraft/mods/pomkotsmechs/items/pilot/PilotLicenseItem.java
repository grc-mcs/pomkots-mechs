package grcmcs.minecraft.mods.pomkotsmechs.items.pilot;

import grcmcs.minecraft.mods.pomkotsmechs.client.gui.pilot.PilotMenuProvider;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PilotLicenseItem extends Item {
    public PilotLicenseItem(Properties properties) {
        super(properties);
    }

    public static class PilotLicenseNovice extends PilotLicenseItem {
        public PilotLicenseNovice(Properties properties) {
            super(properties);
        }
    }

    public static class PilotLicenseIntermediate extends PilotLicenseItem {
        public PilotLicenseIntermediate(Properties properties) {
            super(properties);
        }
    }

    public static class PilotLicenseAdvanced extends PilotLicenseItem {
        public PilotLicenseAdvanced(Properties properties) {
            super(properties);
        }
    }

    public static class PilotLicenseLegend extends PilotLicenseItem {
        public PilotLicenseLegend(Properties properties) {
            super(properties);
        }
    }
}