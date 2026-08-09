package grcmcs.minecraft.mods.pomkotsmechs.mission.event;

import grcmcs.minecraft.mods.pomkotsmechs.mission.event.MissionSpawnContext;

/**
 * Mission中に、後からObjective Entityを生成し得るEntityが実装する。
 */
public interface MissionSpawnSource {
    boolean hasPendingMissionSpawns();

    MissionSpawnContext getMissionSpawnContext();

    void setMissionSpawnContext(MissionSpawnContext context);
}
