package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParticleUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    public static void addParticles(Entity hitTarget) {
        if (hitTarget.level().isClientSide) {
            var offset = hitTarget.getBoundingBox().getCenter();
            addSparkParticles(offset, hitTarget.level());
        }
    }

    public static void addParticles(DamageSource ds, Entity hitTarget) {
        if (hitTarget.level().isClientSide) {
            Vec3 offset;

            var srcEnt = ds.getDirectEntity();

            // 時々srcEntがnullになるんやがどういう事や…
            if (srcEnt != null) {
                if (srcEnt.getBoundingBox().getSize() < 8 && srcEnt instanceof ThrowableProjectile) {
                    offset = srcEnt.getBoundingBox().getCenter();
                } else {
                    offset = hitTarget.getBoundingBox().getCenter();
                }

                addSparkParticles(offset, hitTarget.level());
            }
        }
    }

    private static void addSparkParticles(Vec3 offset, Level level) {
        RandomSource random = level.getRandom();

        for (int i = 0; i < 30; i++) {
            // ランダムな速度を生成
            double velocityX = random.nextDouble() * 2.0 - 1;
            double velocityY = random.nextDouble() * 2.0 - 1;
            double velocityZ = random.nextDouble() * 2.0 - 1;

            // パーティクルをクライアント側で発生させる
            level.addAlwaysVisibleParticle(PomkotsMechs.SPARK.get(), true,
                    offset.x(), offset.y(), offset.z(), // 位置
                    velocityX, velocityY, velocityZ // 速度
            );


        }

        for (int i = 0; i < 3; i++) {
            level.addAlwaysVisibleParticle(PomkotsMechs.MISSILE_SMOKE.get(), true,
                    offset.x(), offset.y() + random.nextDouble(), offset.z(), // 位置
                    0, random.nextDouble(), 0 // 速度
            );
        }
    }
}
