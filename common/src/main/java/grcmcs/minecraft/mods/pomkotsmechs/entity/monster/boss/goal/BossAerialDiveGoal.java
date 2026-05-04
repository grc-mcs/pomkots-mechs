package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class BossAerialDiveGoal extends BaseBossGoal {
    private final double requiredHeightDifference; // ターゲットとの高度差がこれ以上の時に発動
    private final double jumpStrength; // 初動ジャンプの強さ
    private final double boosterSpeed; // ブースター上昇速度
    private final double flySpeed; // 空中移動速度
    private final double diveSpeed; // 急降下速度
    private final int maxChaseTime; // 最大追跡時間（ティック）

    private LivingEntity target;
    private Vec3 targetPosition;
    private int chaseTimer;

    private enum Phase {
        CHARGE,      // 初動ジャンプ
        JUMP,      // 初動ジャンプ
        BOOST,     // ブースター上昇
        FLY,       // 空中移動
        DIVE,      // 急降下
        TIMEOUT    // タイムアウト落下
    }

    private Phase currentPhase = Phase.CHARGE;
    private int phaseTimer = 0;

    public BossAerialDiveGoal(BaseBossEntity mob,
                              double requiredHeightDifference,
                              double jumpStrength, double boosterSpeed, double flySpeed,
                              double diveSpeed, int maxChaseTime) {
        super(mob);
        this.requiredHeightDifference = requiredHeightDifference;
        this.jumpStrength = jumpStrength;
        this.boosterSpeed = boosterSpeed;
        this.flySpeed = flySpeed;
        this.diveSpeed = diveSpeed;
        this.maxChaseTime = maxChaseTime;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUseInternal() {
        target = mob.getTarget();

        if (target != null) {
            double heightDiff = target.getY() - mob.getY();

            if (heightDiff >= requiredHeightDifference) {
                var vehicle = target.getVehicle();
                if (vehicle != null) {
                    return vehicle.onGround();
                } else {
                    return target.onGround();
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive() &&
                (currentPhase != Phase.DIVE || !mob.onGround()) &&
                (currentPhase != Phase.TIMEOUT);
    }

    @Override
    public void start() {
        currentPhase = Phase.CHARGE;
        phaseTimer = 0;
        chaseTimer = 0;

        if (target != null) {
            targetPosition = target.position();
        }
    }

    @Override
    public void tick() {
        if (target == null || !target.isAlive()) {
            return;
        }

        mob.rotateToTarget(target);

        phaseTimer++;
        chaseTimer++;

        // タイムアウトチェック
        if (chaseTimer >= maxChaseTime) {
//            if (chaseTimer >= maxChaseTime && currentPhase != Phase.DIVE && currentPhase != Phase.TIMEOUT) {
            currentPhase = Phase.TIMEOUT;
            phaseTimer = 0;
        }

        switch (currentPhase) {
            case CHARGE -> handleChargePhase();
            case JUMP -> handleJumpPhase();
            case BOOST -> handleBoostPhase();
            case FLY -> handleFlyPhase();
            case DIVE -> handleDivePhase();
            case TIMEOUT -> handleTimeoutPhase();
        }

        // 向きの更新（ダイブ中以外）
        if (currentPhase != Phase.DIVE) {
            updateRotation();
        }
    }

    private void handleChargePhase() {
        if (phaseTimer == 1) {
            mob.triggerAnim("action_controller", "jump");

        } else if (phaseTimer == 10) {
            currentPhase = Phase.BOOST;
            phaseTimer = 0;
            performInitialJump();
        }
    }

    private void performInitialJump() {
        Vec3 jumpVec = new Vec3(0, jumpStrength, 0);
        mob.setDeltaMovement(jumpVec);
        mob.setNoGravity(true);
        mob.hasImpulse = true;
    }

    private void handleJumpPhase() {
        // ジャンプの頂点に達したらブーストフェーズに移行
        if (mob.getDeltaMovement().y <= 0) {
            currentPhase = Phase.BOOST;
            phaseTimer = 0;
        }
    }

    private void handleBoostPhase() {
        // ブースターでゆっくり上昇
        Vec3 currentMovement = mob.getDeltaMovement();
        Vec3 boostMovement = new Vec3(currentMovement.x * 0.9, boosterSpeed, currentMovement.z * 0.9);
        mob.setDeltaMovement(boostMovement);

        // ターゲットより十分高くなったら飛行フェーズに移行
        if (mob.getY() > target.getY() + 5 || phaseTimer > 60) {
            currentPhase = Phase.FLY;
            phaseTimer = 0;
            targetPosition = target.position().add(0, 2, 0); // ターゲットの少し上を目標に
        }
    }

    private void handleFlyPhase() {
        // ターゲット位置を更新
        targetPosition = target.position().add(0, 2, 0);

        // ターゲットに向かって水平移動
        Vec3 toTarget = targetPosition.subtract(mob.position());
        Vec3 horizontalDir = new Vec3(toTarget.x, 0, toTarget.z).normalize();

        // 高度を維持しながら移動
        double currentY = mob.getY();
        double targetY = target.getY() + 8; // ターゲットより8ブロック上を維持
        double verticalAdjustment = (targetY - currentY) * 0.1;

        Vec3 flyMovement = horizontalDir.scale(flySpeed).add(0, verticalAdjustment, 0);
        mob.setDeltaMovement(flyMovement);

        // ターゲットの真上に来たらダイブフェーズに移行
        double horizontalDistance = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
        if (horizontalDistance < 15 || phaseTimer > 80) {
            currentPhase = Phase.DIVE;
            phaseTimer = 0;
        }
    }

    private void handleDivePhase() {
        // 急降下
//        Vec3 diveMovement = new Vec3(0, -diveSpeed, 0);
//        mob.setDeltaMovement(diveMovement);

        mob.setNoGravity(false);

        // 着地判定
        if (mob.onGround() || isNearGround()) {
            mob.getActionController().getAction("onground").tryAction();
        }
    }

    private void handleTimeoutPhase() {

        mob.setNoGravity(false);

//        // タイムアウト時の落下
//        Vec3 currentMovement = mob.getDeltaMovement();
//        Vec3 fallMovement = new Vec3(currentMovement.x * 0.8, -diveSpeed, currentMovement.z * 0.8);
//        mob.setDeltaMovement(fallMovement);
//
//        // 着地判定
//        if (mob.onGround() || isNearGround()) {
//            mob.getActionController().getAction("onground").tryAction();
//            mob.setNoGravity(false);
//        }
    }

    private void updateRotation() {
        Vec3 toTarget = target.position().subtract(mob.position());
        float desiredYaw = (float)(Mth.atan2(toTarget.x, toTarget.z) * (-180 / Math.PI));
        mob.setYRot(rotLerp(mob.getYRot(), desiredYaw, 5F));
        mob.yBodyRot = mob.getYRot();

        // 飛行中はピッチも調整
        if (currentPhase == Phase.FLY || currentPhase == Phase.BOOST) {
            double horizontalDist = Math.sqrt(toTarget.x * toTarget.x + toTarget.z * toTarget.z);
            float desiredPitch = (float)(Math.atan2(-toTarget.y, horizontalDist) * (180 / Math.PI));
            mob.setXRot(rotLerp(mob.getXRot(), desiredPitch, 3F));
        }
    }

    private float rotLerp(float current, float target, float maxChange) {
        float delta = Mth.wrapDegrees(target - current);
        if (delta > maxChange) delta = maxChange;
        if (delta < -maxChange) delta = -maxChange;
        return current + delta;
    }

    private boolean isNearGround() {
        // 地面に近いかチェック
        BlockPos belowPos = new BlockPos((int)mob.getX(), (int)(mob.getY() - diveSpeed), (int)mob.getZ());
        return !mob.level().getBlockState(belowPos).isAir();
    }

    @Override
    public void stop() {
        currentPhase = Phase.CHARGE;
        phaseTimer = 0;
        chaseTimer = 0;
        target = null;
        targetPosition = null;

        // 停止時に重力を正常に戻す
        mob.setNoGravity(false);
    }

    // デバッグ用：現在のフェーズを取得
    public Phase getCurrentPhase() {
        return currentPhase;
    }

    public int getChaseTimer() {
        return chaseTimer;
    }
}
