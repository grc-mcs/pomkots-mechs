package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidObjectiveEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.RaidTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.util.ServerElectricSparkEffect;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class BaseSmallMonsterEntity extends GenericPomkotsMonster {
    private SmallMobHitBoxEntity damageHitBox;

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0F)
                .add(Attributes.FOLLOW_RANGE, 100);
    }

    protected BaseSmallMonsterEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);

        this.setMaxUpStep(getMechData().maxStepUp);
        this.setSpeed(getMechData().speed);

        this.setNoGravity(false);
        this.setYRot(0F);
//        this.noCulling = true;
    }

    private int closedTick = 0;

    @Override
    public void tick() {
        if (this.firstTick && this.isInEvent()) {
            this.noCulling = true;
        }

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

        handleElectricStun();

        if (!level().isClientSide && usesSeparateDamageHitBox()) {
            ensureDamageHitBox();
        }
    }

    protected boolean usesSeparateDamageHitBox() {
        return true;
    }

    protected float getDamageHitBoxWidth() {
        return 3.0F;
    }

    protected float getDamageHitBoxHeight() {
        return 3.0F;
    }

    public SmallMobHitBoxEntity getDamageHitBox() {
        return damageHitBox;
    }

    private void ensureDamageHitBox() {
        if (!isAlive() || (damageHitBox != null && !damageHitBox.isRemoved())) {
            return;
        }

        SmallMobHitBoxEntity hitBox = new SmallMobHitBoxEntity(PomkotsMechs.SMALL_MOB_HITBOX.get(), level());
        hitBox.attachTo(this, getDamageHitBoxWidth(), getDamageHitBoxHeight());
        if (level().addFreshEntity(hitBox)) {
            damageHitBox = hitBox;
        }
    }

    abstract protected void fireOpenAnimation();

    abstract protected void fireCloseAnimation();

    @Override
    public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
        return false;
    }

    private int electricStunTick = 0;
    
    public void onElectricStun() {
        if (isServerSide() && electricStunTick == 0) {
            electricStunTick = 40;
            
            double height = getBbHeight() / 2;
            ServerElectricSparkEffect.spawnOverTime(
                (ServerLevel)this.level(),
                this.position().add(0, height, 0),
                height, // 球の半径
                40,   // 期間中に生成する総数
                2.0D  // 発生時間（秒）
            );
        }
    }

    private void handleElectricStun() {
        if (isServerSide() && electricStunTick > 0) {
            this.setDeltaMovement(Vec3.ZERO);
            electricStunTick--;
        }
    }

    private static final EntityDataAccessor<Boolean> CLOSED = SynchedEntityData.defineId(BaseSmallMonsterEntity.class, EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CLOSED, false);
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
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.registerBaseGoals();
    }

    protected void registerBaseGoals() {
        this.targetSelector.addGoal(1, new RaidTargetGoal<RaidObjectiveEntity>(
                this,
                RaidObjectiveEntity.class, // 拠点中心を表すエンティティ
                this.getAttribute(Attributes.FOLLOW_RANGE).getBaseValue(),
                200,
                this::isInRaid
        ));
        this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Player.class, true, this::predicate));
    }

    public boolean predicate(Object target) {
        if (target instanceof Player p) {
            boolean ridingMech = p.getVehicle() instanceof PomkotsVehicleBase;

            if (ridingMech) {
                return true;

            } else if (p.distanceTo(this) <= this.getAttribute(Attributes.FOLLOW_RANGE).getBaseValue() * 0.25) {
                double dx = p.getX() - this.getX();
                double dz = p.getZ() - this.getZ();
                Vec3 look = this.getLookAngle();
                double dot = dx * look.x + dz * look.z;

                if (dot > 0) {
                    if (p.getMainHandItem().is(PomkotsMechs.CARTON.get())) {
                        return p.isSprinting();
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    };
}
