package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class ArenaFighterData {
    public enum FighterType {
        PLAYER,
        NPC
    }

    private UUID fighterId = UUID.randomUUID();

    private FighterType type = FighterType.NPC;

    private String displayName = "";

    private int rating = 0;

    private int wins = 0;

    private int losses = 0;

    private int draws = 0;

    private long lastBattleTime = 0;

    private String comment = "";

    private String aiLevel = "";

    private ResourceLocation texture = PomkotsMechs.id("textures/entity/pilot/mech_pilot_wide.png");

    private ResourceLocation model = PomkotsMechs.id("geo/mech_pilot_wide.geo.json");

    private ArenaRank rank = ArenaRank.E;

    private ArenaMechData mechData;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public UUID getFighterId() {
        return fighterId;
    }

    public void setFighterId(UUID fighterId) {
        this.fighterId = fighterId;
    }

    public long getLastBattleTime() {
        return lastBattleTime;
    }

    public void setLastBattleTime(long lastBattleTime) {
        this.lastBattleTime = lastBattleTime;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public ArenaMechData getMechData() {
        return mechData;
    }

    public void setMechData(ArenaMechData mechData) {
        this.mechData = mechData;
    }

    public ArenaRank getRank() {
        return rank;
    }

    public void setRank(ArenaRank rank) {
        this.rank = rank;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public FighterType getType() {
        return type;
    }

    public void setType(FighterType type) {
        this.type = type;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAiLevel() {
        return aiLevel;
    }

    public void setAiLevel(String aiLevel) {
        this.aiLevel = aiLevel;
    }

    public ResourceLocation getModel() {
        return model;
    }

    public void setModel(ResourceLocation model) {
        this.model = model;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public void setTexture(ResourceLocation texture) {
        this.texture = texture;
    }

    public CompoundTag save() {

        CompoundTag tag = new CompoundTag();

        tag.putUUID("FighterId", fighterId);

        tag.putString("Name", displayName);
        tag.putString("Comment", comment);

        tag.putInt("Rating", rating);

        if (type == FighterType.PLAYER) {
            tag.putInt("Type", 0);
        } else {
            tag.putInt("Type", 1);
        }

        tag.putInt("Wins", wins);
        tag.putInt("Losses", losses);
        tag.putInt("Draws", draws);

        tag.putString("AiLevel", aiLevel);
        tag.putString("Texture", texture.toString());
        tag.putString("Model", model.toString());

        tag.put("Mech", mechData.save());

        return tag;
    }

    public static ArenaFighterData load(CompoundTag tag) {
        ArenaFighterData data = new ArenaFighterData();

        data.fighterId = tag.getUUID("FighterId");

        data.displayName = tag.getString("Name");
        data.comment = tag.getString("Comment");

        data.rating = tag.getInt("Rating");

        data.wins = tag.getInt("Wins");
        data.losses = tag.getInt("Losses");
        data.draws = tag.getInt("Draws");

        var type = tag.getInt("Type");
        if (type == 0) {
            data.type = FighterType.PLAYER;
        } else {
            data.type = FighterType.NPC;
        }

        data.aiLevel = tag.getString("AiLevel");
        data.texture = new ResourceLocation(tag.getString("Texture"));
        data.model = new ResourceLocation(tag.getString("Model"));

        data.mechData =
                ArenaMechData.load(tag.getCompound("Mech"));

        return data;
    }
}
