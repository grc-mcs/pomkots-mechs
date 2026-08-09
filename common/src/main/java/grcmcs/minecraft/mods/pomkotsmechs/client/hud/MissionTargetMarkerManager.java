package grcmcs.minecraft.mods.pomkotsmechs.client.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MissionTargetMarkerManager {
    private static final Map<UUID, Marker> MARKERS = new HashMap<>();

    private MissionTargetMarkerManager() { }

    public static void replace(Map<UUID, Update> updates) {
        Map<UUID, Marker> next = new HashMap<>();
        updates.forEach((uuid, update) -> {
            Marker old = MARKERS.get(uuid);
            Vec3 from = old == null ? update.position() : old.position(1.0F);
            next.put(uuid, new Marker(update.entityId(), uuid, update.hostile(), update.dimension(),
                    from, update.position(), update.height(), 0));
        });
        MARKERS.clear();
        MARKERS.putAll(next);
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null) {
            MARKERS.clear();
            return;
        }
        MARKERS.replaceAll((uuid, marker) -> marker.withAge(marker.age() + 1));
    }

    public static Iterable<Marker> markers() { return MARKERS.values(); }

    public record Update(int entityId, boolean hostile, ResourceLocation dimension,
                         Vec3 position, float height) { }

    public record Marker(int entityId, UUID uuid, boolean hostile, ResourceLocation dimension,
                         Vec3 from, Vec3 to, float height, int age) {
        public Vec3 position(float partialTick) {
            float progress = Math.min(1.0F, (age + partialTick) / 10.0F);
            return from.lerp(to, progress);
        }

        Marker withAge(int value) {
            return new Marker(entityId, uuid, hostile, dimension, from, to, height, value);
        }
    }
}
