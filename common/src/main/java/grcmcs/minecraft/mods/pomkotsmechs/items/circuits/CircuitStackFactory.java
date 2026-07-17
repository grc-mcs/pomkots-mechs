package grcmcs.minecraft.mods.pomkotsmechs.items.circuits;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitInstance;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitPrefix;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.generator.CircuitGenerator;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry.CircuitRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public final class CircuitStackFactory {

    private CircuitStackFactory() {
    }

    public static ItemStack createUnidentified(
            CircuitPrefix prefix,
            CircuitRarity rarity
    ) {
        ItemStack stack =
                new ItemStack(PomkotsMechs.CIRCUIT_BASE.get());

        CircuitItemStackHelper.setUnidentified(
                stack,
                prefix,
                rarity
        );

        return stack;
    }

    public static ItemStack createIdentified(
            RandomSource random,
            CircuitPrefix prefix,
            CircuitRarity rarity
    ) {
        ItemStack stack =
                new ItemStack(PomkotsMechs.CIRCUIT_BASE.get());

        CircuitInstance instance =
                CircuitGenerator.generate(
                        random,
                        prefix,
                        rarity
                );

        CircuitItemStackHelper.setIdentified(
                stack,
                instance
        );

        return stack;
    }

    public static ItemStack createCreativeIdentifiedSample(
            CircuitPrefix prefix,
            CircuitRarity rarity,
            long seed
    ) {
        RandomSource random =
                RandomSource.create(seed);

        return createIdentified(
                random,
                prefix,
                rarity
        );
    }

    public static ItemStack createCreativeIdentifiedSampleSafe(
            CircuitPrefix prefix,
            CircuitRarity rarity,
            long seed
    ) {
        if (CircuitRegistries.SKILLS.values().isEmpty()) {
            return createUnidentified(
                    prefix,
                    rarity
            );
        }

        return createCreativeIdentifiedSample(
                prefix,
                rarity,
                seed
        );
    }

    public static ItemStack createCreativeUnidentifiedSample(
            CircuitPrefix prefix,
            CircuitRarity rarity
    ) {
        return createUnidentified(
                prefix,
                rarity
        );
    }

    public static void addAll(
            CreativeModeTab.Output output
    ) {
        for (CircuitPrefix prefix : CircuitPrefix.creativeOrder()) {
            for (CircuitRarity rarity : CircuitRarity.creativeOrder()) {

                ItemStack stack =
                        CircuitStackFactory.createCreativeUnidentifiedSample(
                                prefix,
                                rarity
                        );

                output.accept(stack);
            }
        }
    }
}
