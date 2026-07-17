package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai;

import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.pilot.PilotLicenseItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.pilot.PilotRoleItem;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Consumer;

public class MechAutoController {
    private static final int MAX_SEEK_RANGE = 100;

    private static final int VERTICAL_STUCK_TRIGGER = 200;
    private static final double VERTICAL_DIFF = 30.0D;
    private static final double VERTICAL_CHECK_RANGE = 40.0D;

    private int verticalStuckTick;

    private final LivingEntity pilot;
    private final Pmvc01Entity mech;

    private LivingEntity target;

    private final RandomSource random = RandomSource.create();

    private PilotRole role;
    private PilotRank rank;
    private final CombatStyle combatStyle;

    private final WeaponAIProfile rightHandProfile;
    private final WeaponAIProfile leftHandProfile;
    private final WeaponAIProfile rightShoulderProfile;
    private final WeaponAIProfile leftShoulderProfile;

    public enum PilotRole {
        GUARD,
        RAIDER,
        WINGMAN,
        GLADIATOR
    }

    public enum PilotRank {
        NOVICE,
        INTERMEDIATE,
        ADVANCED,
        LEGEND
    }

    public enum SystemMode {
        IDLE,
        COMBAT
    }

    public enum CombatStyle {
        CLOSE_RANGE(30),
        MID_RANGE(70),
        MID_RANGE_MELEE(70),
        LONG_RANGE(120);

        float preferedDistance;

        CombatStyle(float d) {
            preferedDistance = d;
        }

        float getPreferedDistance() {
            return preferedDistance;
        }
    }

    private SystemMode mode = SystemMode.IDLE;
    private final List<MovePattern.MovePatternEntry> movePatternTable;
    private MovePattern curMovePattern = null;
    private Class<? extends MovePattern> lastMovePattern;

    public static class WeaponAIState {
        // 連射中残りTick
        public int burstTick;
        // 最後に発射したTick
        public long lastFireTick;
        // 次回発射可能になるまでの待ちTick
        public int nextFireDelay;
    }

    private final WeaponAIState rightHandState = new WeaponAIState();
    private final WeaponAIState leftHandState = new WeaponAIState();
    private final WeaponAIState rightShoulderState = new WeaponAIState();
    private final WeaponAIState leftShoulderState = new WeaponAIState();

    public MechAutoController(LivingEntity pilot, Pmvc01Entity mech) {
        this.pilot = pilot;
        this.mech = mech;

        this.role = chooseRole(pilot, mech);
        this.rank = chooseRank(pilot, mech);
        this.combatStyle = chooseStyle(pilot, mech);
        this.movePatternTable = MovePatterns.createCombatMovePatternTable(
                combatStyle, role, rank, pilot, mech, this
        );

        this.rightHandProfile = createWeaponProfile(mech.getRightArmWeapon());
        this.leftHandProfile = createWeaponProfile(mech.getLeftArmWeapon());
        this.rightShoulderProfile = createWeaponProfile(mech.getRightShoulderWeapon());
        this.leftShoulderProfile = createWeaponProfile(mech.getLeftShoulderWeapon());
    }

    private WeaponAIProfile createWeaponProfile(ItemStack stack) {
        if (!(stack.getItem() instanceof BasePartsItem.Weapon weapon)) {
            return WeaponAIProfile.PROF_DEFAULT;
        }

        var category = weapon.getWeaponCategory();

        return WeaponAIProfile.getProfile(category, combatStyle, rank);
    }

    private DriverInput prevInput = new DriverInput((short)0);

    public void tick() {
        DriverInput currentInput = new DriverInput((short)0, prevInput);

        switch (mode) {
            case IDLE ->
                    tickIdle(currentInput);
            case COMBAT ->
                    tickCombat(currentInput);
        }

        // 終了処理
        mech.setDriverInput(currentInput);
        prevInput = currentInput;
    }

    private void tickIdle(
            DriverInput input
    ) {
        if (mech.tickCount % 100 == 0) {
            LivingEntity found = findCombatTarget();

            if (found != null) {
                target = found;
                mode = SystemMode.COMBAT;
                return;
            }
        }

        updateMovePatternModeIdle();

        if (curMovePattern != null) {
            curMovePattern.tick(input);
        }
    }

    private LivingEntity findCombatTarget() {
        return switch (role) {
            case GUARD ->
                    findTargetGuard();

            case RAIDER ->
                    findTargetRaider();

            case WINGMAN ->
                    findTargetWingman();

            case GLADIATOR ->
                    findTargetGladiator();
        };
    }

    private LivingEntity findTargetGuard() {
        List<GenericPomkotsMonster> candidates =
                mech.level().getEntitiesOfClass(
                        GenericPomkotsMonster.class,
                        mech.getBoundingBox()
                                .inflate(MAX_SEEK_RANGE)
                );

        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;

        for (GenericPomkotsMonster monster : candidates) {

//            if (!isInFov(monster, 120F)) {
//                continue;
//            }

            if (!mech.hasLineOfSight(monster)) {
                continue;
            }

            if (monster instanceof BaseBossEntity boss && !boss.isActivated()) {
                continue;
            }

            double distance = mech.distanceToSqr(monster);

            if (distance < bestDistance) {

                bestDistance = distance;
                best = monster;
            }
        }

        return best;
    }

    private boolean isInFov(
            LivingEntity target,
            float fovDegrees
    ) {

        Vec3 look =
                mech.getLookAngle()
                        .normalize();

        Vec3 toTarget =
                target.position()
                        .subtract(mech.position())
                        .normalize();

        double dot =
                look.dot(toTarget);

        double limit =
                Math.cos(
                        Math.toRadians(
                                fovDegrees * 0.5
                        )
                );

        return dot >= limit;
    }

    private LivingEntity findTargetRaider() {
        List<Pmvc01Entity> mechs =
                mech.level()
                        .getEntitiesOfClass(
                                Pmvc01Entity.class,
                                mech.getBoundingBox()
                                        .inflate(MAX_SEEK_RANGE)
                        );

        double bestDistance =
                Double.MAX_VALUE;

        Pmvc01Entity best =
                null;

        for (Pmvc01Entity other : mechs) {
            if (other == mech) {
                continue;
            }

            LivingEntity pilot = other.getDrivingPassenger();

            if (pilot == null) {
                continue;
            }

            if (pilot.getOffhandItem().getItem() instanceof PilotRoleItem.PlotRoleRaider) {
                continue;
            }

            double distance =
                    mech.distanceToSqr(other);

            if (distance < bestDistance) {

                bestDistance = distance;
                best = other;
            }
        }

        return best;
    }

    private LivingEntity findTargetWingman() {
        return findTargetGuard();
    }

    private LivingEntity findTargetGladiator() {
        return null;
    }

    private void updateMovePatternModeIdle() {

        if (curMovePattern != null
                && !curMovePattern.shouldExit()) {
            return;
        }

        curMovePattern = chooseIdlePattern();

        curMovePattern.start(null);
    }

    private MovePattern chooseIdlePattern() {
        return switch (role) {
            case GUARD, RAIDER ->
                    new MovePattern.PatrolPattern(
                            pilot,
                            mech,
                            this,
                            40
                    );

            case WINGMAN ->
                    new MovePattern.FollowPlayerPattern(
                            pilot,
                            mech,
                            this,
                            40
                    );

            case GLADIATOR ->
                    new MovePattern.IdlePattern(
                            pilot,
                            mech,
                            this,
                            40
                    );
        };
    }

    public void startArenaBattle(LivingEntity target) {
        if (role != PilotRole.GLADIATOR) {
            return;
        }

        if (target != null) {
            this.target = target;
            this.mode = SystemMode.COMBAT;
        }
    }

    private LivingEntity findNearestArenaTarget() {
        List<Pmvc01Entity> mechs =
                mech.level()
                        .getEntitiesOfClass(
                                Pmvc01Entity.class,
                                mech.getBoundingBox()
                                        .inflate(MAX_SEEK_RANGE)
                        );

        double bestDistance = Double.MAX_VALUE;

        Pmvc01Entity best = null;
        for (Pmvc01Entity other : mechs) {
            if (other == mech) {
                continue;
            }

            LivingEntity pilot = other.getDrivingPassenger();

            if (pilot == null) {
                continue;
            }

            double distance = mech.distanceToSqr(other);

            if (distance < bestDistance) {
                bestDistance = distance;
                best = other;
            }
        }

        return best;
    }

    private void tickCombat(
            DriverInput input
    ) {
        if (target == null || !target.isAlive()) {
            leaveCombat();
            return;
        }

        if (target instanceof Pmvc01Entity enemyMech
                && enemyMech.isBroken()) {
            leaveCombat();
            return;
        }

        mech.getLockTargets().lockTargetSoft(target);

        updateRotation(target);

        updateVerticalStuck();

        updateMovePatternModeCombat();

        curMovePattern.tick(input);

        updateWeapons(input);
    }

    private void leaveCombat() {
        target = findCombatTarget();

        if (target == null) {
            curMovePattern = null;
            mech.getLockTargets()
                    .lockTargetSoft(null);
            mode = SystemMode.IDLE;
        }

    }


    private void updateVerticalStuck() {
        if (target == null) {
            verticalStuckTick = 0;
            return;
        }

        double yDiff =
                Math.abs(
                        target.getY()
                                - mech.getY()
                );

        double horizontalDistance =
                Math.sqrt(
                        Mth.square(
                                target.getX() - mech.getX()
                        )
                                +
                                Mth.square(
                                        target.getZ() - mech.getZ()
                                )
                );

        if (
                yDiff >= VERTICAL_DIFF
                        &&
                        horizontalDistance <= VERTICAL_CHECK_RANGE
        ) {
            verticalStuckTick++;
        } else {
            verticalStuckTick = 0;
        }
    }

    private void updateWeapons(DriverInput input) {
        if (target == null) {
            return;
        }

        double distance = mech.distanceTo(target);

        evaluateWeapon(
                mech.getRightArmWeapon(),
                mech.getLockTargets().getLockTargetMulti(Pmvc01Entity.INV_WEAPON_RIGHT_HAND),
                rightHandProfile,
                rightHandState,
                distance,
                input::setWeaponRightHandPressed
        );

        evaluateWeapon(
                mech.getLeftArmWeapon(),
                mech.getLockTargets().getLockTargetMulti(Pmvc01Entity.INV_WEAPON_LEFT_HAND),
                leftHandProfile,
                leftHandState,
                distance,
                input::setWeaponLeftHandPressed
        );

        evaluateWeapon(
                mech.getRightShoulderWeapon(),
                mech.getLockTargets().getLockTargetMulti(Pmvc01Entity.INV_WEAPON_RIGHT_SHOULDER),
                rightShoulderProfile,
                rightShoulderState,
                distance,
                input::setWeaponRightShoulderPressed
        );

        evaluateWeapon(
                mech.getLeftShoulderWeapon(),
                mech.getLockTargets().getLockTargetMulti(Pmvc01Entity.INV_WEAPON_LEFT_SHOULDER),
                leftShoulderProfile,
                leftShoulderState,
                distance,
                input::setWeaponLeftShoulderPressed
        );
    }

    private void evaluateWeapon(
            ItemStack itemStack,
            List<Entity> lockTargets,
            WeaponAIProfile profile,
            WeaponAIState state,
            double distance,
            Consumer<Boolean> trigger
    ) {
        if (!(itemStack.getItem() instanceof BasePartsItem.Weapon weapon)) {
            return;
        }

        if (distance < profile.minRange()) {
            return;
        }

        if (distance > profile.maxRange()) {
            return;
        }

        long gameTime = mech.level().getGameTime();

        // 連射継続中
        if (state.burstTick > 0) {
            state.burstTick--;
            trigger.accept(true);

            return;
        }

        // クールダウン中
        if (gameTime - state.lastFireTick
                < state.nextFireDelay) {
            return;
        }

        // 発射
        trigger.accept(true);

        var category = weapon.getWeaponCategory();
        if (category == BasePartsItem.WeaponCategory.MISSILE && target != null) {
            lockTargets.clear();
            for (int i = 0; i < weapon.maxMultiLockNum(); i++) {
                lockTargets.add(target);
            }
        }

        state.lastFireTick = gameTime;

        state.nextFireDelay =
                (int)(profile.minFireDelay()
                        + random.nextInt(
                        profile.maxFireDelay()
                                - profile.minFireDelay()
                                + 1
                ));

        // マシンガン等
        if (profile.burstFire()) {
            state.burstTick =
                    profile.burstDuration()
                            + random.nextInt(
                            profile.burstDuration() / 2 + 1
                    );
        }
    }

    private void updateMovePatternModeCombat() {
        if (curMovePattern == null || curMovePattern.shouldExit()) {
            pilot.xxa = 0;
            pilot.zza = 0;

            curMovePattern = chooseMovePattern();
            curMovePattern.start(target);
        }
    }

    private MovePattern chooseMovePattern() {
        if (verticalStuckTick >= VERTICAL_STUCK_TRIGGER) {

            verticalStuckTick = 0;

            return new MovePattern.VerticalApproachPattern(
                    pilot,
                    mech,
                    this,
                    combatStyle.getPreferedDistance()
            );
        }

        int totalWeight = 0;

        for (MovePattern.MovePatternEntry entry : movePatternTable) {
            totalWeight += getWeight(entry);
        }

        int roll = random.nextInt(totalWeight);

        for (MovePattern.MovePatternEntry entry : movePatternTable) {
            roll -= getWeight(entry);

            if (roll < 0) {
                MovePattern pattern = entry.factory().get();

                lastMovePattern = entry.patternClass();

                return pattern;
            }
        }

        MovePattern.MovePatternEntry fallback =
                movePatternTable.get(0);

        lastMovePattern =
                fallback.patternClass();

        return fallback.factory().get();
    }

    private int getWeight(
            MovePattern.MovePatternEntry entry
    ) {
        int weight = entry.baseWeight();

        if (entry.patternClass() == lastMovePattern) {
            weight /= 4;
        }

        return Math.max(weight, 1);
    }

    void updateRotation(LivingEntity target) {
        double dx = target.getX() - mech.getX();
        double dz = target.getZ() - mech.getZ();

        float yaw = (float)(
                Math.atan2(-dx, dz)
                        * 180.0D
                        / Math.PI
        );

        pilot.setYRot(yaw);
        pilot.setYHeadRot(yaw);
    }

    private LivingEntity findTarget() {
        if (target != null
                && target.isAlive()
                && mech.distanceToSqr(target) < MAX_SEEK_RANGE * MAX_SEEK_RANGE) {
            return target;
        }

        List<Pmvc01Entity> list =
                mech.level().getEntitiesOfClass(
                        Pmvc01Entity.class,
                        mech.getBoundingBox().inflate(MAX_SEEK_RANGE),
                        e -> e != pilot
                                && e != mech
                                && e.isAlive()
                                && e.getDrivingPassenger() != null
                );

        double nearest = Double.MAX_VALUE;
        LivingEntity result = null;

        for (LivingEntity e : list) {

            double dist = mech.distanceToSqr(e);

            if (dist < nearest) {
                nearest = dist;
                result = e;
            }
        }

        return result;
    }

    public void handleHurt(DamageSource source, float amount) {

    }

    void stopBoosting() {
        mech.setBoost(false);
        mech.actionController.setBoost(false);
    }

    void startBoosting() {
        mech.setBoost(true);
        mech.actionController.setBoost(true);
    }

    private CombatStyle chooseStyle(LivingEntity pilot, Pmvc01Entity mech) {
        int meleeWeapons = 0;
        int midWeapons = 0;
        int longWeapons = 0;

        for (int i = 0; i < 4; i++) {
            var w = mech.getItem(i + Pmvc01Entity.INV_WEAPON_RIGHT_HAND).getItem();
            if (!(w instanceof BasePartsItem.Weapon weapon)) {
                continue;
            }

            switch (weapon.getWeaponCategory()) {
                case MELEE -> meleeWeapons++;
                case MACHINE_GUN, SHOT_GUN -> midWeapons++;
                case RIFLE, GRENADE, MISSILE -> longWeapons++;
            }
        }

        // 近接2本持ち以上は間違いなく近接…っ！！
        if (meleeWeapons > 1) {
            return CombatStyle.CLOSE_RANGE;
        }

        // 中間レンジがあればそっちに寄せる
        if (midWeapons > 0) {
            if (meleeWeapons > 0) {
                return CombatStyle.MID_RANGE_MELEE;
            } else {
                return CombatStyle.MID_RANGE;
            }
        }

        if (longWeapons > 1) {
            return CombatStyle.LONG_RANGE;
        }

        // コンセプトがよくわかんなかったら取り合えずバランス型
        return CombatStyle.MID_RANGE_MELEE;
    }

    private PilotRank chooseRank(LivingEntity pilot, Pmvc01Entity mech) {
        Item item = pilot.getOffhandItem().getItem();

        if (item instanceof PilotLicenseItem.PilotLicenseNovice) {
            return PilotRank.NOVICE;
        } else if (item instanceof PilotLicenseItem.PilotLicenseIntermediate) {
            return PilotRank.INTERMEDIATE;
        } else if (item instanceof PilotLicenseItem.PilotLicenseAdvanced) {
            return PilotRank.ADVANCED;
        } else if (item instanceof PilotLicenseItem.PilotLicenseLegend) {
            return PilotRank.LEGEND;
        } else {
            return PilotRank.NOVICE;
        }
    }

    private PilotRole chooseRole(LivingEntity pilot, Pmvc01Entity mech) {
        Item item = pilot.getOffhandItem().getItem();

        if (item instanceof PilotRoleItem.PlotRoleGuardian) {
            return PilotRole.GUARD;
        } else if (item instanceof PilotRoleItem.PlotRoleWingman) {
            return PilotRole.WINGMAN;
        } else if (item instanceof PilotRoleItem.PlotRoleGladiator) {
            return PilotRole.GLADIATOR;
        } else if (item instanceof PilotRoleItem.PlotRoleRaider) {
            return PilotRole.RAIDER;
        } else {
            return PilotRole.GUARD;
        }
    }
}
