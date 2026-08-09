package grcmcs.minecraft.mods.pomkotsmechs.mission.definition;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record MissionDefinition(
        ResourceLocation id,
        int version,
        Component displayName,
        Settings settings,
        List<LoadedArea> loadedAreas,
        DynamicLoadedArea dynamicLoadedArea,
        List<Stage> stages,
        JsonObject presentation,
        List<String> successCommands,
        List<String> failureCommands
) {
    public MissionDefinition {
        stages = List.copyOf(stages);
        loadedAreas = List.copyOf(loadedAreas);
        presentation = presentation.deepCopy();
        successCommands = List.copyOf(successCommands);
        failureCommands = List.copyOf(failureCommands);
    }

    public record LoadedArea(JsonObject from, JsonObject to) {
        public LoadedArea { from = from.deepCopy(); to = to.deepCopy(); }
    }

    public record DynamicLoadedArea(String followGroup, int radiusChunks, int forwardChunks) {
    }

    public record Settings(
            boolean allowConcurrentInstances,
            String ownerDisconnect,
            String participantDisconnect,
            String ownerDeath,
            String participantDeath,
            String participantMode
    ) {
    }

    public record Stage(
            String id,
            Component objective,
            JsonObject hudProgress,
            JsonObject presentation,
            boolean cleanupTrackedEntities,
            List<String> preservedTrackedGroups,
            int successGraceTicks,
            int timeLimitTicks,
            String timeLimitResult,
            List<Event> events,
            List<TriggeredSequence> sequences,
            Condition success,
            Condition failure
    ) {
        public Stage {
            events = List.copyOf(events);
            sequences = List.copyOf(sequences);
            preservedTrackedGroups = List.copyOf(preservedTrackedGroups);
            hudProgress = hudProgress.deepCopy();
            presentation = presentation.deepCopy();
        }
    }

    public record Event(String id, int atTick, ResourceLocation type, JsonObject data) {
    }

    public record TriggeredSequence(String id, Condition trigger, List<SequenceEvent> events) {
        public TriggeredSequence {
            events = List.copyOf(events);
        }
    }

    public record SequenceEvent(String id, int afterTicks, ResourceLocation type, JsonObject data) {
        public SequenceEvent {
            data = data.deepCopy();
        }
    }

    public record Condition(ResourceLocation type, int ticks, List<Condition> children, JsonObject data) {
        public Condition {
            children = List.copyOf(children);
            data = data.deepCopy();
        }
    }
}
