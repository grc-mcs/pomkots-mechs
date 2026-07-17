package grcmcs.minecraft.mods.pomkotsmechs.tags;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class ModEntityTags {

    public static final TagKey<EntityType<?>> SCAN_TARGETS =
            TagKey.create(
                    Registries.ENTITY_TYPE,
                    new ResourceLocation(
                            PomkotsMechs.MODID,
                            "scan_targets"
                    )
            );

    private ModEntityTags() {
    }
}
