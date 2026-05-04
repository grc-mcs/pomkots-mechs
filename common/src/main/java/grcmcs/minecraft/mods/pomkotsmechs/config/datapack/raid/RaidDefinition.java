package grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid;

import java.io.Serializable;
import java.util.List;

public class RaidDefinition implements Serializable {
    public String type;
    public String display_name;
    public String advancement;
    public List<WaveDefinition> waves;

    @Override
    public String toString() {
        return "RaidDefinition{" +
                "display_name='" + display_name + '\'' +
                ", waves=" + waves +
                '}';
    }
}