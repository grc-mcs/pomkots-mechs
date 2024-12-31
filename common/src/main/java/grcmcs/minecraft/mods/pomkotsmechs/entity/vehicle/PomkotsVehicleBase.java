package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.LockTargets;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.network.SerializableDataTicket;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.LinkedList;

public abstract class PomkotsVehicleBase extends LivingEntity implements GeoEntity, GeoAnimatable, PomkotsVehicle {
    public static final float DEFAULT_SCALE = 0.5f;

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.MAX_HEALTH, BattleBalance.MECH_HEALTH);
    }

    // ロボ君のアクションの状態（サーバと他クライアントにも同期する）
    public ActionController actionController = new ActionController();

    // 搭乗してから操作開始するまでの間のティック
    protected short rideCoolTick = 0;

    private boolean onGroundPrev = true;

    abstract protected String getMechName();

    public PomkotsVehicleBase(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);

        this.noCulling = true;
        this.setYRot(0F);
        this.setNoGravity(false);

//        this.setSpeed(2);

        this.registerActions();
        this.setupProperties();
    }

    protected static final int ACT_EVASION = 0;
    protected static final int ACT_JUMP = 1;

    protected void registerActions() {
        this.actionController.registerAction(ACT_EVASION, new Action(10, 0, 10), ActionController.ActionType.BASE);
        this.actionController.registerAction(ACT_JUMP, new Action(10, 6, 4), ActionController.ActionType.BASE);
    }

    protected void setupProperties() {
        this.setMaxUpStep(2.0F);
    }

    @Override
    public void tick() {
        this.updatePosHistory(this.position());

        super.tick();

        this.actionController.tick();

        if (this.isAlive() && this.isVehicle()) {
            if (rideCoolTick > 0) {
                rideCoolTick--;

            } else {
                chargeEnergy();

                // クライアントサイドの場合、キー入力をサーバサイドからとってくる
                if (isClientSide()) {
                    Short b = getAnimData(DRIVER_INPUT_SERIALIZABLE_DATA_TICKET);
                    if (b == null) {
                        b = 0;
                    }
                    driverInput = new DriverInput(b);
                }

                if (driverInput != null) {
                    this.applyPlayerInput(driverInput);
                }

                this.fireWeapons();
            }

        } else {
            this.setNoGravity(false);

        }

        this.onGroundPrev = this.onGround();
    }

    protected void applyPlayerInput(DriverInput driverInput) {
        getUserIntentionForDirectionFromKey(driverInput);

        this.applyPlayerInputWeapons(driverInput);
        this.applyPlayerInputBoost(driverInput);
        this.applyPlayerInputEvasion(driverInput);
        this.applyPlayerInputJump(driverInput);
        this.applyPlayerInputInAirActions(driverInput);
    }

    protected void applyPlayerInputWeapons(DriverInput driverInput) {
        //NOP
    }

    protected void applyPlayerInputBoost(DriverInput driverInput) {
        if (this.getDeltaMovement().z == 0 && this.getDeltaMovement().x == 0) {
            this.actionController.setBoost(false);
        }
    }

    protected void applyPlayerInputEvasion(DriverInput driverInput) {
        if (driverInput.isEvasionPressed() && this.actionController.getAction(ACT_EVASION).startAction()) {
            this.startEvasion();
            this.actionController.setBoost(true);
        }
    }

    protected void startEvasion() {
        if (isServerSide()) {
            Vec3 vel;
            if (forwardIntention == 0 && sidewayIntention == 0) {
                vel = new Vec3(0, 0, 1);
            } else {
                vel = new Vec3(sidewayIntention, 0, forwardIntention).normalize();
            }

            vel = vel.yRot((float) Math.toRadians((-1.0) * this.getYRot()));
            var distance = 0.475 * 15;
            vel = vel.scale(distance);

            this.push(vel.x, vel.y, vel.z);
        }
    }

    protected void applyPlayerInputJump(DriverInput driverInput) {
        if (driverInput.isJumpPressed() && this.onGround()) {
            this.actionController.getAction(ACT_JUMP).startAction();
        }
        if (this.actionController.getAction(ACT_JUMP).isOnFire()) {
            if (isServerSide()) {
                this.push(0, 2, 0);
            }
        }
    }

    protected void applyPlayerInputInAirActions(DriverInput driverInput) {
        if (!this.onGround()) {
            if (driverInput.isJumpPressed()) {
                this.setNoGravity(true);

                if (useEnergy(5)) {
                    if (isServerSide() && this.getDeltaMovement().y() < 0.7) {
                        this.push(0, 0.3, 0);
                    }
                } else {
                    if (isServerSide()) {
                        this.push(0, -0.18 * 0.9800000190734863D, 0);
                    }
                }
            } else if (isServerSide()){
                this.push(0, -0.18 * 0.9800000190734863D, 0);
            }
        } else {
            this.setNoGravity(false);
        }
    }

    protected void fireWeapons() {
        // NOP
    }

    protected float[] getShootingAngle(Entity bullet, boolean useLockTarget) {
        double xRot = 0;
        double yRot = 0;

        var lockTarget = (lockTargets.getLockTargetHard() != null)?lockTargets.getLockTargetHard():lockTargets.getLockTargetSoft();
        if (useLockTarget && lockTarget != null) {
            Vec3 positionA = bullet.position();
            // エンティティBの位置を取得
            Vec3 positionB = lockTarget.getBoundingBox().getCenter();

            // エンティティAからエンティティBへの相対ベクトル
            double deltaX = positionB.x - positionA.x;
            double deltaY = positionB.y - positionA.y;
            double deltaZ = positionB.z - positionA.z;

            // 水平角度 (Yaw) の計算 (XZ平面上の角度)
            yRot = Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0; // 90度引いて北基準に

            // 垂直角度 (Pitch) の計算 (Y軸方向の角度)
            double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ); // 水平方向の距離
            xRot = -Math.toDegrees(Math.atan2(deltaY, distanceXZ)); // Y

        } else {
            var driver = this.getDrivingPassenger();
            var lookAngle = driver.getLookAngle();

            xRot = -Math.toDegrees(Math.asin(lookAngle.y)) - 7.5;
            yRot = Math.toDegrees(Math.atan2(lookAngle.z, lookAngle.x)) - 90.0;
        }

        return new float[]{(float)xRot, (float)yRot};
    }

    // これを有効にするときはsetSpeedもつける事。
    // ジャンプできなくなる問題は謎
    // Entity#isControlledByLocalInstanceに鍵がありそう
//    @Override
//    public LivingEntity getControllingPassenger() {
//        var passes = this.getPassengers();
//
//        if (passes.isEmpty()) {
//            return null;
//        } else {
//            for (var ent: passes) {
//                if (ent instanceof LivingEntity le) {
//                    return le;
//                }
//            }
//            return null;
//        }
//    }

    @Override
    public void travel(Vec3 pos) {
        if (this.isAlive() && this.isVehicle()) {
            var pilot = this.getDrivingPassenger();

            // ROTATE Vehicle
            this.setYRot(pilot.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(pilot.getXRot() * 0.5F);
            this.setRot(this.getYRot(), this.getXRot());
            this.setYBodyRot(this.getYRot());
            this.setYHeadRot(this.getYRot());

            float f = pilot.xxa * 0.5F;
            float f1 = pilot.zza * 0.5F;

            // BOOST
            if (isServerSide()) {
                if (this.actionController.isBoost()) {
                    this.setSpeed(1.5F);
                } else {
                    this.setSpeed(0.5F);
                }
            }

            super.travel(new Vec3(f, pos.y, f1));
            this.hasImpulse = true;
        } else {
            super.travel(pos);
        }
    }

    @Override protected float getFlyingSpeed() {
        if (isNoGravity() && isServerSide()) {
            if (this.actionController.isBoost()) {
                return 0.4f;
            } else {
                return 0.2f;
            }
        } else {
            return 0.02F;
        }
    }

    @Override
    public boolean hurt(DamageSource ds, float a) {
        if (ds.getEntity() != null && ds.getEntity().equals(this.getDrivingPassenger())) {
            return false;
        } else {
            if (this.isClientSide()) {
                this.playSoundEffect(PomkotsMechs.SE_HIT_EVENT.get());
                ParticleUtil.addParticles(ds,this);
            }
            return super.hurt(ds, a);
        }
    }

    /**
     * 乗り降りとか乗客関連の処理群
     */

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        if (passenger instanceof Player player) {
            if (player.isDeadOrDying()) {
                player.getAbilities().mayfly = true;
            }
        }

        lockTargets.clearLockTargets();

        return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());        // -1.0 for less head bonk.
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.getDrivingPassenger() == null) {
            if (isServerSide()) {
                player.setYRot(this.getYRot());
                player.setXRot(this.getXRot());
                player.startRiding(this);
            }

            this.rideCoolTick = 3;
            this.actionController.reset();
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    // 運転手さんを取得する（getControllingPassengerをオーバーライドするとなんか変な感じになるので独自メソッド）
    public LivingEntity getDrivingPassenger() {
        return this.getPassengers().isEmpty() ? null : (LivingEntity) this.getPassengers().get(0);
    }

    /**
     * アニメーション関連の処理群
     */

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, event -> {
            if (this.getDrivingPassenger() == null) {
                event.getController().forceAnimationReset();
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".idle"));
            }

            PlayState res = controllAnimationWeapons(event);

            if (res != null) {
                return res;
            }

            return controllAnimationBasicMove(event);
        }).setSoundKeyframeHandler(soundKeyframeEvent -> {
            this.registerAnimationSoundHandlers(soundKeyframeEvent);
        }));

        controllers.add(new AnimationController<>(this, "fly", 0, event -> {
            return controllAnimationFlyingMotion(event);
        }).setSoundKeyframeHandler(soundKeyframeEvent -> {
            this.registerAnimationSoundHandlers(soundKeyframeEvent);
        }));

        controllers.add(new AnimationController<>(this, "rotation", 3, event -> {
            return controllAnimationRotation(event);
        }));


        controllers.add(new AnimationController<>(this, "boosters", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".booster"));
        }));

        addExtraAnimationController(controllers);
    }

    protected PlayState controllAnimationWeapons(AnimationState<PomkotsVehicleBase> event) {
        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".idle"));
    }

    protected PlayState controllAnimationBasicMove(AnimationState<PomkotsVehicleBase> event) {
        if (this.actionController.getAction(ACT_JUMP).isInAction()) {
            if (this.actionController.getAction(ACT_JUMP).isOnStart()) {
                event.getController().forceAnimationReset();
            }
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".jump"));

        }

        if (event.isMoving()) {
            if (this.actionController.getAction(ACT_EVASION).isInAction()) {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".evasion"));
            } else if (this.actionController.isBoost()) {
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".dash"));
            } else {
                if (this.isNoGravity()) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".idle"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".walk"));
                }
            }
        } else {
            if (this.yRotO != this.getYRot()) {
                return event.setAndContinue(RawAnimation.begin().thenLoop("animation." + getMechName() + ".walk"));
            } else {
                event.getController().forceAnimationReset();
                return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".idle"));
            }
        }
    }

    protected PlayState controllAnimationFlyingMotion(AnimationState<PomkotsVehicleBase> event) {
        if (justLanded(event.getAnimatable())) {
//            event.getController().forceAnimationReset();
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + getMechName() + ".onground"));
        } else if (event.getAnimatable().onGround()) {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
        } else {
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".flylegs"));
        }
    }

    protected PlayState controllAnimationRotation(AnimationState<PomkotsVehicleBase> event) {
        if (event.isMoving()) {
            if (this.isNoGravity() && !this.actionController.isBoost() && !this.actionController.getAction(ACT_EVASION).isInAction()) {
                if (this.forwardIntention > 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".frontfly"));
                } else if (forwardIntention < 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".backfly"));
                } else if (this.sidewayIntention < 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".rightfly"));
                } else if (sidewayIntention > 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".leftfly"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
                }
            } else {
                if (this.sidewayIntention < 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".right"));
                } else if (sidewayIntention > 0) {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".left"));
                } else {
                    return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
                }
            }
        } else {
            if (!this.actionController.isInActionAll()) {
                event.getController().forceAnimationReset();
            }
            return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + getMechName() + ".nop"));
        }
    }

    protected void addExtraAnimationController(AnimatableManager.ControllerRegistrar controllers) {
        // NOP
    }

    protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
        // NOP
    }

    /**
     * キー入力関係の処理群
     */
    private DriverInput driverInput = null;

    public static final SerializableDataTicket<Short> DRIVER_INPUT_SERIALIZABLE_DATA_TICKET =
            GeckoLibUtil.addDataTicket(
                    new SerializableDataTicket<Short>(PomkotsMechs.MODID.toString() + ".driverInput", Short.class) {
                        public void encode(Short data, FriendlyByteBuf buffer) {
                            try {
                                buffer.writeShort((int)data);
                            } catch (Exception e) {
                                Utils.logError("", e);
                            }
                        }

                        public Short decode(FriendlyByteBuf buffer) {
                            Object o = null;
                            try {
                                o = buffer.readShort();
                            } catch (Exception e) {
                                Utils.logError("", e);
                            }
                            return (Short) o;
                        }
                    }
            );

    public void setDriverInput(DriverInput di) {
        this.driverInput = di;

        if (!level().isClientSide) {
            setAnimData(DRIVER_INPUT_SERIALIZABLE_DATA_TICKET, driverInput.getStatus());
            if (di.isModeChangePressed()) {
                this.setMainMode(!this.isMainMode());
            }
        }
    }

    public DriverInput getDriverInput() {
        return this.driverInput;
    }

    /**
     * モード関係の処理群
     */
    private static final EntityDataAccessor<Boolean> MODE = SynchedEntityData.defineId(PomkotsVehicleBase.class, EntityDataSerializers.BOOLEAN);

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

    public boolean isSubMode() {
        return !this.entityData.get(MODE);
    }

    /**
     * タゲロック関係の処理群
     */
    protected LockTargets lockTargets = new LockTargets();

    @Override
    public LockTargets getLockTargets() {
        return lockTargets;
    }

    /**
     * エネルギー関係の処理群
     */
    private static final int MAX_ENERGY = 100;
    private int energy = MAX_ENERGY;

    public int getEnergy() {
        return this.energy;
    }

    protected void chargeEnergy() {
        if (this.energy + 2 > MAX_ENERGY) {
            this.energy = MAX_ENERGY;
        } else {
            this.energy += 2;
        }
    }

    protected boolean useEnergy(int dec) {
        if (this.energy - dec < 0) {
            this.energy = 0;
            return false;
        } else {
            this.energy -= dec;
            return true;
        }
    }

    /**
     * プロパティ系の処理群
     */
    @Override
    public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
        return false;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return NonNullList.withSize(4, ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {

    }

    @Override
    public HumanoidArm getMainArm() {
        return null;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 2.5F;
    }


    @Override
    public boolean isPushable() {
        return false;
    }

    /**
     * 座席関連の処理群
     */

    private Vec3 clientSeatPos = Vec3.ZERO;

    public void setClientSeatPos(Vec3 seatPos) {
        this.clientSeatPos = seatPos;
    }

    @Override
    public Vec3 getClientSeatPos(Entity passenger) {
        return this.clientSeatPos;
    }

    /**
     * 音声関連の処理群
     */
    protected void playSoundEffect(SoundEvent event) {
        this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), event, SoundSource.PLAYERS, 1.0F, 1.0F, false);
    }

    @Override
    public void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    /**
     * そのほか
     */

    // よくわからんけどサーバとクライアントで位置情報に3tick分ぐらい差があるっぽい。その辺を無理やり対処するためにposの履歴を取っとく
    protected LinkedList<Vec3> posHistory = new LinkedList<Vec3>();

    private void updatePosHistory(Vec3 v) {
        var vec = new Vec3(v.x, v.y, v.z);

        if (posHistory.size() > 3) {
            posHistory.remove(0);
        }

        posHistory.add(vec);
    }

    protected boolean justLanded(Entity ent) {
//        return this.onGroundPrev != this.onGround();
        return ent.getDeltaMovement().y == 0 && (ent.yOld - ent.getY()) > 0.4;
    }

    // ユーザが縦に移動しようとしてるか横に移動しようとしてるか（速度じゃないので注意）
    protected float forwardIntention = 0;
    protected float sidewayIntention = 0;

    protected void getUserIntentionForDirectionFromKey(DriverInput driverInput) {
        if (driverInput.isForwardPressed()) {
            forwardIntention = 1;
        } else if (driverInput.isBackPressed()) {
            forwardIntention = -1;
        } else {
            forwardIntention = 0;
        }

        if (driverInput.isRightPressed()) {
            sidewayIntention = -1;
        } else if (driverInput.isLeftPressed()) {
            sidewayIntention = 1;
        } else {
            sidewayIntention = 0;
        }
    }

    protected boolean isSelf(Entity ent) {
        return ent.equals(this) || ent.equals(this.getDrivingPassenger());
    }


    protected boolean isServerSide() {
        return !isClientSide();
    }

    protected boolean isClientSide() {
        return this.level().isClientSide();
    }

    @Override
    public boolean shouldRenderDefaultHud(String hudName) {
        return false;
    }

    @Override
    public boolean shouldLockMulti(DriverInput driverInput) {
        return false;
    }

    @Override
    public boolean shouldLockWeak(DriverInput driverInput) {
        return false;
    }

    @Override
    public boolean shouldLockStrong(DriverInput driverInput) {
        return driverInput.isLockPressed();
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }
}
