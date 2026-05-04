package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SearchDroneGoal extends SmallMobGoalBase {
    private LivingEntity target;
    private final double baseSpeed;

    private Vec3 currentVelocity;
    private Vec3 targetVelocity;

    private static final double VELOCITY_SMOOTHING = 0.15;

    public SearchDroneGoal(BaseSmallMonsterEntity mob, double baseSpeed, float longRange, float midRange, float closeRange, int attackCooldown) {
        super(mob, (float)baseSpeed);
        this.mob = mob;
        this.baseSpeed = baseSpeed;

        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));

        // 初期化
        this.currentVelocity = Vec3.ZERO;
    }

    @Override
    public boolean canUse() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return true;
    }

    @Override
    public void start() {
    }

    @Override
    public void tick() {
        Level level = mob.level();
        BlockPos ground = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, mob.blockPosition());
        double baseY = ground.getY() + 20;
        Vec3 currentPos = mob.position();

        if (baseY > currentPos.y) {
            double y = baseY + mob.getRandom().nextDouble() * (30 - 20);
            Vec3 targetVec = new Vec3(currentPos.x, y, currentPos.z);
            Vec3 motion = targetVec.subtract(currentPos).normalize().scale(0.6);

            // スムーズな慣性を保った移動
            mob.setDeltaMovement(mob.getDeltaMovement().add(motion).scale(baseSpeed));

        } else {
            Vec3 wobble = new Vec3(
                    (mob.getRandom().nextDouble() - 0.5) * 0.1,
                    (mob.getRandom().nextDouble() - 0.5) * 0.1,
                    (mob.getRandom().nextDouble() - 0.5) * 0.1
            );
            targetVelocity = currentVelocity.scale(0.1).add(wobble);
            applySmoothedMovement();
        }
    }

    private void applySmoothedMovement() {
        // 速度の平滑化
        currentVelocity = currentVelocity.lerp(targetVelocity, VELOCITY_SMOOTHING);

        // 飛行エンティティの場合のY軸制御
        if (mob.isNoGravity()) {
            mob.setDeltaMovement(currentVelocity);
        } else {
            // 重力の影響を受ける場合
            Vec3 currentMovement = mob.getDeltaMovement();
            mob.setDeltaMovement(currentVelocity.x, currentMovement.y, currentVelocity.z);
        }
    }

    @Override
    public void stop() {
        target = null;
        currentVelocity = Vec3.ZERO;
        targetVelocity = Vec3.ZERO;
    }
}