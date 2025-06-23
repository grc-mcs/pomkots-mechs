package grcmcs.minecraft.mods.pomkotsmechs.config.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.*;
import java.util.List;

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

        if (Platform.getEnvironment() == Env.CLIENT && !dataPackServer.isEmpty()) {
            dataPackClient = dataPackServer;
        }

        PomkotsMechs.LOGGER.info("DP:" + dataPackServer);
    }

    private void loadAllPartsData(ResourceManager manager) {
        ResourceLocation path = new ResourceLocation(PomkotsMechs.MODID, "parts.json");
        List<Resource> resources;

        try {
            resources = manager.getResourceStack(path);
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to load resource stack:" + path, e);
            return;
        }

        for (Resource resource : resources) {
            try (InputStream stream = resource.open()) {
                JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                if (json != null) {
                    loadRootPartsData(json);
                } else {
                    PomkotsMechs.LOGGER.error("No parts on a parts pack:" + resource);
                }
            } catch (IOException e) {
                PomkotsMechs.LOGGER.error("Failed to load a parts pack:" + resource, e);
            }
        }
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
                levelData.energyChargePerTick = getInt(levelParamsObj, "energy_charge_per_tick");
                levelData.workSecPerFuel = getInt(levelParamsObj, "work_sec_per_fuel");
                levelData.energyConsumeEvasion = getInt(levelParamsObj, "energy_consume_evasion");
                levelData.energyConsumeVertical = getInt(levelParamsObj, "energy_consume_vertical");
                levelData.speedModifierEvasion = getFloat(levelParamsObj, "speed_modifier_evasion");
                levelData.speedModifierVertical = getFloat(levelParamsObj, "speed_modifier_vertical");
                levelData.bulletsPerMagazine = getInt(levelParamsObj, "bullets_per_magazine");
                levelData.energy = getInt(levelParamsObj, "energy");

                data.levels.add(levelData);
            }
        }

        dataPackServer.addPartsData(itemName, data);
    }


    private void loadAllEnemyData(ResourceManager manager) {
        ResourceLocation path = new ResourceLocation(PomkotsMechs.MODID, "enemies.json");
        List<Resource> resources;

        try {
            resources = manager.getResourceStack(path);
        } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to load resource stack:" + path, e);
            return;
        }

        for (Resource resource : resources) {
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
        }
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
