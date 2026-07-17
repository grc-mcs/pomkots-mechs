package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai;

import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class FindAndEnterMechGoal extends Goal {
    private static final double SEARCH_RANGE = 32.0D;
    private static final double ENTER_DISTANCE = 2.0D;
    private static final double DASH_SPEED = 0.7D;

    /**
     * 40tick = 2秒
     */
    private static final int SEARCH_COOLDOWN = 100;

    private final MechPilotEntity pilot;

    private Pmvc01Entity targetMech;

    private int searchCooldown;

    public FindAndEnterMechGoal(MechPilotEntity pilot) {
        this.pilot = pilot;

        this.setFlags(EnumSet.of(
                Goal.Flag.MOVE,
                Goal.Flag.LOOK
        ));
    }

    @Override
    public boolean canUse() {
        if (pilot.isPassenger()) {
            return false;
        }

        // クールダウン中
        if (searchCooldown > 0) {
            searchCooldown--;
            return false;
        }

        searchCooldown = SEARCH_COOLDOWN;

        targetMech = findNearestFreeMech();

        return targetMech != null;
    }

    @Override
    public boolean canContinueToUse() {

        if (pilot.isPassenger()) {
            return false;
        }

        if (targetMech == null) {
            return false;
        }

        if (!isRidableMech(targetMech)) {
            return false;
        }

        // 他の誰かが先に乗った
        if (!targetMech.getPassengers().isEmpty()) {
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        pilot.getNavigation().moveTo(
                targetMech,
                DASH_SPEED
        );
    }

    @Override
    public void tick() {
        if (targetMech == null) {
            return;
        }

        pilot.getLookControl().setLookAt(
                targetMech,
                30.0F,
                30.0F
        );

        double distanceSq =
                pilot.distanceToSqr(targetMech);

        if (distanceSq <= ENTER_DISTANCE * ENTER_DISTANCE) {
            pilot.startRiding(targetMech, true);

            return;
        }

        // 経路が切れた場合だけ再計算
        if (pilot.getNavigation().isDone()) {

            pilot.getNavigation().moveTo(
                    targetMech,
                    DASH_SPEED
            );
        }
    }

    @Override
    public void stop() {

        pilot.getNavigation().stop();

        targetMech = null;
    }

    private Pmvc01Entity findNearestFreeMech() {

        AABB box = pilot.getBoundingBox()
                .inflate(SEARCH_RANGE);

        List<Pmvc01Entity> mechs =
                pilot.level().getEntitiesOfClass(
                        Pmvc01Entity.class,
                        box
                );

        Pmvc01Entity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Pmvc01Entity mech : mechs) {
            if (!isRidableMech(mech)) {
                continue;
            }

            if (!mech.getPassengers().isEmpty()) {
                continue;
            }

            double distance =
                    pilot.distanceToSqr(mech);

            if (distance < nearestDistance) {

                nearestDistance = distance;
                nearest = mech;
            }
        }

        return nearest;
    }

    private boolean isRidableMech(Pmvc01Entity mech) {
        return mech != null && mech.isAlive() && !mech.isBroken();
    }
}
