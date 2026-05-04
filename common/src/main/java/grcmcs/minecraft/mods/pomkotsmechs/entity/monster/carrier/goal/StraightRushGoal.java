package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import java.util.EnumSet;

/**
 * ターゲットに向かって直進する突撃ゴール
 * - ターゲット発見時に向きを変更
 * - Y座標を維持しながら水平直進
 * - ターゲットを通り過ぎてもそのまま直進継続
 * - 重力無効化
 */
public class StraightRushGoal extends Goal {
    private final Mob mob;
    private final double speed;

    // 状態管理
    private Vec3 rushDirection;
    private double fixedYLevel;
    private boolean isRushing;
    private int rushDuration;
    private final int maxRushDuration;

    // 設定パラメータ
    private static final double MIN_SPEED = 0.1;
    private static final double MAX_SPEED = 2.0;
    private static final int DEFAULT_MAX_RUSH_DURATION = 200; // 10秒間直進

    public StraightRushGoal(Mob mob, double speed) {
        this.mob = mob;
        this.speed = Mth.clamp(speed, MIN_SPEED, MAX_SPEED);
        this.maxRushDuration = DEFAULT_MAX_RUSH_DURATION;

        // このゴールは移動と視線の両方を制御
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // 既に突撃中なら継続
        if (isRushing) {
            return true;
        }

        return hasTarget();
    }

    private boolean hasTarget() {
        return mob.getTarget() != null && mob.getTarget().isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        // 突撃中は指定時間まで継続
        return isRushing && rushDuration < maxRushDuration && mob.isAlive() && hasTarget();
    }

    @Override
    public void start() {
        if (!isRushing && hasTarget()) {
            // 突撃開始時の初期設定
            initializeRush();
        }

        // 重力無効化
        mob.setNoGravity(true);
    }

    @Override
    public void stop() {
        // 突撃終了
        isRushing = false;
        rushDirection = null;
        rushDuration = 0;

        // 重力復活（必要に応じて）
        // mob.setNoGravity(false);

        // 移動停止
        mob.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public void tick() {
        if (!hasTarget()) {
            stop();
            return;
        }

        if (!isRushing) {
            // まだ突撃していない場合、開始準備
            initializeRush();
        }

        if (isRushing && rushDirection != null) {
            // 直進移動の実行
            executeRush();
            rushDuration++;
        }
    }

    /**
     * 突撃の初期化
     */
    private void initializeRush() {
        if (!hasTarget()) {
            return;
        }

        Vec3 mobPos = mob.position();
        Vec3 targetPos = mob.getTarget().position();

        // 現在のY座標を固定
        fixedYLevel = mobPos.y;

        // ターゲットへの水平方向ベクトルを計算
        Vec3 horizontalDirection = new Vec3(
                targetPos.x - mobPos.x,
                0, // Y方向は無視
                targetPos.z - mobPos.z
        ).normalize();

        // 突撃方向として保存
        rushDirection = horizontalDirection.scale(speed);

        // ターゲット方向を向く
        faceTarget(targetPos);

        // 突撃開始
        isRushing = true;
        rushDuration = 0;
    }

    /**
     * 突撃の実行
     */
    private void executeRush() {
        if (rushDirection == null) return;

        // Y座標を固定して直進
        Vec3 currentPos = mob.position();
        Vec3 newVelocity = new Vec3(
                rushDirection.x,
                0, // Y方向の移動なし
                rushDirection.z
        );

        // 移動を適用
        mob.setDeltaMovement(newVelocity);

        // Y座標を強制的に固定
        if (Math.abs(currentPos.y - fixedYLevel) > 0.1) {
            mob.setPos(currentPos.x, fixedYLevel, currentPos.z);
        }

        // 進行方向を向き続ける（オプション）
        maintainRushDirection();
    }

    /**
     * ターゲット方向に向きを変更
     */
    private void faceTarget(Vec3 targetPos) {
        Vec3 mobPos = mob.position();
        double deltaX = targetPos.x - mobPos.x;
        double deltaZ = targetPos.z - mobPos.z;

        // 水平角度（Yaw）を計算
        float yaw = (float) (Mth.atan2(deltaZ, deltaX) * (180D / Math.PI)) - 90.0F;

        // 向きを設定
        mob.setYRot(yaw);
        mob.setYHeadRot(yaw);
        mob.yRotO = yaw;
        mob.yHeadRotO = yaw;
    }

    /**
     * 突撃方向を維持
     */
    private void maintainRushDirection() {
        if (rushDirection != null) {
            double deltaX = rushDirection.x;
            double deltaZ = rushDirection.z;

            float yaw = (float) (Mth.atan2(deltaZ, deltaX) * (180D / Math.PI)) - 90.0F;
            mob.setYRot(yaw);
            mob.setYHeadRot(yaw);
        }
    }
}
