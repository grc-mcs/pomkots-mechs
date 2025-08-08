package grcmcs.minecraft.mods.pomkotsmechs.client.input;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TargetPriority {
    private final double distanceWeight;
    private final double angleWeight;
    private final double sizeWeight;

    public TargetPriority(double distanceWeight, double angleWeight, double sizeWeight) {
        this.distanceWeight = distanceWeight;
        this.angleWeight = angleWeight;
        this.sizeWeight = sizeWeight;
    }

    public double calculatePriority(Player player, Entity target) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDirection = player.getLookAngle();

        // 距離スコア (近いほど高い)
        double distance = eyePos.distanceTo(target.position());
        double distanceScore = Math.max(0, 1.0 - (distance / 150.0));

        // 角度スコア (中心に近いほど高い)
        Vec3 toTarget = target.position().subtract(eyePos).normalize();
        double dot = lookDirection.dot(toTarget);
        double angleScore = Math.max(0, dot); // -1 to 1 → 0 to 1

        // サイズスコア (大きいほど高い)
        AABB bounds = target.getBoundingBox();
        double size = bounds.getXsize() * bounds.getYsize() * bounds.getZsize();
        double sizeScore = Math.min(1.0, size / 8.0); // 8ブロック³を基準

        return distanceScore * distanceWeight +
                angleScore * angleWeight +
                sizeScore * sizeWeight;
    }
}

