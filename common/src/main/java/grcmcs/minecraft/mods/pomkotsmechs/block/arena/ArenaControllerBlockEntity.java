package grcmcs.minecraft.mods.pomkotsmechs.block.arena;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.ArenaReceptionistEntity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaLayout;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class ArenaControllerBlockEntity
        extends BlockEntity {

    private String arenaId = ArenaManager.DEFAULT_ARENA_ID;

    private UUID receptionistUuid;

    public ArenaControllerBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        super(
                PomkotsMechs.ARENA_CONTROLLER_BLOCK_ENTITY.get(),
                pos,
                state
        );
    }

    @Override
    protected void saveAdditional(
            CompoundTag tag
    ) {
        super.saveAdditional(tag);

        tag.putString(
                "ArenaId",
                arenaId
        );

        if (receptionistUuid != null) {
            tag.putUUID(
                    "ReceptionistUuid",
                    receptionistUuid
            );
        }
    }

    @Override
    public void load(
            CompoundTag tag
    ) {
        super.load(tag);

        arenaId =
                tag.getString(
                        "ArenaId"
                );

        if (tag.hasUUID(
                "ReceptionistUuid"
        )) {
            receptionistUuid =
                    tag.getUUID(
                            "ReceptionistUuid"
                    );
        }
    }

    public static void tick(
            Level level,
            BlockPos pos,
            BlockState state,
            ArenaControllerBlockEntity be
    ) {
        if (level.isClientSide() || Utils.isMapEditingMode(level)) {
            return;
        }

        if (level.getGameTime() % 200 != 0) {
            return;
        }

        be.ensureReceptionist();
    }

    public void ensureReceptionist() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ArenaManager.ensureArena(serverLevel.getServer(), arenaId, GlobalPos.of(serverLevel.dimension(), this.getBlockPos()));

        Entity entity = null;
        if (receptionistUuid != null) {
            entity =
                    serverLevel.getEntity(
                            receptionistUuid
                    );
        }

        if (entity instanceof ArenaReceptionistEntity) {
            return;
        }

        spawnReceptionist(
                serverLevel
        );
    }

    private void spawnReceptionist(
            ServerLevel level
    ) {

        ArenaReceptionistEntity npc =
                PomkotsMechs.ARENA_RECEP
                        .get()
                        .create(level);

        if (npc == null) {
            return;
        }

        npc.moveTo(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 1,
                worldPosition.getZ() + 0.5
        );

        npc.setArenaId(
                arenaId
        );

        level.addFreshEntity(
                npc
        );

        receptionistUuid =
                npc.getUUID();

        setChanged();
    }

    public ArenaLayout scanLayout() {
        ArenaLayout layout =
                new ArenaLayout();

        BlockPos center =
                getBlockPos();

        int radius = 300;

        BlockPos.betweenClosedStream(
                center.offset(
                        -radius,
                        -16,
                        -radius
                ),
                center.offset(
                        radius,
                        40,
                        radius
                )
        ).forEach(pos -> {
            BlockState state =
                    level.getBlockState(pos);

            if (state.is(
                    PomkotsMechs.ARENA_GATE_BLOCK.get()
            )) {
                layout.getGates()
                        .add(pos.immutable());
            }

            if (state.is(
                    PomkotsMechs.ARENA_BATTLEFIELD_ANCHOR_BLOCK.get()
            )) {
                layout.setBattleAnchor(
                        pos.immutable()
                );
            }

            if (state.is(
                    PomkotsMechs.ARENA_TELEPORT_BLOCK.get()
            )) {
                layout.setTeleportPos(
                        pos.immutable()
                );
            }
        });

        return layout;
    }
}
