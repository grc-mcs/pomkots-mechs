package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.*;

public class ArenaInstance {

    private String arenaId;

    private String displayName;

    private ArenaMatchData activeMatch;

    private final Map<UUID, ArenaFighterData>
            fighters = new HashMap<>();

    private final List<ArenaBattleRecord>
            battleHistory = new ArrayList<>();

    private GlobalPos controllerPos;

    ArenaInstance() {

    }

    public CompoundTag save() {
        CompoundTag tag =
                new CompoundTag();

        tag.putString(
                "ArenaId",
                arenaId
        );

        tag.putString(
                "DisplayName",
                displayName
        );

        // POS
        if (controllerPos != null) {
            CompoundTag posTag =
                    new CompoundTag();

            posTag.putString(
                    "Dimension",
                    controllerPos.dimension()
                            .location()
                            .toString()
            );

            posTag.putInt(
                    "X",
                    controllerPos.pos().getX()
            );

            posTag.putInt(
                    "Y",
                    controllerPos.pos().getY()
            );

            posTag.putInt(
                    "Z",
                    controllerPos.pos().getZ()
            );

            tag.put(
                    "ControllerPos",
                    posTag
            );
        }

        // Fighter
        ListTag fighterList = new ListTag();

        for (ArenaFighterData fighter : fighters.values()) {
            fighterList.add(fighter.save());
        }

        tag.put("Fighters", fighterList);

        // History
        ListTag historyList = new ListTag();

        for (ArenaBattleRecord record : battleHistory) {
            historyList.add(record.save());
        }

        tag.put("History", historyList);

        // Match
        if (activeMatch != null) {
            tag.put(
                    "ActiveMatch",
                    activeMatch.save()
            );
        }

        return tag;
    }

    public static ArenaInstance load(CompoundTag tag) {
        ArenaInstance arena =
                new ArenaInstance();

        arena.arenaId = tag.getString("ArenaId");
        arena.displayName = tag.getString("DisplayName");

        // POS
        if (tag.contains(
                "ControllerPos",
                Tag.TAG_COMPOUND
        )) {
            CompoundTag posTag =
                    tag.getCompound(
                            "ControllerPos"
                    );

            ResourceKey<Level> dimension =
                    ResourceKey.create(
                            Registries.DIMENSION,
                            new ResourceLocation(
                                    posTag.getString(
                                            "Dimension"
                                    )
                            )
                    );

            BlockPos pos =
                    new BlockPos(
                            posTag.getInt("X"),
                            posTag.getInt("Y"),
                            posTag.getInt("Z")
                    );

            arena.controllerPos =
                    GlobalPos.of(
                            dimension,
                            pos
                    );
        }

        // Fighter
        ListTag fighterList =
                tag.getList("Fighters", Tag.TAG_COMPOUND);

        for (Tag element : fighterList) {
            ArenaFighterData fighter =
                    ArenaFighterData.load(
                            (CompoundTag) element
                    );

            arena.fighters.put(
                    fighter.getFighterId(),
                    fighter
            );
        }

        // History
        ListTag historyList =
                tag.getList("History", Tag.TAG_COMPOUND);

        for (Tag element : historyList) {
            arena.battleHistory.add(
                    ArenaBattleRecord.load(
                            (CompoundTag) element
                    )
            );
        }

        // Match
        if (tag.contains(
                "ActiveMatch"
        )) {

            arena.activeMatch =
                    ArenaMatchData.load(
                            tag.getCompound(
                                    "ActiveMatch"
                            )
                    );
        }

        return arena;
    }

    // getter/setter
    public ArenaMatchData getActiveMatch() {
        return activeMatch;
    }

    public void setActiveMatch(ArenaMatchData activeMatch) {
        this.activeMatch = activeMatch;
    }

    public String getArenaId() {
        return arenaId;
    }

    public void setArenaId(String arenaId) {
        this.arenaId = arenaId;
    }

    public List<ArenaBattleRecord> getBattleHistory() {
        return battleHistory;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<UUID, ArenaFighterData> getFighters() {
        return fighters;
    }

    public void addFighter(ArenaFighterData fighter) {
        this.fighters.put(fighter.getFighterId(), fighter);
    }

    public ArenaFighterData getFighter(UUID fighter) {
        return this.fighters.get(fighter);
    }

    public void removeFighter(UUID fighter) {
        this.fighters.remove(fighter);
    }

    public GlobalPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerPos(GlobalPos controllerPos) {
        this.controllerPos = controllerPos;
    }
}
