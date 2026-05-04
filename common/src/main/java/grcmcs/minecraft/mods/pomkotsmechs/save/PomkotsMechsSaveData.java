package grcmcs.minecraft.mods.pomkotsmechs.save;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PomkotsMechsSaveData extends SavedData {
    private static final String DATA_NAME = PomkotsMechs.nbtName("SaveData");
    private final Map<UUID, MechLocation> mechPositions = new HashMap<>();

    public record MechLocation(ResourceKey<Level> dimension, ChunkPos chunkPos) {}

    public static PomkotsMechsSaveData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(
                        PomkotsMechsSaveData::load,
                        PomkotsMechsSaveData::new,
                        DATA_NAME);
    }

    public static PomkotsMechsSaveData load(CompoundTag tag) {
        PomkotsMechsSaveData data = new PomkotsMechsSaveData();
        ListTag list = tag.getList("mechs", Tag.TAG_COMPOUND);
        for (Tag t : list) {
            CompoundTag entry = (CompoundTag) t;
            UUID uuid = entry.getUUID("uuid");
            ResourceLocation dimId = new ResourceLocation(entry.getString("dimension"));
            ResourceKey<Level> dim = ResourceKey.create(Registries.DIMENSION, dimId);
            int x = entry.getInt("x");
            int z = entry.getInt("z");
            data.mechPositions.put(uuid, new MechLocation(dim, new ChunkPos(x, z)));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, MechLocation> e : mechPositions.entrySet()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("uuid", e.getKey());
            entry.putString("dimension", e.getValue().dimension.location().toString());
            entry.putInt("x", e.getValue().chunkPos.x);
            entry.putInt("z", e.getValue().chunkPos.z);
            list.add(entry);
        }
        tag.put("mechs", list);
        return tag;
    }

    public void updateMech(UUID uuid, ResourceKey<Level> dim, ChunkPos pos) {
        mechPositions.put(uuid, new MechLocation(dim, pos));
        setDirty();
    }

    public void removeMech(UUID uuid) {
        mechPositions.remove(uuid);
        setDirty();
    }

    public MechLocation get(UUID uuid) {
        return mechPositions.get(uuid);
    }
}
