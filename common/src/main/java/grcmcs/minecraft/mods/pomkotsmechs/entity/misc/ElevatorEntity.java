package grcmcs.minecraft.mods.pomkotsmechs.entity.misc;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ElevatorEntity extends LivingEntity implements GeoEntity, GeoAnimatable {

    public enum Mode {
        IDLE(0),
        WAITING(1),
        MOVING_TO_DST(2),
        MOVING_TO_SRC(3),
        LOCKING(4);

        private final int code;

        Mode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Mode byCode(
                int modeCode
        ) {
            for (Mode mode: values()) {
                if (mode.code == modeCode) {
                    return mode;
                }
            }
            return IDLE;
        }
    }

    public static final Mode INITIAL_MODE = Mode.IDLE;

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.MAX_HEALTH, 1024);
    }

    private static final float WIDTH = 6.5F;
    private static final float HEIGHT = 0.5F;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private BlockPos srcBlockPos = BlockPos.ZERO;
    private BlockPos dstBlockPos = BlockPos.ZERO;

    private Vec3 moveVelocity = Vec3.ZERO;

    private int curModeTicks = 0;
    private int moveTicks = 0;
    private boolean moveStarted = false;
    private boolean arrivalShakeDone = false;

    public ElevatorEntity(
            EntityType<? extends LivingEntity> type,
            Level level
    ) {
        super(type, level);
        setNoGravity(true);
        this.noCulling = true;
    }

    // ==============================================================================================================
    // メインの挙動
    // ==============================================================================================================

    @Override
    public void tick() {
        setNoGravity(true);

        super.tick();

        lockYaw();

        if (level().isClientSide()) {
            return;
        }

        curModeTicks++;

        switch (getMode()) {
            case IDLE -> {
                moveVelocity = Vec3.ZERO;
                if (!getPassengers().isEmpty()) {
                    ejectPassengers();
                }
            }

            case WAITING -> {
                moveVelocity = Vec3.ZERO;
                tickWaiting();
            }

            case MOVING_TO_DST -> {
                tickMoveTo(dstBlockPos);
            }

            case MOVING_TO_SRC -> {
                tickMoveTo(srcBlockPos);
            }

            case LOCKING -> {
                tickLocking();
            }
        }

        move(
                MoverType.SELF,
                moveVelocity
        );

    }

    private void lockYaw() {
        setYRot(getFixedYaw());
        yRotO = getFixedYaw();
        yBodyRot = getFixedYaw();
        yBodyRotO = getFixedYaw();
        yHeadRot = getFixedYaw();
        yHeadRotO = getFixedYaw();
    }

    private void tickWaiting() {
        if (curModeTicks < 20 || !this.getPassengers().isEmpty()) {
            return;
        }

        AABB area =
                getBoundingBox()
                        .inflate(2.0);

        List<Pmvc01Entity> mechs =
                level().getEntitiesOfClass(
                        Pmvc01Entity.class,
                        area
                );

        for (Pmvc01Entity mech : mechs) {
            if (
                !mech.isPassenger()
                    && mech.getDrivingPassenger() != null
                    && mech.getDrivingPassenger().getUUID().equals(getPassengerUUID())
            ) {
                mech.startRiding(
                        this,
                        true
                );

                playSoundEffect(PomkotsMechs.SE_GASHAN.get());

                break;
            }
        }
    }

    public Mode getNextMoveMode() {
        double srcDistance =
                distanceToSqr(
                        Vec3.atCenterOf(srcBlockPos)
                );

        double dstDistance =
                distanceToSqr(
                        Vec3.atCenterOf(dstBlockPos)
                );

        return srcDistance <= dstDistance
                ? Mode.MOVING_TO_DST
                : Mode.MOVING_TO_SRC;
    }

    private static final double MOVE_SPEED = 0.7;
    private enum MovePhase {
        START_SHAKE,
        MOVING,
        END_SHAKE
    }

    private MovePhase movePhase = MovePhase.START_SHAKE;

    private int phaseTicks = 0;

    public void beginMove() {
        phaseTicks = 0;
        this.setMode(getNextMoveMode());
        movePhase = MovePhase.START_SHAKE;
    }

    private void tickMoveTo(
            BlockPos targetPos
    ) {
        switch (movePhase) {
            case START_SHAKE -> tickStartShake();
            case MOVING -> tickMoving(targetPos);
            case END_SHAKE -> tickEndShake();
        }
    }

    private void tickStartShake() {

        phaseTicks++;

        if (phaseTicks < 20) {
            if (phaseTicks == 1) {
                playSoundEffect(PomkotsMechs.SE_ELEVATOR.get());
            }

            double x =
                    Mth.sin(
                            phaseTicks * 3F
                    ) * 0.1;

            double z =
                    Mth.cos(
                            phaseTicks * 3F
                    ) * 0.1;

            double y =
                    Mth.sin(
                            phaseTicks * 5F
                    ) * 0.6;

            move(
                    MoverType.SELF,
                    new Vec3(
                            x,
                            y,
                            z
                    )
            );
        }

        phaseTicks = 0;
        movePhase = MovePhase.MOVING;

    }

    private void tickMoving(
            BlockPos targetPos
    ) {
        phaseTicks++;

        Vec3 target =
                Vec3.atCenterOf(targetPos);

        Vec3 diff =
                target.subtract(
                        position()
                );

        double distance =
                diff.length();

        if (distance < 0.3) {

            moveVelocity = Vec3.ZERO;

            phaseTicks = 0;

            movePhase = MovePhase.END_SHAKE;

            playSoundEffect(PomkotsMechs.SE_ELEVATOR.get());

            return;
        }

        double t =
                (phaseTicks % 5) * 0.2;

        Vec3 shake =
                new Vec3(
                        (random.nextDouble() - 0.5) * 0.02,
                        Math.sin(t * Math.PI) * 0.01,
                        (random.nextDouble() - 0.5) * 0.02
                );

        moveVelocity =
                diff.normalize()
                        .scale(MOVE_SPEED)
                        .add(shake);
    }

    private void tickEndShake() {
        phaseTicks++;

        double x =
                Mth.sin(
                        phaseTicks * 3F
                ) * 0.1;

        double z =
                Mth.cos(
                        phaseTicks * 3F
                ) * 0.1;

        double y =
                Mth.sin(
                        phaseTicks * 5F
                ) * 0.6;

        move(
                MoverType.SELF,
                new Vec3(
                        x,
                        y,
                        z
                )
        );

        if (phaseTicks >= 8) {

            phaseTicks = 0;

            movePhase = MovePhase.START_SHAKE;

            setMode(
                    Mode.LOCKING
            );
        }
    }

    private void tickLocking() {
        if (curModeTicks > 100) {
            ejectPassengers();
            setMode(Mode.IDLE);
        }
    }

    private void playSoundEffect(SoundEvent se) {
        level().playSound(
                null,
                blockPosition(),
                se,
                SoundSource.BLOCKS,
                1.0F,
                0.7F
        );
    }

    public void setMoveVelocity(
            Vec3 velocity
    ) {
        this.moveVelocity = velocity;
    }

    private void moveEntitiesOnTop(
            Vec3 delta
    ) {
        if (delta.lengthSqr() < 0.0001) {
            return;
        }

        AABB area = getBoundingBox()
                        .move(0,0.1,0)
                        .inflate(0,1.5,0);

        List<Entity> entities =
                level().getEntities(this, area);

        for (Entity entity : entities) {
            if (entity.isPassenger()) {
                continue;
            }
            entity.move(
                    MoverType.SHULKER_BOX,
                    delta
            );
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean canAddPassenger(
            Entity passenger
    ) {
        return true;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.55;
    }

    @Override
    public EntityDimensions getDimensions(
            Pose pose
    ) {
        return EntityDimensions.fixed(
                WIDTH,
                HEIGHT
        );
    }

    // ==============================================================================================================
    // Getter/Setter
    // ==============================================================================================================

    public BlockPos getDstBlockPos() {
        return dstBlockPos;
    }

    public void setDstBlockPos(BlockPos dstBlockPos) {
        this.dstBlockPos = dstBlockPos;
    }

    public BlockPos getSrcBlockPos() {
        return srcBlockPos;
    }

    public void setSrcBlockPos(BlockPos srcBlockPos) {
        this.srcBlockPos = srcBlockPos;
    }

    public void setMode(Mode mode) {
        curModeTicks = 0;
        moveTicks = 0;
        moveStarted = false;
        arrivalShakeDone = false;

        this.entityData.set(MODE, mode.getCode());
    }

    public Mode getMode() {
        return Mode.byCode(this.entityData.get(MODE));
    }

    public float getFixedYaw() {
        return this.entityData.get(YAW);
    }

    public void setFixedYaw(float fixedYaw) {
        this.entityData.set(YAW, fixedYaw);
    }

    public UUID getPassengerUUID() {
        var u = this.entityData.get(UUID);
        return u.orElse(null);
    }

    public void setPassengerUUID(UUID uuid) {
        this.entityData.set(UUID, Optional.of(uuid));
    }

    // ==============================================================================================================
    // データ保存/同期
    // ==============================================================================================================

    private static final EntityDataAccessor<Optional<UUID>> UUID = SynchedEntityData.defineId(ElevatorEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> MODE = SynchedEntityData.defineId(ElevatorEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> YAW = SynchedEntityData.defineId(ElevatorEntity.class, EntityDataSerializers.FLOAT);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(MODE, INITIAL_MODE.code);
        this.entityData.define(YAW, 0F);
        this.entityData.define(UUID, Optional.empty());
    }

    @Override
    public void readAdditionalSaveData(
            CompoundTag tag
    ) {
        super.readAdditionalSaveData(tag);
        setMode(Mode.byCode(tag.getInt(PomkotsMechs.nbtName("Mode"))));
        dstBlockPos = Utils.loadBlockPos("Dst", tag);
        srcBlockPos = Utils.loadBlockPos("Src", tag);
        setFixedYaw(tag.getFloat(PomkotsMechs.nbtName("FixedYaw")));
    }


    @Override
    public void addAdditionalSaveData(
            CompoundTag tag
    ) {
        super.addAdditionalSaveData(tag);
        tag.putInt(PomkotsMechs.nbtName("Mode"), getMode().code);
        Utils.saveBlockPos(tag, "Dst", dstBlockPos);
        Utils.saveBlockPos(tag, "Src", srcBlockPos);
        tag.putFloat(PomkotsMechs.nbtName("FixedYaw"), getFixedYaw());
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    // ==============================================================================================================
    // アニメーション関連
    // ==============================================================================================================

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "basic_move", 0, event -> {
            return event.setAndContinue(RawAnimation.begin().thenPlay("animation.elevator.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }


    // ==============================================================================================================
    // 他
    // ==============================================================================================================

    @Override
    public boolean isInWall() {
        return false;
    }

    @Override
    protected void checkInsideBlocks() {
    }

    @Override
    public boolean isAffectedByFluids() {
        return false;
    }

    @Override
    public int decreaseAirSupply(int air) {
        return 0;
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
    public boolean isPushable() {
        return false;
    }

    @Override
    public HumanoidArm getMainArm() {
        return null;
    }

}
