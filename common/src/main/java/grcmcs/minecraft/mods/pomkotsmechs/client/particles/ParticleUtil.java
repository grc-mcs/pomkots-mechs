package grcmcs.minecraft.mods.pomkotsmechs.client.particles;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.PomkotsControllable;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

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

    public static void addSparkParticles(Vec3 offset, Level level) {
        RandomSource random = level.getRandom();

        for (int i = 0; i < 10; i++) {
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
//
//        for (int i = 0; i < 3; i++) {
//            level.addAlwaysVisibleParticle(PomkotsMechs.MISSILE_SMOKE.get(), true,
//                    offset.x(), offset.y() + random.nextDouble(), offset.z(), // 位置
//                    0, random.nextDouble(), 0 // 速度
//            );
//        }
    }

    public static void addSparkParticles(Vec3 offset, Level level, int num, ParticleOptions particle) {
        RandomSource random = level.getRandom();

        for (int i = 0; i < num; i++) {
            // ランダムな速度を生成
            double velocityX = random.nextDouble() * 2.0 - 1;
            double velocityY = random.nextDouble() * 2.0 - 1;
            double velocityZ = random.nextDouble() * 2.0 - 1;

            // パーティクルをクライアント側で発生させる
            level.addAlwaysVisibleParticle(particle, true,
                    offset.x(), offset.y(), offset.z(), // 位置
                    velocityX, velocityY, velocityZ // 速度
            );
        }
//
//        for (int i = 0; i < num / 3; i++) {
//            level.addAlwaysVisibleParticle(PomkotsMechs.MISSILE_SMOKE.get(), true,
//                    offset.x(), offset.y() + random.nextDouble(), offset.z(), // 位置
//                    0, random.nextDouble(), 0 // 速度
//            );
//        }
    }

    public static void addSparkParticlesMedium(Vec3 offset, Level level) {
        RandomSource random = level.getRandom();

        for (int i = 0; i < 20; i++) {
            // ランダムな速度を生成
            double velocityX = random.nextDouble() * 2.0 - 1;
            double velocityY = random.nextDouble() * 2.0 - 1;
            double velocityZ = random.nextDouble() * 2.0 - 1;

            level.addAlwaysVisibleParticle(PomkotsMechs.SPARK.get(), true,
                    offset.x(), offset.y(), offset.z(), // 位置
                    velocityX, velocityY, velocityZ // 速度
            );
        }
    }

    public static void addSparkParticlesSmall(Vec3 offset, Level level) {
        RandomSource random = level.getRandom();

        for (int i = 0; i < 5; i++) {
            // ランダムな速度を生成
            double velocityX = random.nextDouble() * 2.0 - 1;
            double velocityY = random.nextDouble() * 2.0 - 1;
            double velocityZ = random.nextDouble() * 2.0 - 1;

            level.addAlwaysVisibleParticle(PomkotsMechs.SPARK.get(), true,
                    offset.x(), offset.y(), offset.z(), // 位置
                    velocityX, velocityY, velocityZ // 速度
            );
        }
    }

    public static void spawnAttachedMuzzleFlash(
            ClientLevel level,
            Entity mech,
            int weaponAttachPoint,
            double size,
            Vec3 localOffset
    ) {
        AttachedMuzzleFlashOptions options =
                new AttachedMuzzleFlashOptions(
                        mech.getId(),
                        weaponAttachPoint,
                        (float) size,
                        (float) localOffset.x,
                        (float) localOffset.y,
                        (float) localOffset.z
                );

        level.addParticle(
                options,
                localOffset.x,
                localOffset.y,
                localOffset.z,
                0D,
                0D,
                0.0D
        );
    }

    public static void spawnAttachedMuzzleFlash(
            ServerLevel level,
            Entity mech,
            double size,
            Vec3 localOffset
    ) {
        AttachedMuzzleFlashOptions options =
                new AttachedMuzzleFlashOptions(
                        mech.getId(),
                        // 今んとこサーバサイドからはボスしか呼ばないので適当においとく
                        AttachedMuzzleFlashOptions.WEAPON_POINT_RIGHT_ARM,
                        (float) size,
                        (float) localOffset.x,
                        (float) localOffset.y,
                        (float) localOffset.z
                );


        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(mech) > 200D * 200D) {
                continue;
            }

            level.sendParticles(
                    player,
                    options,
                    true,
                    mech.getX(),
                    mech.getY(),
                    mech.getZ(),
                    1,
                    0.0D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }
}
