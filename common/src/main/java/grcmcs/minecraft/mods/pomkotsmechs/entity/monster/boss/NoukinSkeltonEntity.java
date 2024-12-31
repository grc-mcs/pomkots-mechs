package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.AttackMeleeBossGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.WalkBossGoal;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
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

public class NoukinSkeltonEntity extends GenericPomkotsMonster implements GeoEntity, GeoAnimatable {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final float DEFAULT_SCALE = 2f;
    public static final Logger LOGGER = LoggerFactory.getLogger(PomkotsMechs.MODID);

    private final BossActionController actionController;

    public static AttributeSupplier.Builder createMobAttributes() {
        return createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, BattleBalance.MOB_FOLLOW_RANGE)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.KNOCKBACK_RESISTANCE, BattleBalance.BOSS_KNOCKBACK_RESISTANCE)
                .add(Attributes.MAX_HEALTH, BattleBalance.BOSS_HEALTH);
    }

    public NoukinSkeltonEntity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);

        this.actionController = new BossActionController();
        this.setMaxUpStep(BattleBalance.BOSS_STEP_UP);
        this.setSpeed(BattleBalance.BOSS_SPEED);
        this.setPersistenceRequired();
        this.setNoGravity(false);
        this.setYRot(0F);
        this.noCulling = true;

        // 各アクションのクールタイムやらアニメーション、発動処理のトリガーを行う設定をする
        this.actionController.registerAction("attack", new BossActionController.BossAction(120,55,15,
                (Void) -> {if (this.isServerSide()) {this.triggerAnim("action_controller", "attack");}},
                (Void) -> {this.attackNormal();}));
        this.actionController.registerAction("charge", new BossActionController.BossAction(120,43,12,
                (Void) -> {if (this.isServerSide()) {this.triggerAnim("action_controller", "charge");}},
                (Void) -> {this.attackCharge();}));
        this.actionController.registerAction("punch", new BossActionController.BossAction(120,63,20,
                (Void) -> {if (this.isServerSide()) {this.triggerAnim("action_controller", "punch");}},
                (Void) -> {this.attackPunch();}));
        this.actionController.registerAction("tatsumaki", new BossActionController.BossAction(12,20,15,
                (Void) -> {if (this.isServerSide()) {this.triggerAnim("action_controller", "tatsumaki");}},
                (Void) -> {this.attackTatsumaki();}));

        // 上記の各アクションを、AIのGoalとして登録する
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, false, false));
        this.goalSelector.addGoal(1, new AttackMeleeBossGoal(actionController.getAction("tatsumaki"), this, 0.8F));
        this.goalSelector.addGoal(1, new AttackMeleeBossGoal(actionController.getAction("attack"), this, 0.8F));
        this.goalSelector.addGoal(1, new AttackMeleeBossGoal(actionController.getAction("charge"), this, 0.8F));
        this.goalSelector.addGoal(1, new AttackMeleeBossGoal(actionController.getAction("punch"), this, 0.8F));
        this.goalSelector.addGoal(2, new WalkBossGoal(this, 0.6F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    private void attackNormal() {
    }

    private void attackCharge() {
    }

    private void attackPunch() {
    }

    private void attackTatsumaki() {

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0, event -> {
            if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.noukinskelton.walk"));
            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.noukinskelton.idle"));
            }
        }));

        controllers.add(new AnimationController<>(this, "action_controller", state -> PlayState.STOP)
                .triggerableAnim("attack", RawAnimation.begin().thenPlay("animation.noukinskelton.attack1"))
                .triggerableAnim("charge", RawAnimation.begin().thenPlay("animation.noukinskelton.charge1").thenPlay("animation.noukinskelton.charge2"))
                .triggerableAnim("punch", RawAnimation.begin().thenPlay("animation.noukinskelton.punch").thenPlay("animation.noukinskelton.punch2").thenPlay("animation.noukinskelton.punch3"))
                .triggerableAnim("tatsumaki", RawAnimation.begin().thenPlay("animation.noukinskelton.tatsumaki"))
        );
    }

    @Override
    public void tick() {
        var world = this.level();
        super.tick();
        this.actionController.tick();

        if (this.isServerSide()) {
            if (this.actionController.getAction("tatsumaki").currentActionTick < 15 && this.actionController.getAction("tatsumaki").currentActionTick > 0) {
                AABB atari = this.getBoundingBox().inflate(5);

                var kbVel = new Vec3(0, 0, -3F).yRot((float) Math.toRadians((-1.0) * this.getYRot()));
                for (var ent : world.getEntities(null, atari)) {
                    if (ent.equals(this)) {
                        continue;
                    }

                    if (ent instanceof LivingEntity le) {
                        if (!world.isClientSide()) {
                            le.knockback(2, kbVel.x, kbVel.z);
                            le.hurt(this.damageSources().generic(), 1);
                        }
                    }
                }
            }
        }

        rotateToTarget(getTarget());

    }

    private final ServerBossEvent bossInfo = new ServerBossEvent(this.getDisplayName(), ServerBossEvent.BossBarColor.RED, ServerBossEvent.BossBarOverlay.PROGRESS);

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossInfo.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossInfo.removePlayer(player);
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        this.bossInfo.setProgress(this.getHealth() / this.getMaxHealth());
    }

    public void rotateToTarget(LivingEntity target) {
        if (target == null) {
            return;
        }

        Vec3 tgtPos = target.position();
        Vec3 slfPos = this.position();
        Vec3 v = slfPos.vectorTo(tgtPos);

        this.setYRot((float) Math.toDegrees(Mth.atan2(v.z, v.x)) - 90);
        this.setYBodyRot(this.getYRot());
        this.setYHeadRot(this.getYRot());
        this.yRotO = this.getYRot();
        this.yBodyRotO = this.getYRot();
        this.yHeadRotO = this.getYRot();
    }

    @Override
    public boolean tryAttack() {
        // Goalで実装するので使わない
        return false;
    }

    @Override
    public void doAttack() {
        // Goalで実装するので使わない
    }

    @Override
    public int getMaxAttackCooltime() {
        // Goalで実装するので使わない
        return 100;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private void playSoundEffect(SoundEvent event) {
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), event, SoundSource.PLAYERS, 1.0F, 1.0F, false);
    }

    @Override
    public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
        return false;
    }
}
