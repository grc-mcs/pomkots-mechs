package grcmcs.minecraft.mods.pomkotsmechs.util;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** Server-side trigger for the client electric spark emitter. */
public final class ServerElectricSparkEffect {
    public static final double DEFAULT_VIEW_DISTANCE = 150.0D;

    private ServerElectricSparkEffect() {
    }

    public static void spawnOverTime(ServerLevel level, BlockPos center, double radius, int count, double durationSeconds) {
        spawnOverTime(level, Vec3.atCenterOf(center), radius, count, durationSeconds);
    }

    /** Starts the effect for every player in this dimension within 150 blocks of its center. */
    public static void spawnOverTime(ServerLevel level, Vec3 center, double radius, int count, double durationSeconds) {
        spawnOverTime(level, center, radius, count, durationSeconds, DEFAULT_VIEW_DISTANCE);
    }

    /** Starts the effect for every player in this dimension within the supplied view distance. */
    public static void spawnOverTime(
            ServerLevel level,
            Vec3 center,
            double radius,
            int count,
            double durationSeconds,
            double viewDistance
    ) {
        if (radius < 0.0D || count <= 0 || durationSeconds <= 0.0D || viewDistance < 0.0D) {
            return;
        }

        double viewDistanceSqr = viewDistance * viewDistance;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(center) > viewDistanceSqr) {
                continue;
            }

            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeDouble(center.x);
            buffer.writeDouble(center.y);
            buffer.writeDouble(center.z);
            buffer.writeDouble(radius);
            buffer.writeVarInt(count);
            buffer.writeDouble(durationSeconds);
            NetworkManager.sendToPlayer(
                    player,
                    PomkotsMechs.id(PomkotsMechs.PACKET_ELECTRIC_SPARK_EMITTER),
                    buffer
            );
        }
    }
}
