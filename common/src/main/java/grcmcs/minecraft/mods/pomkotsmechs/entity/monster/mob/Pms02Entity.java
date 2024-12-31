package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.GenericMobGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.MaintainAltitudeGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BulletMiddleEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileEnemyEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
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

// Flying Mob
public class Pms02Entity extends GenericPomkotsMonster implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final float DEFAULT_SCALE = 1.0f;
    public static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    public Pms02Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);

        this.setMaxUpStep(BattleBalance.MOB_STEP_UP);
        this.setSpeed(1F);
        this.setPersistenceRequired();
        this.setNoGravity(true);
        this.setYRot(0F);
        this.noCulling = true;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new MaintainAltitudeGoal(this, 30, 0.3F));
        this.goalSelector.addGoal(2, new GenericMobGoal(this, 0.8F) {
            protected void startRandomWalk(float speedModifier) {
                Vec3 rnd = DefaultRandomPos.getPos(this.mob, 10, 7);
                if (rnd != null) {
                    this.mob.getNavigation().moveTo(rnd.x, rnd.y, rnd.z, this.speedModifier);
                    this.walkCount = 1;

                }
            }
            protected void startWalk(LivingEntity target, float speedModifier) {
                this.mob.getNavigation().moveTo(target, this.speedModifier);
                this.walkCount = 1;
            }
        });
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, false, false));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public void doAttack() {
        if (this.isServerSide()) {
            this.rotateToTarget(this.getTarget());

            long pattern = this.level().getGameTime() % 3;

            if (pattern == 0) {
                for (int i = 0; i < 2; i++) {
                    MissileEnemyEntity be = new MissileEnemyEntity(PomkotsMechs.MISSILE_ENEMY.get(), this.level(), this);

                    var offset = this.position();

                    // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                    var muzzlPos = new Vec3(1 - i * 2, 3.0F, 0);
                    muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                    be.setPos(offset.add(muzzlPos));
                    be.shootFromRotation(be, -10, this.getYRot() -30 + i * 60, this.getFallFlyingTicks(), 0.9F, 0F);

                    this.level().addFreshEntity(be);
                }

            } else {
                for (int i = 0; i < 2; i++) {
                    BulletMiddleEntity be = new BulletMiddleEntity(PomkotsMechs.BULLETMIDDLE.get(), this.level(), this);

                    var offset = this.position();

                    // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                    var muzzlPos = new Vec3(1 - i * 2, 3.0F, 0);
                    muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

                    be.setPos(offset.add(muzzlPos));
                    be.setYRot(this.getYRot());
                    be.setXRot(this.getXRot());
                    be.yRotO = this.getYRot();
                    be.xRotO = this.getXRot();

                    be.shootFromRotation(be, this.getXRot(), this.getYRot(), this.getFallFlyingTicks(), 0.9F, 0F);

                    this.level().addFreshEntity(be);
                }
            }

            this.triggerAnim("shoot_controller", "shoot");
        }
    }

    @Override
    public int getMaxAttackCooltime() {
        return 30;
    }

//    @Override
//    protected PathNavigation createNavigation(Level level) {
//        return new FlyingPathNavigation(this, level);
//    }

    @Override protected float getFlyingSpeed() {
        return 0.6f;
    }

    @Override
    public boolean onGround() {
        return false; // モンスターが地上にいると認識されないようにする
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0, event -> {
            if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms02.idle"));

            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms02.idle"));

            }
        }));

        controllers.add(new AnimationController<>(this, "shoot_controller", state -> PlayState.STOP)
                .triggerableAnim("shoot", RawAnimation.begin().thenPlay("animation.pms02.idle"))
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
