package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.entity.Entity;

import java.util.*;

public class ArenaMatchRuntime {

    private final UUID matchId;

    private ServerBossEvent bossBar;

    private String cancelReason;

    private Entity challengerEntity;
    private Entity challengerMech;

    private Entity opponentEntity;
    private Entity opponentMech;

    private Map<String, Object> generalObjects = new HashMap<>();

    private BattleResult result;

    private final Set<String> executedEvents =
            new HashSet<>();

    public ArenaMatchRuntime(
            UUID matchId
    ) {
        this.matchId = matchId;
    }

    public UUID getMatchId() {
        return matchId;
    }

    public ServerBossEvent getBossBar() {
        return bossBar;
    }

    public void setBossBar(
            ServerBossEvent bossBar
    ) {
        this.bossBar = bossBar;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(
            String cancelReason
    ) {
        this.cancelReason = cancelReason;
    }

    public Entity getChallengerEntity() {
        return challengerEntity;
    }

    public void setChallengerEntity(Entity challengerEntity) {
        this.challengerEntity = challengerEntity;
    }

    public Entity getChallengerMech() {
        return challengerMech;
    }

    public void setChallengerMech(Entity challengerMech) {
        this.challengerMech = challengerMech;
    }

    public Entity getOpponentEntity() {
        return opponentEntity;
    }

    public void setOpponentEntity(Entity opponentEntity) {
        this.opponentEntity = opponentEntity;
    }

    public Entity getOpponentMech() {
        return opponentMech;
    }

    public void setOpponentMech(Entity opponentMech) {
        this.opponentMech = opponentMech;
    }

    public BattleResult getResult() {
        return result;
    }

    public void setResult(BattleResult result) {
        this.result = result;
    }

    public boolean runOnce(
            String key
    ) {
        return executedEvents.add(key);
    }

    public void resetOnceFlag() {
        executedEvents.clear();
    }

    public void put(String key, Object value) {
        generalObjects.put(key, value);
    }

    public Object get(String key) {
        return generalObjects.get(key);
    }

    public Object remove(String key) {
        return generalObjects.remove(key);
    }

    public void clear() {
        generalObjects.clear();
    }

    public static class BattleResult {

        public enum ResultType {
            CHALLENGER_WIN,
            OPPONENT_WIN,
            DRAW
        }

        private ResultType resultType;

        private UUID winnerId;
        private UUID loserId;

        private String reason;

        public UUID getLoserId() {
            return loserId;
        }

        public void setLoserId(UUID loserId) {
            this.loserId = loserId;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public ResultType getResultType() {
            return resultType;
        }

        public void setResultType(ResultType resultType) {
            this.resultType = resultType;
        }

        public UUID getWinnerId() {
            return winnerId;
        }

        public void setWinnerId(UUID winnerId) {
            this.winnerId = winnerId;
        }
    }
}
