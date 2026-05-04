package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RaidTargetGoal<T extends LivingEntity> extends TargetGoal {
    private final Class<T> baseTargetClass; // 拠点エンティティのクラス
    private final double targetRange;
    private final int playerRetargetTicks; // プレイヤーを狙う時間

    @Nullable
    private LivingEntity currentTarget;

    private final java.util.function.Supplier<Boolean> raidActiveSupplier; // レイド中かどうか

    public RaidTargetGoal(Mob mob, Class<T> baseTargetClass, double targetRange, int playerRetargetTicks,
                          java.util.function.Supplier<Boolean> raidActiveSupplier) {
        super(mob, false);
        this.baseTargetClass = baseTargetClass;
        this.targetRange = targetRange;
        this.playerRetargetTicks = playerRetargetTicks;
        this.raidActiveSupplier = raidActiveSupplier;
    }

    @Override
    public boolean canUse() {
        // レイド中以外は発動しない
        if (!raidActiveSupplier.get()) return false;

        // 攻撃者チェック（プレイヤー優先）
        currentTarget = findTarget();

        return currentTarget != null;
    }

    protected LivingEntity findTarget() {
        long time = mob.tickCount;
        long lastHurtTime = mob.getLastHurtByMobTimestamp();

        if (time - lastHurtTime < playerRetargetTicks && mob.getLastHurtByMob() instanceof Player attacker) {
            return attacker;
        }

        // それ以外は拠点ターゲットを探す
        return findBaseTarget();
    }

    @Override
    public void start() {
        if (currentTarget != null) {
            this.mob.setTarget(currentTarget);
            super.start();
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (!raidActiveSupplier.get()) return false; // レイド中以外は停止

        LivingEntity target = findTarget();
        mob.setTarget(target);

        return target != null && target.isAlive();
    }

    @Override
    public void stop() {
        super.stop();
        this.mob.setTarget(null);
        this.currentTarget = null;
    }

    @Nullable
    private T findBaseTarget() {
        if (!(mob.level() instanceof ServerLevel level)) return null;
        Vec3 pos = mob.position();
        AABB area = new AABB(pos.x - targetRange, pos.y - 100, pos.z - targetRange,
                pos.x + targetRange, pos.y + 100, pos.z + targetRange);
        List<T> list = level.getEntitiesOfClass(baseTargetClass, area,
                e -> e.isAlive() && !e.isRemoved());
        return list.isEmpty() ? null : list.get(0);
    }
}