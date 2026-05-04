package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.goal.MeleeAttackGoal2;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

public class Pmss01Entity extends BaseTinyMonsterEntity implements GeoEntity, GeoAnimatable {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final float DEFAULT_SCALE = 1.0f;

    private static final Vec3 ATTACK_OFFSET = new Vec3(0, 0, 2);
    private static final double ATTACK_RADIUS = 2.5F;

    @Override
    public String getMechName() {
        return "pmss01";
    }

    public Pmss01Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public void tick() {
        super.tick();
        if (isAttacking()) {
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
        }
    }

    @Override
    protected void startAttack() {
        super.startAttack();
        this.triggerAnim("shoot_controller", "attack_melee");
    }

    @Override
    protected void tickAttack() {
        super.tickAttack();

        if (this.attackingTime == 10) {
            actualAttack();
        }
    }

    private void actualAttack() {
        var impactPoint = Utils.computeAoeCenter(this, ATTACK_OFFSET);
        Utils.doCircleAoeDamageAndKnockback(
                this.level(),
                this,
                impactPoint,
                ATTACK_RADIUS,
                getMechData().meleeDamage,
                2,
                0.5
        );
    }

    @Override
    public int getMaxAttackCooltime() {
        return 60;
    }

    @Override
    public int getMaxAttackTicks() {
        return 20;
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
                .triggerableAnim("attack_melee", RawAnimation.begin().thenPlay("animation.pms.attack_melee"))
                .triggerableAnim("close", RawAnimation.begin().thenPlayAndHold("animation.pms.close"))
                .triggerableAnim("open", RawAnimation.begin().thenPlay("animation.pms.boot"))
                .setSoundKeyframeHandler(this::playSounds)
        );
    }

    protected void playSounds(SoundKeyframeEvent event) {
        if ("se_impact".equals(event.getKeyframeData().getSound())) {
            this.playSoundEffect(PomkotsMechs.SE_GASHAN.get(), 1, 32);

            var impactPoint = Utils.computeAoeCenter(this, ATTACK_OFFSET);
            Utils.spawnAoeDustParticles(this.level(), impactPoint, ATTACK_RADIUS, 40);

        } else {
            super.playSounds(event);
        }
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
