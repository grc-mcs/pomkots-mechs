package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class ArenaBattleRecord {
    private UUID battleId;

    private UUID fighterA;

    private UUID fighterB;

    private UUID winner;

    private long battleTime;

    public UUID getBattleId() {
        return battleId;
    }

    public void setBattleId(UUID battleId) {
        this.battleId = battleId;
    }

    public CompoundTag save() {

        CompoundTag tag = new CompoundTag();

        tag.putUUID("BattleId", battleId);
        tag.putUUID("FighterA", fighterA);
        tag.putUUID("FighterB", fighterB);
        tag.putUUID("Winner", winner);
        tag.putLong("BattleTime", battleTime);

        return tag;
    }

    public static ArenaBattleRecord load(CompoundTag tag) {
        ArenaBattleRecord data = new ArenaBattleRecord();

        data.battleId = tag.getUUID("BattleId");
        data.fighterA = tag.getUUID("FighterA");
        data.fighterB = tag.getUUID("FighterB");
        data.winner = tag.getUUID("Winner");
        data.battleTime = tag.getLong("BattleTime");

        return data;
    }
}