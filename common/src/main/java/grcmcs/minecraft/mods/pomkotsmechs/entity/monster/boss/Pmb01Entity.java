package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.PomkotsControllable;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.AttackBreakBossGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.AttackLongRangeBossGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.AttackMeleeBossGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal.WalkBossGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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

public class Pmb01Entity extends GenericPomkotsMonster implements GeoEntity, GeoAnimatable, PomkotsControllable {
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

    public Pmb01Entity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
        super(entityType, world);

        this.actionController = new BossActionController();
        this.setMaxUpStep(BattleBalance.BOSS_STEP_UP);
        this.setSpeed(BattleBalance.BOSS_SPEED);
        this.setPersistenceRequired();
        this.setNoGravity(false);
        this.setYRot(0F);
        this.noCulling = true;

        // 各アクションのクールタイムやらアニメーション、発動処理のトリガーを行う設定をする
        this.actionController.registerAction("punch", new BossActionController.BossAction(180,20,15,
                (Void) -> {if (this.isServerSide()) {this.triggerAnim("action_controller", "punch");}},
                (Void) -> {this.attackPunch();}));
        this.actionController.registerAction("upper", new BossActionController.BossAction(60,20,12,
                (Void) -> {if (this.isServerSide()) {this.triggerAnim("action_controller", "upper");}},
                (Void) -> {this.attackUpper();}));
        this.actionController.registerAction("jump", new BossActionController.BossAction(60,25,20,
                (Void) -> {if (this.isServerSide()) {this.triggerAnim("action_controller", "jump");}},
                (Void) -> {this.attackJump();}));
        this.actionController.registerAction("msl", new BossActionController.BossAction(180,20,15,
                (Void) -> {if (this.isServerSide()) {this.triggerAnim("action_controller", "msl");}},
                (Void) -> {this.attackMissile();}));
        this.actionController.registerAction("lmsl", new BossActionController.BossAction(600,20,15,
                (Void) -> {if (this.isServerSide()) {this.triggerAnim("action_controller", "lmsl");}},
                (Void) -> {this.attackLargeMissile();}));
        this.actionController.registerAction("gat", new BossActionController.BossAction(180,5,1, 10,
                (Void) -> {if (this.isServerSide()) {this.triggerAnim("action_controller", "gat");}},
                (Void) -> {this.attackGatling();}));
        this.actionController.registerAction("grn", new BossActionController.BossAction(180,20,12,
                (Void) -> {if (this.isServerSide()) {this.triggerAnim("action_controller", "grn");}},
                (Void) -> {this.attackGrenade();}));
        this.actionController.registerAction("lbrk", new BossActionController.BossAction(180,95,12,
                (Void) -> {if (this.isServerSide()) {this.triggerAnim("action_controller", "lbrk");}},
                (Void) -> {}));

        // 上記の各アクションを、AIのGoalとして登録する
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal(this, Player.class, false, false));
        this.goalSelector.addGoal(1, new AttackBreakBossGoal(actionController.getAction("lbrk"), this, 0.8F));
        this.goalSelector.addGoal(1, new AttackMeleeBossGoal(actionController.getAction("punch"), this, 0.8F));
        this.goalSelector.addGoal(1, new AttackMeleeBossGoal(actionController.getAction("upper"), this, 0.8F));
        this.goalSelector.addGoal(1, new AttackMeleeBossGoal(actionController.getAction("jump"), this, 0.8F));
        this.goalSelector.addGoal(1, new AttackLongRangeBossGoal(actionController.getAction("msl"), this, 0.8F));
        this.goalSelector.addGoal(1, new AttackLongRangeBossGoal(actionController.getAction("lmsl"), this, 0.8F));
        this.goalSelector.addGoal(1, new AttackLongRangeBossGoal(actionController.getAction("gat"), this, 0.8F, true));
        this.goalSelector.addGoal(1, new AttackLongRangeBossGoal(actionController.getAction("grn"), this, 0.8F));
        this.goalSelector.addGoal(2, new WalkBossGoal(this, 0.6F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    private void attackPunch() {
        if (isServerSide()) {
            var offset = this.position();

            for (int i = 0; i < 12; i++) {

                var muzzlPos = new Vec3(0, 0, 4);
                muzzlPos = muzzlPos.yRot((float) Math.toRadians(-90 + i * 15));
                muzzlPos = offset.add(muzzlPos);
                SlashEntity be = new SlashEntity(PomkotsMechs.EXPLOADSLASH.get(), this.level());

                be.setPos(offset);
//                be.setYRot(i * 30 - 180);
                be.shootFromRotation(be, 0, -180 + i * 30, this.getFallFlyingTicks(), 1.5F, 0F);
                this.level().addFreshEntity(be);
            }
        } else {
            this.playSoundEffect(PomkotsMechs.SE_EXPLOADSPARK_EVENT.get());
        }
    }

    private void attackUpper() {
        if (isServerSide()) {
            var offset = this.position();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(0, 0, 3);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            var vec = muzzlPos.normalize().multiply(6,6,6);
            muzzlPos = offset.add(muzzlPos);

            EarthraiseEntity be = new EarthraiseEntity(PomkotsMechs.EARTHRAISE.get(), this.level(), vec, this, 8);
            be.setPos(muzzlPos);
            be.setYRot((float)(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90.0));

//            be.shootFromRotation(be, 0, 0, this.getFallFlyingTicks(), 0F, 0F);
            this.level().addFreshEntity(be);

        } else {
            this.playSoundEffect(PomkotsMechs.SE_PILEBUNKER_EVENT.get());
        }
    }

    private void attackJump() {
        if (isServerSide()) {
            var offset = this.position();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(0, 0, -3);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            muzzlPos = offset.add(muzzlPos);

            EarthbreakEntity be = new EarthbreakEntity(PomkotsMechs.EARTHBREAK.get(), this.level(), this);
            be.setPos(muzzlPos);
            be.shootFromRotation(be, 0, 0, this.getFallFlyingTicks(), 0F, 0F);
            this.level().addFreshEntity(be);

        } else {
            this.playSoundEffect(PomkotsMechs.SE_PILEBUNKER_EVENT.get());
        }
    }

    private void attackMissile() {
        if (this.isServerSide()) {
            var offset = this.position();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(-10, 15F, 0);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            muzzlPos = offset.add(muzzlPos);

            for (int i = 0; i < 5; i++) {
                MissileEnemyEntity be = new MissileEnemyEntity(PomkotsMechs.MISSILE_ENEMY.get(), this.level(), this);

                be.setPos(muzzlPos.add(0, i, 0));
                be.shootFromRotation(be, -i * 2, this.getYRot() + 20, this.getFallFlyingTicks(), 1.5F, 0F);
                this.level().addFreshEntity(be);
            }

            muzzlPos = new Vec3(10, 15F, 0);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            muzzlPos = offset.add(muzzlPos);

            for (int i = 0; i < 5; i++) {
                MissileEnemyEntity be = new MissileEnemyEntity(PomkotsMechs.MISSILE_ENEMY.get(), this.level(), this);

                be.setPos(muzzlPos.add(0, i, 0));
                be.shootFromRotation(be, -i * 2, this.getYRot() - 20, this.getFallFlyingTicks(), 1.5F, 0F);
                this.level().addFreshEntity(be);
            }
        } else {
            this.playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get());
        }
    }

    private void attackLargeMissile() {
        if (isServerSide()) {
            var offset = this.position();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(0, 15F, 1);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            muzzlPos = offset.add(muzzlPos);

            MissileEnemyLargeEntity be = new MissileEnemyLargeEntity(PomkotsMechs.MISSILE_ENEMY_LARGE.get(), this.level(), this);

            be.setPos(muzzlPos);
            be.shootFromRotation(be, -89, this.getYRot(), this.getFallFlyingTicks(), 1.5F, 0F);
            this.level().addFreshEntity(be);

        } else {
            this.playSoundEffect(PomkotsMechs.SE_MISSILE_EVENT.get());
        }
    }

    private void attackGatling() {
        if (isServerSide()) {
            var target = this.getTarget();

            if (target != null && !this.isVehicle()) {
                this.rotateToTarget(target);
            }

            GrenadeEntity be = new GrenadeEntity(PomkotsMechs.GRENADE.get(), this.level(), this, BattleBalance.BOSS_GATLING_EXPLOSION);

            var offset = this.position();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(-8, 8.0F, -1);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            be.setPos(offset.add(muzzlPos));
            be.setYRot(this.getYRot());
            be.setXRot(this.getXRot());
            be.yRotO = this.getYRot();
            be.xRotO = this.getXRot();

            var angle = getShootingAngle(be, target);
            be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), 2F, 0F);

            this.level().addFreshEntity(be);
        } else {
            this.playSoundEffect(PomkotsMechs.SE_HIT_EVENT.get());
        }
    }

    private void attackGrenade() {
        if (isServerSide()) {
            var target = this.getTarget();

            if (target != null && !this.isVehicle()) {
                this.rotateToTarget(target);
            }

            GrenadeLargeEntity be = new GrenadeLargeEntity(PomkotsMechs.GRENADELARGE.get(), this.level(), this, BattleBalance.BOSS_GRENADE_EXPLOSION);

            var offset = this.position();

            // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
            var muzzlPos = new Vec3(8, 8.0F, -1);
            muzzlPos = muzzlPos.yRot((float) Math.toRadians((-1.0) * this.getYRot()));

            be.setPos(offset.add(muzzlPos));
            be.setYRot(this.getYRot());
            be.setXRot(this.getXRot());
            be.yRotO = this.getYRot();
            be.xRotO = this.getXRot();

            var angle = getShootingAngle(be, target);
            be.shootFromRotation(be, angle[0], angle[1], this.getFallFlyingTicks(), 2F, 0F);

            this.level().addFreshEntity(be);
        } else {
            this.playSoundEffect(PomkotsMechs.SE_GRENADE_EVENT.get());
        }
    }

    private float[] getShootingAngle(Entity bullet, LivingEntity target) {
        double xRot = 0;
        double yRot = 0;

        if (target != null && this.getControllingPassenger() == null) {
            Vec3 positionA = bullet.position();
            // エンティティBの位置を取得
            Vec3 positionB = target.getBoundingBox().getCenter();

            // エンティティAからエンティティBへの相対ベクトル
            double deltaX = positionB.x - positionA.x;
            double deltaY = target.getBoundingBox().minY - positionA.y; // 足元を狙うためにY軸だけは中心じゃなくて下限
            double deltaZ = positionB.z - positionA.z;

            // 水平角度 (Yaw) の計算 (XZ平面上の角度)
            yRot = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0; // 90度引いて北基準に

            // 垂直角度 (Pitch) の計算 (Y軸方向の角度)
            double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ); // 水平方向の距離
            xRot = -Math.toDegrees(Math.atan2(deltaY, distanceXZ)); // Y

        } else {
            Vec3 lookAngle;

            if (this.getControllingPassenger() == null) {
                lookAngle = this.getLookAngle();
                xRot = -Math.toDegrees(Math.asin(lookAngle.y)) - 7.5;
                yRot = Math.toDegrees(Math.atan2(lookAngle.z, lookAngle.x)) - 90.0;

            } else {
                lookAngle = this.getControllingPassenger().getLookAngle();
                xRot = -Math.toDegrees(Math.asin(lookAngle.y)) - 20;
                yRot = Math.toDegrees(Math.atan2(lookAngle.z, lookAngle.x)) - 90.0;

            }

        }

        return new float[]{(float)xRot, (float)yRot};
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0, event -> {
            if (event.isMoving()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.walk"));

            } else {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmb01.idle"));

            }
        }));

        controllers.add(new AnimationController<>(this, "action_controller", state -> PlayState.STOP)
                .triggerableAnim("lbrk", RawAnimation.begin().thenPlay("animation.pmb01.hurt").thenPlay("animation.pmb01.down"))
                .triggerableAnim("punch", RawAnimation.begin().thenPlay("animation.pmb01.attackpunch"))
                .triggerableAnim("upper", RawAnimation.begin().thenPlay("animation.pmb01.attackupper"))
                .triggerableAnim("jump", RawAnimation.begin().thenPlay("animation.pmb01.attackjump"))
                .triggerableAnim("msl", RawAnimation.begin().thenPlay("animation.pmb01.attackmissile"))
                .triggerableAnim("lmsl", RawAnimation.begin().thenPlay("animation.pmb01.attacklargemissile"))
                .triggerableAnim("gat", RawAnimation.begin().thenPlayXTimes("animation.pmb01.attackgatling", 10))
                .triggerableAnim("grn", RawAnimation.begin().thenPlay("animation.pmb01.attackgrenade"))
        );

        controllers.add(new AnimationController<>(this, "break_controller", state -> PlayState.STOP)
                .triggerableAnim("break", RawAnimation.begin().thenPlayAndHold("animation.pmb01.bodyfrontbreak"))
        );
    }

    private HitBoxEntity hitBox1 = null;
    private HitBoxEntity hitBox2 = null;

    private boolean breakFlag = false;
    private void setBreakFlag(boolean bool) {
        this.breakFlag = bool;
    }

    public boolean consumeBreakFlag() {
        if (breakFlag) {
            breakFlag = false;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            var offset = this.position();

            if (this.firstTick) {
                // 追加の当たり判定を乗っけていく
                hitBox1 = new HitBoxEntity(PomkotsMechs.HITBOX1.get(), this.level(), this);
                hitBox1.setPos(offset.x, offset.y + 9, offset.z);
                hitBox1.setBreakCallback(
                        (Void) -> {if (this.isServerSide()) {this.triggerAnim("break_controller", "break");}}
                );
                this.level().addFreshEntity(hitBox1);

                hitBox2 = new HitBoxLegsEntity(PomkotsMechs.HITBOX2.get(), this.level(), this);
                hitBox2.setPos(offset.x, offset.y, offset.z);
                hitBox2.setBreakCallback(
                        (Void) -> {if (this.isServerSide()) {setBreakFlag(true);}}
                );
                this.level().addFreshEntity(hitBox2);

            } else {
                if (actionController.getAction("lbrk").isInAction()) {
                    hitBox1.setPos(offset.x, offset.y, offset.z);
                } else {
                    hitBox1.setPos(offset.x, offset.y + 9, offset.z);
                }

                hitBox2.setPos(offset.x, offset.y, offset.z);
            }
        }

        super.tick();
        this.actionController.tick();

        if (this.isAlive() && this.isVehicle()) {
            if (rideCoolTick == 0) {
                applyPlayerControll();
            }
        } else {
            rotateToTarget(getTarget());

        }

    }

    private void applyPlayerControll() {
        if (driverInput != null && isServerSide()) {
            if (driverInput.isWeaponRightHandPressed()) {
                if (isMainMode()) {
                    actionController.getAction("punch").tryAction();
                } else {
                    actionController.getAction("gat").tryAction();
                }
            } else if (driverInput.isWeaponLeftHandPressed()) {
                if (isMainMode()) {
                    actionController.getAction("upper").tryAction();
                } else {
                    actionController.getAction("grn").tryAction();
                }
            } else if (driverInput.isWeaponRightShoulderPressed()) {
                if (isMainMode()) {
                    actionController.getAction("jump").tryAction();
                } else {
                    actionController.getAction("msl").tryAction();
                }
            } else if (driverInput.isWeaponLeftShoulderPressed()) {
                if (isMainMode()) {
                    actionController.getAction("jump").tryAction();
                } else {
                    actionController.getAction("lmsl").tryAction();
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.EXPLOSION)) {
            amount *= BattleBalance.BOSS_DEFENCE_EXPLOSION;
        }
        amount *= BattleBalance.BOSS_DEFENCE;

        ParticleUtil.addParticles(source,this);

        return super.hurt(source, amount);
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
    public double getPassengersRidingOffset() {
        return 19F;
    }

    private Vec3 clientSeatPos = Vec3.ZERO;

    public void setClientSeatPos(Vec3 seatPos) {
        this.clientSeatPos = seatPos;
    }

    public Vec3 getClientSeatPos() {
        return this.clientSeatPos;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }


    private void playSoundEffect(SoundEvent event) {
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), event, SoundSource.PLAYERS, 1.0F, 1.0F, false);
    }

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
        if (this.isClientSide() && this.tickCount % 10 == 0) {
            this.playSoundEffect(PomkotsMechs.SE_WALK_EVENT.get());
        }
    }

    // プレイヤーが搭乗しているときの処理

    // 操作しているドライバーのキー入力（サーバと他クライアントにも同期する）
    private DriverInput driverInput = null;

    @Override
    public void setDriverInput(DriverInput di) {
        this.driverInput = di;

        if (isServerSide() && di.isModeChangePressed()) {
            this.setMainMode(!this.isMainMode());
        }
    }

    private static final EntityDataAccessor<Boolean> MODE = SynchedEntityData.defineId(Pmb01Entity.class, EntityDataSerializers.BOOLEAN);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MODE, true); // 初期値を設定
    }

    private void setMainMode(boolean value) {
        this.entityData.set(MODE, value);
    }

    public boolean isMainMode() {
        return this.entityData.get(MODE);
    }

    @Override
    public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
        return false;
    }
}
