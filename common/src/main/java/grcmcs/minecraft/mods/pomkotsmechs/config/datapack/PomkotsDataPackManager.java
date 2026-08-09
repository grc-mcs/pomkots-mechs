package grcmcs.minecraft.mods.pomkotsmechs.config.datapack;

import grcmcs.minecraft.mods.pomkotsmechs.config.definition.PomkotsDefinitionManager;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.EventDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.RaidDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.WaveDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitSkill;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.data.CircuitSkillJsonParser;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry.CircuitRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static net.minecraft.util.datafix.fixes.BlockEntitySignTextStrictJsonFix.GSON;

public class PomkotsDataPackManager {
    private static final PomkotsDataPackManager singleton = new PomkotsDataPackManager();
    public static PomkotsDataPackManager getInstance() {
        return singleton;
    }

    private final PomkotsDataPack dataPackServer = new PomkotsDataPack();
    private PomkotsDataPack dataPackClient = new PomkotsDataPack();

    public PomkotsDataPack getDataPack() {
        if (Platform.getEnvironment() == Env.SERVER) {
            return dataPackServer;
        } else {
            return dataPackClient;
        }
    }

    public void loadDataPack(ResourceManager manager) {
        dataPackServer.reset();

        loadAllPartsData(manager);
        loadAllEnemyData(manager);
        loadAllRaidData(manager);
        loadAllChestData(manager);
        loadAllTraderData(manager);
        loadAllFighterData(manager);
        loadAllAssetsData(manager);
        loadAllSkillData(manager);
        PomkotsDefinitionManager.initialize(manager);

        if (Platform.getEnvironment() == Env.CLIENT && !dataPackServer.isEmpty()) {
            dataPackClient = dataPackServer;
        }

        PomkotsMechs.LOGGER.info("Data Pack loaded");
    }

    private void loadResources(
            ResourceLocation path,
            ResourceManager manager,
            Consumer<Resource> handler
    ) {
        List<Resource> resources;

        try {
            resources = manager.getResourceStack(path);
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to load resource stack: {}", path, e);
            return;
        }

        resources.sort((a, b) -> {
            String idA = a.source().packId();
            String idB = b.source().packId();

            int prioA = getPriority(idA);
            int prioB = getPriority(idB);

            return Integer.compare(prioA, prioB);
        });

        // 共通ループ
        for (Resource resource : resources) {
            try {
                handler.accept(resource);
            } catch (Exception e) {
                PomkotsMechs.LOGGER.error("Failed to handle resource: {}", resource, e);
            }
        }
    }

    private int getPriority(String packId) {
        if (packId.equals(PomkotsMechs.MODID)) return 0; // 本体（最初）
        if (packId.contains(PomkotsMechs.MODID)) return 1;           // addon
        return 2;                                         // その他（datapack）
    }

    private void loadAllPartsData(ResourceManager manager) {
        ResourceLocation path = new ResourceLocation(PomkotsMechs.MODID, "parts.json");

        loadResources(path, manager, resource -> {
            try (InputStream stream = resource.open()) {

                JsonObject json = JsonParser
                        .parseReader(new InputStreamReader(stream))
                        .getAsJsonObject();

                if (json != null) {
                    loadRootPartsData(json);
                } else {
                    PomkotsMechs.LOGGER.error("No parts on a parts pack: {}", resource);
                }

            } catch (IOException e) {
                PomkotsMechs.LOGGER.error("Failed to load a parts pack: {}", resource, e);
            }
        });
    }

    private void loadRootPartsData(JsonObject json) {
        var root = json.get("mech_parts").getAsJsonObject();
        if (root == null) {
            return;
        }

        for (var item: root.asMap().entrySet()) {
            loadPartsData(item.getValue(), item.getKey());
        }
    }

    private void loadPartsData(JsonElement itemEle, String itemName) {
        var typeEle = itemEle.getAsJsonObject().get("type");

        if (typeEle != null && "parts".equals(typeEle.getAsString())) {
            var obj = itemEle.getAsJsonObject();

            registerPartsDataSingle(itemName + "head", obj.get("head"));
            registerPartsDataSingle(itemName + "body", obj.get("body"));
            registerPartsDataSingle(itemName + "arm", obj.get("arm"));
            registerPartsDataSingle(itemName + "legs", obj.get("legs"));

        } else {
            registerPartsDataSingle(itemName, itemEle);
        }
    }

    private void registerPartsDataSingle(String itemName, JsonElement itemRootEle) {
        if (itemRootEle == null) {
            return;
        }

        var itemRoot = itemRootEle.getAsJsonObject();

        PomkotsDataPack.PartsData data = new PomkotsDataPack.PartsData();
        data.id = itemName;
        data.weight = getInt(itemRoot, "weight");
        data.description = getString(itemRoot, "description");

        var levelArrayEle = itemRoot.get("level_param");

        if (levelArrayEle != null) {
            var levelArray = levelArrayEle.getAsJsonArray();

            for (var levelParamsEle: levelArray.asList()) {
                var levelParamsObj = levelParamsEle.getAsJsonObject();
                var levelData = new PomkotsDataPack.LevelData();

                levelData.durability = getInt(levelParamsObj, "durability");
                levelData.maxWeight = getInt(levelParamsObj, "max_weight");
                levelData.speedModifier = getFloat(levelParamsObj, "speed_modifier");
                levelData.jumpModifier = getFloat(levelParamsObj, "jump_modifier");
                levelData.damage = getFloat(levelParamsObj, "damage");
                levelData.missileMaxNum = getInt(levelParamsObj, "missile_max_num");
                levelData.missileLockInterval = getFloat(levelParamsObj, "missile_lock_interval");
                levelData.maxEnergy = getInt(levelParamsObj, "max_energy");
                levelData.energyChargePerTick = getFloat(levelParamsObj, "energy_charge_per_tick");
                levelData.workSecPerFuel = getInt(levelParamsObj, "work_sec_per_fuel");
                levelData.energyConsumeEvasion = getInt(levelParamsObj, "energy_consume_evasion");
                levelData.energyConsumeVertical = getInt(levelParamsObj, "energy_consume_vertical");
                levelData.speedModifierEvasion = getFloat(levelParamsObj, "speed_modifier_evasion");
                levelData.speedModifierVertical = getFloat(levelParamsObj, "speed_modifier_vertical");
                levelData.bulletsPerMagazine = getInt(levelParamsObj, "bullets_per_magazine");
                levelData.energy = getInt(levelParamsObj, "energy");
                levelData.energyConsumePerTick = getInt(levelParamsObj, "energy_consume_per_tick");

                data.levels.add(levelData);
            }
        }

        var recipeArrayEle = itemRoot.get("recipes");
        if (recipeArrayEle != null) {
            var recipeArray = recipeArrayEle.getAsJsonArray();

            for (var recipeEle: recipeArray.asList()) {
                var recipe = recipeEle.getAsJsonObject().asMap();

                List<PomkotsDataPack.SerializablePair<String, Integer>> rec = new ArrayList<>();
                for (var material: recipe.entrySet()) {
                    rec.add(new PomkotsDataPack.SerializablePair<String, Integer>(material.getKey(), material.getValue().getAsInt()));
                }
                data.recipes.add(rec);
            }
        }

        dataPackServer.addPartsData(itemName, data);
    }

    private void loadAllEnemyData(ResourceManager manager) {
        ResourceLocation path = new ResourceLocation(PomkotsMechs.MODID, "enemies.json");

        loadResources(path, manager, resource -> {
            try (InputStream stream = resource.open()) {
                JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                if (json != null) {
                    loadRootEnemyData(json);
                } else {
                    PomkotsMechs.LOGGER.error("No enemy on a enemies pack:" + resource);
                }
            } catch (IOException e) {
                PomkotsMechs.LOGGER.error("Failed to load a enemies pack:" + resource, e);
            }
        });
    }

    private void loadRootEnemyData(JsonObject json) {
        var root = json.get("enemy_data").getAsJsonObject();
        if (root == null) {
            return;
        }

        for (var enemy: root.asMap().entrySet()) {
            loadEnemyData(enemy.getValue(), enemy.getKey());
        }
    }

    private void loadEnemyData(JsonElement enemyEle, String enemyName) {
        PomkotsDataPack.EnemyData data = new PomkotsDataPack.EnemyData();

        data.id = enemyName;
        data.speed = getFloat(enemyEle, "speed");
        data.maxStepUp = getFloat(enemyEle, "max_step_up");
        data.knockBackResistance = getFloat(enemyEle, "knock_back_resistance");
        data.followRange = getFloat(enemyEle, "follow_range");
        data.health = getInt(enemyEle, "health");
        data.baseDamageModifier = getFloat(enemyEle, "base_damage_modifier");
        data.explosionDamageModifier = getFloat(enemyEle, "explosion_damage_modifier");
        data.armor = getFloat(enemyEle, "armor");
        data.armorToughness = getFloat(enemyEle, "armor_toughness");
        data.bulletDamage = getFloat(enemyEle, "bullet_damage");
        data.bulletSpeed = getFloat(enemyEle, "bullet_speed");
        data.missileDamage = getFloat(enemyEle, "missile_damage");
        data.missileSpeed = getFloat(enemyEle, "missile_speed");
        data.grenadeDamage = getFloat(enemyEle, "grenade_damage");
        data.grenadeSpeed = getFloat(enemyEle, "grenade_speed");
        data.grenadeExplosionScale = getFloat(enemyEle, "grenade_explosion_scale");
        data.meleeDamage = getFloat(enemyEle, "melee_damage");
        data.meleeSpeed = getFloat(enemyEle, "melee_speed");
        data.laserDamage = getFloat(enemyEle, "laser_damage");
        data.laserSpeed = getFloat(enemyEle, "laser_speed");
        data.exAttack1Damage = getFloat(enemyEle, "ex_attack_1_damage");
        data.exAttack1Speed = getFloat(enemyEle, "ex_attack_1_speed");
        data.exAttack2Damage = getFloat(enemyEle, "ex_attack_2_damage");
        data.exAttack2Speed = getFloat(enemyEle, "ex_attack_2_speed");
        data.exAttack3Damage = getFloat(enemyEle, "ex_attack_3_damage");
        data.exAttack3Speed = getFloat(enemyEle, "ex_attack_3_speed");

        dataPackServer.addEnemyData(enemyName, data);
    }

    private int getInt(JsonElement parent, String name) {
        if (parent == null) {
            return 0;
        } else {
            var el = parent.getAsJsonObject().get(name);
            if (el == null) {
                return 0;
            }
            return el.getAsInt();
        }
    }

    private float getFloat(JsonElement parent, String name) {
        if (parent == null) {
            return 0F;
        } else {
            var el = parent.getAsJsonObject().get(name);
            if (el == null) {
                return 0F;
            }
            return el.getAsFloat();
        }
    }

    private String getString(JsonElement parent, String name) {
        if (parent == null) {
            return "";
        } else {
            var el = parent.getAsJsonObject().get(name);
            if (el == null) {
                return "";
            }
            return el.getAsString();
        }
    }

    private void loadAllRaidData(ResourceManager manager) {
        ResourceLocation path = new ResourceLocation(PomkotsMechs.MODID, "raid.json");

        loadResources(path, manager, resource -> {
            PomkotsMechs.LOGGER.info("Resource Pack:" + resource.sourcePackId() + ":" + resource);
            try (InputStream stream = resource.open()) {
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                Map<String, RaidDefinition> allRd = GSON.fromJson(reader, new TypeToken<Map<String, RaidDefinition>>(){}.getType());

                for (var entry: allRd.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        RaidDefinition rd = entry.getValue();

                        if (rd.waves != null) {
                            for (WaveDefinition wd: rd.waves) {
                                if (wd.events != null) {
                                    for (EventDefinition ed: wd.events) {
                                        if ("time".equals(ed.trigger_type)) {
                                            if (wd.timeline == null) {
                                                wd.timeline = new HashMap<Integer, EventDefinition>();
                                            }
                                            wd.timeline.put(ed.trigger_tick, ed);
                                        }
                                    }
                                }
                            }

                        }

                        dataPackServer.addRaidData(entry.getKey(), rd);
                    }
                }
            } catch (IOException e) {
                PomkotsMechs.LOGGER.error("Failed to load a raid pack:" + resource, e);
            }
        });
    }

    private void loadAllChestData(ResourceManager manager) {
        ResourceLocation path = new ResourceLocation(PomkotsMechs.MODID, "chest.json");

        loadResources(path, manager, resource -> {
            try (InputStream stream = resource.open()) {
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                Map<String, PomkotsDataPack.ChestData> allRd = GSON.fromJson(reader, new TypeToken<Map<String, PomkotsDataPack.ChestData>>(){}.getType());

                for (var entry: allRd.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        dataPackServer.addChestData(entry.getKey(), entry.getValue());
                    }
                }
            } catch (IOException e) {
                PomkotsMechs.LOGGER.error("Failed to load a raid pack:" + resource, e);
            }
        });
    }

    private void loadAllTraderData(ResourceManager manager) {
        ResourceLocation path = new ResourceLocation(PomkotsMechs.MODID, "trader_pool.json");

        loadResources(path, manager, resource -> {
            try (InputStream stream = resource.open()) {
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                Map<String, List<PomkotsDataPack.TraderPoolItem>> allRd = GSON.fromJson(reader, new TypeToken<Map<String, List<PomkotsDataPack.TraderPoolItem>>>(){}.getType());

                for (var entry: allRd.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        dataPackServer.setTraderPool(entry.getValue());
                    }
                }
            } catch (IOException e) {
                PomkotsMechs.LOGGER.error("Failed to load a trader pack:" + resource, e);
            }
        });
    }

    private void loadAllFighterData(ResourceManager manager) {
        ResourceLocation path = new ResourceLocation(PomkotsMechs.MODID, "arena_fighter_pool.json");

        loadResources(path, manager, resource -> {
            try (InputStream stream = resource.open()) {
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                Map<String, List<PomkotsDataPack.FighterJson>> allRd = GSON.fromJson(reader, new TypeToken<Map<String, List<PomkotsDataPack.FighterJson>>>(){}.getType());

                for (var entry: allRd.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        dataPackServer.setFighterPool(entry.getValue());
                    }
                }
            } catch (IOException e) {
                PomkotsMechs.LOGGER.error("Failed to load a trader pack:" + resource, e);
            }
        });
    }


    private void loadAllAssetsData(ResourceManager manager) {
        ResourceLocation path = new ResourceLocation(PomkotsMechs.MODID, "assets.json");

        loadResources(path, manager, resource -> {
            try (InputStream stream = resource.open()) {
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                Map<String, List<PomkotsDataPack.AssetDefinition>> all = GSON.fromJson(reader, new TypeToken<Map<String, List<PomkotsDataPack.AssetDefinition>>>(){}.getType());

                for (var entry: all.entrySet()) {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        dataPackServer.setAssetDefinitions(entry.getValue());
                    }
                }
            } catch (IOException e) {
                PomkotsMechs.LOGGER.error("Failed to load a trader pack:" + resource, e);
            }
        });
    }

    private void loadAllSkillData(ResourceManager manager) {
        ResourceLocation path = new ResourceLocation(PomkotsMechs.MODID, "circuit_skills.json");

        loadResources(path, manager, resource -> {
            List<CircuitSkill> skills =
                    new ArrayList<>();

            try (InputStream stream = resource.open()) {
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

                JsonObject root =
                        GsonHelper.fromJson(
                                GSON,
                                reader,
                                JsonObject.class
                        );

                if (root == null) {
                    throw new IllegalArgumentException(
                            "Root JSON object is null."
                    );
                }

                JsonArray array =
                        GsonHelper.getAsJsonArray(
                                root,
                                "skills"
                        );

                for (JsonElement element : array) {
                    if (!element.isJsonObject()) {
                        throw new IllegalArgumentException(
                                "Skill entry must be object in file: circuit_skills.json"
                        );
                    }

                    CircuitSkill skill =
                            CircuitSkillJsonParser.parse(
                                    element.getAsJsonObject()
                            );

                    skills.add(skill);
                }

            } catch (IOException e) {
                PomkotsMechs.LOGGER.error("Failed to load a trader pack:" + resource, e);
            }

            applySkills(skills);
        });
    }

    private void applySkills(
            List<CircuitSkill> skills
    ) {
        CircuitRegistries.SKILLS.unfreezeForReload();
        CircuitRegistries.SKILLS.clearForReload();

        int loaded = 0;
        int failed = 0;

        for (CircuitSkill skill : skills) {
            try {
                CircuitRegistries.SKILLS.register(skill);
                loaded++;

            } catch (Exception e) {
                failed++;

                PomkotsMechs.LOGGER.error(
                        "Failed to register circuit skill '{}'",
                        skill.id(),
                        e
                );
            }
        }

        PomkotsMechs.LOGGER.info(
                "Loaded {} circuit skills. Failed registrations: {}",
                loaded,
                failed
        );
    }

    public void serializeServerData(OutputStream outputStream) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);

        objectOutputStream.writeObject(dataPackServer);
        objectOutputStream.flush();

        objectOutputStream.close();
    }

    public PomkotsDataPack deSerialize(InputStream inputStream) throws IOException, ClassNotFoundException {

        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);

        dataPackClient = (PomkotsDataPack) objectInputStream.readObject();

        objectInputStream.close();

        return dataPackClient;
    }
}
