package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto;

public enum ArenaRank {
    E("E", 0x808080),
    D("D", 0x55FF55),
    C("C", 0x55FFFF),
    B("B", 0x5555FF),
    A("A", 0xFF55FF),
    S("S", 0xFFAA00),
    SS("SS", 0xFFFF55);

    private final String displayName;
    private final int color;

    ArenaRank(String displayName, int color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getName() {
        return displayName;
    }

    public int getColor() {
        return color;
    }

    public static final ArenaRank getRank(int rating) {
        if (rating >= 1400) return ArenaRank.SS;
        if (rating >= 1200) return ArenaRank.S;
        if (rating >= 900) return ArenaRank.A;
        if (rating >= 600) return ArenaRank.B;
        if (rating >= 300) return ArenaRank.C;

        return ArenaRank.D;
    }

    public static ArenaRank getRank(String name) {
        if ("SS".equals(name)) {
            return SS;
        } else if ("S".equals(name)) {
            return S;
        } else if ("A".equals(name)) {
            return A;
        } else if ("B".equals(name)) {
            return B;
        } else if ("C".equals(name)) {
            return C;
        } else {
            return D;
        }
    }
}
