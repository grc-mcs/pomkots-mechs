package grcmcs.minecraft.mods.pomkotsmechs.client.input;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PlateEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class TargetFinder {
    private final double maxDistance;
    private final double viewAngle;
    private final float viewAngleCosineThreshold;;
    private final TargetPriority targetPriority;


    private Minecraft minecraft = Minecraft.getInstance();

    TargetFinder() {
        maxDistance = 150;
        viewAngle = 10;
        viewAngleCosineThreshold = Mth.cos((float)Math.toRadians(viewAngle));
        targetPriority = new TargetPriority(1D, 1D, 2D);
    }

    public LivingEntity findTargetEntity(Player player) {
        Camera cam = minecraft.gameRenderer.getMainCamera();
        List<LivingEntity> candidates = getEntitiesInViewDirection(cam, player);

        return candidates.stream()
                .filter(entity -> isValidTarget(cam, entity))
                .max((a, b) -> Double.compare(
                        targetPriority.calculatePriority(player, a),
                        targetPriority.calculatePriority(player, b)
                ))
                .orElse(null);
    }

    private List<LivingEntity> getEntitiesInViewDirection(Camera cam, Player player) {
        Level level = player.level();

        Vec3 eyePos = cam.getPosition();
        Vec3 lookDirection = new Vec3(cam.getLookVector());

        double searchWidth = maxDistance * Math.tan(Math.toRadians(viewAngle / 2.0)) * 2.2; // 少し余裕を持たせる
        AABB roughBox = createDirectionalAABB(eyePos, lookDirection, maxDistance, searchWidth);

        return level.getEntitiesOfClass(LivingEntity.class, roughBox, entity -> {
            return entity != player && !isSelf(entity, player) && isTargetClass(entity) && hasLineOfSight(player, entity);
        });
    }

    private boolean isTargetClass(LivingEntity entity) {
        return (entity instanceof PomkotsVehicleBase && PomkotsMechs.CONFIG.targetLockPomkotsVehicles)
                || entity instanceof GenericPomkotsMonster
                || entity instanceof BossHitBoxEntity
                || (entity instanceof Player && PomkotsMechs.CONFIG.targetLockPlayers)
                || entity instanceof PlateEntity
                || (PomkotsMechs.CONFIG.targetLockNonPomkotsMobs && entity != null);
    }

    private boolean hasLineOfSight(Entity src, Entity candidate) {
        if (candidate.level() != src.level()) {
            return false;
        } else {
            Vec3 vec3 = new Vec3(src.getX(), src.getEyeY(), src.getZ());
            Vec3 vec32 = new Vec3(candidate.getX(), candidate.getEyeY(), candidate.getZ());
            if (vec32.distanceTo(vec3) > maxDistance) {
                return false;
            } else {
                return src.level().clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, src)).getType() == HitResult.Type.MISS;
            }
        }
    }

    private boolean isSelf(LivingEntity entity, LivingEntity player) {
        if (entity instanceof PomkotsVehicleBase) {
            var driver = ((PomkotsVehicleBase) entity).getDrivingPassenger();
            if (driver == null) {
                return false;
            } else {
                return driver.equals(player);
            }
        } else {
            return false;
        }
    }

    private boolean isValidTarget(Camera cam, LivingEntity entity) {
        return isInLockonTraceRange(cam, entity, viewAngleCosineThreshold);
    }

    private boolean isInLockonTraceRange(Camera cam, Entity ent, float cosineThreshold) {
        Vec3 playerToEnemy = ent.position().subtract(cam.getPosition()).normalize();
        Vec3 playerLookDirection = new Vec3(cam.getLookVector().normalize());

        double dotProduct = playerToEnemy.dot(playerLookDirection);

        return dotProduct >= cosineThreshold;
    }

    private AABB createDirectionalAABB(Vec3 eyePos, Vec3 lookDirection, double maxDistance, double width) {
        // 視線方向のベクトルを正規化
        Vec3 direction = lookDirection.normalize();

        // 視線方向の終点
        Vec3 endPoint = eyePos.add(direction.scale(maxDistance));

        // 視線に垂直な方向のベクトルを計算（幅を決定）
        Vec3 right = direction.cross(new Vec3(0, 1, 0)).normalize();
        Vec3 up = right.cross(direction).normalize();

        // 視線方向の直方体を作成
        double halfWidth = width / 2.0;

        Vec3 min = new Vec3(
                Math.min(eyePos.x, endPoint.x) - halfWidth,
                Math.min(eyePos.y, endPoint.y) - halfWidth,
                Math.min(eyePos.z, endPoint.z) - halfWidth
        );

        Vec3 max = new Vec3(
                Math.max(eyePos.x, endPoint.x) + halfWidth,
                Math.max(eyePos.y, endPoint.y) + halfWidth,
                Math.max(eyePos.z, endPoint.z) + halfWidth
        );

        return new AABB(min, max);
    }
}
