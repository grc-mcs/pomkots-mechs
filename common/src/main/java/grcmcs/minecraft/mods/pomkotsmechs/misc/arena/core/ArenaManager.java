package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.block.arena.ArenaControllerBlockEntity;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaMenuData;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaRankingEntry;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena.ArenaReceptionistMenu;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state.ArenaMatchState;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state.ArenaStateProcessors;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaFighterData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaInstance;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaMatchData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaSavedData;
import io.netty.buffer.Unpooled;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs.PACKET_ARENA_REFRESH_RANKING;

public final class ArenaManager {

    public static String DEFAULT_ARENA_ID = "default";

    private ArenaManager() {}

    public static void ensureArena(MinecraftServer server, String arenaId, GlobalPos controllerPos) {
        ArenaSavedData data = ArenaSavedData.get(server);
        ArenaInstance arena = data.getArena(arenaId);

        if (arena == null) {
            arena = data.getOrCreateArena(arenaId);
            arena.setControllerPos(controllerPos);
            data.setDirty();
        } else if (arena.getControllerPos() == null) {
            arena.setControllerPos(controllerPos);
            data.setDirty();
        }
    }

    // =====================================================
    // INIT SERVER
    // =====================================================

    public static void onServerStarted(MinecraftServer server) {
        ArenaSavedData data =
                ArenaSavedData.get(server);

        for (ArenaInstance arena : data.getArenas().values()) {
            ArenaMatchData match = arena.getActiveMatch();

            if (match == null) {
                continue;
            }
//            else if (arena.getControllerPos() != null){
//                ArenaUtil.finalizeMatch(match, arena, server.getLevel(arena.getControllerPos().dimension()));
//            }

            PomkotsMechs.LOGGER.info(
                    "[Arena] Cancel orphaned match {}",
                    match.getMatchId()
            );

            arena.setActiveMatch(null);
        }

        data.setDirty();
    }

    // =====================================================
    // MAIN
    // =====================================================

    public static void tick(MinecraftServer server) {
        if ((server.getTickCount() % 20) != 0) {
            return;
        }

        ArenaSavedData data = ArenaSavedData.get(server);
        for (ArenaInstance arena : data.getArenas().values()) {
            ArenaMatchProcessor.tick(
                server,
                arena
            );
        }
    }

    // =====================================================
    // MATCHES
    // =====================================================

    public static ArenaMatchData startRankMatch(
            ServerPlayer player,
            boolean isFreeMatch,
            String arenaId,
            UUID opponentId
    ) {
        ArenaSavedData data = ArenaSavedData.get(player.server);
        ArenaInstance arena = data.getArena(arenaId);

        if (arena == null) {
            player.sendSystemMessage(
                    Component.literal("Arena Instance not found.")
            );
            return null;
        } else if (arena.getControllerPos() == null) {
            player.sendSystemMessage(
                    Component.literal("Arena Controller Block not found")
            );
            return null;
        }

        var cPos = arena.getControllerPos();
        var dim = cPos.dimension();
        var level = player.server.getLevel(dim);
        var bEnt = level.getBlockEntity(cPos.pos());

        if (!(bEnt instanceof ArenaControllerBlockEntity controllerBlockEntity)) {
            player.sendSystemMessage(
                    Component.literal("Arena Controller Block not found at" + arena.getControllerPos().pos())
            );
            return null;
        }

        var arenaLocation = controllerBlockEntity.scanLayout();
        if (arenaLocation.getBattleAnchor() == null) {
            player.sendSystemMessage(
                    Component.literal("Arena Battle Filed Anchor Block not found")
            );
            return null;

        } else if (arenaLocation.getGates() == null || arenaLocation.getGates().size() < 2) {
            player.sendSystemMessage(
                    Component.literal("Arena Gate Block not found")
            );
            return null;

        } else if (arenaLocation.getTeleportPos() == null) {
            player.sendSystemMessage(
                    Component.literal("Arena Teleport Block not found")
            );
            return null;

        }

        UUID challengerId = player.getUUID();

        ArenaFighterData challenger = arena.getFighters().get(challengerId);
        if (challenger == null) {
            player.sendSystemMessage(
                    Component.literal("Arena profile not found.")
            );

            return null;
        }

        ArenaFighterData opponent = arena.getFighters().get(opponentId);
        if (opponent == null) {
            player.sendSystemMessage(
                    Component.literal("Opponent not found.")
            );
            return null;
        }

        if (arena.getActiveMatch() != null) {
            player.sendSystemMessage(
                    Component.literal("Arena is currently in use.")
            );
            return null;
        }

        if (opponent.getType() == ArenaFighterData.FighterType.PLAYER
            && player.server.getPlayerList().getPlayer(opponent.getFighterId()) == null
        ) {
            player.sendSystemMessage(
                    Component.literal("Target fighter is off-line: " + opponent.getDisplayName())
            );
            return null;
        }

        ArenaMatchData match = new ArenaMatchData();

        match.setMatchId(UUID.randomUUID());

        match.setChallengerId(challengerId);
        match.setOpponentId(opponentId);

        match.setState(ArenaMatchState.WAIT_FOR_ENTRY);

        var now = System.currentTimeMillis();
        match.setCreatedTime(now);
        match.setStateChangedTime(now);

        match.setBattleFieldAnchorPos(arenaLocation.getBattleAnchor());
        match.setGatePosA(arenaLocation.getGates().get(0));
        match.setGatePosB(arenaLocation.getGates().get(1));
        match.setTeleportPointPos(arenaLocation.getTeleportPos());

        startMatch(player.server, (ServerLevel) player.level(), arena, match);

        return match;
    }

    private static void startMatch(
            MinecraftServer server,
            ServerLevel level,
            ArenaInstance arena,
            ArenaMatchData match
    ) {
        arena.setActiveMatch(match);

        ArenaSavedData.get(server)
                .setDirty();

        ArenaMatchContext context = ArenaMatchProcessor.createContext(
                        server,
                        level,
                        arena,
                        match
                );

        ArenaStateProcessors
                .get(match.getState())
                .onEnter(context);

        PomkotsMechs.LOGGER.info(
                "[Arena] Match created {} vs {} ({})",
                match.getChallengerId(),
                match.getOpponentId(),
                match.getMatchId()
        );
    }

    public static void cancelMatch(String reason, String arenaId, UUID matchId, MinecraftServer server) {
        ArenaMatchProcessor.cancelMatch(reason, arenaId, matchId, server);
    }

    // =====================================================
    // FIGHTERS
    // =====================================================

    public static void registerFighter(
            MinecraftServer server,
            String arenaId,
            ArenaFighterData fighter
    ) {
        ArenaSavedData data = ArenaSavedData.get(server);
        ArenaInstance arena = data.getOrCreateArena(arenaId);
        if (arena == null) {
            return;
        }

        arena.addFighter(fighter);
        data.setDirty();

        broadcastRankingUpdate(server, arenaId);
    }

    public static void removeFighter(
            MinecraftServer server,
            String arenaId,
            UUID fighterId
    ) {
        ArenaSavedData data = ArenaSavedData.get(server);
        ArenaInstance arena = data.getOrCreateArena(arenaId);
        if (arena == null) {
            return;
        }

        arena.removeFighter(fighterId);
        data.setDirty();

        broadcastRankingUpdate(server, arenaId);
    }

    public static ArenaFighterData getFighter(
            MinecraftServer server,
            String arenaId,
            UUID fighterId
    ) {
        ArenaSavedData data = ArenaSavedData.get(server);
        ArenaInstance arena = data.getOrCreateArena(arenaId);
        if (arena == null) {
            return null;
        }

        return arena.getFighter(fighterId);
    }

    public static List<ArenaFighterData> getFighters(
            MinecraftServer server,
            String arenaId
    ) {
        ArenaSavedData data = ArenaSavedData.get(server);
        ArenaInstance arena = data.getOrCreateArena(arenaId);
        if (arena == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(
                arena.getFighters().values()
        );
    }

    // =====================================================
    // RANKINGS
    // =====================================================

    public static List<ArenaRankingEntry> buildRanking(MinecraftServer server, String arenaId) {
        List<ArenaRankingEntry> list = new ArrayList<>();

        for (var fighter: getRanking(server, arenaId)) {
            var ent = new ArenaRankingEntry();
            ent.setFighterId(fighter.getFighterId());
            ent.setDisplayName(fighter.getDisplayName());
            ent.setRating(fighter.getRating());
            ent.setWins(fighter.getWins());
            ent.setLosses(fighter.getLosses());
            ent.setMechName(fighter.getMechData().getMechName());
            ent.setComment(fighter.getComment());

            if (fighter.getType() == ArenaFighterData.FighterType.PLAYER) {
                ent.setType(0);

                ServerPlayer player = null;

                if (fighter.getFighterId() != null) {
                    player = server.getPlayerList().getPlayer(
                            fighter.getFighterId()
                    );
                }

                ent.setOnline(player != null);

            } else {
                ent.setType(1);
                ent.setOnline(true);
            }

            list.add(ent);
        }

        return list;
    }

    public static List<ArenaFighterData> getRanking(
            MinecraftServer server,
            String arenaId
    ) {
        List<ArenaFighterData> fighters = getFighters(server, arenaId);
        fighters.sort(
                Comparator.comparingInt(
                        ArenaFighterData::getRating
                ).reversed()
        );

        return fighters;
    }

    public static void broadcastRankingUpdate(
            MinecraftServer server,
            String arenaId
    ) {
        var ranking = ArenaManager.buildRanking(
                server, arenaId
        );

        for (ServerPlayer player :
                server.getPlayerList()
                        .getPlayers()) {
            if (player.containerMenu instanceof ArenaReceptionistMenu) {
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());

                ArenaMenuData.write(
                        buf,
                        new ArenaMenuData.Entry(
                                arenaId,
                                ArenaReceptionistMenu.MODE_RECEPTION,
                                ItemStack.EMPTY,
                                ranking
                                )
                );

                NetworkManager.sendToPlayer(
                        player,
                        PomkotsMechs.id(PACKET_ARENA_REFRESH_RANKING),
                        buf
                );
            }
        }
    }

    public static ArenaMatchData getActiveMatch(String arenaId, UUID matchUUID, MinecraftServer server) {
        ArenaSavedData data = ArenaSavedData.get(server);
        ArenaInstance arena = data.getArena(arenaId);

        if (arena == null || arena.getActiveMatch() == null) {
            return null;
        }

        var match = arena.getActiveMatch();

        if (matchUUID.equals(match.getMatchId())) {
            return match;
        } else {
            return null;
        }
    }

    public static ArenaInstance getArena(String arenaId, MinecraftServer server) {
        ArenaSavedData data = ArenaSavedData.get(server);
        return data.getArena(arenaId);
    }

    public static void update(MinecraftServer server, String arenaId) {
        ArenaSavedData data = ArenaSavedData.get(server);
        data.setDirty();
        broadcastRankingUpdate(server, arenaId);
    }
}
