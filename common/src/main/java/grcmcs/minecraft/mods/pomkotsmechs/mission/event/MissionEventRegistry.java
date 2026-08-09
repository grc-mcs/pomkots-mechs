package grcmcs.minecraft.mods.pomkotsmechs.mission.event;

import grcmcs.minecraft.mods.pomkotsmechs.mission.event.MissionPilotedMechSpawner;
import grcmcs.minecraft.mods.pomkotsmechs.mission.event.MissionPositionResolver;
import grcmcs.minecraft.mods.pomkotsmechs.mission.event.MissionSpawnContext;
import grcmcs.minecraft.mods.pomkotsmechs.mission.event.MissionSpawnSource;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionInstance;
import grcmcs.minecraft.mods.pomkotsmechs.mission.runtime.MissionSavedData;
import grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionChunkManager;
import grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionEntityCleanupPolicy;
import grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionPresentationService;
import grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionSpawnTrackingService;
import grcmcs.minecraft.mods.pomkotsmechs.mission.support.MissionTargetMarkerService;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.mission.definition.MissionDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.entity.misc.MissionMarkerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.TransportShipEntity;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItemStackHelper;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitInstance;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitModifier;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry.CircuitRegistries;
import grcmcs.minecraft.mods.pomkotsmechs.radio.RadioCommunicationService;
import grcmcs.minecraft.mods.pomkotsmechs.radio.RadioInterruptMode;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.BGMState;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.ServerBGMTracker;
import grcmcs.minecraft.mods.pomkotsmechs.cutscene.CutsceneDefinitionRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.cutscene.CutsceneService;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.levelgen.Heightmap;
import io.netty.buffer.Unpooled;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class MissionEventRegistry {
    private static final Map<ResourceLocation, Handler> HANDLERS = new HashMap<>();

    static {
        register("noop", context -> { });
        register("radio", MissionEventRegistry::radio);
        register("spawn_entities", MissionEventRegistry::spawnEntities);
        register("spawn_piloted_mech", MissionEventRegistry::spawnPilotedMech);
        register("activate_entities", MissionEventRegistry::activateEntities);
        register("teleport_players", MissionEventRegistry::teleportPlayers);
        register("drop_item", MissionEventRegistry::dropItem);
        register("command", MissionEventRegistry::command);
        register("mission_presentation", MissionEventRegistry::presentation);
        register("bgm", MissionEventRegistry::bgm);
        register("mission_effect", MissionEventRegistry::missionEffect);
        register("cutscene", MissionEventRegistry::cutscene);
        register("spawn_transport_ship", MissionEventRegistry::spawnTransportShip);
        register("transport_ship_control", MissionEventRegistry::controlTransportShip);
    }

    private MissionEventRegistry() {
    }

    public static void execute(Context context) throws Exception {
        Handler handler = HANDLERS.get(context.event().type());
        if (handler == null) throw new IllegalArgumentException("Unknown mission event: " + context.event().type());
        handler.execute(context);
    }

    private static void register(String path, Handler handler) {
        HANDLERS.put(PomkotsMechs.id(path), handler);
    }

    private static void radio(Context context) {
        JsonObject data = context.event().data();
        ResourceLocation message = new ResourceLocation(GsonHelper.getAsString(data, "message"));
        RadioInterruptMode mode = RadioInterruptMode.valueOf(
                GsonHelper.getAsString(data, "interrupt", "queue").toUpperCase(Locale.ROOT));
        if (!RadioCommunicationService.play(context.targets(GsonHelper.getAsString(data, "targets", "participants")), message, mode)) {
            throw new IllegalArgumentException("Unknown radio message: " + message);
        }
    }

    private static void spawnTransportShip(Context context) throws Exception {
        JsonObject data = context.event().data();
        String group = GsonHelper.getAsString(data, "group");
        MissionPositionResolver.ResolvedPosition spawn = context.resolve(
                GsonHelper.getAsJsonObject(data, "position"));
        JsonObject route = GsonHelper.getAsJsonObject(data, "route");
        JsonArray waypoints = GsonHelper.getAsJsonArray(route, "waypoints");
        if (waypoints.isEmpty()) throw new IllegalArgumentException("Transport ship route has no waypoints");

        List<TransportShipEntity.RoutePoint> points = new ArrayList<>();
        Set<String> waypointIds = new HashSet<>();
        for (JsonElement element : waypoints) {
            JsonObject waypoint = element.getAsJsonObject();
            String id = GsonHelper.getAsString(waypoint, "id");
            if (id.isBlank() || !waypointIds.add(id)) {
                throw new IllegalArgumentException("Blank or duplicate transport ship waypoint: " + id);
            }
            MissionPositionResolver.ResolvedPosition resolved;
            try {
                // Long-distance route points intentionally do not need to be in
                // the currently loaded rolling area.
                resolved = MissionPositionResolver.resolve(
                        context.server(), context.instance(), context.owner(),
                        GsonHelper.getAsJsonObject(waypoint, "position"));
            } catch (MissionPositionResolver.ResolutionException e) {
                throw new ExpectedEventFailure(e.playerMessage());
            }
            if (resolved.level() != spawn.level()) {
                throw new IllegalArgumentException("Transport ship route crosses dimensions");
            }
            points.add(new TransportShipEntity.RoutePoint(id, resolved.position()));
        }

        double speed = GsonHelper.getAsDouble(route, "speed", 0.35D);
        float turnSpeed = GsonHelper.getAsFloat(route, "turn_speed_degrees", 2.0F);
        double arrivalRadius = GsonHelper.getAsDouble(route, "arrival_radius", 4.0D);
        int stuckLimitTicks = GsonHelper.getAsInt(route, "stuck_limit_ticks", 200);
        double waterSurfaceOffset = GsonHelper.getAsDouble(route, "water_surface_offset", 0.0D);
        int waterSearchRange = GsonHelper.getAsInt(route, "water_search_range", 6);
        if (speed <= 0.0D || turnSpeed <= 0.0F || arrivalRadius <= 0.0D
                || stuckLimitTicks <= 0 || waterSearchRange <= 0) {
            throw new IllegalArgumentException("Transport ship route values must be positive");
        }

        TransportShipEntity ship = new TransportShipEntity(PomkotsMechs.TRANSPORT_SHIP.get(), spawn.level());
        ship.moveTo(spawn.position().x, spawn.position().y, spawn.position().z,
                GsonHelper.getAsFloat(data, "yaw", 0.0F), 0.0F);
        ship.configureRoute(points, speed, turnSpeed, arrivalRadius, stuckLimitTicks,
                waterSurfaceOffset, waterSearchRange);
        ship.addTag("pomkots_mission:" + context.instance().instanceId());
        ship.addTag("pomkots_mission_group:" + group);
        if (!GsonHelper.getAsBoolean(data, "show_marker", true)) {
            ship.addTag(MissionTargetMarkerService.HIDE_MARKER_TAG);
        }
        context.instance().markGroupSpawned(group);
        if (!spawn.level().addFreshEntity(ship)) {
            throw new IllegalStateException("Could not spawn transport ship");
        }
        context.instance().trackEntity(group, ship.getUUID(), cleanupPolicy(data), null);
        context.savedData().setDirty();
    }

    private static void controlTransportShip(Context context) {
        JsonObject data = context.event().data();
        String group = GsonHelper.getAsString(data, "group");
        String action = GsonHelper.getAsString(data, "action");
        int matched = 0;
        for (UUID id : context.instance().trackedEntityGroups().getOrDefault(group, Set.of())) {
            Entity entity = findLoadedEntity(context.server(), id);
            if (!(entity instanceof TransportShipEntity ship) || !ship.isAlive()) continue;
            switch (action) {
                case "move_to" -> {
                    String waypoint = GsonHelper.getAsString(data, "waypoint");
                    if (!ship.moveToWaypoint(waypoint)) {
                        throw new IllegalArgumentException("Unknown transport ship waypoint: " + waypoint);
                    }
                }
                case "stop" -> ship.stop();
                default -> throw new IllegalArgumentException("Unknown transport ship action: " + action);
            }
            matched++;
        }
        if (matched == 0) {
            throw new IllegalArgumentException("Transport ship group could not be resolved: " + group);
        }
    }

    private static void missionEffect(Context context) {
        JsonObject data = context.event().data();
        MissionPositionResolver.ResolvedPosition resolved = context.resolve(
                data.has("position") ? GsonHelper.getAsJsonObject(data, "position") : anchorPosition());
        String targets = GsonHelper.getAsString(data, "targets", "participants");
        double effectRadius = GsonHelper.getAsDouble(data, "effect_radius", 0.0D);
        if (effectRadius < 0.0D) throw new IllegalArgumentException("effect_radius must not be negative");

        JsonObject sound = data.has("sound") ? GsonHelper.getAsJsonObject(data, "sound") : null;
        ResourceLocation soundId = null;
        SoundSource soundSource = SoundSource.PLAYERS;
        float volume = 1.0F;
        float pitch = 1.0F;
        if (sound != null) {
            soundId = new ResourceLocation(GsonHelper.getAsString(sound, "id"));
            if (BuiltInRegistries.SOUND_EVENT.getOptional(soundId).isEmpty()) {
                throw new IllegalArgumentException("Unknown sound event: " + soundId);
            }
            try {
                soundSource = SoundSource.valueOf(
                        GsonHelper.getAsString(sound, "source", "players").toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown sound source: "
                        + GsonHelper.getAsString(sound, "source"));
            }
            volume = GsonHelper.getAsFloat(sound, "volume", 1.0F);
            pitch = GsonHelper.getAsFloat(sound, "pitch", 1.0F);
            if (volume < 0.0F || volume > 16.0F) {
                throw new IllegalArgumentException("sound.volume must be between 0 and 16");
            }
            if (pitch <= 0.0F || pitch > 4.0F) {
                throw new IllegalArgumentException("sound.pitch must be greater than 0 and at most 4");
            }
        }

        JsonObject shake = data.has("screen_shake")
                ? GsonHelper.getAsJsonObject(data, "screen_shake") : null;
        float intensity = shake == null ? 0.0F : GsonHelper.getAsFloat(shake, "intensity", 1.0F);
        int duration = shake == null ? 0 : GsonHelper.getAsInt(shake, "duration_ticks", 20);
        float frequency = shake == null ? 0.0F : GsonHelper.getAsFloat(shake, "frequency", 1.0F);
        if (shake != null && (intensity <= 0.0F || intensity > 20.0F)) {
            throw new IllegalArgumentException("screen_shake.intensity must be greater than 0 and at most 20");
        }
        if (shake != null && (duration <= 0 || duration > 1200)) {
            throw new IllegalArgumentException("screen_shake.duration_ticks must be between 1 and 1200");
        }
        if (shake != null && (frequency <= 0.0F || frequency > 10.0F)) {
            throw new IllegalArgumentException("screen_shake.frequency must be greater than 0 and at most 10");
        }
        if (sound == null && shake == null) {
            throw new IllegalArgumentException("mission_effect requires sound or screen_shake");
        }

        for (ServerPlayer player : context.targets(targets)) {
            if (player.serverLevel() != resolved.level()) continue;
            double distance = player.position().distanceTo(resolved.position());
            if (effectRadius > 0.0D && distance > effectRadius) continue;
            float playerIntensity = intensity;
            if (shake != null && effectRadius > 0.0D) {
                playerIntensity *= (float) Math.max(0.0D, 1.0D - distance / effectRadius);
                if (playerIntensity <= 0.0F) continue;
            }

            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeResourceLocation(resolved.level().dimension().location());
            buffer.writeDouble(resolved.position().x);
            buffer.writeDouble(resolved.position().y);
            buffer.writeDouble(resolved.position().z);
            buffer.writeBoolean(sound != null);
            if (sound != null) {
                buffer.writeResourceLocation(soundId);
                buffer.writeEnum(soundSource);
                buffer.writeFloat(volume);
                buffer.writeFloat(pitch);
            }
            buffer.writeBoolean(shake != null);
            if (shake != null) {
                buffer.writeFloat(playerIntensity);
                buffer.writeVarInt(duration);
                buffer.writeFloat(frequency);
            }
            NetworkManager.sendToPlayer(
                    player, PomkotsMechs.id(PomkotsMechs.PACKET_MISSION_EFFECT), buffer);
        }
    }

    private static void cutscene(Context context) throws Exception {
        JsonObject data = context.event().data();
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(data, "cutscene"));
        var definition = CutsceneDefinitionRegistry.get(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown cutscene: " + id));
        JsonObject targetData = GsonHelper.getAsJsonObject(data, "target");
        String type = GsonHelper.getAsString(targetData, "type");
        Entity targetEntity = null;
        MissionPositionResolver.ResolvedPosition resolved;
        if (type.equals("position")) {
            resolved = context.resolve(GsonHelper.getAsJsonObject(targetData, "position"));
        } else if (type.equals("entity_group")) {
            String group = GsonHelper.getAsString(targetData, "group");
            targetEntity = findFirstLivingEntity(context, group);
            if (targetEntity == null) throw new IllegalArgumentException("Cutscene target group is empty: " + group);
            resolved = new MissionPositionResolver.ResolvedPosition(
                    (ServerLevel) targetEntity.level(), targetEntity.getBoundingBox().getCenter());
        } else {
            throw new IllegalArgumentException("Unknown cutscene target type: " + type);
        }
        for (ServerPlayer viewer : context.targets(GsonHelper.getAsString(data, "targets", "participants"))) {
            if (viewer.serverLevel() == resolved.level()) {
                CutsceneService.play(viewer, definition, resolved.position(), targetEntity);
            }
        }
    }

    private static LivingEntity findFirstLivingEntity(Context context, String group) {
        for (UUID uuid : context.instance().trackedEntityGroups().getOrDefault(group, Set.of())) {
            for (ServerLevel level : context.server().getAllLevels()) {
                Entity entity = level.getEntity(uuid);
                if (entity instanceof LivingEntity living && living.isAlive()) return living;
            }
        }
        return null;
    }

    private static Entity findLoadedEntity(MinecraftServer server, UUID id) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(id);
            if (entity != null) return entity;
        }
        return null;
    }

    private static void spawnEntities(Context context) throws Exception {
        JsonObject data = context.event().data();
        String group = GsonHelper.getAsString(data, "group");
        MissionEntityCleanupPolicy cleanupPolicy = cleanupPolicy(data);
        context.instance().markGroupSpawned(group);
        JsonArray entries = GsonHelper.getAsJsonArray(data, "entries");
        Random random = new Random(context.instance().instanceId().getLeastSignificantBits() ^ context.instance().stageTick());
        for (JsonElement element : entries) {
            JsonObject entry = element.getAsJsonObject();
            List<SpawnType> candidates = spawnTypes(entry);
            int count = Math.max(1, GsonHelper.getAsInt(entry, "count", 1));
            List<MissionPositionResolver.ResolvedPosition> resolvedPositions =
                    MissionPositionResolver.resolveAllForSpawn(
                            context.server(), context.instance(), context.owner(),
                            GsonHelper.getAsJsonObject(entry, "position"));
            JsonObject spread = entry.has("spread") ? GsonHelper.getAsJsonObject(entry, "spread") : new JsonObject();
            for (MissionPositionResolver.ResolvedPosition resolved : resolvedPositions) {
            for (int i = 0; i < count; i++) {
                SpawnType selected = candidates.get(random.nextInt(candidates.size()));
                Entity entity = selected.type().create(resolved.level());
                if (entity == null) throw new IllegalStateException("Could not create entity: " + selected.id());
                if (entry.has("nbt")) applyEntityNbt(entity, GsonHelper.getAsString(entry, "nbt"));
                Vec3 pos = resolveSpawnPosition(context, entry, resolved, entity, spread, random);
                if (!MissionChunkManager.contains(context.instance(),
                        new MissionPositionResolver.ResolvedPosition(resolved.level(), pos))) {
                    String coordinates = String.format(Locale.ROOT, "%.1f, %.1f, %.1f", pos.x, pos.y, pos.z);
                    throw new ExpectedEventFailure(Component.translatable(
                            "mission.pomkotsmechs.error.spawn_outside_loaded_areas", coordinates));
                }
                entity.moveTo(pos.x, pos.y, pos.z, GsonHelper.getAsFloat(entry, "yaw", 0.0F), 0.0F);
                entity.setGlowingTag(GsonHelper.getAsBoolean(entry, "glowing", false));
                entity.addTag("pomkots_mission:" + context.instance().instanceId());
                entity.addTag("pomkots_mission_group:" + group);
                if (!GsonHelper.getAsBoolean(entry, "show_marker", true)) {
                    entity.addTag(MissionTargetMarkerService.HIDE_MARKER_TAG);
                }
                float healthModifier = GsonHelper.getAsFloat(entry, "health_modifier", 1.0F);
                float attackModifier = GsonHelper.getAsFloat(entry, "attack_modifier", 1.0F);
                if (healthModifier <= 0.0F || attackModifier <= 0.0F) {
                    throw new IllegalArgumentException("health_modifier and attack_modifier must be positive");
                }
                if (entity instanceof GenericPomkotsMonster pomkots) {
                    pomkots.setModifiers(healthModifier, attackModifier);
                }
                if (entity instanceof BaseBossEntity boss) {
                    boss.setSuppressHealthStageDrops(
                            GsonHelper.getAsBoolean(entry, "suppress_health_stage_drops", false));
                    boss.setSuppressDeathDrops(
                            GsonHelper.getAsBoolean(entry, "suppress_death_drops", false));
                }
                if (entity instanceof LivingEntity living && entry.has("max_health")) {
                    double maxHealth = GsonHelper.getAsDouble(entry, "max_health");
                    if (maxHealth <= 0.0D) throw new IllegalArgumentException("max_health must be positive");
                    var attribute = living.getAttribute(Attributes.MAX_HEALTH);
                    if (attribute == null) throw new IllegalArgumentException("Entity has no max health attribute: " + selected.id());
                    attribute.setBaseValue(maxHealth);
                    living.setHealth((float) maxHealth);
                }
                LivingEntity initialTarget = null;
                boolean raidBehavior = GsonHelper.getAsBoolean(entry, "enable_raid_behavior", false);
                if (entity instanceof Mob mob) {
                    if (GsonHelper.getAsBoolean(entry, "persistent", false)) mob.setPersistenceRequired();
                    boolean hasAttackObjective = entry.has("attack_objective");
                    String targetSpec = hasAttackObjective
                            ? GsonHelper.getAsString(entry, "attack_objective")
                            : GsonHelper.getAsString(entry, "target", "none");
                    initialTarget = resolveInitialTarget(context, targetSpec);
                    if (hasAttackObjective && initialTarget == null) {
                        throw new IllegalArgumentException("attack_objective could not be resolved: " + targetSpec);
                    }
                    if (initialTarget != null) mob.setTarget(initialTarget);
                    if (entity instanceof BaseSmallMonsterEntity smallMonster && initialTarget != null) {
                        smallMonster.setInRaid(true);
                        smallMonster.setMissionDefenseTargetId(initialTarget.getUUID());
                    }
                    if (raidBehavior) {
                        if (!(mob instanceof GenericPomkotsMonster pomkots)) {
                            throw new IllegalArgumentException("enable_raid_behavior requires GenericPomkotsMonster: " + selected.id());
                        }
                        if (initialTarget == null) {
                            throw new IllegalArgumentException("enable_raid_behavior requires attack_objective");
                        }
                        pomkots.setInRaid(true);
                        pomkots.setMissionDefenseTargetId(initialTarget.getUUID());
                    }
                }
                if (!resolved.level().addFreshEntity(entity)) {
                    throw new IllegalStateException("Could not add entity: " + selected.id());
                }
                context.instance().trackEntity(group, entity.getUUID(), cleanupPolicy, null);
                if (entry.has("spawn_source")) {
                    if (!(entity instanceof MissionSpawnSource source)) {
                        throw new IllegalArgumentException("spawn_source requires MissionSpawnSource: " + selected.id());
                    }
                    JsonObject sourceData = GsonHelper.getAsJsonObject(entry, "spawn_source");
                    String sourceGroup = GsonHelper.getAsString(sourceData, "source_group");
                    String objectiveGroup = GsonHelper.getAsString(sourceData, "objective_group");
                    MissionEntityCleanupPolicy objectivePolicy = cleanupPolicy(sourceData);
                    float childHealthModifier = GsonHelper.getAsFloat(
                            sourceData, "child_health_modifier", healthModifier);
                    float childAttackModifier = GsonHelper.getAsFloat(
                            sourceData, "child_attack_modifier", attackModifier);
                    if (childHealthModifier <= 0.0F || childAttackModifier <= 0.0F) {
                        throw new IllegalArgumentException(
                                "child_health_modifier and child_attack_modifier must be positive");
                    }
                    MissionSpawnContext spawnContext = new MissionSpawnContext(
                            context.instance().instanceId(), sourceGroup, objectiveGroup, objectivePolicy,
                            childHealthModifier, childAttackModifier, raidBehavior,
                            initialTarget == null ? null : initialTarget.getUUID());
                    MissionSpawnTrackingService.configureSource(
                            context.server(), source, entity, spawnContext);
                }
            }
            }
        }
    }

    private static void spawnPilotedMech(Context context) throws Exception {
        JsonObject data = context.event().data();
        String group = GsonHelper.getAsString(data, "group");
        MissionEntityCleanupPolicy cleanupPolicy = cleanupPolicy(data);
        MissionPositionResolver.ResolvedPosition resolved =
                context.resolve(GsonHelper.getAsJsonObject(data, "position"));
        float yaw = GsonHelper.getAsFloat(data, "yaw", 0.0F);

        JsonObject pilotData = GsonHelper.getAsJsonObject(data, "pilot");
        String licenseName = GsonHelper.getAsString(pilotData, "license", "novice");
        String roleName = GsonHelper.getAsString(pilotData, "role", "guardian");
        ItemStack license = pilotLicense(licenseName);
        ItemStack role = pilotRole(roleName);
        UUID masterId = resolveWingmanMaster(context, resolved, pilotData, roleName);
        String model = GsonHelper.getAsString(
                pilotData, "model", PomkotsMechs.id("geo/mech_pilot_wide.geo.json").toString());
        String texture = GsonHelper.getAsString(
                pilotData, "texture", PomkotsMechs.id("textures/entity/pilot/mech_pilot_wide.png").toString());

        JsonObject mechData = GsonHelper.getAsJsonObject(data, "mech");
        ItemStack[] equipment = new ItemStack[12];
        JsonObject parts = GsonHelper.getAsJsonObject(mechData, "parts");
        equipment[0] = requiredItem(parts, "head");
        equipment[1] = requiredItem(parts, "body");
        equipment[2] = requiredItem(parts, "arms");
        equipment[3] = requiredItem(parts, "legs");
        equipment[4] = requiredItem(parts, "generator");
        equipment[5] = requiredItem(parts, "booster");
        JsonObject weapons = mechData.has("weapons")
                ? GsonHelper.getAsJsonObject(mechData, "weapons") : new JsonObject();
        equipment[6] = optionalItem(weapons, "right_hand");
        equipment[7] = optionalItem(weapons, "left_hand");
        equipment[8] = optionalItem(weapons, "right_shoulder");
        equipment[9] = optionalItem(weapons, "left_shoulder");
        equipment[10] = optionalItem(weapons, "ext1");
        equipment[11] = optionalItem(weapons, "ext2");
        ItemStack[] additionalCircuits = additionalCircuits(mechData);

        MissionPilotedMechSpawner.Configuration configuration =
                new MissionPilotedMechSpawner.Configuration(
                        equipment,
                        GsonHelper.getAsInt(mechData, "texture_color", 0),
                        GsonHelper.getAsBoolean(mechData, "supply_consumables", true),
                        license,
                        role,
                        model,
                        texture,
                        masterId,
                        GsonHelper.getAsDouble(pilotData, "action_range", 0.0D),
                        additionalCircuits);
        MissionPilotedMechSpawner.Result result =
                MissionPilotedMechSpawner.spawn(resolved.level(), resolved.position(), yaw, configuration);

        if (data.has("attack_objective")) {
            String targetSpec = GsonHelper.getAsString(data, "attack_objective");
            LivingEntity initialTarget = resolveInitialTarget(context, targetSpec);
            if (initialTarget == null) {
                result.pilot().discard();
                result.mech().discard();
                throw new IllegalArgumentException("attack_objective could not be resolved: " + targetSpec);
            }
            result.mech().setNpcMissionObjective(initialTarget);
        }

        String instanceTag = "pomkots_mission:" + context.instance().instanceId();
        result.pilot().addTag(instanceTag);
        result.pilot().addTag("pomkots_mission_group:" + group);
        result.mech().addTag(instanceTag);

        String objectiveEntity = GsonHelper.getAsString(data, "objective_entity", "pilot");
        if (objectiveEntity.equals("mech")) {
            context.instance().trackEntity(group, result.mech().getUUID(), cleanupPolicy, null);
            context.instance().trackEntityForCleanup(
                    group, result.pilot().getUUID(), cleanupPolicy, null);
        } else if (objectiveEntity.equals("pilot")) {
            context.instance().trackEntity(group, result.pilot().getUUID(), cleanupPolicy, null);
            context.instance().trackEntityForCleanup(
                    group, result.mech().getUUID(), cleanupPolicy, null);
        } else {
            result.pilot().discard();
            result.mech().discard();
            throw new IllegalArgumentException(
                    "spawn_piloted_mech objective_entity must be pilot or mech: " + objectiveEntity);
        }
    }

    private static ItemStack[] additionalCircuits(JsonObject mechData) {
        if (!mechData.has("additional_circuits")) {
            return new ItemStack[0];
        }

        JsonArray definitions = GsonHelper.getAsJsonArray(mechData, "additional_circuits");
        List<ItemStack> circuits = new ArrayList<>();
        for (JsonElement element : definitions) {
            JsonObject definition = element.getAsJsonObject();
            var prefix = CircuitRegistries.PREFIXES.get(new ResourceLocation(
                    GsonHelper.getAsString(definition, "prefix")));
            CircuitRarity rarity = CircuitRarity.byName(
                    GsonHelper.getAsString(definition, "rarity"), CircuitRarity.COMMON);
            List<CircuitModifier> modifiers = new ArrayList<>();
            for (JsonElement skillElement : GsonHelper.getAsJsonArray(definition, "skills")) {
                var skill = CircuitRegistries.SKILLS.get(new ResourceLocation(skillElement.getAsString()));
                modifiers.add(new CircuitModifier(skill));
            }

            ItemStack stack = new ItemStack(PomkotsMechs.CIRCUIT_BASE.get());
            CircuitItemStackHelper.setIdentified(
                    stack, new CircuitInstance(prefix, rarity, List.copyOf(modifiers)));
            circuits.add(stack);
        }
        return circuits.toArray(ItemStack[]::new);
    }

    private static UUID resolveWingmanMaster(
            Context context,
            MissionPositionResolver.ResolvedPosition position,
            JsonObject pilotData,
            String role
    ) throws ExpectedEventFailure {
        if (!pilotData.has("master")) return null;
        if (!role.equalsIgnoreCase("wingman")) {
            throw new IllegalArgumentException("pilot.master is only valid for role=wingman");
        }
        String selector = GsonHelper.getAsString(pilotData, "master").toLowerCase(Locale.ROOT);
        ServerPlayer master;
        if (selector.equals("owner")) {
            master = context.owner();
        } else if (selector.equals("nearest_participant")) {
            master = context.targets("participants").stream()
                    .filter(player -> player.serverLevel() == position.level())
                    .min(java.util.Comparator.comparingDouble(
                            player -> player.distanceToSqr(position.position())))
                    .orElse(null);
        } else {
            throw new IllegalArgumentException("Unknown wingman master selector: " + selector);
        }
        if (master == null) {
            throw new ExpectedEventFailure(Component.translatable(
                    "mission.pomkotsmechs.error.wingman_master_not_found", selector));
        }
        return master.getUUID();
    }

    private static ItemStack pilotLicense(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "novice" -> new ItemStack(PomkotsMechs.PILOT_LICENSE_NOVICE_ITEM.get());
            case "intermediate" -> new ItemStack(PomkotsMechs.PILOT_LICENSE_INTERMEDIATE_ITEM.get());
            case "advanced" -> new ItemStack(PomkotsMechs.PILOT_LICENSE_ADVANCED_ITEM.get());
            case "legend" -> new ItemStack(PomkotsMechs.PILOT_LICENSE_LEGEND_ITEM.get());
            default -> throw new IllegalArgumentException("Unknown pilot license: " + name);
        };
    }

    private static ItemStack pilotRole(String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "guardian" -> new ItemStack(PomkotsMechs.PILOT_ROLE_GUARDIAN_ITEM.get());
            case "raider" -> new ItemStack(PomkotsMechs.PILOT_ROLE_RAIDER_ITEM.get());
            case "wingman" -> new ItemStack(PomkotsMechs.PILOT_ROLE_WINGMAN_ITEM.get());
            case "gladiator" -> new ItemStack(PomkotsMechs.PILOT_ROLE_GLADIATOR_ITEM.get());
            default -> throw new IllegalArgumentException("Unknown pilot role: " + name);
        };
    }

    private static ItemStack requiredItem(JsonObject object, String key) {
        if (!object.has(key)) throw new IllegalArgumentException("Missing piloted mech item: " + key);
        ItemStack stack = itemStack(object.get(key).getAsString());
        if (stack.isEmpty()) throw new IllegalArgumentException("Piloted mech item must not be empty: " + key);
        return stack;
    }

    private static ItemStack optionalItem(JsonObject object, String key) {
        return object.has(key) ? itemStack(object.get(key).getAsString()) : ItemStack.EMPTY;
    }

    private static ItemStack itemStack(String serializedId) {
        ResourceLocation id = new ResourceLocation(serializedId);
        Item item = BuiltInRegistries.ITEM.getOptional(id)
                .orElseThrow(() -> new IllegalArgumentException("Unknown item: " + id));
        if (item == net.minecraft.world.item.Items.AIR) return ItemStack.EMPTY;
        return new ItemStack(item);
    }

    private static void activateEntities(Context context) {
        JsonObject data = context.event().data();
        JsonObject selector = GsonHelper.getAsJsonObject(data, "selector");
        String baseClass = GsonHelper.getAsString(selector, "base_class", "pomkotsmechs:base_boss");
        if (!baseClass.equals("pomkotsmechs:base_boss")) {
            throw new IllegalArgumentException("Unsupported activate_entities base_class: " + baseClass);
        }
        String aiMode = GsonHelper.getAsString(selector, "ai_mode", "inactive");
        if (!aiMode.equals("inactive")) {
            throw new IllegalArgumentException("Unsupported activate_entities ai_mode: " + aiMode);
        }

        MissionPositionResolver.ResolvedPosition resolved =
                context.resolve(GsonHelper.getAsJsonObject(selector, "position"));
        double radius = GsonHelper.getAsDouble(selector, "radius", 40.0D);
        int limit = Math.max(1, GsonHelper.getAsInt(selector, "limit", 1));
        String group = GsonHelper.getAsString(data, "group");
        String onFailure = GsonHelper.getAsString(data, "on_failure", "restore");
        if (!onFailure.equals("restore")) {
            throw new IllegalArgumentException("activate_entities currently supports only on_failure=restore");
        }
        boolean preserveHealth = GsonHelper.getAsBoolean(data, "preserve_health_on_restore", true);
        float initialHate = GsonHelper.getAsFloat(data, "initial_hate", 50.0F);
        boolean activate = GsonHelper.getAsBoolean(data, "activate", true);

        AABB searchArea = AABB.ofSize(resolved.position(), radius * 2.0D, radius * 2.0D, radius * 2.0D);
        List<BaseBossEntity> bosses = resolved.level().getEntitiesOfClass(BaseBossEntity.class, searchArea,
                        boss -> boss.isAlive() && boss.getAiMode() == BaseBossEntity.AI_MODE_INACTIVE)
                .stream()
                .sorted(java.util.Comparator.comparingDouble(boss -> boss.distanceToSqr(resolved.position())))
                .limit(limit)
                .toList();
        if (bosses.isEmpty()) {
            throw new ExpectedEventFailure(Component.translatable(
                    "mission.pomkotsmechs.error.activate_boss_not_found"));
        }

        context.instance().markGroupSpawned(group);
        List<ServerPlayer> targets = context.targets(GsonHelper.getAsString(data, "targets", "participants"));
        for (BaseBossEntity boss : bosses) {
            CompoundTag restoreData = new CompoundTag();
            CompoundTag metadata = new CompoundTag();
            metadata.putString("Dimension", boss.level().dimension().location().toString());
            metadata.putDouble("X", boss.getX());
            metadata.putDouble("Y", boss.getY());
            metadata.putDouble("Z", boss.getZ());
            metadata.putFloat("Yaw", boss.getYRot());
            metadata.putFloat("Pitch", boss.getXRot());
            metadata.putBoolean("Activated", boss.isActivated());
            metadata.putInt("AiMode", boss.getAiMode());
            metadata.putBoolean("PreserveHealth", preserveHealth);
            restoreData.put("PomkotsMissionBossRestore", metadata);

            boss.addTag("pomkots_mission:" + context.instance().instanceId());
            boss.addTag("pomkots_mission_group:" + group);
            context.instance().trackEntity(
                    group, boss.getUUID(), MissionEntityCleanupPolicy.RESTORE_ON_FAILURE, restoreData);
            if (activate) {
                boss.boot();
                for (ServerPlayer target : targets) boss.addHateToEntity(target, initialHate);
            }
        }
    }

    private static Vec3 resolveSpawnPosition(
            Context context,
            JsonObject entry,
            MissionPositionResolver.ResolvedPosition resolved,
            Entity entity,
            JsonObject spread,
            Random random
    ) {
        JsonObject radialSpread = entry.has("radial_spread")
                ? GsonHelper.getAsJsonObject(entry, "radial_spread") : null;
        JsonObject placement = entry.has("placement")
                ? GsonHelper.getAsJsonObject(entry, "placement") : null;
        if (placement == null) {
            return applySurfaceSnap(entry, resolved.level(),
                    spreadPosition(random, resolved.position(), spread, radialSpread));
        }

        String type = GsonHelper.getAsString(placement, "type");
        boolean connectedAirSpace = type.equals("connected_air_space");
        boolean outdoorAirSpace = type.equals("outdoor_air_space");
        if (!connectedAirSpace && !outdoorAirSpace) {
            throw new IllegalArgumentException("Unknown spawn placement type: " + type);
        }
        int attempts = Math.max(1, GsonHelper.getAsInt(placement, "attempts", 20));
        JsonObject required = placement.has("required_space")
                ? GsonHelper.getAsJsonObject(placement, "required_space") : new JsonObject();
        double width = GsonHelper.getAsDouble(required, "x", Math.max(2.0D, entity.getBbWidth()));
        double height = GsonHelper.getAsDouble(required, "y", Math.max(2.0D, entity.getBbHeight()));
        double depth = GsonHelper.getAsDouble(required, "z", Math.max(2.0D, entity.getBbWidth()));
        for (int attempt = 0; attempt < attempts; attempt++) {
            Vec3 candidate = applySurfaceSnap(entry, resolved.level(),
                    spreadPosition(random, resolved.position(), spread, radialSpread));
            MissionPositionResolver.ResolvedPosition candidatePosition =
                    new MissionPositionResolver.ResolvedPosition(resolved.level(), candidate);
            if (!MissionChunkManager.contains(context.instance(), candidatePosition)) continue;

            if (connectedAirSpace) {
                HitResult hit = resolved.level().clip(new ClipContext(
                        candidate.add(0.0D, Math.min(1.0D, height * 0.5D), 0.0D),
                        resolved.position().add(0.0D, 1.5D, 0.0D),
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
                if (hit.getType() != HitResult.Type.MISS) continue;
            }

            if (outdoorAirSpace) {
                BlockPos skyCheck = BlockPos.containing(
                        candidate.x, candidate.y + height, candidate.z);
                if (!resolved.level().canSeeSky(skyCheck)) continue;
            }

            AABB requiredSpace = new AABB(
                    candidate.x - width * 0.5D, candidate.y, candidate.z - depth * 0.5D,
                    candidate.x + width * 0.5D, candidate.y + height, candidate.z + depth * 0.5D);
            if (resolved.level().noCollision(requiredSpace)) return candidate;
        }

        String fallback = GsonHelper.getAsString(placement, "fallback", "event_failure");
        if (fallback.equals("origin")) return resolved.position();
        if (!fallback.equals("event_failure")) {
            throw new IllegalArgumentException("Unknown connected_air_space fallback: " + fallback);
        }
        String errorKey = outdoorAirSpace
                ? "mission.pomkotsmechs.error.outdoor_spawn_not_found"
                : "mission.pomkotsmechs.error.connected_spawn_not_found";
        throw new ExpectedEventFailure(Component.translatable(errorKey, attempts));
    }

    private static Vec3 applySurfaceSnap(JsonObject entry, ServerLevel level, Vec3 pos) {
        if (!GsonHelper.getAsBoolean(entry, "snap_to_surface", false)) return pos;
        int surfaceY = level.getHeight(
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (int) Math.floor(pos.x), (int) Math.floor(pos.z));
        return new Vec3(pos.x, surfaceY, pos.z);
    }

    private static List<SpawnType> spawnTypes(JsonObject entry) {
        List<ResourceLocation> ids = new ArrayList<>();
        if (entry.has("entities")) {
            JsonArray array = GsonHelper.getAsJsonArray(entry, "entities");
            for (JsonElement element : array) ids.add(new ResourceLocation(element.getAsString()));
            if (ids.isEmpty()) throw new IllegalArgumentException("spawn_entities entries[].entities must not be empty");
        } else if (entry.has("entity")) {
            ids.add(new ResourceLocation(GsonHelper.getAsString(entry, "entity")));
        } else {
            throw new IllegalArgumentException("spawn_entities entry requires entity or entities");
        }

        List<SpawnType> types = new ArrayList<>(ids.size());
        for (ResourceLocation id : ids) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown entity: " + id));
            types.add(new SpawnType(id, type));
        }
        return types;
    }

    private static void teleportPlayers(Context context) {
        JsonObject data = context.event().data();
        MissionPositionResolver.ResolvedPosition resolved = context.resolve(GsonHelper.getAsJsonObject(data, "position"));
        float yaw = GsonHelper.getAsFloat(data, "yaw", 0.0F);
        float pitch = GsonHelper.getAsFloat(data, "pitch", 0.0F);
        Set<UUID> movedRoots = new HashSet<>();
        for (ServerPlayer player : context.targets(GsonHelper.getAsString(data, "targets", "participants"))) {
            Entity root = player.getRootVehicle();
            if (root == player) {
                teleportPlayer(player, resolved, yaw, pitch);
            } else if (movedRoots.add(root.getUUID())) {
                teleportVehicleWithPlayers(root, context.targets("participants"), resolved, yaw, pitch);
            } else if (player.serverLevel() != resolved.level()) {
                teleportPlayer(player, resolved, yaw, pitch);
            }
        }
    }

    private static void dropItem(Context context) throws Exception {
        JsonObject data = context.event().data();
        JsonObject itemJson = GsonHelper.getAsJsonObject(data, "item");
        ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(itemJson, "id"));
        Item item = BuiltInRegistries.ITEM.getOptional(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown item: " + itemId));
        ItemStack stack = new ItemStack(item, Math.max(1, GsonHelper.getAsInt(itemJson, "count", 1)));
        if (itemJson.has("nbt")) stack.setTag(TagParser.parseTag(GsonHelper.getAsString(itemJson, "nbt")));
        MissionPositionResolver.ResolvedPosition resolved = context.resolve(GsonHelper.getAsJsonObject(data, "position"));
        Vec3 pos = resolved.position();
        ItemEntity entity = new ItemEntity(resolved.level(), pos.x, pos.y, pos.z, stack);
        entity.setPickUpDelay(GsonHelper.getAsInt(data, "pickup_delay", 0));
        String group = GsonHelper.getAsString(data, "group");
        MissionEntityCleanupPolicy cleanupPolicy = cleanupPolicy(data);
        context.instance().markGroupSpawned(group);
        entity.addTag("pomkots_mission:" + context.instance().instanceId());
        entity.addTag("pomkots_mission_group:" + group);
        if (!resolved.level().addFreshEntity(entity)) throw new IllegalStateException("Could not drop item: " + itemId);
        context.instance().trackEntity(group, entity.getUUID(), cleanupPolicy, null);

        MissionMarkerEntity marker = new MissionMarkerEntity(PomkotsMechs.MISSION_MARKER.get(), resolved.level());
        marker.configureItemArrow(entity.getUUID());
        marker.setPos(pos.x, pos.y, pos.z);
        marker.addTag("pomkots_mission_marker:" + context.instance().instanceId());
        if (resolved.level().addFreshEntity(marker)) {
            context.instance().trackEntity("__marker_item_" + group, marker.getUUID());
        }
    }

    private static void command(Context context) {
        JsonObject data = context.event().data();
        String raw = GsonHelper.getAsString(data, "command");
        if (raw.startsWith("/")) raw = raw.substring(1);
        boolean ownerSource = GsonHelper.getAsString(data, "source", "server").equals("owner");
        CommandSourceStack source;
        if (ownerSource) {
            if (context.owner() == null) throw new IllegalArgumentException("Mission owner is offline");
            source = context.owner().createCommandSourceStack();
        } else {
            MissionPositionResolver.ResolvedPosition anchor = context.resolve(anchorPosition());
            source = context.server().createCommandSourceStack().withLevel(anchor.level()).withPosition(anchor.position());
        }
        source = source.withPermission(GsonHelper.getAsInt(data, "permission", 4));
        if (GsonHelper.getAsBoolean(data, "suppress_output", true)) source = source.withSuppressedOutput();
        context.server().getCommands().performPrefixedCommand(source, raw);
    }

    private static void presentation(Context context) {
        JsonObject data = context.event().data();
        Component title = component(data, "title", Component.empty());
        Component message = component(data, "message", Component.empty());
        MissionPresentationService.show(
                context.targets(GsonHelper.getAsString(data, "targets", "participants")),
                GsonHelper.getAsString(data, "style", "stage_start"), title, message,
                GsonHelper.getAsInt(data, "duration_ticks", 60));
    }

    private static void bgm(Context context) {
        JsonObject data = context.event().data();
        List<ServerPlayer> targets = context.targets(GsonHelper.getAsString(data, "targets", "participants"));
        if (GsonHelper.getAsString(data, "action", "set").equals("unset")) {
            targets.forEach(ServerBGMTracker::forceUnsetBGMState);
            return;
        }
        BGMState state = BGMState.valueOf(GsonHelper.getAsString(data, "state").toUpperCase(Locale.ROOT));
        targets.forEach(player -> ServerBGMTracker.forceSetBGMState(player, state));
    }

    private static Component component(JsonObject data, String key, Component fallback) {
        if (!data.has(key)) return fallback;
        Component value = Component.Serializer.fromJson(data.get(key));
        return value == null ? fallback : value;
    }

    /** ミッション定義やランダム配置によって通常起こり得る、プレイヤー通知対象の失敗。 */
    public static final class ExpectedEventFailure extends RuntimeException {
        private final Component playerMessage;

        public ExpectedEventFailure(Component playerMessage) {
            super(playerMessage.getString());
            this.playerMessage = playerMessage;
        }

        public Component playerMessage() {
            return playerMessage;
        }
    }

    private static void applyEntityNbt(Entity entity, String snbt) throws Exception {
        CompoundTag safe = TagParser.parseTag(snbt);
        safe.remove("UUID");
        safe.remove("UUIDMost");
        safe.remove("UUIDLeast");
        safe.remove("Pos");
        safe.remove("Dimension");
        CompoundTag base = new CompoundTag();
        entity.saveWithoutId(base);
        base.merge(safe);
        entity.load(base);
    }

    private static void teleportPlayer(ServerPlayer player, MissionPositionResolver.ResolvedPosition target,
                                       float yaw, float pitch) {
        Vec3 pos = target.position();
        player.teleportTo(target.level(), pos.x, pos.y, pos.z, yaw, pitch);
    }

    private static void teleportVehicleWithPlayers(Entity root, List<ServerPlayer> missionPlayers,
                                                    MissionPositionResolver.ResolvedPosition target,
                                                    float yaw, float pitch) {
        Vec3 pos = target.position();
        if (root.level() == target.level()) {
            root.teleportTo(pos.x, pos.y, pos.z);
            root.setYRot(yaw);
            root.setXRot(pitch);
            return;
        }

        List<ServerPlayer> riders = missionPlayers.stream()
                .filter(player -> player.getRootVehicle() == root)
                .toList();
        riders.forEach(Entity::stopRiding);
        Entity moved = root.changeDimension(target.level());
        if (moved == null) {
            riders.forEach(player -> teleportPlayer(player, target, yaw, pitch));
            throw new IllegalStateException("Could not teleport mission vehicle across dimensions");
        }
        moved.teleportTo(pos.x, pos.y, pos.z);
        moved.setYRot(yaw);
        moved.setXRot(pitch);
        for (ServerPlayer rider : riders) {
            teleportPlayer(rider, target, yaw, pitch);
            rider.startRiding(moved, true);
        }
    }

    private static double centered(Random random, JsonObject spread, String axis) {
        double size = GsonHelper.getAsDouble(spread, axis, 0.0D);
        return size == 0.0D ? 0.0D : (random.nextDouble() - 0.5D) * size;
    }

    private static Vec3 spreadPosition(Random random, Vec3 origin, JsonObject spread, JsonObject radialSpread) {
        double y = centered(random, spread, "y");
        if (radialSpread == null) {
            return origin.add(centered(random, spread, "x"), y, centered(random, spread, "z"));
        }
        double minRadius = Math.max(0.0D, GsonHelper.getAsDouble(radialSpread, "min_radius", 0.0D));
        double maxRadius = Math.max(minRadius, GsonHelper.getAsDouble(radialSpread, "max_radius"));
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = minRadius + random.nextDouble() * (maxRadius - minRadius);
        return origin.add(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
    }

    private static LivingEntity resolveInitialTarget(Context context, String target) {
        if (target.equals("owner")) return context.owner();
        if (!target.startsWith("group:")) return null;
        String group = target.substring("group:".length());
        for (UUID id : context.instance().trackedEntityGroups().getOrDefault(group, Set.of())) {
            for (var level : context.server().getAllLevels()) {
                Entity entity = level.getEntity(id);
                if (entity instanceof LivingEntity living && living.isAlive()) return living;
            }
        }
        return null;
    }

    private static JsonObject anchorPosition() {
        JsonObject position = new JsonObject();
        position.addProperty("type", "anchor");
        return position;
    }

    private static MissionEntityCleanupPolicy cleanupPolicy(JsonObject data) {
        String value = GsonHelper.getAsString(data, "cleanup_policy", "discard");
        try {
            return MissionEntityCleanupPolicy.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown mission cleanup_policy: " + value);
        }
    }

    private record SpawnType(ResourceLocation id, EntityType<?> type) { }

    @FunctionalInterface
    private interface Handler {
        void execute(Context context) throws Exception;
    }

    public record Context(
            MinecraftServer server,
            MissionSavedData savedData,
            MissionInstance instance,
            MissionDefinition.Event event,
            ServerPlayer owner
    ) {
        public MissionPositionResolver.ResolvedPosition resolve(JsonObject position) throws ExpectedEventFailure {
            MissionPositionResolver.ResolvedPosition resolved;
            try {
                resolved = MissionPositionResolver.resolve(server, instance, owner, position);
            } catch (MissionPositionResolver.ResolutionException e) {
                throw new ExpectedEventFailure(e.playerMessage());
            }
            if (!MissionChunkManager.contains(instance, resolved)) {
                throw new ExpectedEventFailure(Component.translatable(
                        "mission.pomkotsmechs.error.position_outside_loaded_areas", resolved.position().toString()));
            }
            return resolved;
        }

        public List<ServerPlayer> targets(String target) {
            List<ServerPlayer> players = new ArrayList<>();
            if (target.equals("owner")) {
                if (owner != null) players.add(owner);
                return players;
            }
            for (var id : instance.participantIds()) {
                ServerPlayer player = server.getPlayerList().getPlayer(id);
                if (player != null) players.add(player);
            }
            return players;
        }
    }
}
