package grcmcs.minecraft.mods.pomkotsmechs.block.arena;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.ElevatorEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class ArenaGateBlockEntity
        extends BlockEntity {

    public ArenaGateBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                PomkotsMechs.ARENA_GATE_BLOCK_ENTITY.get(),
                pos,
                state
        );
    }

    public static void tick(
            Level level,
            BlockPos pos,
            BlockState state,
            ArenaGateBlockEntity be
    ) {
        if (level.isClientSide() || Utils.isMapEditingMode(level)) {
            return;
        }

        if (level.getGameTime() % 200 != 0) {
            return;
        }

        be.ensureElevator();
    }

    public void ensureElevator() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity entity = null;

        if (elevatorUuid != null) {
            entity = serverLevel.getEntity(elevatorUuid);
        }

        if (entity instanceof ElevatorEntity) {
            return;
        }

        spawnElevator(
                serverLevel
        );
    }

    public ElevatorEntity getElevator() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        Entity entity = null;

        if (elevatorUuid != null) {
            entity = serverLevel.getEntity(elevatorUuid);
        }

        if (entity instanceof ElevatorEntity e) {
            return e;
        }

        return null;
    }

    public ElevatorEntity refreshElevator() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        if (elevatorUuid != null) {
            Entity entity = serverLevel.getEntity(elevatorUuid);
            if (entity instanceof ElevatorEntity) {
                entity.discard();
                elevatorUuid = null;
            }
        }

        return spawnElevator(
                serverLevel
        );
    }

    private ElevatorEntity spawnElevator(
            ServerLevel level
    ) {
        ElevatorEntity elevator =
                PomkotsMechs.ELEVATOR
                        .get()
                        .create(level);

        if (elevator == null) {
            return null;
        }

        elevator.moveTo(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 1.2,
                worldPosition.getZ() + 0.5
        );

        var src = this.getBlockPos();
        var dstR = this.relativeDstBlockPos;

        elevator.setSrcBlockPos(src.above(1));
        elevator.setDstBlockPos(new BlockPos(src.getX() + dstR.getX(), src.getY() + dstR.getY(), src.getZ() + dstR.getZ()));
        elevator.setMode(ElevatorEntity.Mode.IDLE);
        elevator.setFixedYaw(getBlockYaw());

        level.addFreshEntity(elevator);
        elevatorUuid = elevator.getUUID();

        setChanged();

        return elevator;
    }

    private UUID elevatorUuid = null;
    private BlockPos relativeDstBlockPos = BlockPos.ZERO;

    @Override
    protected void saveAdditional(
            CompoundTag tag
    ) {
        super.saveAdditional(tag);

        if (elevatorUuid != null) {
            tag.putUUID(
                    PomkotsMechs.nbtName("ElevatorUuid"),
                    elevatorUuid
            );
        }

        Utils.saveBlockPos(tag, "Dst", relativeDstBlockPos);
    }

    @Override
    public void load(
            CompoundTag tag
    ) {
        super.load(tag);

        String nbtName = PomkotsMechs.nbtName("ElevatorUuid");
        if (tag.hasUUID(nbtName)) {
            elevatorUuid = tag.getUUID(nbtName);
        }

        relativeDstBlockPos = Utils.loadBlockPos("Dst", tag);
    }


    private float getBlockYaw() {
        var state = this.getBlockState();
        Direction facing = state.getValue(ArenaGateBlock.FACING);

        return switch (facing) {
            case SOUTH -> 0F;
            case WEST  -> 90F;
            case NORTH -> 180F;
            case EAST  -> -90F;
            default    -> 0F;
        };
    }

    private void rotateEntity(Entity ent, float yaw) {
        ent.setYRot(yaw);
        ent.yRotO = yaw;
        ent.setYHeadRot(yaw);
        ent.setYBodyRot(yaw);
    }
}
