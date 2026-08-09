package grcmcs.minecraft.mods.pomkotsmechs.entity.event;

import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

public class TransportShipEntity extends LivingEntity implements GeoEntity {
    private static final EntityDataAccessor<Boolean> DATA_MOVING =
            SynchedEntityData.defineId(TransportShipEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int DEFAULT_STUCK_LIMIT_TICKS = 200;

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private final List<RoutePoint> route = new ArrayList<>();
    private ShipState shipState = ShipState.IDLE;
    private String targetWaypoint = "";
    private String currentWaypoint = "";
    private String reachedWaypoint = "";
    private double routeSpeed = 0.35D;
    private float turnSpeedDegrees = 2.0F;
    private double arrivalRadius = 4.0D;
    private int stuckLimitTicks = DEFAULT_STUCK_LIMIT_TICKS;
    private int stuckTicks;
    private double waterSurfaceOffset;
    private int waterSearchRange = 6;

    public TransportShipEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noCulling = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 3048.0D)
                .add(Attributes.ARMOR, 20.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_MOVING, false);
    }

    public void configureRoute(
            List<RoutePoint> points,
            double speed,
            float turnSpeed,
            double arrivalRadius,
            int stuckLimitTicks,
            double waterSurfaceOffset,
            int waterSearchRange
    ) {
        route.clear();
        route.addAll(points);
        routeSpeed = Math.max(0.01D, speed);
        turnSpeedDegrees = Math.max(0.01F, turnSpeed);
        this.arrivalRadius = Math.max(0.1D, arrivalRadius);
        this.stuckLimitTicks = Math.max(1, stuckLimitTicks);
        this.waterSurfaceOffset = waterSurfaceOffset;
        this.waterSearchRange = Math.max(1, waterSearchRange);
        stop();
    }

    public boolean moveToWaypoint(String waypointId) {
        int targetIndex = findWaypointIndex(waypointId);
        if (targetIndex < 0) return false;

        int reachedIndex = findWaypointIndex(reachedWaypoint);
        if (reachedIndex == targetIndex) {
            targetWaypoint = "";
            currentWaypoint = "";
            stuckTicks = 0;
            setDeltaMovement(Vec3.ZERO);
            setShipState(ShipState.WAITING);
            return true;
        }

        targetWaypoint = waypointId;
        if (reachedIndex < 0) {
            currentWaypoint = route.get(0).id();
        } else {
            int direction = Integer.compare(targetIndex, reachedIndex);
            currentWaypoint = route.get(reachedIndex + direction).id();
        }
        stuckTicks = 0;
        setShipState(ShipState.MOVING);
        return true;
    }

    public void stop() {
        targetWaypoint = "";
        currentWaypoint = "";
        stuckTicks = 0;
        setDeltaMovement(Vec3.ZERO);
        setShipState(ShipState.WAITING);
    }

    public boolean hasReached(String waypointId) {
        return shipState == ShipState.WAITING && reachedWaypoint.equals(waypointId);
    }

    public boolean isRouteStuck(int ticks) {
        return shipState == ShipState.STUCK || stuckTicks >= Math.max(1, ticks);
    }

    public ShipState shipState() {
        return shipState;
    }

    @Override
    public void tick() {
        setNoGravity(true);
        super.tick();
        if (level().isClientSide()) return;
        if (shipState != ShipState.MOVING) {
            setDeltaMovement(Vec3.ZERO);
            return;
        }

        RoutePoint target = findWaypoint(currentWaypoint);
        if (target == null) {
            setShipState(ShipState.STUCK);
            return;
        }
        Vec3 offset = target.position().subtract(position());
        double distance = Math.sqrt(offset.x * offset.x + offset.z * offset.z);
        if (distance <= arrivalRadius) {
            Double surfaceY = findWaterSurfaceY(target.position().x, target.position().z, target.position().y);
            if (surfaceY == null) {
                setDeltaMovement(Vec3.ZERO);
                if (++stuckTicks >= stuckLimitTicks) setShipState(ShipState.STUCK);
                return;
            }
            setPos(target.position().x, surfaceY + waterSurfaceOffset, target.position().z);
            reachedWaypoint = target.id();
            stuckTicks = 0;
            if (reachedWaypoint.equals(targetWaypoint)) {
                targetWaypoint = "";
                currentWaypoint = "";
                setDeltaMovement(Vec3.ZERO);
                setShipState(ShipState.WAITING);
            } else if (!advanceToNextWaypoint()) {
                setDeltaMovement(Vec3.ZERO);
                setShipState(ShipState.STUCK);
            }
            return;
        }

        float desiredYaw = distance < 1.0E-5D
                ? getYRot()
                : (float) (Mth.atan2(offset.z, offset.x) * Mth.RAD_TO_DEG) - 90.0F;
        float yaw = Mth.approachDegrees(getYRot(), desiredYaw, turnSpeedDegrees);
        setYRot(yaw);
        yRotO = yaw;
        setYHeadRot(yaw);
        yBodyRot = yaw;

        double radians = Math.toRadians(yaw);
        double horizontalSpeed = Math.min(routeSpeed, distance);
        double movementX = -Math.sin(radians) * horizontalSpeed;
        double movementZ = Math.cos(radians) * horizontalSpeed;
        Double surfaceY = findWaterSurfaceY(
                getX() + movementX, getZ() + movementZ, getY() - waterSurfaceOffset);
        if (surfaceY == null) {
            setDeltaMovement(Vec3.ZERO);
            if (++stuckTicks >= stuckLimitTicks) setShipState(ShipState.STUCK);
            return;
        }
        double desiredY = surfaceY + waterSurfaceOffset;
        double movementY = Mth.clamp(desiredY - getY(), -0.25D, 0.25D);
        Vec3 movement = new Vec3(movementX, movementY, movementZ);

        if (!level().noCollision(this, getBoundingBox().move(movement))) {
            setDeltaMovement(Vec3.ZERO);
            if (++stuckTicks >= stuckLimitTicks) setShipState(ShipState.STUCK);
            return;
        }

        Vec3 before = position();
        setDeltaMovement(movement);
        move(MoverType.SELF, movement);
        hasImpulse = true;
        if (position().distanceToSqr(before) < 1.0E-8D) {
            if (++stuckTicks >= stuckLimitTicks) setShipState(ShipState.STUCK);
        } else {
            stuckTicks = 0;
        }
    }

    private Double findWaterSurfaceY(double x, double z, double referenceY) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int top = Mth.floor(referenceY) + waterSearchRange;
        int bottom = Mth.floor(referenceY) - waterSearchRange;
        for (int y = top; y >= bottom; y--) {
            pos.set(Mth.floor(x), y, Mth.floor(z));
            var fluid = level().getFluidState(pos);
            if (!fluid.is(FluidTags.WATER)) continue;
            return (double) y + fluid.getHeight(level(), pos);
        }
        return null;
    }

    private void setShipState(ShipState state) {
        shipState = state;
        entityData.set(DATA_MOVING, state == ShipState.MOVING);
    }

    private RoutePoint findWaypoint(String id) {
        for (RoutePoint point : route) if (point.id().equals(id)) return point;
        return null;
    }

    private int findWaypointIndex(String id) {
        if (id == null || id.isBlank()) return -1;
        for (int i = 0; i < route.size(); i++) {
            if (route.get(i).id().equals(id)) return i;
        }
        return -1;
    }

    private boolean advanceToNextWaypoint() {
        int reachedIndex = findWaypointIndex(reachedWaypoint);
        int targetIndex = findWaypointIndex(targetWaypoint);
        if (reachedIndex < 0 || targetIndex < 0 || reachedIndex == targetIndex) {
            currentWaypoint = "";
            return false;
        }
        currentWaypoint = route.get(reachedIndex + Integer.compare(targetIndex, reachedIndex)).id();
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("ShipState", shipState.name());
        tag.putString("TargetWaypoint", targetWaypoint);
        tag.putString("CurrentWaypoint", currentWaypoint);
        tag.putString("ReachedWaypoint", reachedWaypoint);
        tag.putDouble("RouteSpeed", routeSpeed);
        tag.putFloat("TurnSpeedDegrees", turnSpeedDegrees);
        tag.putDouble("ArrivalRadius", arrivalRadius);
        tag.putInt("StuckLimitTicks", stuckLimitTicks);
        tag.putInt("StuckTicks", stuckTicks);
        tag.putDouble("WaterSurfaceOffset", waterSurfaceOffset);
        tag.putInt("WaterSearchRange", waterSearchRange);
        ListTag points = new ListTag();
        for (RoutePoint point : route) {
            CompoundTag pointTag = new CompoundTag();
            pointTag.putString("Id", point.id());
            pointTag.putDouble("X", point.position().x);
            pointTag.putDouble("Y", point.position().y);
            pointTag.putDouble("Z", point.position().z);
            points.add(pointTag);
        }
        tag.put("Route", points);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        route.clear();
        for (Tag element : tag.getList("Route", Tag.TAG_COMPOUND)) {
            CompoundTag point = (CompoundTag) element;
            route.add(new RoutePoint(point.getString("Id"),
                    new Vec3(point.getDouble("X"), point.getDouble("Y"), point.getDouble("Z"))));
        }
        targetWaypoint = tag.getString("TargetWaypoint");
        currentWaypoint = tag.contains("CurrentWaypoint")
                ? tag.getString("CurrentWaypoint") : targetWaypoint;
        reachedWaypoint = tag.getString("ReachedWaypoint");
        routeSpeed = tag.contains("RouteSpeed") ? tag.getDouble("RouteSpeed") : 0.35D;
        turnSpeedDegrees = tag.contains("TurnSpeedDegrees") ? tag.getFloat("TurnSpeedDegrees") : 2.0F;
        arrivalRadius = tag.contains("ArrivalRadius") ? tag.getDouble("ArrivalRadius") : 4.0D;
        stuckLimitTicks = tag.contains("StuckLimitTicks")
                ? Math.max(1, tag.getInt("StuckLimitTicks")) : DEFAULT_STUCK_LIMIT_TICKS;
        stuckTicks = tag.getInt("StuckTicks");
        waterSurfaceOffset = tag.getDouble("WaterSurfaceOffset");
        waterSearchRange = tag.contains("WaterSearchRange")
                ? Math.max(1, tag.getInt("WaterSearchRange")) : 6;
        try {
            setShipState(ShipState.valueOf(tag.getString("ShipState")));
        } catch (IllegalArgumentException e) {
            setShipState(ShipState.WAITING);
        }
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

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
        return air;
    }

    @Override
    public boolean displayFireAnimation() {
        return false;
    }

    @Override
    public void die(DamageSource source) {
        if (!dead && !level().isClientSide()) triggerAnim("action_controller", "destroy");
        setShipState(ShipState.DESTROYED);
        super.die(source);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return NonNullList.withSize(4, ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<TransportShipEntity>(
                this, "movement_controller", state -> state.setAndContinue(
                RawAnimation.begin().thenLoop(entityData.get(DATA_MOVING)
                        ? "animation.transport_ship.move"
                        : "animation.transport_ship.idle"))));
        controllers.add(new AnimationController<TransportShipEntity>(
                this, "action_controller", state -> software.bernie.geckolib.core.object.PlayState.STOP)
                .triggerableAnim("destroy",
                        RawAnimation.begin().thenPlayAndHold("animation.transport_ship.destroy")));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    public record RoutePoint(String id, Vec3 position) {
    }

    public enum ShipState {
        IDLE,
        MOVING,
        WAITING,
        STUCK,
        DESTROYED
    }
}
