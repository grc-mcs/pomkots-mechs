package grcmcs.minecraft.mods.pomkotsmechs.mission.support;

import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionInstance;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.MissionMarkerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.PilotDisposition;
import grcmcs.minecraft.mods.pomkotsmechs.cutscene.CutsceneCameraEntity;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MissionTargetMarkerService {
    public static final int SYNC_INTERVAL_TICKS = 10;
    public static final String HIDE_MARKER_TAG = "pomkots_mission_hide_marker";
    private static final float POMKOTS_MONSTER_EXTRA_HEIGHT = 1.5F;

    private MissionTargetMarkerService() { }

    public static void sync(MinecraftServer server, MissionInstance instance) {
        List<Target> targets = collect(server, instance);
        for (ServerPlayer player : onlineParticipants(server, instance)) send(player, targets);
    }

    public static void sync(ServerPlayer player, MissionInstance instance) {
        send(player, collect(player.server, instance));
    }

    public static void clear(Iterable<ServerPlayer> players) {
        for (ServerPlayer player : players) send(player, List.of());
    }

    private static List<Target> collect(MinecraftServer server, MissionInstance instance) {
        List<Target> targets = new ArrayList<>();
        for (var group : instance.trackedEntityGroups().values()) {
            for (UUID uuid : group) {
                Entity entity = find(server, uuid);
                // drop_itemには追従用MissionMarkerEntityもあるため、アイテム本体は二重表示しない。
                if (entity == null || entity.isRemoved()
                        || entity instanceof ItemEntity
                        || entity instanceof CutsceneCameraEntity
                        || entity.getTags().contains(HIDE_MARKER_TAG)) continue;
                targets.add(new Target(entity, entity instanceof Enemy
                        || PilotDisposition.isHostilePilot(entity)
                        || PilotDisposition.hasHostilePilot(entity)));
            }
        }
        return targets;
    }

    private static Entity find(MinecraftServer server, UUID uuid) {
        for (var level : server.getAllLevels()) {
            Entity entity = level.getEntity(uuid);
            if (entity != null) return entity;
        }
        return null;
    }

    private static List<ServerPlayer> onlineParticipants(MinecraftServer server, MissionInstance instance) {
        List<ServerPlayer> players = new ArrayList<>();
        for (UUID uuid : instance.participantIds()) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player != null) players.add(player);
        }
        return players;
    }

    private static void send(ServerPlayer player, List<Target> targets) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeVarInt(targets.size());
        for (Target target : targets) {
            Entity entity = target.entity();
            buf.writeVarInt(entity.getId());
            buf.writeUUID(entity.getUUID());
            buf.writeBoolean(target.hostile());
            buf.writeResourceLocation(entity.level().dimension().location());
            buf.writeDouble(displayX(entity));
            buf.writeDouble(entity.getY());
            buf.writeDouble(displayZ(entity));
            buf.writeFloat(displayHeight(entity));
        }
        NetworkManager.sendToPlayer(player, PomkotsMechs.id(PomkotsMechs.PACKET_MISSION_TARGET_MARKERS), buf);
    }

    private static float displayHeight(Entity entity) {
        if (entity instanceof MissionMarkerEntity marker) {
            return marker.markerMode() == MissionMarkerEntity.REACHED_AREA ? marker.maxY() : 2.5F;
        }
        if (entity instanceof GenericPomkotsMonster) {
            return entity.getBbHeight() + POMKOTS_MONSTER_EXTRA_HEIGHT;
        }
        return entity.getBbHeight();
    }

    private static double displayX(Entity entity) {
        if (entity instanceof MissionMarkerEntity marker
                && marker.markerMode() == MissionMarkerEntity.REACHED_AREA) {
            return entity.getX() + (marker.minX() + marker.maxX()) * 0.5D;
        }
        return entity.getX();
    }

    private static double displayZ(Entity entity) {
        if (entity instanceof MissionMarkerEntity marker
                && marker.markerMode() == MissionMarkerEntity.REACHED_AREA) {
            return entity.getZ() + (marker.minZ() + marker.maxZ()) * 0.5D;
        }
        return entity.getZ();
    }

    private record Target(Entity entity, boolean hostile) { }
}
