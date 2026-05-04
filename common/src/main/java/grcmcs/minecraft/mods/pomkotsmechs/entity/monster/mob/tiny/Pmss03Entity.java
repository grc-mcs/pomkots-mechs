package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.goal.MeleeAttackGoal2;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Pmss03Entity extends BaseTinyMonsterEntity implements GeoEntity, GeoAnimatable {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final float DEFAULT_SCALE = 1.0f;

    private static final Vec3 ATTACK_OFFSET = new Vec3(0, 0, 2);
    private static final double ATTACK_RADIUS = 2.5F;

    @Override
    public String getMechName() {
        return "pmss03";
    }

    public Pmss03Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();
        if (isAttacking()) {
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        }

        int fuse = this.getFuse();
        if (fuse >= 0) {
            fuse--;
            this.setFuse(fuse);

            if (fuse == 0) {
                explode();
            }
        }
    }

    private static final EntityDataAccessor<Integer> FUSE =
            SynchedEntityData.defineId(Pmss03Entity.class, EntityDataSerializers.INT);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FUSE, -1); // -1 = 非自爆状態
    }

    public void startSelfDestruct() {
        this.entityData.set(FUSE, 40);
    }

    public void stopSelfDestruct() {
        this.entityData.set(FUSE, -1);
        this.attackingTime = 0;
    }

    public int getFuse() {
        return this.entityData.get(FUSE);
    }

    public void setFuse(int fuse) {
        this.entityData.set(FUSE, fuse);
    }

    public boolean isSelfDestructing() {
        return getFuse() >= 0;
    }

    private void explode() {
        if (!this.level().isClientSide) {
            Utils.explode(
                    this,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    this.getMechData().grenadeExplosionScale,
                    Level.ExplosionInteraction.MOB,
                    this.level()
            );
            this.discard();
        }
    }

    @Override
    protected void startAttack() {
        super.startAttack();
        this.startSelfDestruct();
        this.triggerAnim("shoot_controller", "attack_charge");
    }

    @Override
    protected void tickAttack() {
        super.tickAttack();
    }

    @Override
    public int getMaxAttackCooltime() {
        return 80;
    }

    @Override
    public int getMaxAttackTicks() {
        return 50;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isSelfDestructing()) {
            this.stopSelfDestruct();
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MeleeAttackGoal2(this, getMechData().speed, false));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, getMechData().speed));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
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
        }).setSoundKeyframeHandler(this::playSounds));

        controllers.add(new AnimationController<>(this, "shoot_controller", state -> PlayState.STOP)
                .triggerableAnim("attack_charge", RawAnimation.begin().thenPlay("animation.pms.attack_charge"))
                .triggerableAnim("close", RawAnimation.begin().thenPlayAndHold("animation.pms.close"))
                .triggerableAnim("open", RawAnimation.begin().thenPlay("animation.pms.boot"))
                .setSoundKeyframeHandler(this::playSounds)
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
}
