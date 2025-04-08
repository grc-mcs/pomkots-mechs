package grcmcs.minecraft.mods.pomkotsmechs.config.datapack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PomkotsDataPack implements Serializable {

    private final Map<String, PartsData> partsData = new HashMap<>();

    public PartsData getPartsData(String partsName) {
        return partsData.get(partsName);
    }

    public void addPartsData(String partsName, PartsData data) {
        partsData.put(partsName, data);
    }

    public void reset() {
        partsData.clear();
    }

    @Override
    public String toString() {
        return partsData.toString();
    }

    public static class PartsData implements Serializable {
        // 共通
        public String id;
        public int weight;
        public String description;
        public List<LevelData> levels = new ArrayList<>();

        @Override
        public String toString() {
            return "{" + id + "," + weight + "," + levels.toString();
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

        @Override
        public String toString() {
            return "[" + durability + "," + maxWeight + "," + speedModifier + "," + jumpModifier + "," + damage + "," + missileMaxNum + "," + missileLockInterval + "]";
        }
    }
}
