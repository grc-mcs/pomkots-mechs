package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.GenericMobGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BulletMiddleEntity;
import net.minecraft.world.entity.EntityType;
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

// Gun Mob
public class Pms03Entity extends GenericPomkotsMonster implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final float DEFAULT_SCALE = 1.0f;
    public static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    public Pms03Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);

        this.setMaxUpStep(BattleBalance.MOB_STEP_UP);
        this.setSpeed(BattleBalance.MOB_SPEED);
        this.setPersistenceRequired();
        this.setNoGravity(false);
        this.setYRot(0F);
        this.noCulling = true;
    }

    @Override
    public void doAttack() {
        if (this.isServerSide()) {
            this.rotateToTarget(this.getTarget());

            BulletMiddleEntity be = new BulletMiddleEntity(PomkotsMechs.BULLETMIDDLE.get(), this.level(), this);

            var offset = this.position();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(1, 3.0F, 0);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            be.setPos(offset.add(muzzlPos));
            be.setYRot(this.getYRot());
            be.setXRot(this.getXRot());
            be.yRotO = this.getYRot();
            be.xRotO = this.getXRot();

            be.shootFromRotation(be, this.getXRot(), this.getYRot(), this.getFallFlyingTicks(), 2F, 0F);

            this.level().addFreshEntity(be);

            this.triggerAnim("shoot_controller", "shoot");
        }
    }

    @Override
    public int getMaxAttackCooltime() {
        return 20;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new GenericMobGoal(this, 0.8F));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, false, false));
        this.goalSelector.addGoal(4, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0, event -> {
            if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms03.walk"));

            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms03.idle"));

            }
        }));

        controllers.add(new AnimationController<>(this, "shoot_controller", state -> PlayState.STOP)
                .triggerableAnim("shoot", RawAnimation.begin().thenPlay("animation.pms03.attack"))
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
