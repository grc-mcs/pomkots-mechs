package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.MechCapsuleItem;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public class MechCapsuleProjectileEntity extends ThrowableItemProjectile {

    public MechCapsuleProjectileEntity(EntityType<? extends MechCapsuleProjectileEntity> type, Level level) {
        super(type, level);
    }

    public MechCapsuleProjectileEntity(Level level, LivingEntity owner) {
        super(PomkotsMechs.MECH_CAPSULE_PROJECTILE.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return PomkotsMechs.MECH_CAPSULE_ITEM.get();
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide) {
            LivingEntity ent = MechCapsuleItem.generateEntity(this.level(), this.getItem());

            if (ent != null) {
                Vec3 safePos = resolveSpawnCollision(
                        this.level(),
                        this.position(),
                        ent.getBoundingBox(),
                        this.getDeltaMovement().normalize().reverse()
                );

                ent.moveTo(safePos);
                this.level().addFreshEntity(ent);
            }

            this.discard();

        } else {
            playBasicExplosion(this.level(), this.position());

        }
    }

    public static Vec3 resolveSpawnCollision(
            Level level,
            Vec3 desiredPos,
            AABB baseAabb,
            Vec3 incomingDir
    ) {
        // AABBを理想位置へ移動
        AABB aabb = baseAabb.move(desiredPos.subtract(baseAabb.getCenter()));

        // 衝突Shape収集
        var collisions = level.getBlockCollisions(null, aabb);

        // 押し戻し量
        double dx = incomingDir.x;
        double dy = incomingDir.y;
        double dz = incomingDir.z;

        Direction hitDir = Direction.getNearest(
                incomingDir.x,
                incomingDir.y,
                incomingDir.z
        );

        switch (hitDir.getAxis()) {
            case X -> dx = Shapes.collide(Direction.Axis.X, aabb, collisions, dx);
            case Z -> dz = Shapes.collide(Direction.Axis.Z, aabb, collisions, dz);
            case Y -> dy = Shapes.collide(Direction.Axis.Y, aabb, collisions, dy);
        }

        Vec3 corrected = desiredPos.add(dx, dy, dz);

        return corrected;
    }

    @Override
    public void onClientRemoval() {
        if (this.level().isClientSide) {
            playBasicExplosion(this.level(), this.position());
        }
    }

    public static void playBasicExplosion(Level level, Vec3 pos) {
        for (int i = 0; i < 50; i++) {
            double offsetX = (Math.random() - 0.5) * 2.0;
            double offsetY = Math.random() * 1.5;
            double offsetZ = (Math.random() - 0.5) * 2.0;

            double velocityX = offsetX * 0.1;
            double velocityY = 0.1 + Math.random() * 0.1;
            double velocityZ = offsetZ * 0.1;

            level.addParticle(
                    ParticleTypes.POOF,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    velocityX,
                    velocityY,
                    velocityZ
            );
        }

        // 爆発パーティクル
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * Math.PI * 2;
            double distance = Math.random() * 5;

            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;
            double offsetY = Math.random() * distance;

            level.addParticle(
                    ParticleTypes.EXPLOSION,
                    pos.x + offsetX,
                    pos.y + offsetY,
                    pos.z + offsetZ,
                    0, 0, 0
            );
        }

        level.playLocalSound(
                pos.x, pos.y, pos.z,
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.BLOCKS,
                0.5f,
                1.2f,
                false
        );
    }
}
