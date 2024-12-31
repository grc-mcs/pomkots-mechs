package grcmcs.minecraft.mods.pomkotsmechs.entity.monster;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.items.CoreStonePMB01Item;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericPomkotsMonster extends Monster {

    public static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);
    // 搭乗してから操作開始するまでの間のティック
    protected short rideCoolTick = 0;

    protected GenericPomkotsMonster(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, BattleBalance.MOB_FOLLOW_RANGE)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.KNOCKBACK_RESISTANCE, BattleBalance.MOB_KNOCKBACK_RESISTANCE)
                .add(Attributes.MAX_HEALTH, BattleBalance.MOB_HEALTH);
    }

    protected int attackCooltime = 0;

    public boolean tryAttack() {
        if (this.attackCooltime == 0) {
            if (this.isClientSide()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), PomkotsMechs.SE_GRENADE_EVENT.get(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
            }
            doAttack();
            this.attackCooltime = getMaxAttackCooltime();

            return true;
        } else {
            return false;
        }
    }

    public boolean isInAttackCooltime() {
        return attackCooltime > 0;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        var passes = this.getPassengers();

        if (passes.isEmpty()) {
            return null;
        } else {
            for (var ent: passes) {
                if (ent instanceof LivingEntity le) {
                    return le;
                }
            }
            return null;
        }
    }

    @Override
    public void tick() {
        if (!(this.getControllingPassenger() instanceof Player player)) {
            this.setNoAi(false);
        } else {
            if (rideCoolTick > 0) {
                rideCoolTick--;

            } else {
                if (player.swinging) {
                    this.tryAttack();
                }
            }
        }

        super.tick();

        if (this.attackCooltime > 0) {
            this.attackCooltime--;
        }

        LivingEntity target = this.getTarget();

        if (!level().isClientSide && target != null) {
            this.getLookControl().setLookAt(target, 30F, 30F);
        }
    }

    @Override
    public void travel(Vec3 pos) {
        if (this.isAlive() && this.isVehicle()) {
            LivingEntity pilot = this.getControllingPassenger();

            if (pilot != null) {
                // ROTATE Vehicle
                this.setYRot(pilot.getYRot());
                this.yRotO = this.getYRot();
                this.setXRot(pilot.getXRot() * 0.5F);
                this.setRot(this.getYRot(), this.getXRot());
                this.setYBodyRot(this.getYRot());
                this.setYHeadRot(this.getYRot());
                float f = pilot.xxa * 0.5F;
                float f1 = pilot.zza * 0.5F;

                super.travel(new Vec3(f, pos.y, f1));
            }
        } else {
            super.travel(pos);
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && player.getMainHandItem().getItem() instanceof CoreStonePMB01Item) {
            player.setYRot(this.getYRot());
            player.setXRot(this.getXRot());
            player.startRiding(this);
            this.setNoAi(true);
        }
        this.rideCoolTick = 3;
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    public void rotateToTarget(LivingEntity target) {
        if (this.level().isClientSide || target == null) {
            return; // サーバー側のみで処理する
        }

        Vec3 tgtPos = target.position();
        Vec3 slfPos = this.position();
        Vec3 v = slfPos.vectorTo(tgtPos);

        this.setYRot((float) Math.toDegrees(Mth.atan2(v.z, v.x)) - 90);
        this.setYBodyRot(this.getYRot());
        this.setYHeadRot(this.getYRot());
        this.yRotO = this.getYRot();
        this.yBodyRotO = this.getYRot();
        this.yHeadRotO = this.getYRot();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return true;
    }

    @Override
    public boolean isPersistenceRequired() {
        // デスポーンを防ぐために常にtrueを返す
        return true;
    }

    @Override
    public void checkDespawn() {
    }

    protected boolean isServerSide() {
        return !isClientSide();
    }

    protected boolean isClientSide() {
        return this.level().isClientSide();
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime == 10) {
            this.remove(RemovalReason.KILLED);
            if (isServerSide()) {
                var level = this.level();
                ExplosionEntity e = new ExplosionEntity(PomkotsMechs.EXPLOSION.get(), level);
                e.setPos(this.position());
                level.addFreshEntity(e);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource ds, float dmg) {
        ParticleUtil.addParticles(ds,this);
        return super.hurt(ds, dmg);
    }

    @Override
    public void playHurtSound(DamageSource ds) {
        if (this.isClientSide()) {
            this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), PomkotsMechs.SE_HIT_EVENT.get(), SoundSource.PLAYERS, 1.0F, 1.0F, false);
        }
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    protected abstract void doAttack();

    protected abstract int getMaxAttackCooltime();
}
