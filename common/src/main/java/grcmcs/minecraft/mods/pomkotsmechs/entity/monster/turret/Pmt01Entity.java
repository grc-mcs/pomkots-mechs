package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal.NearestEntityTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.goal.ContinuousAttackGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Pmt01Entity extends BaseTurretEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final float DEFAULT_SCALE = 1.0f;

    @Override
    public String getMechName() {
        return "pmt01";
    }

    public Pmt01Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
    }

    protected void registerTargetSelectorGoals() {
        super.registerTargetSelectorGoals();
        this.goalSelector.addGoal(1, new ContinuousAttackGoal(this, 0.8F, 100, 60, 10));
    }

    public void tick() {
        super.tick();

        LivingEntity target = this.getTarget();
        if (!level().isClientSide && target != null) {
            this.rotateToTarget(target);
        }
    }

    private int curSide = 1;
    public void startActionAnimation() {
        this.triggerAnim("shoot_controller", "shoot");
        curSide = 1;
    }

    public void stopActionAnimation() {
        this.triggerAnim("shoot_controller", "stop");
    }

    @Override
    public void doAttack() {
        var target = this.getTarget();
        if (target != null) {
            BulletGrenadeEntity be = new BulletGrenadeEntity(PomkotsMechs.BULLET_GRENADE.get(), this.level(), this, getMechData().bulletDamage);
            be.setNoGravity(true);
            be.setExplosionScale(3);
            be.setStunPoint(2);

            var offset = this.position();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）

            var muzzlPos = new Vec3(1 * curSide, 2, 4);
            curSide *= -1;
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            be.setPos(offset.add(muzzlPos));

            if (!Utils.isObstructed(this.level(), be, target)) {
                float[] angle = Utils.getShootingAngle(be, target, true);

                be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), getMechData().bulletSpeed, 0F);

                this.level().addFreshEntity(be);
            }
        }
    }

//    @Override
//    protected void registerGoals() {
//        super.registerGoals();
//
//        this.goalSelector.addGoal(1, new ContinuousAttackGoal(this, 0.8F, 100, 60, 10));
//        this.targetSelector.addGoal(1, new NearestEntityTargetGoal<>(this, Player.class, false, false));
//    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0,
                event -> event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmt01.idle"))));

        controllers.add(new AnimationController<>(this, "shoot_controller", state -> PlayState.STOP)
                .triggerableAnim("shoot", RawAnimation.begin().thenLoop("animation.pmt01.attack"))
                .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.pmt01.idle"))
                .setSoundKeyframeHandler(this::registerAnimationSoundHandlers)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
