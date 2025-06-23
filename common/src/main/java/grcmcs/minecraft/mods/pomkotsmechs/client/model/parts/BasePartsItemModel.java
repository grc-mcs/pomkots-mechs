package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasePartsItemModel<T extends BasePartsItem> extends GeoModel<T> {
    public static List<String> BASE_COLORS = List.of("gray", "darkgray", "white", "red", "green", "lightgreen","blue", "orange");
    public static Map<String, Integer> COLOR_INDEX_MAP;

    static {
        COLOR_INDEX_MAP = new HashMap<>();
        for (int i = 0; i < BASE_COLORS.size(); i++) {
            COLOR_INDEX_MAP.put(BASE_COLORS.get(i), i);
        }
    }

    public static int getColorIndex(String colorName) {
        return COLOR_INDEX_MAP.get(colorName);
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return new ResourceLocation(PomkotsMechs.MODID, "animations/weapon.animation.json");
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        if (animatable.getParentEntity() != null) {
            String color = BASE_COLORS.get(animatable.getParentEntity().getTextureColor());

            if (color != null) {
                return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/parts/"+ color + ".png");
            } else {
                return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/parts/gray.png");
            }
        } else {
            return new ResourceLocation(PomkotsMechs.MODID, "textures/entity/parts/" + animatable.getDefaultColor() + ".png");
        }
    }
}
