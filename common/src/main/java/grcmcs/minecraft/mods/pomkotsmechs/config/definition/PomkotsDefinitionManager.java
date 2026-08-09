package grcmcs.minecraft.mods.pomkotsmechs.config.definition;

import dev.architectury.platform.Platform;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.cutscene.CutsceneDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.cutscene.CutsceneDefinitionRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.mission.definition.MissionDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.mission.definition.MissionDefinitionRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionManager;
import grcmcs.minecraft.mods.pomkotsmechs.radio.RadioMessage;
import grcmcs.minecraft.mods.pomkotsmechs.radio.RadioMessageRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

public final class PomkotsDefinitionManager {
    private static final String MISSION_RESOURCE_DIRECTORY = "pomkots_missions";
    private static final String CUTSCENE_RESOURCE_DIRECTORY = "pomkots_cutscenes";
    private static final String RADIO_RESOURCE_DIRECTORY = "pomkots_radio";
    private static final ResourceLocation RADIO_RESOURCE =
            new ResourceLocation(PomkotsMechs.MODID, "pomkots_radio_messages.json");

    private static final Path ROOT = Platform.getConfigFolder().resolve(PomkotsMechs.MODID);
    private static final Path MISSIONS = ROOT.resolve("missions");
    private static final Path CUTSCENES = ROOT.resolve("cutscenes");
    private static final Path RADIO = ROOT.resolve("radio");
    private static final Path INITIALIZED_MARKER = ROOT.resolve(".definitions_initialized");
    private static boolean initialized;

    private PomkotsDefinitionManager() {
    }

    public static synchronized void initialize(ResourceManager resourceManager) {
        // Datapack /reload also invokes the loader. External definitions are
        // intentionally reloaded only through the guarded mission command.
        if (initialized) return;
        try {
            seedDefaultsOnce(resourceManager);
            ReloadResult result = reloadFromDisk();
            initialized = true;
            PomkotsMechs.LOGGER.info(
                    "External definitions loaded from {}: {} missions, {} cutscenes, {} radio messages",
                    ROOT.toAbsolutePath(), result.missions(), result.cutscenes(), result.radioMessages());
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to initialize external definitions from {}",
                    ROOT.toAbsolutePath(), e);
        }
    }

    public static synchronized ReloadResult reload(MinecraftServer server) throws IOException {
        int activeMissions = MissionManager.activeInstanceCount(server);
        if (activeMissions > 0) {
            throw new IllegalStateException(
                    "Definitions cannot be reloaded while " + activeMissions + " mission instance(s) are active");
        }
        return reloadFromDisk();
    }

    public static Path root() {
        return ROOT;
    }

    private static ReloadResult reloadFromDisk() throws IOException {
        Map<ResourceLocation, MissionDefinition> missions = MissionDefinitionRegistry.load(MISSIONS);
        Map<ResourceLocation, CutsceneDefinition> cutscenes = CutsceneDefinitionRegistry.load(CUTSCENES);
        Map<ResourceLocation, RadioMessage> radioMessages = RadioMessageRegistry.load(RADIO);

        // Nothing is published until every file in all three definition sets parsed successfully.
        MissionDefinitionRegistry.replace(missions);
        CutsceneDefinitionRegistry.replace(cutscenes);
        RadioMessageRegistry.replace(radioMessages);
        return new ReloadResult(missions.size(), cutscenes.size(), radioMessages.size());
    }

    private static void seedDefaultsOnce(ResourceManager resourceManager) throws IOException {
        Files.createDirectories(MISSIONS);
        Files.createDirectories(CUTSCENES);
        Files.createDirectories(RADIO);
        Set<String> knownSeeds = Files.exists(INITIALIZED_MARKER)
                ? new HashSet<>(Files.readAllLines(INITIALIZED_MARKER)) : new HashSet<>();
        Set<String> availableSeeds = new HashSet<>();

        int missionSeeds = copyDirectoryDefaults(
                resourceManager, MISSION_RESOURCE_DIRECTORY, MISSIONS, "missions/", knownSeeds, availableSeeds);
        int cutsceneSeeds = copyDirectoryDefaults(
                resourceManager, CUTSCENE_RESOURCE_DIRECTORY, CUTSCENES, "cutscenes/", knownSeeds, availableSeeds);
        resourceManager.getResource(RADIO_RESOURCE).ifPresent(radioSeed -> {
            try {
                copyNewSeedIfMissing(
                        radioSeed, RADIO.resolve("messages.json"),
                        "radio/messages.json", knownSeeds, availableSeeds);
            } catch (IOException e) {
                throw new DefinitionSeedException(e);
            }
        });
        int radioSeeds = copyDirectoryDefaults(
                resourceManager, RADIO_RESOURCE_DIRECTORY, RADIO, "radio/", knownSeeds, availableSeeds);
        if (missionSeeds == 0 || cutsceneSeeds == 0 || radioSeeds == 0) {
            throw new IOException("Bundled definition seeds were not available from the server resource manager");
        }

        Files.write(
                INITIALIZED_MARKER,
                availableSeeds.stream().sorted().toList(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }

    private static int copyDirectoryDefaults(
            ResourceManager resourceManager,
            String resourceDirectory,
            Path targetDirectory,
            String seedPrefix,
            Set<String> knownSeeds,
            Set<String> availableSeeds
    ) throws IOException {
        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                resourceDirectory,
                id -> id.getNamespace().equals(PomkotsMechs.MODID) && id.getPath().endsWith(".json"));
        String prefix = resourceDirectory + "/";
        int seeds = 0;
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            String resourcePath = entry.getKey().getPath();
            if (!resourcePath.startsWith(prefix)) continue;
            Path target = targetDirectory.resolve(resourcePath.substring(prefix.length())).normalize();
            if (!target.startsWith(targetDirectory.normalize())) {
                throw new IOException("Definition seed path escaped target directory: " + resourcePath);
            }
            copyNewSeedIfMissing(
                    entry.getValue(), target, seedPrefix + resourcePath.substring(prefix.length()),
                    knownSeeds, availableSeeds);
            seeds++;
        }
        return seeds;
    }

    private static void copyIfMissing(Resource resource, Path target) throws IOException {
        if (Files.exists(target)) return;
        Files.createDirectories(target.getParent());
        try (var input = resource.open()) {
            Files.copy(input, target);
        }
    }

    private static void copyNewSeedIfMissing(
            Resource resource,
            Path target,
            String seedId,
            Set<String> knownSeeds,
            Set<String> availableSeeds
    ) throws IOException {
        availableSeeds.add(seedId);
        if (!knownSeeds.contains(seedId)) {
            copyIfMissing(resource, target);
        }
    }

    public record ReloadResult(int missions, int cutscenes, int radioMessages) {
    }

    private static final class DefinitionSeedException extends RuntimeException {
        private DefinitionSeedException(IOException cause) {
            super(cause);
        }
    }

}
