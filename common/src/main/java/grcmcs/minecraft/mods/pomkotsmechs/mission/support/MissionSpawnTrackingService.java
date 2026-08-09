package grcmcs.minecraft.mods.pomkotsmechs.mission.support;

import grcmcs.minecraft.mods.pomkotsmechs.mission.event.MissionSpawnContext;
import grcmcs.minecraft.mods.pomkotsmechs.mission.event.MissionSpawnSource;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionInstance;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionSavedData;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;

public final class MissionSpawnTrackingService {
    private MissionSpawnTrackingService() {
    }

    public static void configureSource(MinecraftServer server, MissionSpawnSource source,
                                       Entity sourceEntity, MissionSpawnContext context) {
        MissionInstance instance = MissionSavedData.get(server).instances().get(context.instanceId());
        if (instance == null) throw new IllegalStateException("Mission instance no longer exists: " + context.instanceId());
        source.setMissionSpawnContext(context);
        instance.markGroupSpawned(context.objectiveGroup());
        instance.trackSpawnSource(context.sourceGroup(), sourceEntity.getUUID());
        MissionSavedData.get(server).setDirty();
    }

    public static boolean registerSpawnedChild(Entity parent, Entity child) {
        if (!(parent instanceof MissionSpawnSource source) || parent.getServer() == null) return true;
        MissionSpawnContext context = source.getMissionSpawnContext();
        if (context == null) return true;

        MinecraftServer server = parent.getServer();
        MissionSavedData data = MissionSavedData.get(server);
        MissionInstance instance = data.instances().get(context.instanceId());
        if (instance == null) {
            child.discard();
            return false;
        }

        child.addTag("pomkots_mission:" + context.instanceId());
        if (child instanceof GenericPomkotsMonster pomkots) {
            pomkots.setModifiers(context.healthModifier(), context.attackModifier());
            if (context.raidBehavior()) {
                pomkots.setInRaid(true);
                if (context.defenseTargetId() != null) {
                    pomkots.setMissionDefenseTargetId(context.defenseTargetId());
                }
            }
        }

        if (child instanceof MissionSpawnSource childSource) {
            child.addTag("pomkots_mission_group:" + context.sourceGroup());
            childSource.setMissionSpawnContext(context);
            instance.trackEntity(context.sourceGroup(), child.getUUID());
            instance.trackSpawnSource(context.sourceGroup(), child.getUUID());
        } else {
            child.addTag("pomkots_mission_group:" + context.objectiveGroup());
            instance.trackEntity(context.objectiveGroup(), child.getUUID(),
                    context.objectiveCleanupPolicy(), null);
        }
        data.setDirty();
        return true;
    }

    public static void completeSource(Entity sourceEntity) {
        if (!(sourceEntity instanceof MissionSpawnSource source) || sourceEntity.getServer() == null) return;
        MissionSpawnContext context = source.getMissionSpawnContext();
        if (context == null) return;
        MissionSavedData data = MissionSavedData.get(sourceEntity.getServer());
        MissionInstance instance = data.instances().get(context.instanceId());
        if (instance != null && instance.markSpawnSourceCompleted(sourceEntity.getUUID())) data.setDirty();
    }

    public static void discardSpawnedChild(Entity parent, Entity child) {
        if (parent instanceof MissionSpawnSource source && parent.getServer() != null
                && source.getMissionSpawnContext() != null) {
            MissionSavedData data = MissionSavedData.get(parent.getServer());
            MissionInstance instance = data.instances().get(source.getMissionSpawnContext().instanceId());
            if (instance != null && instance.markTrackedEntityDead(child.getUUID())) data.setDirty();
        }
        child.discard();
    }
}
