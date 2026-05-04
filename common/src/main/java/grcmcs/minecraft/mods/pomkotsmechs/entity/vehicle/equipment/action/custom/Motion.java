package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom;

import java.util.HashSet;
import java.util.Set;

public abstract class Motion {
    protected int maxActionTick;
    protected int interval;
    protected Set<Integer> actualFireTick;

    public int getMaxActionTick() {
        return maxActionTick;
    }

    public int getInterval() {
        return interval;
    }

    public Set<Integer> getActualFireTick() {
        return actualFireTick;
    }

    public abstract MotionType getType();

    public abstract String getAnimationName(ActionWeapon act, String side);

    public abstract boolean concurrentAvailable();

    public enum MotionType {
        ONE_SHOT,
        CONTINUOUS,
        MULTI_LOCK,
        CHARGE,
        TOGGLE
    };

    public static Motion RIFLE = new Motion() {
        public Motion init() {
            maxActionTick = 10;
            interval = -1;
            actualFireTick = new HashSet<>();
            actualFireTick.add(2);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.ONE_SHOT;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            return "animation.pmv01.w_rifle_" + side;
        }

        @Override
        public boolean concurrentAvailable() {
            return true;
        }
    }.init();

    public static Motion GATLING_GUN = new Motion() {
        public Motion init() {
            maxActionTick = 9;
            interval = 2;
            actualFireTick = new HashSet<>();
            actualFireTick.add(4);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.CONTINUOUS;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            if (!act.isInFire()) {
                return "animation.pmv01.w_gatring1_" + side;
            } else {
                return "animation.pmv01.w_gatring2_" + side;
            }
        }

        @Override
        public boolean concurrentAvailable() {
            return true;
        }
    }.init();

    public static Motion SUB_MACHINE_GUN = new Motion() {
        public Motion init() {
            maxActionTick = 9;
            interval = 2;
            actualFireTick = new HashSet<>();
            actualFireTick.add(4);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.CONTINUOUS;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            if (!act.isInFire()) {
                return "animation.pmv01.w_gatring1_" + side;
            } else {
                return "animation.pmv01.w_gatring2_" + side;
            }
        }

        @Override
        public boolean concurrentAvailable() {
            return true;
        }
    }.init();

    public static Motion SHOULDER_MACHINE_GUN = new Motion() {
        public Motion init() {
            maxActionTick = 9;
            interval = 2;
            actualFireTick = new HashSet<>();
            actualFireTick.add(4);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.CONTINUOUS;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            if (!act.isInFire()) {
                return "animation.pmv01.idle";
            } else {
                return "animation.pmv01.idle";
            }
        }

        @Override
        public boolean concurrentAvailable() {
            return true;
        }
    }.init();

    public static Motion HAND_MISSILE = new Motion() {
        public Motion init() {
            maxActionTick = 10;
            interval = -1;
            actualFireTick = new HashSet<>();
            actualFireTick.add(2);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.MULTI_LOCK;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            return "animation.pmv01.w_rifle_" + side;
        }

        @Override
        public boolean concurrentAvailable() {
            return true;
        }
    }.init();

    public static Motion PUNCH = new Motion() {
        public Motion init() {
            maxActionTick = 19;
            interval = -1;
            actualFireTick = new HashSet<>();
            actualFireTick.add(10);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.ONE_SHOT;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            return "animation.pmv01.w_punch_" + side;
        }

        @Override
        public boolean concurrentAvailable() {
            return false;
        }
    }.init();

    public static Motion SABER = new Motion() {
        public Motion init() {
            maxActionTick = 19;
            interval = -1;
            actualFireTick = new HashSet<>();
            actualFireTick.add(10);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.ONE_SHOT;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            return "animation.pmv01.w_saber_" + side;
        }

        @Override
        public boolean concurrentAvailable() {
            return false;
        }
    }.init();

    public static Motion CIRCLE_BLADE = new Motion() {
        public Motion init() {
            maxActionTick = 19;
            interval = -1;
            actualFireTick = new HashSet<>();
            actualFireTick.add(7);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.ONE_SHOT;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            return "animation.pmv01.w_circle_blade_" + side;
        }

        @Override
        public boolean concurrentAvailable() {
            return false;
        }
    }.init();

    public static Motion SHOULDER_BLADE = new Motion() {
        public Motion init() {
            maxActionTick = 19;
            interval = -1;
            actualFireTick = new HashSet<>();
            actualFireTick.add(7);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.ONE_SHOT;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            return "animation.pmv01.w_blade_shoulder_" + side;
        }

        @Override
        public boolean concurrentAvailable() {
            return true;
        }
    }.init();

    public static Motion YARI = new Motion() {
        public Motion init() {
            maxActionTick = 19;
            interval = -1;
            actualFireTick = new HashSet<>();
            actualFireTick.add(10);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.ONE_SHOT;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            return "animation.pmv01.w_yari_" + side;
        }

        @Override
        public boolean concurrentAvailable() {
            return false;
        }
    }.init();

    public static Motion DRILL = new Motion() {
        public Motion init() {
            maxActionTick = 20;
            interval = 10;
            actualFireTick = new HashSet<>();
            actualFireTick.add(10);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.CONTINUOUS;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            if (!act.isInFire()) {
                return "animation.pmv01.w_drill1_" + side;
            } else {
                return "animation.pmv01.w_drill2_" + side;
            }
        }

        @Override
        public boolean concurrentAvailable() {
            return false;
        }
    }.init();

    public static Motion CHARGE_HUMMER = new Motion() {
        public Motion init() {
            maxActionTick = 19;
            interval = -1;
            actualFireTick = new HashSet<>();
            actualFireTick.add(Integer.MAX_VALUE - maxActionTick - 1);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.CHARGE;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            if (!act.isInFire()) {
                return "animation.pmv01.w_hummer1_" + side;
            } else {
                return "animation.pmv01.w_hummer2_" + side;
            }
        }

        @Override
        public boolean concurrentAvailable() {
            return true;
        }
    }.init();

    public static Motion MULTI_MISSILE = new Motion() {
        public Motion init() {
            maxActionTick = 14;
            interval = -1;
            actualFireTick = new HashSet<>();
            actualFireTick.add(2);
            actualFireTick.add(4);
            actualFireTick.add(6);
            actualFireTick.add(8);
            actualFireTick.add(10);
            actualFireTick.add(12);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.MULTI_LOCK;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            return "animation.pmv01.w_missile_" + side;
        }

        @Override
        public boolean concurrentAvailable() {
            return true;
        }
    }.init();

    public static Motion GRENADE = new Motion() {
        public Motion init() {
            maxActionTick = 14;
            interval = -1;
            actualFireTick = new HashSet<>();
            actualFireTick.add(4);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.ONE_SHOT;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            return "animation.pmv01.w_grenade_" + side;
        }

        @Override
        public boolean concurrentAvailable() {
            return true;
        }
    }.init();

    public static Motion LARGE_MISSILE = new Motion() {
        public Motion init() {
            maxActionTick = 14;
            interval = -1;
            actualFireTick = new HashSet<>();
            actualFireTick.add(4);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.MULTI_LOCK;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            return "animation.pmv01.w_largemissile";
        }

        @Override
        public boolean concurrentAvailable() {
            return false;
        }
    }.init();

    public static Motion GRENADE_HAND = new Motion() {
        public Motion init() {
            maxActionTick = 14;
            interval = -1;
            actualFireTick = new HashSet<>();
            actualFireTick.add(4);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.ONE_SHOT;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            return "animation.pmv01.w_baz_" + side;
        }

        @Override
        public boolean concurrentAvailable() {
            return true;
        }
    }.init();

    public static Motion ROAD_ROLLER = new Motion() {
        public Motion init() {
            maxActionTick = 21;
            interval = 10;
            actualFireTick = new HashSet<>();
            actualFireTick.add(20);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.CONTINUOUS;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            if (!act.isInFire()) {
                return "animation.pmv01.w_roller1_" + side;
            } else {
                return "animation.pmv01.w_roller2_" + side;
            }
        }

        @Override
        public boolean concurrentAvailable() {
            return false;
        }
    }.init();

    public static Motion RAISE_THROW = new Motion() {
        public Motion init() {
            maxActionTick = 9;
            interval = -1;
            actualFireTick = new HashSet<>();
            actualFireTick.add(4);

            return this;
        }

        @Override
        public MotionType getType() {
            return MotionType.TOGGLE;
        }

        @Override
        public String getAnimationName(ActionWeapon act, String side) {
            var mech = act.getOwner();
            if (mech != null) {
                if (act.isInAction()) {
                    if (act.isToggleOnStart()) {
                        return "animation.pmv01.w_throw_1";
                    } else {
                        return "animation.pmv01.w_throw_3";
                    }
                } else {
                    if (act.isToggleOn()) {
                        return "animation.pmv01.w_throw_2";
                    } else {
                        return "animation.pmv01.idle";
                    }
                }
            } else {
                return "animation.pmv01.idle";
            }
        }

        @Override
        public boolean concurrentAvailable() {
            return false;
        }
    }.init();
}
