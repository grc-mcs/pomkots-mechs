package grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid;

import java.io.Serializable;
import java.util.Map;

public class SpawnTarget implements Serializable {
    public String mob_type;  // e.g. "minecraft:zombie"
    public String position;  // "x,y,z" （後で BlockPos に変換してもよい）
    public String snbt; // SNBTのやつ
    public Map<String, String> tags; // 任意のNBTライクなタグ

    @Override
    public String toString() {
        return "SpawnTarget{" +
                "mob_type='" + mob_type + '\'' +
                ", position='" + position + '\'' +
                ", snbt=" + snbt +
                ", tags=" + tags +
                '}';
    }
}
