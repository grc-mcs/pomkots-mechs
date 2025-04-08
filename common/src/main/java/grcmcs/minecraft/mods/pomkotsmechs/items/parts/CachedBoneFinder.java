package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class CachedBoneFinder {
    private BakedGeoModel model;
    private Map<String, GeoBone> cache = new WeakHashMap<>();

    public CachedBoneFinder() {
    }

    public void setModel(BakedGeoModel model) {
        if (this.model != model) {
            cache.clear();
            this.model = model;
        }
    }

    public GeoBone getBone(String name) {
        var bone = cache.get(name);

        if (bone == null) {
            var rawBone = model.getBone(name);
            if (rawBone != null) {
                bone = rawBone.get();
                cache.put(name, bone);
            }
        }

        return bone;
    }
}
