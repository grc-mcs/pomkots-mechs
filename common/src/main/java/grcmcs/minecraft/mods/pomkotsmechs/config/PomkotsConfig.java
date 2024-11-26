package grcmcs.minecraft.mods.pomkotsmechs.config;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = PomkotsMechs.MODID)
public class PomkotsConfig implements ConfigData {
    public boolean enableEntityBlockDestruction = false;
    public boolean enableHudHealthBar = true;
}
