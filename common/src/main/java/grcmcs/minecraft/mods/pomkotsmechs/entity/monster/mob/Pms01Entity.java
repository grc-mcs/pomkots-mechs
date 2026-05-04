package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidObjectiveEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal.NearestEntityTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.ChargingMobGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.RaidTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

// Charging Mob
public class Pms01Entity extends BaseSmallMonsterEntity implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final float DEFAULT_SCALE = 1.0f;

    private int attackingTime = 0;

    public boolean isAttacking() {
        return attackingTime > 0;
    }

    @Override
    public String getMechName() {
        return "pms01";
    }

    public Pms01Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
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
            if (ent instanceof Player || ent instanceof PomkotsVehicleBase) {
                LivingEntity le = (LivingEntity) ent;
                if (isServerSide()) {
                    le.knockback(2, kbVel.x, kbVel.z);
                    le.hurt(this.damageSources().generic(), getMechData().meleeDamage);
                } else {
                    addHitParticles(le);
                }
            }
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

        this.goalSelector.addGoal(1, new ChargingMobGoal(this, getMechData().speed));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0, event -> {
            if (isClosed()) {
                return PlayState.STOP;
            } else if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms.walk"));

            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms.idle"));

            }
        }));

        controllers.add(new AnimationController<>(this, "shoot_controller", state -> PlayState.STOP)
                .triggerableAnim("shoot", RawAnimation.begin().thenPlay("animation.pms.attack"))
                .triggerableAnim("close", RawAnimation.begin().thenPlayAndHold("animation.pms.close"))
                .triggerableAnim("open", RawAnimation.begin().thenPlay("animation.pms.boot"))
        );
    }

    @Override
    protected void fireOpenAnimation() {
        this.triggerAnim("shoot_controller", "open");
    }

    @Override
    protected void fireCloseAnimation() {
        this.triggerAnim("shoot_controller", "close");
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    public static boolean canSpawn(EntityType<Pms01Entity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return GenericPomkotsMonster.canSpawnCommon(type, world, reason, pos, random);
    }
}
