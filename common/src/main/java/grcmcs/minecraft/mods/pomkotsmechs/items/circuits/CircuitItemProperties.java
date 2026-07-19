package grcmcs.minecraft.mods.pomkotsmechs.items.circuits;

import dev.architectury.registry.item.ItemPropertiesRegistry;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class CircuitItemProperties {

    private static final String TAG_CIRCUIT = "Circuit";
    private static final String TAG_PREFIX = "Prefix";

    public static final ResourceLocation CIRCUIT_PREFIX_PROPERTY =
            new ResourceLocation(PomkotsMechs.MODID, "circuit_prefix");

    private CircuitItemProperties() {
    }

    public static void register() {
        registerPrefixProperty("circuit_offence", "pomkotsmechs:offence");
        registerPrefixProperty("circuit_defence", "pomkotsmechs:defence");
        registerPrefixProperty("circuit_energy", "pomkotsmechs:energy");
        registerPrefixProperty("circuit_mobility", "pomkotsmechs:mobility");
        registerPrefixProperty("circuit_utility", "pomkotsmechs:utility");
        registerPrefixProperty("circuit_balanced", "pomkotsmechs:balanced");
    }

    private static void registerPrefixProperty(
            String propertyName,
            String targetPrefix
    ) {
        ItemPropertiesRegistry.register(
                PomkotsMechs.CIRCUIT_BASE.get(),
                new ResourceLocation(PomkotsMechs.MODID, propertyName),
                (stack, level, entity, seed) ->
                        targetPrefix.equals(readPrefix(stack)) ? 1.0F : 0.0F
        );
    }

    private static String readPrefix(ItemStack stack) {
        if (!stack.hasTag() || stack.getTag() == null) {
            return "";
        }

        CompoundTag root = stack.getTag();

        if (!root.contains("Circuit", Tag.TAG_COMPOUND)) {
            return "";
        }

        CompoundTag circuitTag = root.getCompound("Circuit");

        if (!circuitTag.contains("Prefix", Tag.TAG_STRING)) {
            return "";
        }

        return circuitTag.getString("Prefix");
    }
}
