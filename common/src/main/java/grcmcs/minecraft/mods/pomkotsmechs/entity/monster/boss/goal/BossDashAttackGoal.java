package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BossDashAttackGoal extends BossDashGoal {
    public BossDashAttackGoal(BaseBossEntity mob, double dashSpeed, double maxRotationSpeed, double startDistance, double stopDistance, int maxDuration, double homingStrength) {
        super(mob, dashSpeed, maxRotationSpeed, startDistance, stopDistance, maxDuration, homingStrength);
    }

    @Override
    public boolean canContinueToUse() {
        if (target == null || !target.isAlive()) {
            return false;
        }

        // 時間制限チェック
        if (dashTimer >= maxDuration) {
            return false;
        }

        return true;
    }

    @Override
    public void start() {
        target = mob.getTarget();
        hasStarted = true;
        dashTimer = 0;
        initialYaw = mob.getYRot();

        // 初期ダッシュ方向を設定（ターゲットへの方向）
        Vec3 toTarget = target.position().subtract(mob.position()).normalize();
        dashDirection = new Vec3(toTarget.x, 0, toTarget.z).normalize();

        mob.triggerAnim("action_controller", "dash_attack");
    }


    @Override
    public void tick() {
        super.tick();
        if (dashTimer % 10 == 0) {
            this.dealDashDamage();
        }
    }

    protected void dealDashDamage() {
        Level level = mob.level();
        AABB damageArea = mob.getBoundingBox().inflate(10);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, damageArea);

        for (LivingEntity entity : nearbyEntities) {
            if (!mob.isSelf(entity) && mob.distanceTo(entity) <= 10) {
                // ダメージ処理
                entity.hurt(mob.damageSources().mobAttack(mob), 8.0f);

                // ノックバック
                Vec3 knockback = entity.position().subtract(mob.position()).normalize().scale(1.0);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback.x, 0.3, knockback.z));
            }
        }
    }
}
