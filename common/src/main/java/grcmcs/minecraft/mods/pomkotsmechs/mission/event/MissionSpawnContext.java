package grcmcs.minecraft.mods.pomkotsmechs.mission.event;

import grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionEntityCleanupPolicy;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public record MissionSpawnContext(
        UUID instanceId,
        String sourceGroup,
        String objectiveGroup,
        MissionEntityCleanupPolicy objectiveCleanupPolicy,
        float healthModifier,
        float attackModifier,
        boolean raidBehavior,
        UUID defenseTargetId
) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("InstanceId", instanceId);
        tag.putString("SourceGroup", sourceGroup);
        tag.putString("ObjectiveGroup", objectiveGroup);
        tag.putString("ObjectiveCleanupPolicy", objectiveCleanupPolicy.name());
        tag.putFloat("HealthModifier", healthModifier);
        tag.putFloat("AttackModifier", attackModifier);
        tag.putBoolean("RaidBehavior", raidBehavior);
        if (defenseTargetId != null) tag.putUUID("DefenseTargetId", defenseTargetId);
        return tag;
    }

    public static MissionSpawnContext load(CompoundTag tag) {
        return new MissionSpawnContext(
                tag.getUUID("InstanceId"),
                tag.getString("SourceGroup"),
                tag.getString("ObjectiveGroup"),
                MissionEntityCleanupPolicy.fromSerializedName(tag.getString("ObjectiveCleanupPolicy")),
                tag.contains("HealthModifier") ? tag.getFloat("HealthModifier") : 1.0F,
                tag.contains("AttackModifier") ? tag.getFloat("AttackModifier") : 1.0F,
                tag.getBoolean("RaidBehavior"),
                tag.hasUUID("DefenseTargetId") ? tag.getUUID("DefenseTargetId") : null
        );
    }
}
