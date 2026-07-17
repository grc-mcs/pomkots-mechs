package grcmcs.minecraft.mods.pomkotsmechs.items.circuits;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.*;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.data.CircuitSkillSnapshot;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.generator.CircuitGenerator;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry.CircuitRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class CircuitItemStackHelper {

    private static final String TAG_CIRCUIT = "Circuit";

    private static final String TAG_IDENTIFIED = "Identified";
    private static final String TAG_PREFIX = "Prefix";
    private static final String TAG_RARITY = "Rarity";
    private static final String TAG_SKILLS = "Skills";

    private CircuitItemStackHelper() {
    }

    public static boolean hasCircuitTag(ItemStack stack) {
        return stack.hasTag()
                && stack.getTag() != null
                && stack.getTag().contains(TAG_CIRCUIT, Tag.TAG_COMPOUND);
    }

    public static boolean isIdentified(ItemStack stack) {
        CompoundTag circuitTag =
                getCircuitTag(stack, false);

        if (circuitTag == null) {
            return false;
        }

        return circuitTag.getBoolean(TAG_IDENTIFIED)
                && circuitTag.contains(TAG_SKILLS, Tag.TAG_LIST);
    }

    public static void setUnidentified(
            ItemStack stack,
            CircuitPrefix prefix,
            CircuitRarity rarity
    ) {
        CompoundTag circuitTag =
                getCircuitTag(stack, true);

        circuitTag.putBoolean(TAG_IDENTIFIED, false);
        circuitTag.putString(TAG_PREFIX, prefix.id().toString());
        circuitTag.putString(TAG_RARITY, rarity.serializedName());
        circuitTag.remove(TAG_SKILLS);
    }

    public static void setIdentified(
            ItemStack stack,
            CircuitInstance instance
    ) {
        CompoundTag circuitTag =
                getCircuitTag(stack, true);

        circuitTag.putBoolean(TAG_IDENTIFIED, true);
        circuitTag.putString(TAG_PREFIX, instance.prefix().id().toString());
        circuitTag.putString(TAG_RARITY, instance.rarity().serializedName());

        ListTag skillsTag =
                new ListTag();

        for (CircuitModifier modifier : instance.modifiers()) {
            CircuitSkillSnapshot snapshot =
                    CircuitSkillSnapshot.fromSkill(
                            modifier.skill()
                    );

            skillsTag.add(snapshot.save());
        }

        circuitTag.put(TAG_SKILLS, skillsTag);
    }

    /**
     * 未鑑定なら、Prefix + Rarity からSkillを生成し、
     * Tooltip用Snapshot込みでNBTへ焼き込む。
     *
     * サーバー側から呼ぶ想定。
     */
    public static void ensureGenerated(
            ItemStack stack,
            RandomSource random
    ) {
        if (isIdentified(stack)) {
            return;
        }

        CompoundTag circuitTag =
                getCircuitTag(stack, true);

        CircuitPrefix prefix =
                readPrefix(circuitTag);

        CircuitRarity rarity =
                readRarity(circuitTag);

        CircuitInstance generated =
                CircuitGenerator.generate(
                        random,
                        prefix,
                        rarity
                );

        setIdentified(
                stack,
                generated
        );
    }

    /**
     * 能力計算用。
     *
     * サーバー側ではSkill IDからRegistryを引く。
     * クライアント側やRegistry未同期環境ではnull/空になる可能性があるので、
     * Tooltipには使わない。
     */
    public static CircuitInstance getCircuit(ItemStack stack) {
        if (!isIdentified(stack)) {
            return null;
        }

        CompoundTag circuitTag =
                getCircuitTag(stack, false);

        if (circuitTag == null) {
            return null;
        }

        CircuitPrefix prefix =
                readPrefix(circuitTag);

        CircuitRarity rarity =
                readRarity(circuitTag);

        List<CircuitModifier> modifiers =
                new ArrayList<>();

        ListTag skillsTag =
                circuitTag.getList(TAG_SKILLS, Tag.TAG_COMPOUND);

        for (int i = 0; i < skillsTag.size(); i++) {
            CompoundTag skillTag =
                    skillsTag.getCompound(i);

            ResourceLocation skillId =
                    ResourceLocation.tryParse(
                            skillTag.getString("Id")
                    );

            if (skillId == null) {
                continue;
            }

            CircuitSkill skill =
                    CircuitRegistries.SKILLS.get(skillId);

            if (skill == null) {
                continue;
            }

            modifiers.add(
                    new CircuitModifier(skill)
            );
        }

        return new CircuitInstance(
                prefix,
                rarity,
                List.copyOf(modifiers)
        );
    }

    /**
     * Tooltip用。
     *
     * Registryを見ず、NBTに焼き込まれたSnapshotだけ読む。
     */
    public static List<CircuitSkillSnapshot> getSkillSnapshots(
            ItemStack stack
    ) {
        if (!isIdentified(stack)) {
            return List.of();
        }

        CompoundTag circuitTag =
                getCircuitTag(stack, false);

        if (circuitTag == null) {
            return List.of();
        }

        ListTag skillsTag =
                circuitTag.getList(TAG_SKILLS, Tag.TAG_COMPOUND);

        List<CircuitSkillSnapshot> snapshots =
                new ArrayList<>();

        for (int i = 0; i < skillsTag.size(); i++) {
            CircuitSkillSnapshot snapshot =
                    CircuitSkillSnapshot.load(
                            skillsTag.getCompound(i)
                    );

            if (snapshot != null) {
                snapshots.add(snapshot);
            }
        }

        return List.copyOf(snapshots);
    }

    public static CircuitPrefix getPrefixOrDefault(
            ItemStack stack
    ) {
        CompoundTag circuitTag =
                getCircuitTag(stack, false);

        if (circuitTag == null) {
            return CircuitPrefix.BALANCED;
        }

        return readPrefix(circuitTag);
    }

    public static CircuitRarity getRarityOrDefault(
            ItemStack stack
    ) {
        CompoundTag circuitTag =
                getCircuitTag(stack, false);

        if (circuitTag == null) {
            return CircuitRarity.COMMON;
        }

        return readRarity(circuitTag);
    }

    private static CompoundTag getCircuitTag(
            ItemStack stack,
            boolean create
    ) {
        if (!create) {
            if (!stack.hasTag() || stack.getTag() == null) {
                return null;
            }

            CompoundTag root =
                    stack.getTag();

            if (!root.contains(TAG_CIRCUIT, Tag.TAG_COMPOUND)) {
                return null;
            }

            return root.getCompound(TAG_CIRCUIT);
        }

        CompoundTag root =
                stack.getOrCreateTag();

        if (!root.contains(TAG_CIRCUIT, Tag.TAG_COMPOUND)) {
            root.put(TAG_CIRCUIT, new CompoundTag());
        }

        return root.getCompound(TAG_CIRCUIT);
    }

    private static CircuitPrefix readPrefix(
            CompoundTag circuitTag
    ) {
        if (circuitTag.contains(TAG_PREFIX, Tag.TAG_STRING)) {
            ResourceLocation id =
                    ResourceLocation.tryParse(
                            circuitTag.getString(TAG_PREFIX)
                    );

            if (id != null) {
                CircuitPrefix prefix =
                        CircuitRegistries.PREFIXES.get(id);

                if (prefix != null) {
                    return prefix;
                }
            }
        }

        return CircuitPrefix.BALANCED;
    }

    private static CircuitRarity readRarity(
            CompoundTag circuitTag
    ) {
        if (circuitTag.contains(TAG_RARITY, Tag.TAG_STRING)) {
            return CircuitRarity.byName(
                    circuitTag.getString(TAG_RARITY),
                    CircuitRarity.COMMON
            );
        }

        return CircuitRarity.COMMON;
    }
}
