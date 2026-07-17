package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats;

import java.util.EnumSet;
import java.util.Set;

public final class MechFeatures {

    private final EnumSet<MechFeatureType> enabledFeatures =
            EnumSet.noneOf(MechFeatureType.class);

    public boolean isEnabled(MechFeatureType feature) {
        return enabledFeatures.contains(feature);
    }

    public void setEnabled(
            MechFeatureType feature,
            boolean enabled
    ) {
        if (enabled) {
            enabledFeatures.add(feature);
        } else {
            enabledFeatures.remove(feature);
        }
    }

    public void enable(MechFeatureType feature) {
        enabledFeatures.add(feature);
    }

    public void disable(MechFeatureType feature) {
        enabledFeatures.remove(feature);
    }

    public Set<MechFeatureType> values() {
        return Set.copyOf(enabledFeatures);
    }

    public MechFeatures copy() {
        MechFeatures copy = new MechFeatures();
        copy.enabledFeatures.addAll(this.enabledFeatures);
        return copy;
    }
}