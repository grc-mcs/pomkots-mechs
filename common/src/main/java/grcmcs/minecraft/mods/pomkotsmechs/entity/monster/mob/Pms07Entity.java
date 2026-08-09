package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob;

import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidObjectiveEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal.NearestEntityTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal.RollerDashGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Pms07Entity extends BaseSmallMonsterEntity implements GeoEntity, GeoAnimatable {
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
        return "pms07";
    }
    public Pms07Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new RollerDashGoal(
                this,
                getMechData().speed * 2,
                12F,
                20,
                5,
                5,
                30,
                20
        ));
    }

    private int attackDelay = 0;
    private AABB attackAabb = null;

    public void tick() {
        super.tick();

        if (isServerSide() && attackDelay > 0 && attackAabb != null) {
            attackDelay--;

            if (attackDelay == 0) {
                this.rotateToTarget(this.getTarget());

                var world = this.level();

                var kbVel = new Vec3(0, 0, -1F).yRot((float) Math.toRadians((-1.0) * this.getYRot()));


                for (var ent : world.getEntities(null, attackAabb)) {
                    if (this.equals(ent)) {
                        continue;
                    }

                    if (ent instanceof LivingEntity le) {
                        le.knockback(3, kbVel.x, kbVel.z);

                        DamageSource ds = this.damageSources().generic();
                        le.hurt(ds, getMechData().meleeDamage);
                    }
                }
            }
        }
    }

    @Override
    public void doAttack() {
        if (this.isServerSide() && this.getTarget() != null) {
            Vec3 aabb1;
            Vec3 aabb2;

            int pattern = this.random.nextInt(2);
            if (pattern == 0) {
                aabb1 = new Vec3(6.5, 4.0F, 18F).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
                aabb2 = new Vec3(-2, -4F, -4F).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
                this.triggerAnim("shoot_controller", "pile");

            } else {
                aabb1 = new Vec3(18F, 4.0F, 18F).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
                aabb2 = new Vec3(-18F, -4F, -18F).yRot((float) Math.toRadians((-1.0) * this.getYRot())).add(this.position());
                this.triggerAnim("shoot_controller", "saber");
            }

            this.attackDelay = 5;
            this.attackAabb = new AABB(aabb1, aabb2);
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
                .triggerableAnim("pile", RawAnimation.begin().thenPlay("animation.pms.attack_pile"))
                .triggerableAnim("saber", RawAnimation.begin().thenPlay("animation.pms.attack_blade"))
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

    public static boolean canSpawn(EntityType<Pms07Entity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return GenericPomkotsMonster.canSpawnCommon(type, world, reason, pos, random);
    }
}
