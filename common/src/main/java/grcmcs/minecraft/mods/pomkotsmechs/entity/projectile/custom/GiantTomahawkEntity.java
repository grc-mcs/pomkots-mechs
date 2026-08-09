package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.SmallMobHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.util.ServerElectricSparkEffect;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;

public class GiantTomahawkEntity extends PomkotsCustomThrowableProjectile {
    public static final float DEFAULT_DAMAGE = 50.0F;
    public static final int ELECTRIC_DELAY_TICKS = 20;
    public static final int ELECTRIC_DURATION_TICKS = 80;
    public static final int STUCK_LIFETIME_TICKS = ELECTRIC_DELAY_TICKS + ELECTRIC_DURATION_TICKS;
    private static final double ELECTRIC_HITBOX_INFLATION = 5D;
    private static final double ELECTRIC_EFFECT_RADIUS = 5D;
    private static final int ELECTRIC_SPARK_COUNT = 160;
    private static final int MAX_LIFETIME_TICKS = 100;

    private static final EntityDataAccessor<Boolean> STUCK =
            SynchedEntityData.defineId(GiantTomahawkEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> STUCK_YAW =
            SynchedEntityData.defineId(GiantTomahawkEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FLIGHT_YAW =
            SynchedEntityData.defineId(GiantTomahawkEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> ELECTRIFIED =
            SynchedEntityData.defineId(GiantTomahawkEntity.class, EntityDataSerializers.BOOLEAN);

    private int stuckTicks;
    private boolean impactSoundPlayed;
    private final Set<Integer> electricallyStunnedEntityIds = new HashSet<>();

    public GiantTomahawkEntity(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        this(entityType, level, null, DEFAULT_DAMAGE);
    }

    public GiantTomahawkEntity(
            EntityType<? extends ThrowableProjectile> entityType,
            Level level,
            LivingEntity shooter,
            float damage
    ) {
        super(entityType, level, shooter, MAX_LIFETIME_TICKS, damage, 5);
        setNoGravity(true);
        noPhysics = true;
        noCulling = true;
    }

    @Override
    public void tick() {
        if (isStuck()) {
            setOldPosAndRot();
            setDeltaMovement(Vec3.ZERO);
            if (!level().isClientSide) {
                stuckTicks++;

                if (stuckTicks == ELECTRIC_DELAY_TICKS) {
                    entityData.set(ELECTRIFIED, true);
                    ServerElectricSparkEffect.spawnOverTime(
                            (ServerLevel) level(),
                            position(),
                            ELECTRIC_EFFECT_RADIUS,
                            ELECTRIC_SPARK_COUNT,
                            ELECTRIC_DURATION_TICKS / 20.0D
                    );
                }

                if (isElectrified()) {
                    applyElectricStunInExpandedHitbox();
                }

                if (stuckTicks >= STUCK_LIFETIME_TICKS) {
                    discard();
                }
            }
            return;
        }

        Vec3 velocity = getDeltaMovement();
        updateFlightRotation(velocity);

        if (level().isClientSide) {
            // Predict movement locally; the server remains authoritative for impacts.
            setOldPosAndRot();
            setPos(position().add(velocity));
            return;
        }

        super.tick();
        hasImpulse = true;
    }

    private void updateFlightRotation(Vec3 velocity) {
        double horizontal = velocity.horizontalDistance();
        if (velocity.lengthSqr() > 1.0E-7D) {
            // Inverse of Projectile.shootFromRotation: velocity.x uses -sin(yaw).
            setYRot((float) (Mth.atan2(-velocity.x, velocity.z) * Mth.RAD_TO_DEG));
            setXRot((float) (Mth.atan2(velocity.y, horizontal) * Mth.RAD_TO_DEG));
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        playImpactSound(hitResult.getLocation());
        Vec3 velocity = getDeltaMovement();
        float impactYaw = velocity.horizontalDistanceSqr() > 1.0E-7D
                ? (float) (Mth.atan2(-velocity.x, velocity.z) * Mth.RAD_TO_DEG)
                : getYRot();

        setPos(hitResult.getLocation());
        setDeltaMovement(Vec3.ZERO);
        entityData.set(STUCK_YAW, impactYaw);
        entityData.set(STUCK, true);
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        if (!level().isClientSide) {
            applyElectricStun(hitResult.getEntity());
        }
        
        playImpactSound(hitResult.getLocation());
        super.onHitEntity(hitResult);
        discard();
    }

    private void applyElectricStunInExpandedHitbox() {
        var electricHitbox = getBoundingBox().inflate(ELECTRIC_HITBOX_INFLATION);
        for (Entity target : level().getEntities(this, electricHitbox, Entity::isAlive)) {
            if (target == getShooter() || target == getOwner()) {
                continue;
            }

            if (!electricallyStunnedEntityIds.contains(target.getId()) && applyElectricStun(target)) {
                electricallyStunnedEntityIds.add(target.getId());
            }
        }
    }

    private boolean applyElectricStun(Entity target) {
        if (target instanceof BaseSmallMonsterEntity monster) {
            monster.onElectricStun();
            return true;
        }
        if (target instanceof SmallMobHitBoxEntity hitbox && hitbox.getParentMob() != null) {
            hitbox.getParentMob().onElectricStun();
            return true;
        }
        if (target instanceof Pmvc01Entity mech) {
            mech.onElectricStun();
            return true;
        }
        return false;
    }

    private void playImpactSound(Vec3 position) {
        if (level().isClientSide || impactSoundPlayed) {
            return;
        }

        impactSoundPlayed = true;
        level().playSound(
                null,
                position.x,
                position.y,
                position.z,
                PomkotsMechs.SE_TOMAHAWK_IMPACT.get(),
                SoundSource.PLAYERS,
                0.7F,
                0.85F
        );
    }

    public boolean isStuck() {
        return entityData.get(STUCK);
    }

    public float getStuckYaw() {
        return entityData.get(STUCK_YAW);
    }

    /** Sets the exact yaw used by the firing code, independently of vanilla projectile rotation updates. */
    public void setFlightYaw(float yaw) {
        entityData.set(FLIGHT_YAW, yaw);
    }

    public float getFlightYaw() {
        return entityData.get(FLIGHT_YAW);
    }

    public boolean isElectrified() {
        return entityData.get(ELECTRIFIED);
    }

    @Override
    public float getHitDamage() {
        return damage;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(STUCK, false);
        entityData.define(STUCK_YAW, 0.0F);
        entityData.define(FLIGHT_YAW, 0.0F);
        entityData.define(ELECTRIFIED, false);
    }
}
