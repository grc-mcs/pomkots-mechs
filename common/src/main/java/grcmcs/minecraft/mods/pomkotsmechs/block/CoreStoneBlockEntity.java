package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CoreStoneBlockEntity extends BlockEntity {

    // NBT保存対象
    private String startTargetRaidName = null;
    private UUID raidControllerEntityUUID = null;
    private UUID raidOwnerUUID = null;
    private String raidFinalizeCommandSuccess = null;
    private String raidFinalizeCommandFail = null;

    // レイドコントローラーの実体
    private RaidControllerEntity raidControllerEntity = null;

    public CoreStoneBlockEntity(BlockPos pos, BlockState state) {
        super(PomkotsMechs.CORE_STONE_BLOCK_ENTITY.get(), pos, state);

    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        if (tag.contains(PomkotsMechs.nbtName("StartTargetRaidName"))) {
            startTargetRaidName = tag.getString(PomkotsMechs.nbtName("StartTargetRaidName"));
        }

        if (tag.contains(PomkotsMechs.nbtName("RaidControllerEntityUUID"))) {
            raidControllerEntityUUID = tag.getUUID(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
        }

        if (tag.contains(PomkotsMechs.nbtName("RaidOwnerUUID"))) {
            raidOwnerUUID = tag.getUUID(PomkotsMechs.nbtName("RaidOwnerUUID"));
        }

        if (tag.contains(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"))) {
            raidFinalizeCommandSuccess = tag.getString(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"));
        }

        if (tag.contains(PomkotsMechs.nbtName("RaidFinalizeCommandFail"))) {
            raidFinalizeCommandFail = tag.getString(PomkotsMechs.nbtName("RaidFinalizeCommandFail"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);

        if (this.startTargetRaidName != null) {
            tag.putString(PomkotsMechs.nbtName("StartTargetRaidName"), startTargetRaidName);
        } else {
            tag.remove(PomkotsMechs.nbtName("StartTargetRaidName"));
        }

        if (this.raidControllerEntityUUID != null) {
            tag.putUUID(PomkotsMechs.nbtName("RaidControllerEntityUUID"), raidControllerEntityUUID);
        } else {
            tag.remove(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
        }

        if (this.raidOwnerUUID != null) {
            tag.putUUID(PomkotsMechs.nbtName("RaidOwnerUUID"), raidOwnerUUID);
        } else {
            tag.remove(PomkotsMechs.nbtName("RaidOwnerUUID"));
        }

        if (this.raidFinalizeCommandSuccess != null) {
            tag.putString(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"), raidFinalizeCommandSuccess);
        } else {
            tag.remove(PomkotsMechs.nbtName("RaidFinalizeCommandSuccess"));
        }

        if (this.raidFinalizeCommandFail != null) {
            tag.putString(PomkotsMechs.nbtName("RaidFinalizeCommandFail"), raidFinalizeCommandFail);
        } else {
            tag.remove(PomkotsMechs.nbtName("RaidFinalizeCommandFail"));
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CoreStoneBlockEntity be) {
        if (be.level == null || be.level.isClientSide) {
            return;
        }

        var serverLevel = (ServerLevel) be.level;

        if (be.startTargetRaidName != null) {
            if (be.raidControllerEntityUUID == null) {
                RaidControllerEntity rce = PomkotsMechs.RAID_CONTROLLER.get().create(serverLevel);

                if (rce != null) {

                    rce.setPos(pos.getX(), pos.getY(), pos.getZ());
                    CompoundTag tag = RaidControllerEntity.buildCompoundTag(
                            be.startTargetRaidName,
                            be.raidOwnerUUID,
                            be.raidFinalizeCommandSuccess,
                            be.raidFinalizeCommandFail
                    );
                    rce.readAdditionalSaveData(tag);

                    be.level.addFreshEntity(rce);
                    be.raidControllerEntity = rce;
                    be.raidControllerEntityUUID = rce.getUUID();

                    be.setChanged();
                }
            } else if (be.raidControllerEntity == null) {
                be.raidControllerEntity = (RaidControllerEntity) serverLevel.getEntity(be.raidControllerEntityUUID);

                if (be.raidControllerEntity == null) {
                    be.resetInternalStatus();
                    be.setChanged();
                }
            }

            if (be.raidControllerEntity == null || !be.raidControllerEntity.isAlive()) {
                be.resetInternalStatus();
                be.setChanged();
            }
        }
    }

    protected void resetInternalStatus() {
        this.startTargetRaidName = null;
        this.raidControllerEntity = null;
        this.raidControllerEntityUUID = null;
        this.raidOwnerUUID = null;
        this.raidFinalizeCommandSuccess = null;
        this.raidFinalizeCommandFail = null;
    }

    public void startRaid(String raidName, UUID owner, String successCommand, String failCommand) {
        this.startTargetRaidName = raidName;
        this.raidOwnerUUID = owner;
        this.raidFinalizeCommandSuccess = successCommand;
        this.raidFinalizeCommandFail = failCommand;
    }

    public boolean isRaidActive() {
        return startTargetRaidName != null;
    }
}