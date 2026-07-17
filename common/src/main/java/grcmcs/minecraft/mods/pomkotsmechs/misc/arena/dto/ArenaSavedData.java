package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core.ArenaManager;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class ArenaSavedData extends SavedData {
    public static final String DATA_NAME = "pomkotsmechs_mech_arena";

    private final Map<String, ArenaInstance>
            arenas = new HashMap<>();

    public static ArenaSavedData get(MinecraftServer server) {
        ServerLevel level = server.overworld();

        return level.getDataStorage().computeIfAbsent(
                ArenaSavedData::load,
                ArenaSavedData::create,
                DATA_NAME
        );
    }

    private static ArenaSavedData create() {
        var data = new ArenaSavedData();

        data.setDirty();

        return data;
    }

    public Map<String, ArenaInstance> getArenas() {
        return arenas;
    }

    public ArenaInstance getArena(
            String arenaId
    ) {
        return arenas.get(arenaId);
    }

    public ArenaInstance getOrCreateArena(
            String arenaId
    ) {
        return arenas.computeIfAbsent(
                arenaId,
                id -> {
                    ArenaInstance arena = createArena(arenaId, null);
                    setDirty();

                    return arena;
                }
        );
    }

    private static ArenaInstance createArena(String arenaId, GlobalPos controllerPos) {
        ArenaInstance arena = new ArenaInstance();

        arena.setArenaId(arenaId);
        arena.setDisplayName(arenaId);

        for (var npc: PomkotsDataPackManager.getInstance().getDataPack().getFighterPoolItems()) {
            ArenaFighterData fighter = new ArenaFighterData();
            UUID fighterId = UUID.nameUUIDFromBytes(
                    ("arena:" + npc.name).getBytes(StandardCharsets.UTF_8)
            );

            fighter.setFighterId(fighterId);
            fighter.setAiLevel(npc.ai_level);
            fighter.setDisplayName(npc.name);
            fighter.setComment(npc.comment);
            fighter.setRating(npc.initial_rate);
            fighter.setWins(npc.win);
            fighter.setLosses(npc.lose);
            fighter.setTexture(new ResourceLocation(npc.texture));
            fighter.setModel(new ResourceLocation(npc.model));
            fighter.setType(ArenaFighterData.FighterType.NPC);

            ArenaMechData mech = new ArenaMechData();
            mech.setMechName(npc.mech.name);
            mech.setTextureColor(npc.mech.texture_color);

            for (var part: npc.mech.parts) {
                var item = BuiltInRegistries.ITEM.get(new ResourceLocation(part.item));

                if (item instanceof BasePartsItem bitem) {
                    var stack = new ItemStack(item);

                    if (part.level != 0) {
                        if (part.level <= bitem.getMaxLevel()) {
                            bitem.setLevel(stack, part.level);
                        } else {
                            bitem.setLevel(stack, bitem.getMaxLevel());
                        }
                    } else {
                        bitem.setLevel(stack, 1);
                    }

                    mech.getParts().set(part.slot, stack);
                }
            }

            fighter.setMechData(mech);

            arena.getFighters()
                    .put(
                            fighterId,
                            fighter
                    );

        }

        return arena;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag) {
        ListTag arenaList =
                new ListTag();

        for (ArenaInstance arena : arenas.values()) {
            arenaList.add(
                arena.save()
            );
        }

        tag.put(
                "Arenas",
                arenaList
        );

        return tag;
    }

    public static ArenaSavedData load(CompoundTag tag) {
        ArenaSavedData data = new ArenaSavedData();

        ListTag arenaList =
                tag.getList(
                        "Arenas",
                        Tag.TAG_COMPOUND
                );

        for (Tag element :
                arenaList) {

            ArenaInstance arena =
                    ArenaInstance.load(
                            (CompoundTag) element
                    );

            data.arenas.put(
                    arena.getArenaId(),
                    arena
            );
        }

        return data;
    }
}