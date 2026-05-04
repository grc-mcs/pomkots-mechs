package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class PomkotsCustomThrowableProjectile extends PomkotsThrowableProjectile {
    protected int maxLifeTicks = 20;
    protected int lifeTicks = 0;
    protected float damage;
    protected int stun;

    private static final int SUBSTEPS = 1;

    public PomkotsCustomThrowableProjectile(
            EntityType<? extends ThrowableProjectile> entityType, Level world,
            LivingEntity shooter,
            int maxLifeTicks, float damage, int stun) {
        super(entityType, shooter, world);
        this.maxLifeTicks = maxLifeTicks;
        this.damage = damage;
        this.stun = stun;
    }

    @Override
    public void tick() {
        this.setOldPosAndRot();

        if (this.level().isClientSide) {
            return;
        }

        if (this.lifeTicks++ >= this.maxLifeTicks) {
            this.discard();
            return;
        }

        this.checkInsideBlocks();

        if (!performSubStepMovement()) {
            return;
        }
    }

    protected boolean performSubStepMovement() {
        Vec3 totalMovement = this.getDeltaMovement();
        Vec3 subStepMovement = totalMovement;

        for (int step = 0; step < SUBSTEPS; step++) {
            Vec3 start = this.position();
            Vec3 end = start.add(subStepMovement);

            // ブロックへのレイキャスト
            BlockHitResult blockHit = this.level().clip(new ClipContext(
                    start, end,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    this
            ));

            // エンティティへのレイキャスト（射出者を除外）
            AABB searchBox = new AABB(start, end).inflate(0.5);
            EntityHitResult entityHit = getEntityHitResult(
                    this.level(),
                    this,
                    start,
                    end,
                    searchBox,
                    entity -> entity != this.getShooter()
                            && entity != this
                            && entity.isAlive()
                            && entity.isPickable()
                            && !entity.isSpectator()
                            && !(entity instanceof BossHitBoxEntity hb && hb.isRelatedShooter(this))
            );

            HitResult hit = selectCloserHit(start, blockHit, entityHit);

            if (hit != null && hit.getType() != HitResult.Type.MISS) {
                this.setPosInSubStep(hit.getLocation());
                this.onHit(hit);
                return false;
            }

            this.setPosInSubStep(end);
        }

        return true; // 生存
    }

    protected void setPosInSubStep(Vec3 pos) {
        this.setPos(pos);
    }

    protected EntityHitResult getEntityHitResult(
            Level level,
            Entity projectile,
            Vec3 start,
            Vec3 end,
            AABB searchBox,
            Predicate<Entity> filter) {

        Entity closestEntity = null;
        Vec3 closestHitPos = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity candidate : level.getEntities(projectile, searchBox, filter)) {

            // 当たり判定をわずかに膨らませる（掠り当たり対応）
            AABB candidateBox = candidate.getBoundingBox().inflate(candidate.getPickRadius());

            Optional<Vec3> hitPos = candidateBox.clip(start, end);

            if (hitPos.isPresent()) {
                double dist = start.distanceToSqr(hitPos.get());

                if (dist < closestDist) {
                    closestDist = dist;
                    closestEntity = candidate;
                    closestHitPos = hitPos.get();
                }
            }
        }

        return closestEntity != null
                ? new EntityHitResult(closestEntity, closestHitPos)
                : null;
    }

    /**
     * ブロックとエンティティのヒット結果から近い方を返す
     */
    protected HitResult selectCloserHit(Vec3 origin,
                                      BlockHitResult blockHit,
                                      EntityHitResult entityHit) {
        boolean blockMiss = blockHit.getType() == HitResult.Type.MISS;

        if (entityHit == null && blockMiss) {
            return null; // 両方ミス
        }
        if (entityHit == null) {
            return blockHit;
        }
        if (blockMiss) {
            return entityHit;
        }

        // 両方ヒット → 近い方を返す
        double blockDist = origin.distanceToSqr(blockHit.getLocation());
        double entityDist = origin.distanceToSqr(entityHit.getLocation());
        return entityDist <= blockDist ? entityHit : blockHit;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        var entity = entityHitResult.getEntity();

        if (entity instanceof BossHitBoxEntity hb && hb.isRelatedShooter(this)) {
            return;
        }

        // ダメージ計算（射出直後はボーナスダメージ）
        float finalDamage = damage;
        if (lifeTicks < 10) {
            finalDamage += (10 - (float) lifeTicks) * damage / 20;
        }

        // スタン付与
        if (entity instanceof BaseBossEntity boss) {
            boss.addStunPoint(stun);
        } else if (entity instanceof BossHitBoxEntity hitbox) {
            hitbox.addStunPoint(stun);
        }

        // ダメージ付与
        ProjectileUtil.hurt(entity, this, this.getOwner(), finalDamage);
        entity.invulnerableTime = 0;

        this.discard();
    }

    public int getStunPoint() {
        return this.stun;
    }

    public void setStunPoint(int stunPoint) {
        this.stun = stunPoint;
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        this.discard();
    }

    @Override
    public void onClientRemoval() {
        if (this.level().isClientSide) {
            ParticleUtil.addSparkParticles(this.position(), this.level());
        }
    }

    // 重力を無効化（ThrowableProjectileの挙動を上書き）
    @Override
    protected float getGravity() {
        return 0.0F;
    }

//    protected int maxLifeTicks = 20;
//    protected int lifeTicks = 0;
//    protected float damage;
//    protected int stun;
//
//    public PomkotsCustomThrowableProjectile(
//            EntityType<? extends ThrowableProjectile> entityType, Level world,
//            LivingEntity shooter,
//            int maxLifeTicks, float damage, int stun) {
//        super(entityType, shooter, world);
//        this.maxLifeTicks = maxLifeTicks;
//        this.damage = damage;
//        this.stun = stun;
//    }
//
//    @Override
//    public void tick() {
//        var hitResult = ProjectileUtil.raycastBoundingCheck(this);
//        if (hitResult.getType() != HitResult.Type.MISS) {
//            this.onHit(hitResult);
//        }
//
//        if (this.level().isClientSide) {
//            return;
//        }
//
//        super.tick();
//
//        if(this.lifeTicks++ >= this.maxLifeTicks) {
//            this.discard();
//        }
//    }
//
//    @Override
//    protected void onHitEntity(EntityHitResult entityHitResult) {
//        var entity = entityHitResult.getEntity();
//        if (entity.equals(shooter)) {
//            return;
//        }
//
//        if (lifeTicks < 10) {
//            damage += (10 - (float)lifeTicks) * damage / 20;
//        }
//
//        if (entity instanceof BaseBossEntity boss) {
//            boss.addStunPoint(stun);
//        } else if (entity instanceof BossHitBoxEntity hitbox) {
//            hitbox.addStunPoint(stun);
//        }
//
//        ProjectileUtil.hurt(entity, this, this.shooter, damage);
//        entity.invulnerableTime = 0;
//    }
//
//    @Override
//    protected void onHitBlock(BlockHitResult blockHitResult) {
//        this.discard();
//    }
//
//    @Override
//    public void onClientRemoval() {
//        if (this.level().isClientSide) {
//            ParticleUtil.addSparkParticles(this.position(), this.level());
//        }
//    }
}
