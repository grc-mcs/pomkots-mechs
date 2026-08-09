package grcmcs.minecraft.mods.pomkotsmechs.mission.support;

import java.util.Locale;

public enum MissionEntityCleanupPolicy {
    DISCARD,
    KEEP,
    RESTORE_ON_FAILURE;

    public static MissionEntityCleanupPolicy fromSerializedName(String value) {
        try {
            return valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return DISCARD;
        }
    }
}
