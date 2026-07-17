package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats;

import java.util.Locale;

public enum MechStatType {

    HEALTH,
    KNOCKBACK_RESISTANCE,

    ENERGY_CAPACITY,
    ENERGY_RECOVERY,
    ENERGY_COST,

    MOVE_SPEED,
    VERTICAL_SPEED,
    DASH_SPEED,
    JUMP_POWER,

    DAMAGE_ALL,
    DAMAGE_RIFLE,
    DAMAGE_MACHINE_GUN,
    DAMAGE_SHOTGUN,
    DAMAGE_MISSILE,
    DAMAGE_GRENADE,
    DAMAGE_MELEE;

    public static MechStatType byName(
            String name,
            MechStatType fallback
    ) {
        if (name == null) {
            return fallback;
        }

        String normalized =
                name.toUpperCase(Locale.ROOT);

        for (MechStatType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }

        return fallback;
    }
}
