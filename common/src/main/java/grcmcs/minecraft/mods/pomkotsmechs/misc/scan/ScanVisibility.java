package grcmcs.minecraft.mods.pomkotsmechs.misc.scan;

public enum ScanVisibility {
    OWNER,
    TEAM,
    ALL;

    public static ScanVisibility fromId(byte id) {
        ScanVisibility[] values = values();

        if (id < 0 || id >= values.length) {
            return OWNER;
        }

        return values[id];
    }

    public byte id() {
        return (byte) ordinal();
    }
}
