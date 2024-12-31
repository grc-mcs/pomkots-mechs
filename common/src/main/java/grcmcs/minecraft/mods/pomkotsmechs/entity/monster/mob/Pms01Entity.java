package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.ChargingMobGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

// Charging Mob
public class Pms01Entity extends GenericPomkotsMonster implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final float DEFAULT_SCALE = 1.0f;
    public static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    private int attackingTime = 0;

    public boolean isAttacking() {
        return attackingTime > 0;
    }

    public Pms01Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);

        this.setMaxUpStep(3.0F);
        this.setSpeed(BattleBalance.MOB_SPEED);
        this.setPersistenceRequired();
        this.setNoGravity(false);
        this.setYRot(0F);
        this.noCulling = true;
    }

    @Override
    public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attackingTime > 0) {
            attackingTime--;
        }

        if (isServerSide()) {
            if (attackingTime == 15) {
                this.rotateToTarget(this.getTarget());
                this.updateHomingMovement();
            } else if (attackingTime > 0 && attackingTime < 15) {
                attack();
            }
        }
    }

    private void attack() {
        var kbVel = new Vec3(0, 0, -1F).yRot((float) Math.toRadians((-1.0) * this.getYRot()));
        for (var ent : level().getEntities(null, this.getBoundingBox().inflate(2))) {
            if (ent instanceof Player || ent instanceof Pmv01Entity) {
                LivingEntity le = (LivingEntity) ent;
                if (isServerSide()) {
                    le.knockback(2, kbVel.x, kbVel.z);
                    le.hurt(this.damageSources().generic(), 8);
                } else {
                    addHitParticles(le);
                }
            }
        }
    }

    private void addHitParticles(Entity target) {
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


    protected void updateHomingMovement() {
        Vec3 currentPosition = this.position();
        var target = this.getTarget();

        if (target != null) {
            Vec3 targetPosition = target.position();

            // 現在の進行方向とターゲット方向を計算
            Vec3 directionToTarget = targetPosition.subtract(currentPosition).normalize();

            // 新しい速度ベクトルに基づいて進行方向を更新
            this.setDeltaMovement(directionToTarget.scale(10));
        }
    }

    @Override
    public void doAttack() {
        this.rotateToTarget(this.getTarget());
        attackingTime = 20;

        if (this.isServerSide()) {
            this.setDeltaMovement(new Vec3(0,2,0));
            this.triggerAnim("shoot_controller", "shoot");
        }
    }

    @Override
    public int getMaxAttackCooltime() {
        return 80;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new ChargingMobGoal(this, 0.8F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, false, false));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0, event -> {
            if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms01.walk"));

            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms01.idle"));

            }
        }));

        controllers.add(new AnimationController<>(this, "shoot_controller", state -> PlayState.STOP)
                .triggerableAnim("shoot", RawAnimation.begin().thenPlay("animation.pms01.attack"))
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
