package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class OrbitalFlyGoal extends BaseBossGoal {
    private LivingEntity target;
    private float angle = 0f;
    private boolean clockwise = true;
    private int switchCooldown = 0;

    private final double minRadius;
    private final double maxRadius;
    private double currentRadius;
    private int radiusCooldown = 0;

    private final double heightMin;
    private final double heightMax;

    private final double speed;

    private int ticksUntilNextSwitch = 0;

    public OrbitalFlyGoal(BaseBossEntity mob, double speed, double minRadius, double maxRadius, double heightMin, double heightMax) {
        super(mob);
        this.speed = speed;
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.heightMin = heightMin;
        this.heightMax = heightMax;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        this.currentRadius = getRandomRadius();
    }

    private double getRandomRadius() {
        return minRadius + mob.getRandom().nextDouble() * (maxRadius - minRadius);
    }

    @Override
    public boolean canUseInternal() {
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return --this.ticksUntilNextSwitch > 0;
    }

    @Override
    public void start() {
        this.target = mob.getTarget();
        ticksUntilNextSwitch = mob.getRandom().nextInt(30) + 30;
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void tick() {
        if (target == null) return;

        mob.rotateToTarget(target);

        if (--switchCooldown <= 0) {
            clockwise = mob.getRandom().nextBoolean();
            switchCooldown = 100 + mob.getRandom().nextInt(60);
        }

        if (--radiusCooldown <= 0) {
            currentRadius = getRandomRadius();
            radiusCooldown = 60 + mob.getRandom().nextInt(60); // 半径を1〜2秒ごとに再設定
        }

        // 回転角を進める
        angle += (clockwise ? 1 : -1) * 20.0f;
        double rad = Math.toRadians(angle);

        // ターゲットの周囲を円運動
        double x = target.getX() + currentRadius * Math.cos(rad);
        double z = target.getZ() + currentRadius * Math.sin(rad);

        // 高度：地形からの高さにオフセットを足してランダム上下
        Level level = mob.level();
        BlockPos ground = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, mob.blockPosition());
        double baseY = ground.getY() + heightMin;
        double y = baseY + mob.getRandom().nextDouble() * (heightMax - heightMin);

        // 現在位置との方向を出して、移動ベクトルをスムーズに反映
        Vec3 currentPos = mob.position();
        Vec3 targetVec = new Vec3(x, y, z);
        Vec3 motion = targetVec.subtract(currentPos).normalize().scale(0.6);

        // スムーズな慣性を保った移動
        mob.setDeltaMovement(mob.getDeltaMovement().add(motion).scale(speed));
        mob.hurtMarked = true; // モーション更新を強制
    }
}
