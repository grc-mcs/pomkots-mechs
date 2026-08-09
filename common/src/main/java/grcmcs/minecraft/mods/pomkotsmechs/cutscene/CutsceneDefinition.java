package grcmcs.minecraft.mods.pomkotsmechs.cutscene;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record CutsceneDefinition(ResourceLocation id, String type, int durationTicks, double radius, double height,
                                 double angularSpeedDegrees, boolean clockwise, List<LinearSession> sessions,
                                 boolean relativeToTargetYaw, AerialApproach aerialApproach,
                                 DirectApproach directApproach, boolean startFade) {
    public record LinearSession(Vec3 from, Vec3 to, int durationTicks, boolean trackTarget) {
    }

    public record AerialApproach(double ascentHeight, int ascentDurationTicks, int turnDurationTicks,
                                 int approachDurationTicks, double approachDistance, double targetHeight) {
    }

    public record DirectApproach(int durationTicks, int rotationDurationTicks, int movementDelayTicks,
                                 double approachDistance, double targetHeight) {
    }
}
