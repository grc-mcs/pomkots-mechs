package grcmcs.minecraft.mods.pomkotsmechs.mission.support;

import grcmcs.minecraft.mods.pomkotsmechs.mission.definition.MissionDefinition;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public final class MissionParticipantResolver {
    private MissionParticipantResolver() {
    }

    public static Set<ServerPlayer> resolve(ServerPlayer owner, MissionDefinition definition) {
        Set<ServerPlayer> participants = new LinkedHashSet<>();
        participants.add(owner);
        if (definition.settings().participantMode().equals("team") && owner.getTeam() != null) {
            for (ServerPlayer player : owner.server.getPlayerList().getPlayers()) {
                if (owner.getTeam().equals(player.getTeam())) participants.add(player);
            }
        }
        return participants;
    }

    public static Set<ServerPlayer> explicit(ServerPlayer owner, Collection<ServerPlayer> selected) {
        Set<ServerPlayer> participants = new LinkedHashSet<>(selected);
        participants.add(owner);
        return participants;
    }
}
