package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state;

import grcmcs.minecraft.mods.pomkotsmechs.block.arena.ArenaGateBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaMatchContext;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class CancelledProcessor
        implements IArenaMatchStateProcessor {

    @Override
    public void onEnter(
            ArenaMatchContext context
    ) {

        if (context.runtime().getBossBar()
                != null) {

            context.runtime()
                    .getBossBar()
                    .removeAllPlayers();
        }

        processPilots(context.getChallengerEntity(), context);
        processPilots(context.getOpponentEntity(), context);

        ejectElevatorsPassengers(context);

        context.arena()
                .setActiveMatch(
                        null
                );

        ArenaSavedData
                .get(context.server())
                .setDirty();
    }

    private void processPilots(Entity pilot, ArenaMatchContext context) {
        if (pilot instanceof ServerPlayer player) {
            String reason = context.runtime().getCancelReason();
            if (reason != null) {
                player.sendSystemMessage(Component.literal(reason));
            }

        } else if (pilot != null) {
            // NPCの場合は全部消去する
            var mech = pilot.getVehicle();

            if (mech != null) {
                mech.discard();
            }
            pilot.discard();
        }
    }

    @Override
    public void tick(
            ArenaMatchContext context
    ) {
    }

    @Override
    public void onExit(
            ArenaMatchContext context
    ) {
    }

    @Override
    public void onCancel(
            ArenaMatchContext context
    ) {
    }

    private void ejectElevatorsPassengers(ArenaMatchContext context) {
        ejectElevator(context.getLevel(), context.match().getGatePosA());
        ejectElevator(context.getLevel(), context.match().getGatePosB());
    }

    private void ejectElevator(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ArenaGateBlockEntity gateEntity) {
            gateEntity.refreshElevator();
        }
    }
}
