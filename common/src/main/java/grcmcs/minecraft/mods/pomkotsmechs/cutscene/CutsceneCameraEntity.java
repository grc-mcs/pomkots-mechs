package grcmcs.minecraft.mods.pomkotsmechs.cutscene;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import io.netty.buffer.Unpooled;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class CutsceneCameraEntity extends LivingEntity {
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID =
            SynchedEntityData.defineId(CutsceneCameraEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_TARGET_X =
            SynchedEntityData.defineId(CutsceneCameraEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_TARGET_Y =
            SynchedEntityData.defineId(CutsceneCameraEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_TARGET_Z =
            SynchedEntityData.defineId(CutsceneCameraEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_FIXED_VIEW =
            SynchedEntityData.defineId(CutsceneCameraEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_FIXED_YAW =
            SynchedEntityData.defineId(CutsceneCameraEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_FIXED_PITCH =
            SynchedEntityData.defineId(CutsceneCameraEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_DIRECT_APPROACH_VIEW =
            SynchedEntityData.defineId(CutsceneCameraEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_DIRECT_ROTATION_TICKS =
            SynchedEntityData.defineId(CutsceneCameraEntity.class, EntityDataSerializers.INT);

    private UUID viewerId;
    private UUID targetEntityId;
    private Vec3 fixedTarget;
    private int remainingTicks;
    private int startupTicks = CutsceneService.START_TRANSITION_FADE_TICKS;
    private int forcedClosingTicks = -1;
    private boolean activated;
    private boolean cameraSwitchPending;
    private boolean endFadeStarted;
    private double radius, height, angle, angularSpeed;
    private CutsceneDefinition definition;
    private int sessionIndex;
    private int sessionTick;
    private int sessionTransitionTicks = -1;
    private float relativePathYaw;
    private Vec3 aerialStartPosition;
    private Vec3 aerialAscentPosition;
    private float aerialStartYaw;
    private float aerialStartPitch;
    private int aerialTick;
    private Vec3 directStartPosition;
    private float directStartYaw;
    private float directStartPitch;
    private int directTick;

    public CutsceneCameraEntity(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        setInvulnerable(true);
        noPhysics = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.MAX_HEALTH, 1)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_TARGET_ID, -1);
        entityData.define(DATA_TARGET_X, 0.0F);
        entityData.define(DATA_TARGET_Y, 0.0F);
        entityData.define(DATA_TARGET_Z, 0.0F);
        entityData.define(DATA_FIXED_VIEW, false);
        entityData.define(DATA_FIXED_YAW, 0.0F);
        entityData.define(DATA_FIXED_PITCH, 0.0F);
        entityData.define(DATA_DIRECT_APPROACH_VIEW, false);
        entityData.define(DATA_DIRECT_ROTATION_TICKS, 1);
    }

    public void initialize(ServerPlayer viewer, Vec3 target, Entity targetEntity, CutsceneDefinition definition) {
        viewerId = viewer.getUUID();
        fixedTarget = target;
        targetEntityId = targetEntity == null ? null : targetEntity.getUUID();
        remainingTicks = definition.durationTicks();
        this.definition = definition;
        startupTicks = definition.startFade() ? CutsceneService.START_TRANSITION_FADE_TICKS : 0;
        this.relativePathYaw = definition.relativeToTargetYaw() && targetEntity != null
                ? (float) Math.toRadians(-targetEntity.getYRot()) : 0.0F;
        radius = definition.radius();
        height = definition.height();
        angle = Math.atan2(viewer.getZ() - target.z, viewer.getX() - target.x);
        angularSpeed = Math.toRadians(definition.angularSpeedDegrees()) * (definition.clockwise() ? -1 : 1);
        entityData.set(DATA_TARGET_ID, targetEntity == null ? -1 : targetEntity.getId());
        entityData.set(DATA_TARGET_X, (float) target.x);
        entityData.set(DATA_TARGET_Y, (float) target.y);
        entityData.set(DATA_TARGET_Z, (float) target.z);
        if (definition.type().equals("linear_sessions")) {
            beginLinearSession(target, 0);
        } else if (definition.type().equals("aerial_approach")) {
            initializeAerialApproach(viewer);
        } else if (definition.type().equals("direct_approach")) {
            initializeDirectApproach(viewer);
        } else {
            updateCamera(target);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        ServerPlayer viewer = viewerId == null || level().getServer() == null ? null
                : level().getServer().getPlayerList().getPlayer(viewerId);
        if (viewer == null) { discard(); return; }

        if (!activated) {
            if (startupTicks-- > 0) return;
            activated = true;
            NetworkManager.sendToPlayer(viewer, PomkotsMechs.id(PomkotsMechs.PACKET_CUTSCENE_START),
                    new FriendlyByteBuf(Unpooled.buffer()));
            if (definition.type().equals("direct_approach")) {
                // Give the client one render interval in forced first person so
                // direct_approach can start from the angle that was actually shown.
                cameraSwitchPending = true;
            } else {
                viewer.connection.send(new ClientboundSetCameraPacket(this));
            }
            return;
        }

        if (cameraSwitchPending) {
            cameraSwitchPending = false;
            viewer.connection.send(new ClientboundSetCameraPacket(this));
            return;
        }

        if (forcedClosingTicks >= 0) {
            if (forcedClosingTicks-- <= 0) finish(viewer);
            return;
        }

        Vec3 target = resolveTarget();
        if (target == null) {
            CutsceneService.sendEndScreenFade(viewer);
            forcedClosingTicks = CutsceneService.END_TRANSITION_FADE_TICKS;
            return;
        }

        if (definition.type().equals("linear_sessions")) {
            tickLinearSessions(viewer, target);
            return;
        }
        if (definition.type().equals("aerial_approach")) {
            tickAerialApproach(viewer, target);
            return;
        }
        if (definition.type().equals("direct_approach")) {
            tickDirectApproach(viewer, target);
            return;
        }

        if (!endFadeStarted && remainingTicks <= CutsceneService.END_TRANSITION_FADE_TICKS) {
            CutsceneService.sendEndScreenFade(viewer);
            endFadeStarted = true;
        }

        angle += angularSpeed;
        updateCamera(target);
        hasImpulse = true;
        if (--remainingTicks <= 0) finish(viewer);
    }

    public Vec3 getClientLookTarget(float partialTick) {
        Entity targetEntity = entityData.get(DATA_TARGET_ID) < 0
                ? null : level().getEntity(entityData.get(DATA_TARGET_ID));
        return targetEntity == null
                ? new Vec3(entityData.get(DATA_TARGET_X), entityData.get(DATA_TARGET_Y), entityData.get(DATA_TARGET_Z))
                : targetEntity.getPosition(partialTick).add(0.0, targetEntity.getBbHeight() * 0.5, 0.0);
    }

    public boolean usesFixedView() {
        return entityData.get(DATA_FIXED_VIEW);
    }

    public float getFixedViewYaw(float partialTick) {
        return Mth.rotLerp(partialTick, yRotO, getYRot());
    }

    public float getFixedViewPitch(float partialTick) {
        return Mth.lerp(partialTick, xRotO, getXRot());
    }

    public boolean usesDirectApproachView() {
        return entityData.get(DATA_DIRECT_APPROACH_VIEW);
    }

    public int getDirectRotationTicks() {
        return entityData.get(DATA_DIRECT_ROTATION_TICKS);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        super.lerpTo(x, y, z, yRot, xRot, steps, teleport);
    }

    private void finish(ServerPlayer viewer) {
        float finalYaw = getYRot();
        float finalPitch = Mth.clamp(getXRot(), -90.0F, 90.0F);
        viewer.setYRot(finalYaw);
        viewer.setXRot(finalPitch);
        viewer.setYHeadRot(finalYaw);
        viewer.yBodyRot = finalYaw;

        viewer.connection.send(new ClientboundSetCameraPacket(viewer));
        FriendlyByteBuf endBuffer = new FriendlyByteBuf(Unpooled.buffer());
        endBuffer.writeFloat(finalYaw);
        endBuffer.writeFloat(finalPitch);
        NetworkManager.sendToPlayer(
                viewer, PomkotsMechs.id(PomkotsMechs.PACKET_CUTSCENE_END), endBuffer);
        discard();
    }

    private Vec3 resolveTarget() {
        if (targetEntityId == null) return fixedTarget;
        Entity entity = level() instanceof ServerLevel serverLevel ? serverLevel.getEntity(targetEntityId) : null;
        return entity == null || entity.isRemoved() ? null : entity.getBoundingBox().getCenter();
    }

    private void updateCamera(Vec3 target) {
        setPos(target.x + Math.cos(angle) * radius, target.y + height,
                target.z + Math.sin(angle) * radius);
        lookAt(EntityAnchorArgument.Anchor.EYES, target);
    }

    private void initializeAerialApproach(ServerPlayer viewer) {
        CutsceneDefinition.AerialApproach aerial = definition.aerialApproach();
        aerialStartPosition = viewer.getEyePosition();
        aerialAscentPosition = aerialStartPosition.add(0.0D, aerial.ascentHeight(), 0.0D);
        aerialStartYaw = viewer.getYRot();
        aerialStartPitch = viewer.getXRot();
        aerialTick = 0;
        setPos(aerialStartPosition);
        setFixedView(aerialStartYaw, aerialStartPitch);
    }

    private void initializeDirectApproach(ServerPlayer viewer) {
        directStartPosition = viewer.getEyePosition();
        directStartYaw = viewer.getYRot();
        directStartPitch = viewer.getXRot();
        directTick = 0;
        entityData.set(DATA_DIRECT_APPROACH_VIEW, true);
        entityData.set(DATA_DIRECT_ROTATION_TICKS, definition.directApproach().rotationDurationTicks());
        setPos(directStartPosition);
        setFixedView(directStartYaw, directStartPitch);
        yRotO = directStartYaw;
        xRotO = directStartPitch;
    }

    private void tickDirectApproach(ServerPlayer viewer, Vec3 target) {
        CutsceneDefinition.DirectApproach direct = definition.directApproach();
        double movementProgress = directTick < direct.movementDelayTicks()
                ? 0.0D
                : smoothProgress(
                        (directTick - direct.movementDelayTicks() + 1.0D)
                                / (direct.durationTicks() - direct.movementDelayTicks()));
        double rotationProgress = smoothProgress(
                (directTick + 1.0D) / direct.rotationDurationTicks());
        Vec3 end = directApproachEnd(target, direct);
        Vec3 position = directStartPosition.lerp(end, movementProgress);
        setPos(position);

        ViewRotation targetView = viewRotation(position, target);
        setFixedView(
                Mth.rotLerp((float) rotationProgress, directStartYaw, targetView.yaw()),
                Mth.lerp((float) rotationProgress, directStartPitch, targetView.pitch()));

        directTick++;
        remainingTicks--;
        hasImpulse = true;
        if (directTick >= direct.durationTicks() && !endFadeStarted) {
            CutsceneService.sendEndScreenFade(viewer);
            endFadeStarted = true;
            forcedClosingTicks = CutsceneService.END_TRANSITION_FADE_TICKS;
        }
    }

    private Vec3 directApproachEnd(Vec3 target, CutsceneDefinition.DirectApproach direct) {
        Vec3 horizontalAway = new Vec3(
                directStartPosition.x - target.x, 0.0D,
                directStartPosition.z - target.z);
        if (horizontalAway.lengthSqr() < 1.0E-6D) {
            double yawRadians = Math.toRadians(directStartYaw);
            horizontalAway = new Vec3(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
        } else {
            horizontalAway = horizontalAway.normalize();
        }
        return new Vec3(
                target.x + horizontalAway.x * direct.approachDistance(),
                target.y + direct.targetHeight(),
                target.z + horizontalAway.z * direct.approachDistance());
    }

    private void tickAerialApproach(ServerPlayer viewer, Vec3 target) {
        CutsceneDefinition.AerialApproach aerial = definition.aerialApproach();
        int ascentEnd = aerial.ascentDurationTicks();
        int turnEnd = ascentEnd + aerial.turnDurationTicks();
        int approachEnd = turnEnd + aerial.approachDurationTicks();

        if (aerialTick < ascentEnd) {
            double progress = smoothProgress((aerialTick + 1.0D) / aerial.ascentDurationTicks());
            setPos(aerialStartPosition.lerp(aerialAscentPosition, progress));
            setFixedView(aerialStartYaw, Mth.lerp((float) progress, aerialStartPitch, 90.0F));
        } else if (aerialTick < turnEnd) {
            double progress = smoothProgress(
                    (aerialTick - ascentEnd + 1.0D) / aerial.turnDurationTicks());
            setPos(aerialAscentPosition);
            ViewRotation targetView = viewRotation(aerialAscentPosition, target);
            setFixedView(
                    Mth.rotLerp((float) progress, aerialStartYaw, targetView.yaw()),
                    Mth.lerp((float) progress, 90.0F, targetView.pitch()));
        } else if (aerialTick < approachEnd) {
            double progress = smoothProgress(
                    (aerialTick - turnEnd + 1.0D) / aerial.approachDurationTicks());
            Vec3 end = aerialApproachEnd(target, aerial);
            Vec3 position = aerialAscentPosition.lerp(end, progress);
            setPos(position);
            // Keep the same rotation interpolation path across the turn/approach
            // boundary. Switching to CameraMixin's automatic look-at here causes
            // a visible one-frame snap even when both rotations are nearly equal.
            ViewRotation targetView = viewRotation(position, target);
            setFixedView(targetView.yaw(), targetView.pitch());
        }

        aerialTick++;
        remainingTicks--;
        hasImpulse = true;
        if (aerialTick >= approachEnd && !endFadeStarted) {
            CutsceneService.sendEndScreenFade(viewer);
            endFadeStarted = true;
            forcedClosingTicks = CutsceneService.END_TRANSITION_FADE_TICKS;
        }
    }

    private Vec3 aerialApproachEnd(Vec3 target, CutsceneDefinition.AerialApproach aerial) {
        Vec3 horizontalAway = new Vec3(
                aerialAscentPosition.x - target.x, 0.0D,
                aerialAscentPosition.z - target.z);
        if (horizontalAway.lengthSqr() < 1.0E-6D) {
            double yawRadians = Math.toRadians(aerialStartYaw);
            horizontalAway = new Vec3(-Math.sin(yawRadians), 0.0D, Math.cos(yawRadians));
        } else {
            horizontalAway = horizontalAway.normalize();
        }
        return new Vec3(
                target.x + horizontalAway.x * aerial.approachDistance(),
                target.y + aerial.targetHeight(),
                target.z + horizontalAway.z * aerial.approachDistance());
    }

    private void setFixedView(float yaw, float pitch) {
        entityData.set(DATA_FIXED_VIEW, true);
        entityData.set(DATA_FIXED_YAW, yaw);
        entityData.set(DATA_FIXED_PITCH, pitch);
        setYRot(yaw);
        setXRot(pitch);
    }

    private static ViewRotation viewRotation(Vec3 from, Vec3 target) {
        Vec3 delta = target.subtract(from);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        return new ViewRotation(
                (float) (Math.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F,
                (float) (-(Math.atan2(delta.y, horizontal) * Mth.RAD_TO_DEG)));
    }

    private static double smoothProgress(double progress) {
        double value = Mth.clamp(progress, 0.0D, 1.0D);
        return value * value * (3.0D - 2.0D * value);
    }

    private record ViewRotation(float yaw, float pitch) {
    }

    private void tickLinearSessions(ServerPlayer viewer, Vec3 target) {
        if (sessionTransitionTicks >= 0) {
            sessionTransitionTicks--;
            if (sessionTransitionTicks == CutsceneService.SESSION_TRANSITION_HOLD_TICKS) {
                beginLinearSession(target, sessionIndex + 1);
            }
            if (sessionTransitionTicks <= 0) {
                sessionTransitionTicks = -1;
            }
            return;
        }

        CutsceneDefinition.LinearSession session = definition.sessions().get(sessionIndex);
        sessionTick++;
        double progress = Math.min(1.0D, sessionTick / (double) session.durationTicks());
        Vec3 offset = rotatePathOffset(session.from().lerp(session.to(), progress));
        setPos(target.add(offset));
        hasImpulse = true;
        remainingTicks--;

        if (sessionTick >= session.durationTicks()) {
            if (sessionIndex + 1 >= definition.sessions().size()) {
                CutsceneService.sendEndScreenFade(viewer);
                forcedClosingTicks = CutsceneService.END_TRANSITION_FADE_TICKS;
            } else {
                CutsceneService.sendSessionScreenFade(viewer);
                sessionTransitionTicks = CutsceneService.SESSION_TRANSITION_FADE_TICKS
                        + CutsceneService.SESSION_TRANSITION_HOLD_TICKS;
            }
        }
    }

    private void beginLinearSession(Vec3 target, int index) {
        sessionIndex = index;
        sessionTick = 0;
        CutsceneDefinition.LinearSession session = definition.sessions().get(index);
        Vec3 position = target.add(rotatePathOffset(session.from()));
        setPos(position);
        Vec3 delta = target.subtract(position);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float) (Math.atan2(delta.z, delta.x) * 180.0D / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(delta.y, horizontal) * 180.0D / Math.PI));
        entityData.set(DATA_FIXED_VIEW, !session.trackTarget());
        entityData.set(DATA_FIXED_YAW, yaw);
        entityData.set(DATA_FIXED_PITCH, pitch);
        setYRot(yaw);
        setXRot(pitch);
    }

    private Vec3 rotatePathOffset(Vec3 offset) {
        return definition.relativeToTargetYaw() && targetEntityId != null
                ? offset.yRot(relativePathYaw) : offset;
    }

    @Override
    public void turn(double yawDelta, double pitchDelta) {
        // Cutscene rotation is controlled exclusively by the orbit camera.
    }

    @Override public Iterable<ItemStack> getArmorSlots() { return NonNullList.withSize(4, ItemStack.EMPTY); }
    @Override public @NotNull ItemStack getItemBySlot(EquipmentSlot slot) { return ItemStack.EMPTY; }
    @Override public void setItemSlot(EquipmentSlot slot, ItemStack stack) { }
    @Override public HumanoidArm getMainArm() { return HumanoidArm.RIGHT; }
    @Override public boolean shouldBeSaved() { return false; }
}
