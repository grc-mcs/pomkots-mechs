package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01bEntity;
import net.minecraft.world.level.Level;

public class CoreStonePMV01BItem extends CoreStonePMV01Item {
    public CoreStonePMV01BItem(Properties properties) {
        super(properties);
    }

    @Override
    protected Pmv01Entity createInstance(Level world) {
        return new Pmv01bEntity(PomkotsMechs.PMV01B.get(), world);
    }
}
