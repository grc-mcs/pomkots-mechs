package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto;

import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.state.ArenaMatchState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.UUID;
public class ArenaMatchData {

    private UUID matchId;

    private UUID challengerId;

    private UUID opponentId;

    private ArenaMatchState state;

    private long createdTime;

    private long stateChangedTime;

    private BlockPos gatePosA;
    private BlockPos gatePosB;
    private BlockPos battleFieldAnchorPos;
    private BlockPos teleportPointPos;

    // ここはマッチ開始時に確定する
    private UUID challengerEntityId;
    private UUID challengerMechId;

    private UUID opponentEntityId;
    private UUID opponentMechId;

    public UUID getMatchId() {
        return matchId;
    }

    public void setMatchId(UUID matchId) {
        this.matchId = matchId;
    }

    public UUID getChallengerId() {
        return challengerId;
    }

    public void setChallengerId(UUID challengerId) {
        this.challengerId = challengerId;
    }

    public UUID getOpponentId() {
        return opponentId;
    }

    public void setOpponentId(UUID opponentId) {
        this.opponentId = opponentId;
    }

    public ArenaMatchState getState() {
        return state;
    }

    public void setState(ArenaMatchState state) {
        this.state = state;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getStateChangedTime() {
        return stateChangedTime;
    }

    public void setStateChangedTime(long stateChangedTime) {
        this.stateChangedTime = stateChangedTime;
    }

    public BlockPos getGatePosA() {
        return gatePosA;
    }

    public void setGatePosA(BlockPos gatePosA) {
        this.gatePosA = gatePosA;
    }

    public BlockPos getGatePosB() {
        return gatePosB;
    }

    public void setGatePosB(BlockPos gatePosB) {
        this.gatePosB = gatePosB;
    }

    public BlockPos getBattleFieldAnchorPos() {
        return battleFieldAnchorPos;
    }

    public void setBattleFieldAnchorPos(BlockPos battleFieldAnchorPos) {
        this.battleFieldAnchorPos = battleFieldAnchorPos;
    }

    public BlockPos getTeleportPointPos() {
        return teleportPointPos;
    }

    public void setTeleportPointPos(BlockPos teleportPointPos) {
        this.teleportPointPos = teleportPointPos;
    }

    public UUID getChallengerEntityId() {
        return challengerEntityId;
    }

    public void setChallengerEntityId(UUID challengerEntityId) {
        this.challengerEntityId = challengerEntityId;
    }

    public UUID getChallengerMechId() {
        return challengerMechId;
    }

    public void setChallengerMechId(UUID challengerMechId) {
        this.challengerMechId = challengerMechId;
    }

    public UUID getOpponentEntityId() {
        return opponentEntityId;
    }

    public void setOpponentEntityId(UUID opponentEntityId) {
        this.opponentEntityId = opponentEntityId;
    }

    public UUID getOpponentMechId() {
        return opponentMechId;
    }

    public void setOpponentMechId(UUID opponentMechId) {
        this.opponentMechId = opponentMechId;
    }

    public CompoundTag save() {

        CompoundTag tag = new CompoundTag();

        tag.putUUID(
                "MatchId",
                matchId
        );

        tag.putUUID(
                "ChallengerId",
                challengerId
        );

        tag.putUUID(
                "OpponentId",
                opponentId
        );

        tag.putString(
                "State",
                state.name()
        );

        tag.putLong(
                "CreatedTime",
                createdTime
        );

        tag.putLong(
                "StateChangedTime",
                stateChangedTime
        );

        tag.putInt("GateAX", gatePosA.getX());
        tag.putInt("GateAY", gatePosA.getY());
        tag.putInt("GateAZ", gatePosA.getZ());

        tag.putInt("GateBX", gatePosB.getX());
        tag.putInt("GateBY", gatePosB.getY());
        tag.putInt("GateBZ", gatePosB.getZ());

        tag.putInt("BFieldX", battleFieldAnchorPos.getX());
        tag.putInt("BFieldY", battleFieldAnchorPos.getY());
        tag.putInt("BFieldZ", battleFieldAnchorPos.getZ());

        tag.putInt("TelepoX", teleportPointPos.getX());
        tag.putInt("TelepoY", teleportPointPos.getY());
        tag.putInt("TelepoZ", teleportPointPos.getZ());

        saveEntityUUID("ChallengerEntityId", challengerEntityId, tag);
        saveEntityUUID("ChallengerMechId", challengerMechId, tag);
        saveEntityUUID("OpponentEntityId", opponentEntityId, tag);
        saveEntityUUID("OpponentMechId", opponentMechId, tag);

        return tag;
    }

    private static void saveEntityUUID(String name, UUID id, CompoundTag tag) {
        if (id != null) {
            tag.putUUID(name, id);
        }
    }

    public static ArenaMatchData load(
            CompoundTag tag
    ) {

        ArenaMatchData data =
                new ArenaMatchData();

        data.matchId =
                tag.getUUID(
                        "MatchId"
                );

        data.challengerId =
                tag.getUUID(
                        "ChallengerId"
                );

        data.opponentId =
                tag.getUUID(
                        "OpponentId"
                );

        data.state =
                ArenaMatchState.byId(
                        tag.getString(
                                "State"
                        )
                );

        data.createdTime =
                tag.getLong(
                        "CreatedTime"
                );

        data.stateChangedTime =
                tag.getLong(
                        "StateChangedTime"
                );

        data.gatePosA = new BlockPos(
                tag.getInt("GateAX"),
                tag.getInt("GateAY"),
                tag.getInt("GateAZ")
        );

        data.gatePosB = new BlockPos(
                tag.getInt("GateBX"),
                tag.getInt("GateBY"),
                tag.getInt("GateBZ")
        );

        data.battleFieldAnchorPos = new BlockPos(
                tag.getInt("BFieldX"),
                tag.getInt("BFieldY"),
                tag.getInt("BFieldZ")
        );

        data.teleportPointPos = new BlockPos(
                tag.getInt("TelepoX"),
                tag.getInt("TelepoY"),
                tag.getInt("TelepoZ")
        );

        if (tag.hasUUID("ChallengerEntityId")) {
            data.challengerEntityId = tag.getUUID("ChallengerEntityId");
        }
        if (tag.hasUUID("ChallengerMechId")) {
            data.challengerEntityId = tag.getUUID("ChallengerMechId");
        }
        if (tag.hasUUID("OpponentEntityId")) {
            data.challengerEntityId = tag.getUUID("OpponentEntityId");
        }
        if (tag.hasUUID("OpponentMechId")) {
            data.challengerEntityId = tag.getUUID("OpponentMechId");
        }

        return data;
    }
}