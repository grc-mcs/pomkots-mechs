package grcmcs.minecraft.mods.pomkotsmechs.entity.villager;

import com.google.common.collect.ImmutableSet;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.level.block.Blocks;

import java.util.Set;

public class PomkotsProfessions {
//    public static final DeferredRegister<PoiType> POIS = DeferredRegister.create(PomkotsMechs.MODID, Registries.POINT_OF_INTEREST_TYPE);
//    public static final RegistrySupplier<PoiType> BARTENDER_POI = POIS.register(
//            "bartender",
//            () -> new PoiType(
//                    Set.copyOf(Blocks.IRON_BLOCK.getStateDefinition().getPossibleStates()), 1, 1
//            )
//    );
//
//    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(PomkotsMechs.MODID, Registries.VILLAGER_PROFESSION);
//    public static final RegistrySupplier<VillagerProfession> BARTENDER_PROFESSION = PROFESSIONS.register(
//            "bartender2",
//            () -> new VillagerProfession(
//                    PomkotsMechs.MODID + "bartender2",
//                    holder -> holder.is(BARTENDER_POI.getKey()),
//                    holder -> holder.is(BARTENDER_POI.getKey()),
//                    ImmutableSet.of(),  // Gatherable items
//                    ImmutableSet.of(),  // Secondary job site
//                    SoundEvents.VILLAGER_WORK_LIBRARIAN // 任意の職業音
//            )
//    );
//
//    public static void init() {
//        POIS.register();
//        PROFESSIONS.register();
//
//    }
}
