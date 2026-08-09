package grcmcs.minecraft.mods.pomkotsmechs.mission.runtime;

import java.util.UUID;

public record MissionStartResult(boolean success, UUID instanceId, String reason) {
    public static MissionStartResult started(UUID instanceId) {
        return new MissionStartResult(true, instanceId, "");
    }

    public static MissionStartResult failed(String reason) {
        return new MissionStartResult(false, null, reason);
    }
}
