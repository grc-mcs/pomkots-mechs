package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

/**
 * Named Mech pilot with fixed Karan model resources.
 *
 * Pilot AI, equipment roles, licenses and vehicle behavior are inherited from
 * MechPilotEntity.
 */
public class KaranEntity extends MechPilotEntity {
    public KaranEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public String getModelLocation() {
        return "pomkotsmechs:geo/karan.geo.json";
    }

    @Override
    public void setModelLocation(String location) {
        // Karan always uses the dedicated model.
    }

    @Override
    public String getTextureLocation() {
        return "pomkotsmechs:textures/entity/pilot/karan.png";
    }

    @Override
    public void setTextureLocation(String location) {
        // Karan always uses the dedicated texture.
    }
}
