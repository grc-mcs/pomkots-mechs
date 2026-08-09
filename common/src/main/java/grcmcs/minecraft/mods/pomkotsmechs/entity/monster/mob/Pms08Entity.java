package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidObjectiveEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal.NearestEntityTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.RollerDashGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

// Gun Mob
public class Pms08Entity extends BaseSmallMonsterEntity implements GeoEntity, GeoAnimatable {
    @Override
    protected boolean shouldStayAboveWaterSurface() { return true; }

    @Override
    protected float getDamageHitBoxWidth() { return 5.0F; }

    @Override
    protected float getDamageHitBoxHeight() { return 5.0F; }

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final float DEFAULT_SCALE = 1.0f;

    @Override
    public String getMechName() {
        return "pms08";
    }

    public Pms08Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new RollerDashGoal(
                this,
                getMechData().speed * 2,
                5F,
                20,
                3,
                3,
                30,
                20
        ));
    }

    private boolean hasBeenKilled = true;

    @Override
    public void doAttack() {
        if (this.isServerSide() && this.getTarget() != null) {
            var level = this.level();
            ExplosionEntity e = new ExplosionEntity(PomkotsMechs.EXPLOSION.get(), level, (int)getMechData().grenadeExplosionScale);
            e.setPos(this.position());
            level.addFreshEntity(e);

            hasBeenKilled = false;
            this.kill();
        }
    }

    @Override
    protected void tickDeath() {
        ++this.deathTime;
        if (this.deathTime == 10) {
            this.remove(RemovalReason.KILLED);
            if (isServerSide() && hasBeenKilled) {
                var level = this.level();
                ExplosionEntity e = new ExplosionEntity(PomkotsMechs.EXPLOSION.get(), level);
                e.setPos(this.position());
                level.addFreshEntity(e);
            }
        }
    }

    @Override
    public int getMaxAttackCooltime() {
        return 20;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0, event -> {
            if (isClosed()) {
                return PlayState.STOP;
            } else if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pms.dash"));

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

    public static boolean canSpawn(EntityType<Pms08Entity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return GenericPomkotsMonster.canSpawnCommon(type, world, reason, pos, random);
    }
}
