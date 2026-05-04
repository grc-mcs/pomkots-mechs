package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidObjectiveEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal.NearestEntityTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.SimpleMobGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileEnemyEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEnemyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
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

// Missile Mob
public class Pms04Entity extends BaseSmallMonsterEntity implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final float DEFAULT_SCALE = 1.0f;
    public static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    @Override
    public String getMechName() {
        return "pms04";
    }

    public Pms04Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public void doAttack() {
        if (this.isServerSide()) {
            this.rotateToTarget(this.getTarget());

            MissileGenericEnemyEntity be = new MissileGenericEnemyEntity(PomkotsMechs.MISSILE_ENEMY.get(), this.level(),
                    this, this.getTarget(),
                    getMechData().missileDamage, getMechData().missileSpeed);
            be.setSwitchTick(32);

            var offset = this.position();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(1, 3.0F, 0);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            be.setPos(offset.add(muzzlPos));
            be.shootFromRotation(be, -89, this.getYRot(), this.getFallFlyingTicks(), 0.9F, 0F);

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

        this.goalSelector.addGoal(1, new SimpleMobGoal(this, getMechData().speed));
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

    public static boolean canSpawn(EntityType<Pms04Entity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return GenericPomkotsMonster.canSpawnCommon(type, world, reason, pos, random);
    }
}
