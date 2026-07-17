package grcmcs.minecraft.mods.pomkotsmechs.items.pilot;

import net.minecraft.world.item.Item;

public class PilotRoleItem extends Item {
    public PilotRoleItem(Properties properties) {
        super(properties);
    }

    public static class PlotRoleGuardian extends PilotRoleItem {
        public PlotRoleGuardian(Properties properties) {
            super(properties);
        }
    }

    public static class PlotRoleRaider extends PilotRoleItem {
        public PlotRoleRaider(Properties properties) {
            super(properties);
        }
    }

    public static class PlotRoleWingman extends PilotRoleItem {
        public PlotRoleWingman(Properties properties) {
            super(properties);
        }
    }

    public static class PlotRoleGladiator extends PilotRoleItem {
        public PlotRoleGladiator(Properties properties) {
            super(properties);
        }
    }
}