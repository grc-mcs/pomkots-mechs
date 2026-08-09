package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class MovePatterns {

    public static List<MovePattern.MovePatternEntry> createCombatMovePatternTable(
            MechAutoController.CombatStyle style,
            MechAutoController.PilotRole role,
            MechAutoController.PilotRank rank,
            LivingEntity pilot,
            Pmvc01Entity mech,
            MechAutoController controller,
            float preferredDistance) {

        return switch (style) {
            case CLOSE_RANGE -> createCombatMovePatternTableClose(pilot, mech, role, rank, controller, preferredDistance);
            case MID_RANGE -> createCombatMovePatternTableMiddle(pilot, mech, role, rank, controller, preferredDistance);
            case MID_RANGE_MELEE -> createCombatMovePatternTableMiddleMelee(pilot, mech, role, rank, controller, preferredDistance);
            case LONG_RANGE -> createCombatMovePatternTableLong(pilot, mech, role, rank, controller, preferredDistance);
        };
    }

    public static List<MovePattern.MovePatternEntry> createCombatMovePatternTableClose(
            LivingEntity pilot,
            Pmvc01Entity mech,
            MechAutoController.PilotRole role,
            MechAutoController.PilotRank rank,
            MechAutoController controller,
            float preferredDistance) {

        return List.of(
                new MovePattern.MovePatternEntry(
                        40,
                        MovePattern.OrbitLeftPattern.class,
                        () -> new MovePattern.OrbitLeftPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                ),

                new MovePattern.MovePatternEntry(
                        40,
                        MovePattern.OrbitRightPattern.class,
                        () -> new MovePattern.OrbitRightPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                ),

                new MovePattern.MovePatternEntry(
                        60,
                        MovePattern.ChargePattern.class,
                        () -> new MovePattern.ChargePattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance,
                                20
                        )
                ),

                new MovePattern.MovePatternEntry(
                        40,
                        MovePattern.RetreatPattern.class,
                        () -> new MovePattern.RetreatPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                ),

                new MovePattern.MovePatternEntry(
                        40,
                        MovePattern.AirBoostEvasionPattern.class,
                        () -> new MovePattern.AirBoostEvasionPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                )
        );
    }


    public static List<MovePattern.MovePatternEntry> createCombatMovePatternTableMiddle(
            LivingEntity pilot,
            Pmvc01Entity mech,
            MechAutoController.PilotRole role,
            MechAutoController.PilotRank rank,
            MechAutoController controller,
            float preferredDistance) {

        return List.of(
                new MovePattern.MovePatternEntry(
                        40,
                        MovePattern.OrbitLeftPattern.class,
                        () -> new MovePattern.OrbitLeftPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                ),

                new MovePattern.MovePatternEntry(
                        40,
                        MovePattern.OrbitRightPattern.class,
                        () -> new MovePattern.OrbitRightPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                ),

                new MovePattern.MovePatternEntry(
                        20,
                        MovePattern.RetreatPattern.class,
                        () -> new MovePattern.RetreatPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                ),

                new MovePattern.MovePatternEntry(
                        20,
                        MovePattern.AirBoostEvasionPattern.class,
                        () -> new MovePattern.AirBoostEvasionPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                )
        );
    }


    public static List<MovePattern.MovePatternEntry> createCombatMovePatternTableMiddleMelee(
            LivingEntity pilot,
            Pmvc01Entity mech,
            MechAutoController.PilotRole role,
            MechAutoController.PilotRank rank,
            MechAutoController controller,
            float preferredDistance) {

        return List.of(
                new MovePattern.MovePatternEntry(
                        40,
                        MovePattern.OrbitLeftPattern.class,
                        () -> new MovePattern.OrbitLeftPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                ),

                new MovePattern.MovePatternEntry(
                        40,
                        MovePattern.OrbitRightPattern.class,
                        () -> new MovePattern.OrbitRightPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                ),

                new MovePattern.MovePatternEntry(
                        20,
                        MovePattern.ChargePattern.class,
                        () -> new MovePattern.ChargePattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance,
                                20
                        )
                ),

                new MovePattern.MovePatternEntry(
                        20,
                        MovePattern.RetreatPattern.class,
                        () -> new MovePattern.RetreatPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                ),

                new MovePattern.MovePatternEntry(
                        20,
                        MovePattern.AirBoostEvasionPattern.class,
                        () -> new MovePattern.AirBoostEvasionPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                )
        );
    }


    public static List<MovePattern.MovePatternEntry> createCombatMovePatternTableLong(
            LivingEntity pilot,
            Pmvc01Entity mech,
            MechAutoController.PilotRole role,
            MechAutoController.PilotRank rank,
            MechAutoController controller,
            float preferredDistance) {

        return List.of(
                new MovePattern.MovePatternEntry(
                        40,
                        MovePattern.OrbitLeftPattern.class,
                        () -> new MovePattern.OrbitLeftPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                ),

                new MovePattern.MovePatternEntry(
                        40,
                        MovePattern.OrbitRightPattern.class,
                        () -> new MovePattern.OrbitRightPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                ),

                new MovePattern.MovePatternEntry(
                        20,
                        MovePattern.ChargePattern.class,
                        () -> new MovePattern.ChargePattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance,
                                20
                        )
                ),

                new MovePattern.MovePatternEntry(
                        20,
                        MovePattern.RetreatPattern.class,
                        () -> new MovePattern.RetreatPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                ),

                new MovePattern.MovePatternEntry(
                        20,
                        MovePattern.AirBoostEvasionPattern.class,
                        () -> new MovePattern.AirBoostEvasionPattern(
                                pilot,
                                mech,
                                controller,
                                preferredDistance
                        )
                )
        );
    }
}
