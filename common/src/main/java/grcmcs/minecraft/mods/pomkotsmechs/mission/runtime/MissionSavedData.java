package grcmcs.minecraft.mods.pomkotsmechs.mission.runtime;

import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionInstance;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.PendingMissionCallback;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class MissionSavedData extends SavedData {
    public static final String DATA_NAME = "pomkotsmechs_missions";
    private final Map<UUID, MissionInstance> instances = new LinkedHashMap<>();
    private final Map<UUID, PendingMissionCallback> pendingCallbacks = new LinkedHashMap<>();

    public static MissionSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                MissionSavedData::load,
                MissionSavedData::new,
                DATA_NAME
        );
    }

    public Map<UUID, MissionInstance> instances() { return instances; }
    public Map<UUID, PendingMissionCallback> pendingCallbacks() { return pendingCallbacks; }

    public void addInstance(MissionInstance instance) {
        instances.put(instance.instanceId(), instance);
        setDirty();
    }

    public MissionInstance removeInstance(UUID id) {
        MissionInstance removed = instances.remove(id);
        if (removed != null) setDirty();
        return removed;
    }

    public void addPendingCallback(PendingMissionCallback callback) {
        pendingCallbacks.put(callback.callbackId(), callback);
        setDirty();
    }

    public void removePendingCallback(UUID id) {
        if (pendingCallbacks.remove(id) != null) setDirty();
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        ListTag instanceTags = new ListTag();
        instances.values().forEach(instance -> instanceTags.add(instance.save()));
        tag.put("Instances", instanceTags);

        ListTag callbackTags = new ListTag();
        pendingCallbacks.values().forEach(callback -> callbackTags.add(callback.save()));
        tag.put("PendingCallbacks", callbackTags);
        return tag;
    }

    public static MissionSavedData load(CompoundTag tag) {
        MissionSavedData data = new MissionSavedData();
        for (Tag element : tag.getList("Instances", Tag.TAG_COMPOUND)) {
            MissionInstance instance = MissionInstance.load((CompoundTag) element);
            data.instances.put(instance.instanceId(), instance);
        }
        for (Tag element : tag.getList("PendingCallbacks", Tag.TAG_COMPOUND)) {
            PendingMissionCallback callback = PendingMissionCallback.load((CompoundTag) element);
            data.pendingCallbacks.put(callback.callbackId(), callback);
        }
        return data;
    }
}
