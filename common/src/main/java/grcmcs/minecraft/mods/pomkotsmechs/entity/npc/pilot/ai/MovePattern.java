package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai;

import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public abstract class MovePattern {
    public record MovePatternEntry(
            int baseWeight,
            Class<? extends MovePattern> patternClass,
            Supplier<MovePattern> factory
    ) {
    }

    protected final LivingEntity pilot;
    protected final Pmvc01Entity mech;
    protected final RandomSource random;

    protected LivingEntity target;

    protected int tick;
    protected int maxTicks;

    protected float preferredDistance;

    protected MechAutoController controller;

    protected MovePattern(
            LivingEntity pilot,
            Pmvc01Entity mech,
            MechAutoController controller,
            float preferredDistance
    ) {
        this.pilot = pilot;
        this.mech = mech;
        this.preferredDistance = preferredDistance;
        this.random = RandomSource.create();
        this.controller = controller;
    }

    public void start(LivingEntity target) {
        this.tick = 0;
        this.target = target;
    }

    public abstract void tick(DriverInput input);

    protected void lookAtYaw(
            float yaw
    ) {
        pilot.setYRot(yaw);
        pilot.setYHeadRot(yaw);
        pilot.yRotO = yaw;
    }

    public boolean shouldExit() {
        return tick >= maxTicks;
    }

    protected double distanceToTarget() {
        return pilot.distanceTo(target);
    }

    protected void setMove(
            float xxa,
            float zza,
            DriverInput input
    ) {
        pilot.xxa = xxa;
        pilot.zza = zza;

        input.setForwardPressed(zza > 0);
        input.setBackPressed(zza < 0);

        input.setLeftPressed(xxa > 0);
        input.setRightPressed(xxa < 0);
    }

    public static class OrbitLeftPattern extends MovePattern {
        public OrbitLeftPattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        @Override
        public void start(LivingEntity target) {
            super.start(target);
            maxTicks = 20 + random.nextInt(40);
        }

        @Override
        public void tick(DriverInput input) {
            tick++;

            double distance = distanceToTarget();

            float zza;
            if (distance < preferredDistance * 0.8F) {
                zza = -0.4F;
            } else if (distance > preferredDistance * 1.2F) {
                zza = 1.0F;
            } else {
                zza = 0.2F;
            }

            setMove(
                    1.0F,
                    zza,
                    input
            );
        }
    }

    public static class OrbitRightPattern extends MovePattern {
        public OrbitRightPattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        @Override
        public void start(LivingEntity target) {
            super.start(target);
            maxTicks = 20 + random.nextInt(40);
        }

        @Override
        public void tick(DriverInput input) {
            if (tick == 0) {
                input.setEvasionPressed(true);
            }

            tick++;

            double distance = distanceToTarget();
            float zza;

            if (distance < preferredDistance * 0.8F) {
                zza = -0.4F;
            } else if (distance > preferredDistance * 1.2F) {
                zza = 1.0F;
            } else {
                zza = 0.2F;
            }

            setMove(
                    -1.0F,
                    zza,
                    input
            );
        }
    }

    public static class RetreatPattern extends MovePattern {
        public RetreatPattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        @Override
        public void start(LivingEntity target) {
            super.start(target);
            maxTicks = 20 + random.nextInt(40);
        }

        @Override
        public void tick(DriverInput input) {
            if (tick == 0) {
                input.setEvasionPressed(true);
            }

            tick++;

            setMove(
                    0F,
                    -1F,
                    input
            );
        }

        @Override
        public boolean shouldExit() {
            if (distanceToTarget() > preferredDistance) {
                return true;
            }

            return super.shouldExit();
        }
    }

    public static class ChargePattern extends MovePattern {

        private final float chargeDistance;

        public ChargePattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance,
                float chargeDistance
        ) {
            super(pilot, mech, controller, preferredDistance);

            this.chargeDistance = chargeDistance;
        }

        @Override
        public void start(LivingEntity target) {
            super.start(target);
            maxTicks = 60;
        }

        @Override
        public void tick(DriverInput input) {
            if (tick == 0) {
                input.setEvasionPressed(true);
            }

            tick++;

            setMove(
                    0F,
                    1F,
                    input
            );
        }

        @Override
        public boolean shouldExit() {
            if (distanceToTarget() <= chargeDistance) {
                return true;
            }

            return super.shouldExit();
        }
    }

    public static class KitePattern extends MovePattern {

        private final boolean leftOrbit;

        public KitePattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);

            this.leftOrbit = random.nextBoolean();
        }

        @Override
        public void start(LivingEntity target) {
            super.start(target);
            maxTicks = 80 + random.nextInt(80);
        }

        @Override
        public void tick(DriverInput input) {
            tick++;

            float xxa = leftOrbit ? 0.6F : -0.6F;

            double distance = distanceToTarget();

            float zza;

            if (distance < preferredDistance) {
                zza = -1F;
            } else {
                zza = -0.2F;
            }

            setMove(
                    xxa,
                    zza,
                    input
            );
        }
    }

    public static class AirBoostEvasionPattern extends MovePattern {
        private float moveX;
        private float moveZ;

        public AirBoostEvasionPattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        private void initMovement() {
            int dir = random.nextInt(13);

            switch (dir) {
                case 0, 1 -> { moveX = 0; moveZ = 1; }     // 前
                case 2, 3 -> { moveX = 1; moveZ = 1; }     // 左前
                case 4, 5 -> { moveX = -1; moveZ = 1; }    // 右前
                case 6, 7 -> { moveX = 1; moveZ = 0; }     // 左
                case 8, 9 -> { moveX = -1; moveZ = 0; }    // 右

                case 10 -> { moveX = 0; moveZ = -1; }    // 後
                case 11 -> { moveX = 1; moveZ = -1; }    // 左後
                default -> { moveX = -1; moveZ = -1; }  // 右後
            }
        }

        @Override
        public void start(LivingEntity target) {
            super.start(target);

            maxTicks = 60;
            initMovement();
        }

        @Override
        public void tick(
                DriverInput input
        ) {
            tick++;

            setMove(
                    moveX,
                    moveZ,
                    input
            );

            if (tick < 5) {
                // 離陸
                input.setJumpPressed(true);

            } else if (tick < 15) {
                // ブースト上昇
                input.setJumpPressed(true);

            } else if (tick < 25) {
                // 空中回避
                input.setJumpPressed(false);
                input.setEvasionPressed(true);

            } else {
                // 慣性移動
                input.setJumpPressed(false);
                input.setEvasionPressed(false);
            }
        }

        @Override
        public boolean shouldExit() {
            if (tick > 10 && mech.onGround() || mech.getEnergy() == 0) {
                return true;
            }

            return super.shouldExit();
        }
    }

    public static class VerticalApproachPattern extends MovePattern {
        public VerticalApproachPattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        @Override
        public void start(
                LivingEntity target
        ) {
            super.start(target);

            // 4～6秒くらい継続
            maxTicks = 80 + random.nextInt(40);
        }

        @Override
        public void tick(
                DriverInput input
        ) {
            if (tick == 0) {
                input.setEvasionPressed(true);
            }

            tick++;

            // 常に前進
            setMove(
                    0F,
                    1F,
                    input
            );

            // 常にジャンプ押しっぱ
            input.setJumpPressed(true);
        }

        @Override
        public boolean shouldExit() {

            if (target == null) {
                return true;
            }

            double yDiff =
                    Math.abs(
                            target.getY()
                                    - mech.getY()
                    );

            // 高低差が十分縮まった
            if (yDiff < 10) {
                return true;
            }

            return super.shouldExit();
        }
    }

    public static class IdlePattern extends MovePattern {
        public IdlePattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        @Override
        public void start(
                LivingEntity target
        ) {
            super.start(target);

            maxTicks = 100;
        }

        @Override
        public void tick(
                DriverInput input
        ) {
            tick++;

            setMove(
                    0F,
                    0F,
                    input
            );
        }
    }

    public static class PatrolPattern extends MovePattern {

        private float moveX;
        private float moveZ;

        private float moveYaw;

        public PatrolPattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        @Override
        public void start(
                LivingEntity target
        ) {
            super.start(target);

            controller.stopBoosting();

            maxTicks =
                    20 + random.nextInt(40);

            chooseDirection();
        }

        private void chooseDirection() {
            moveYaw =
                    random.nextFloat() * 360F;

            float rad =
                    moveYaw * Mth.DEG_TO_RAD;

            moveX = -Mth.sin(rad);
            moveZ = Mth.cos(rad);
        }

        @Override
        public void tick(
                DriverInput input
        ) {
            tick++;

            if (tick % 10 == 0) {

                moveYaw +=
                        random.nextFloat() * 20F
                                - 10F;
            }

            lookAtYaw(moveYaw);

            setMove(
                    0F,
                    1F,
                    input
            );

            if (
                    tick % 40 == 0
                            && random.nextFloat() < 0.3F
            ) {
                chooseDirection();
            }
        }
    }

    public static class FollowPlayerPattern extends MovePattern {
        private Player followTarget;

        public FollowPlayerPattern(
                LivingEntity pilot,
                Pmvc01Entity mech,
                MechAutoController controller,
                float preferredDistance
        ) {
            super(pilot, mech, controller, preferredDistance);
        }

        @Override
        public void start(
                LivingEntity target
        ) {
            super.start(target);

            maxTicks = 100;

            followTarget = findNearestPlayer();
        }

        private Player findNearestPlayer() {
            var players =
                    mech.level()
                            .players();

            if (pilot instanceof MechPilotEntity mechPilot
                    && mechPilot.getWingmanMasterId().isPresent()) {
                var masterId = mechPilot.getWingmanMasterId().get();
                return players.stream()
                        .filter(player -> player.getUUID().equals(masterId))
                        .findFirst()
                        .orElse(null);
            }

            double bestDistance =
                    Double.MAX_VALUE;

            Player best = null;

            for (Player player : players) {

                double distance =
                        mech.distanceToSqr(player);

                if (distance < bestDistance) {

                    bestDistance = distance;
                    best = player;
                }
            }

            return best;
        }

        @Override
        public void tick(
                DriverInput input
        ) {
            tick++;

            if (followTarget == null) {
                return;
            }

            controller.updateRotation(followTarget);

            double distance =
                    mech.distanceTo(followTarget);

            if (followTarget.position().y - mech.position().y > 10) {
                input.setJumpPressed(true);
            }

            //
            // 遠いなら追従
            //
            if (distance > 15) {
                if (distance > 100) {
                    input.setEvasionPressed(true);
                } else if (distance > 40) {
                    controller.startBoosting();
                }

                setMove(
                        0F,
                        1F,
                        input
                );
            }
            //
            // 近いなら停止
            //
            else {
                controller.stopBoosting();
                setMove(
                        0F,
                        0F,
                        input
                );
            }
        }
    }
}
