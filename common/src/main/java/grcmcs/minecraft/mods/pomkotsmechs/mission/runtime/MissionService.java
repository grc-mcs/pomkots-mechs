package grcmcs.minecraft.mods.pomkotsmechs.mission.runtime;

import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionManager;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionStartResult;
import grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionParticipantResolver;

import grcmcs.minecraft.mods.pomkotsmechs.mission.definition.MissionDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.mission.definition.MissionDefinitionRegistry;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public final class MissionService {
    private MissionService() {
    }

    public static MissionStartResult start(ServerPlayer owner, ResourceLocation missionId) {
        MissionDefinition definition = MissionDefinitionRegistry.get(missionId).orElse(null);
        if (definition == null) return MissionStartResult.failed("Unknown mission: " + missionId);
        return start(
                owner,
                definition,
                GlobalPos.of(owner.serverLevel().dimension(), owner.blockPosition()),
                MissionParticipantResolver.resolve(owner, definition),
                null,
                null
        );
    }

    public static MissionStartResult start(
            ServerPlayer owner,
            MissionDefinition definition,
            GlobalPos anchor,
            Collection<ServerPlayer> participants,
            List<String> successOverride,
            List<String> failureOverride
    ) {
        return MissionManager.start(
                owner.server,
                owner,
                definition,
                anchor,
                MissionParticipantResolver.explicit(owner, participants),
                successOverride,
                failureOverride
        );
    }
}
