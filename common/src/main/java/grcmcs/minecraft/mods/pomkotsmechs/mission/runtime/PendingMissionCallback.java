package grcmcs.minecraft.mods.pomkotsmechs.mission.runtime;

import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionResult;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record PendingMissionCallback(
        UUID callbackId,
        UUID ownerId,
        String ownerName,
        ResourceLocation missionId,
        UUID instanceId,
        MissionResult result,
        List<String> commands
) {
    public PendingMissionCallback {
        commands = List.copyOf(commands);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("CallbackId", callbackId);
        tag.putUUID("OwnerId", ownerId);
        tag.putString("OwnerName", ownerName);
        tag.putString("MissionId", missionId.toString());
        if (instanceId != null) tag.putUUID("InstanceId", instanceId);
        tag.putString("Result", result.name());
        ListTag commandTags = new ListTag();
        commands.forEach(command -> commandTags.add(StringTag.valueOf(command)));
        tag.put("Commands", commandTags);
        return tag;
    }

    public static PendingMissionCallback load(CompoundTag tag) {
        List<String> commands = new ArrayList<>();
        for (Tag element : tag.getList("Commands", Tag.TAG_STRING)) commands.add(element.getAsString());
        return new PendingMissionCallback(
                tag.getUUID("CallbackId"),
                tag.getUUID("OwnerId"),
                tag.getString("OwnerName"),
                new ResourceLocation(tag.getString("MissionId")),
                tag.hasUUID("InstanceId") ? tag.getUUID("InstanceId") : null,
                MissionResult.valueOf(tag.getString("Result")),
                commands
        );
    }
}
