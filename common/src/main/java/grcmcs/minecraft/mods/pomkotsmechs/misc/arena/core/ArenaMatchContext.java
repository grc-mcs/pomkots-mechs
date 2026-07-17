package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core;

import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaInstance;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaMatchData;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.util.ArenaUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class ArenaMatchContext {

    private final MinecraftServer server;

    private final ServerLevel level;

    private final ArenaInstance arena;

    private final ArenaMatchData match;

    private final ArenaMatchRuntime runtime;

    public ArenaMatchContext(
            MinecraftServer server,
            ServerLevel level,
            ArenaInstance arena,
            ArenaMatchData match,
            ArenaMatchRuntime runtime
    ) {
        this.server = server;
        this.level = level;
        this.arena = arena;
        this.match = match;
        this.runtime = runtime;
    }

    public MinecraftServer server() {
        return server;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public ArenaInstance arena() {
        return arena;
    }

    public ArenaMatchData match() {
        return match;
    }

    public ArenaMatchRuntime runtime() {
        return runtime;
    }

    public long elapsedMillis() {
        return System.currentTimeMillis()
                - match.getStateChangedTime();
    }

    public Entity getChallengerEntity() {
        Entity entity = runtime.getChallengerEntity();

        if (entity != null && entity.isAlive()) {
            return entity;
        }

        entity = ArenaUtil.findEntity(
            level,
            match.getChallengerEntityId(),
            arena.getFighter(match.getChallengerId()).getType()
        );

        runtime.setChallengerEntity(
            entity
        );

        return entity;
    }

    public Entity getChallengerMechEntity() {
        Entity entity = runtime.getChallengerMech();

        if (entity != null && entity.isAlive()) {
            return entity;
        }

        entity = ArenaUtil.findEntity(
                level,
                match.getChallengerMechId()
        );

        runtime.setChallengerMech(
                entity
        );

        return entity;
    }

    public Entity getOpponentEntity() {
        Entity entity = runtime.getOpponentEntity();

        if (entity != null && entity.isAlive()) {
            return entity;
        }

        entity = ArenaUtil.findEntity(
                level,
                match.getOpponentEntityId(),
                arena.getFighter(match.getOpponentId()).getType()
        );

        runtime.setOpponentEntity(
                entity
        );

        return entity;
    }

    public Entity getOpponentMechEntity() {
        Entity entity = runtime.getOpponentMech();

        if (entity != null && entity.isAlive()) {
            return entity;
        }

        entity = ArenaUtil.findEntity(
                level,
                match.getOpponentMechId()
        );

        runtime.setOpponentMech(
                entity
        );

        return entity;
    }
}
