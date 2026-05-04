package grcmcs.minecraft.mods.pomkotsmechs.config.datapack;

import com.mojang.datafixers.util.Pair;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.RaidDefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PomkotsDataPack implements Serializable {

    private final Map<String, PartsData> partsData = new HashMap<>();
    private final Map<String, EnemyData> enemyData = new HashMap<>();
    private final Map<String, RaidDefinition> raidData = new HashMap<>();
    private final Map<String, ChestData> chestData = new HashMap<>();

    public PartsData getPartsData(String partsName) {
        return partsData.get(partsName);
    }

    public void addPartsData(String partsName, PartsData data) {
        partsData.put(partsName, data);
    }

    public void reset() {
        partsData.clear();
        enemyData.clear();
        raidData.clear();
        chestData.clear();
    }

    public boolean isEmpty() {
        return partsData.isEmpty();
    }

    @Override
    public String toString() {
        return "{" + partsData.toString() + "," + enemyData.toString() + "," + raidData.toString() + "}";
    }

    public static class PartsData implements Serializable {
        // 共通
        public String id;
        public int weight;
        public String description;
        public List<LevelData> levels = new ArrayList<>();
        public List<List<SerializablePair<String, Integer>>> recipes = new ArrayList<>();

        @Override
        public String toString() {
            return "{" + id + "," + weight + "," + levels.toString() + "," + recipes.toString() + "}";
        }
    }

    public static class SerializablePair<F, S> implements Serializable {
        private final F first;
        private final S second;

        public SerializablePair(final F first, final S second) {
            this.first = first;
            this.second = second;
        }

        public F getFirst() {
            return first;
        }

        public S getSecond() {
            return second;
        }

        @Override
        public String toString() {
            return "[" + first + "," + second + "]";
        }
    }

    public static class LevelData implements Serializable {
        // 本体パーツ共通
        public int durability;

        // 脚部のみ
        public int maxWeight;
        public float speedModifier;
        public float jumpModifier;

        // 武器パーツ共通
        public float damage;

        // ミサイルのみ
        public int missileMaxNum;
        public float missileLockInterval;

        // ジェネレーターのみ
        public int maxEnergy;
        public int energyChargePerTick;
        public int workSecPerFuel;

        // ブースターのみ
        public int energyConsumeEvasion;
        public int energyConsumeVertical;
        public float speedModifierEvasion;
        public float speedModifierVertical;

        public int bulletsPerMagazine;

        // 電池のみ
        public int energy;

        // エナジー消費系
        public int energyConsumePerTick;

        @Override
        public String toString() {
            return "[" + durability + "," + maxWeight + "," + speedModifier + "," + jumpModifier + "," + damage + "," + missileMaxNum + "," + missileLockInterval + "," + energyConsumePerTick + "]";
        }
    }

    public EnemyData getEnemyData(String enemyName) {
        return enemyData.get(enemyName);
    }

    public void addEnemyData(String enemyName, EnemyData data) {
        enemyData.put(enemyName, data);
    }

    public static class EnemyData implements Serializable {
        // 共通
        public String id;
        public float followRange;
        public float speed;
        public float maxStepUp;
        public float knockBackResistance;
        public float health;
        public float baseDamageModifier;
        public float explosionDamageModifier;
        public float armor;
        public float armorToughness;
        public float bulletDamage;
        public float bulletSpeed;
        public float missileDamage;
        public float missileSpeed;
        public float grenadeDamage;
        public float grenadeSpeed;
        public float grenadeExplosionScale;
        public float meleeDamage;
        public float meleeSpeed;
        public float laserDamage;
        public float laserSpeed;
        public float exAttack1Damage;
        public float exAttack1Speed;
        public float exAttack2Damage;
        public float exAttack2Speed;
        public float exAttack3Damage;
        public float exAttack3Speed;

        @Override
        public String toString() {
            return "{" +
                    id + "," + health + "," + armor
                    +"}";
        }
    }

    public static class ChestData implements Serializable {
        // 共通
        public String key = "";
        public String type = "";
        public String raid_id;
    }

    public static class ChestMobSpawnData implements Serializable {
        public String type = "";
    }

    public RaidDefinition getRaidData(String raidName) {
        return raidData.get(raidName);
    }

    public void addRaidData(String raidName, RaidDefinition data) {
        raidData.put(raidName, data);
    }

    public ChestData getChestData(String chestLootTable) {
        return chestData.get(chestLootTable);
    }

    public void addChestData(String chestLootTable, ChestData data) {
        chestData.put(chestLootTable, data);
    }
}
