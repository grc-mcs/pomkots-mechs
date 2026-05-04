package grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaveDefinition implements Serializable {
    public String title;
    public String sub_title;
    public int duration_ticks;
    public List<EventDefinition> events;
    public HashMap<Integer, EventDefinition> timeline;
    public boolean kill_entities_when_wave_cleared = true;

    @Override
    public String toString() {
        return "WaveDefinition{" +
                "title='" + title + '\'' +
                ", sub_title='" + sub_title + '\'' +
                ", duration_ticks=" + duration_ticks +
                ", timeline=" + timeline +
                '}';
    }
}
