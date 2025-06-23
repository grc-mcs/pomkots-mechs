package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class BaseSmallMonsterEntity extends GenericPomkotsMonster {
    protected BaseSmallMonsterEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);

        this.setMaxUpStep(getMechData().maxStepUp);
        this.setSpeed(getMechData().speed);

        this.setPersistenceRequired();
        this.setNoGravity(false);
        this.setYRot(0F);
        this.noCulling = true;
    }

    private int closedTick = 0;

    @Override
    public void tick() {
        super.tick();

        if (isServerSide() && isClosed()) {
            if (closedTick == 40) {
                fireCloseAnimation();
                closedTick--;
            }

            if (onGround()) {
                if (closedTick > 0) {
                    closedTick--;
                }

                if (closedTick == 20){
                    fireOpenAnimation();
                } else if (closedTick <= 0) {
                    setClosed(false);
                }
            }
        }
    }

    abstract protected void fireOpenAnimation();

    abstract protected void fireCloseAnimation();

    @Override
    public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
        return false;
    }

    private static final EntityDataAccessor<Boolean> CLOSED = SynchedEntityData.defineId(BaseSmallMonsterEntity.class, EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CLOSED, false); // 初期値を設定
    }

    public void setClosed(boolean value) {
        if (value) {
            this.closedTick = 40;
        }
        this.entityData.set(CLOSED, value);
    }

    public boolean isClosed() {
        return this.entityData.get(CLOSED);
    }

    @Override
    public boolean isPersistenceRequired() {
        return isPersistence;
    }

    protected boolean isPersistence = false;

    public void setPersistence(boolean b) {
        this.isPersistence = b;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean(PomkotsMechs.nbtName("IsPersistence"), isPersistence);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.isPersistence = compound.getBoolean(PomkotsMechs.nbtName("IsPersistence"));
    }

    protected void addHitParticles(Entity target) {
        var offset = new Vec3(target.position().x, target.getBoundingBox().getCenter().y, target.position().z);

        for (int i = 0; i < 40; i++) {
            // ランダムな速度を生成
            double velocityX = random.nextDouble() * 4.3 - 1;
            double velocityY = random.nextDouble() * 4.3 - 1;
            double velocityZ = random.nextDouble() * 4.3 - 1;

            // パーティクルをクライアント側で発生させる
            this.level().addAlwaysVisibleParticle(PomkotsMechs.SPARK.get(),
                    true,
                    offset.x(), offset.y(), offset.z(), // 位置
                    velocityX, velocityY, velocityZ // 速度
            );
        }
    }
}
