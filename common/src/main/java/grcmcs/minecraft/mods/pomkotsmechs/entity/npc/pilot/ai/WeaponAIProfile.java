package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai;

import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;

public record WeaponAIProfile(
        float minRange,
        float maxRange,

        boolean burstFire,
        int burstDuration,

        int minFireDelay,
        int maxFireDelay
) {
    public static WeaponAIProfile getProfile(
            BasePartsItem.WeaponCategory category,
            MechAutoController.CombatStyle style,
            MechAutoController.PilotRank rank
    ) {
        float baseDelayModifier = 1;

        switch (rank) {
            case NOVICE -> baseDelayModifier = 2.5F;
            case INTERMEDIATE -> baseDelayModifier = 2F;
            case ADVANCED -> baseDelayModifier = 1.5F;
        }

        float missileDelayModifier = 1;
        if (style == MechAutoController.CombatStyle.LONG_RANGE) {
            missileDelayModifier = 0.5F;
        }

        return switch (category) {
            case RIFLE -> new WeaponAIProfile(
                    5,
                    150,
                    false,
                    0,
                    (int)(10 * baseDelayModifier),
                    (int)(40 * baseDelayModifier)
            );

            case MACHINE_GUN -> new WeaponAIProfile(
                    0,
                    80,
                    true,
                    60,
                    (int)(20 * baseDelayModifier),
                    (int)(60 * baseDelayModifier)
            );

            case SHOT_GUN -> new WeaponAIProfile(
                    0,
                    40,
                    false,
                    0,
                    (int)(10 * baseDelayModifier),
                    (int)(40 * baseDelayModifier)
            );

            case GRENADE -> new WeaponAIProfile(
                    40,
                    200,
                    false,
                    0,
                    (int)(100 * baseDelayModifier),
                    (int)(120 * baseDelayModifier)
            );

            case MELEE -> new WeaponAIProfile(
                    0,
                    40,
                    false,
                    0,
                    (int)(10 * baseDelayModifier),
                    (int)(30 * baseDelayModifier)
            );

            case MISSILE -> new WeaponAIProfile(
                    20,
                    200,
                    false,
                    0,
                    (int)(100 * baseDelayModifier * missileDelayModifier),
                    (int)(150 * baseDelayModifier * missileDelayModifier)
            );

            default -> PROF_DEFAULT;
        };
    }

    public static WeaponAIProfile PROF_DEFAULT =
        new WeaponAIProfile(
                5,
                150,
                false,
                0,
                10,
                40
        );
}
