package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class BossDashAttackDrillGoal extends BossDashGoal {
    public BossDashAttackDrillGoal(BaseBossEntity mob, double dashSpeed, double maxRotationSpeed, double startDistance, double stopDistance, int maxDuration, double homingStrength) {
        super(mob, dashSpeed, maxRotationSpeed, startDistance, stopDistance, maxDuration, homingStrength);
    }

    private int chargeTimer = 0;

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
        chargeTimer = 0;

        // 初期ダッシュ方向を設定（ターゲットへの方向）
        Vec3 toTarget = target.position().subtract(mob.position()).normalize();
        dashDirection = new Vec3(toTarget.x, 0, toTarget.z).normalize();

        mob.triggerAnim("action_controller", "dash_attack");
    }


    @Override
    public void tick() {
        if (chargeTimer < 18) {
            chargeTimer++;
        } else {
            super.tick();
            if (dashTimer % 5 == 0) {
                this.dealDashDamage();
            }
            if (dashTimer % 10 == 0){
                this.breakBlocks();
            }
        }
    }

    protected void breakBlocks() {
        if (Utils.isBlockDestructionAllowed(mob)) {
            Utils.destroyBlockSphere(
                    11,
                    new Vec3(0, 11, 5),
                    mob.blockPosition(),
                    mob.getYRot(),
                    mob.level(),
                    true
            );
        }
    }

    protected void dealDashDamage() {
        Level level = mob.level();
        AABB damageArea = mob.getBoundingBox().inflate(20);
        List<LivingEntity> nearbyEntities = level.getEntitiesOfClass(LivingEntity.class, damageArea);

        for (LivingEntity entity : nearbyEntities) {
            if (!mob.isSelf(entity) && mob.distanceTo(entity) <= 20) {
                // ダメージ処理
                entity.hurt(mob.damageSources().mobAttack(mob), mob.getMechData().meleeDamage);

                // ノックバック
                Vec3 knockback = entity.getDeltaMovement().normalize().scale(3);
                entity.knockback(10, -knockback.x, -knockback.y);
            }
        }
    }
}
