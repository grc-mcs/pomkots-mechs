package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core;

import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state.ArenaMatchState;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state.ArenaStateProcessors;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state.IArenaMatchStateProcessor;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaInstance;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaMatchData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaSavedData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ArenaMatchProcessor {
    private static final Map<UUID, ArenaMatchRuntime> RUNTIMES = new HashMap<>();

    private ArenaMatchProcessor() {}

    public static void tick(
            MinecraftServer server,
            ArenaInstance arena
    ) {
        ArenaMatchData match = arena.getActiveMatch();
        if (match == null || arena.getControllerPos() == null) {
            return;
        }

        ArenaMatchContext context = createContext(
                        server,
                        server.getLevel(arena.getControllerPos().dimension()),
                        arena,
                        match
        );

        ArenaStateProcessors.get(match.getState()).tick(context);
    }

    public static void changeState(
            ArenaMatchContext context,
            ArenaMatchState nextState
    ) {

        ArenaMatchState current =
                context.match()
                        .getState();

        IArenaMatchStateProcessor currentProcessor =
                ArenaStateProcessors.get(
                        current
                );

        if (currentProcessor != null) {

            if (nextState == ArenaMatchState.CANCELLED) {
                currentProcessor.onCancel(
                        context
                );
            } else {
                currentProcessor.onExit(
                        context
                );
            }
        }

        context.match()
                .setState(nextState);

        context.match()
                .setStateChangedTime(
                        System.currentTimeMillis()
                );

        IArenaMatchStateProcessor nextProcessor =
                ArenaStateProcessors.get(
                        nextState
                );

        if (nextProcessor != null) {
            nextProcessor.onEnter(
                    context
            );
        }

        context.runtime().resetOnceFlag();

        ArenaSavedData
                .get(context.server())
                .setDirty();
    }

    public static void cancelMatch(String reason, String arenaId, UUID matchId, MinecraftServer server) {
        ArenaInstance arena = ArenaManager.getArena(arenaId, server);

        if (arena == null || arena.getControllerPos() == null) {
            return;
        }

        var m1 = arena.getActiveMatch();
        if (
                m1 != null
                && m1.getMatchId().equals(matchId)
        ) {
            var context = createContext(server, server.getLevel(arena.getControllerPos().dimension()), arena, m1);
            cancelMatch(context, reason);
        }
    }

    public static void cancelMatch(
            ArenaMatchContext context,
            String reason
    ) {
        context.runtime()
                .setCancelReason(
                        reason
                );

        changeState(
                context,
                ArenaMatchState.CANCELLED
        );
    }

    public static ArenaMatchContext createContext(
            MinecraftServer server,
            ServerLevel level,
            ArenaInstance arena,
            ArenaMatchData match
    ) {
        ArenaMatchRuntime runtime =
                runtime(match);

        return new ArenaMatchContext(
                server,
                level,
                arena,
                match,
                runtime
        );
    }

    private static ArenaMatchRuntime runtime(
            ArenaMatchData match
    ) {
        return RUNTIMES.computeIfAbsent(
                match.getMatchId(),
                ArenaMatchRuntime::new
        );
    }
}