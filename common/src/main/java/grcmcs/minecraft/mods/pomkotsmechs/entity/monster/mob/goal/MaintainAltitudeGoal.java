package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.levelgen.Heightmap;

public class MaintainAltitudeGoal extends Goal {
    private final Mob mob; // 飛行するモンスター
    private final double targetAltitude; // 地面からの目標高度
    private final double movementSpeed;

    public MaintainAltitudeGoal(Mob mob, double targetAltitude, double movementSpeed) {
        this.mob = mob;
        this.targetAltitude = targetAltitude;
        this.movementSpeed = movementSpeed;
    }

    @Override
    public boolean canUse() {
        // このタスクを常に実行
        return true;
    }

    @Override
    public void tick() {
        // モンスターの現在の位置から地面までの高さを取得
        BlockPos currentPosition = mob.blockPosition();
        double groundY = mob.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, currentPosition).getY();
        double currentAltitude = mob.getY() - groundY;

        // 目標高度と現在の高度を比較してモンスターの高度を調整
        if (isInRangeLower(currentAltitude)) {
            // 目標より低い場合は上昇
            mob.setDeltaMovement(mob.getDeltaMovement().add(0.0, movementSpeed, 0.0));
        } else if (isInRangeUpper(currentAltitude)) {
            // 目標より高い場合は下降
            mob.setDeltaMovement(mob.getDeltaMovement().add(0.0, -movementSpeed, 0.0));
        }
    }

    private static final int RANGE = 10;

    private boolean isInRangeLower(double currentAltitude) {
        return currentAltitude < targetAltitude - RANGE;
    }


    private boolean isInRangeUpper(double currentAltitude) {
        return currentAltitude > targetAltitude + RANGE;
    }
}

