package grcmcs.minecraft.mods.pomkotsmechs.items.circuits;

import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitPrefix;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitRarity;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.data.CircuitSkillSnapshot;
import net.minecraft.world.item.Item;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CircuitItem extends Item {

    public CircuitItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(
            ItemStack stack,
            Level level,
            Entity entity,
            int slotId,
            boolean isSelected
    ) {
        super.inventoryTick(
                stack,
                level,
                entity,
                slotId,
                isSelected
        );

        if (level.isClientSide) {
            return;
        }

        CircuitItemStackHelper.ensureGenerated(
                stack,
                level.getRandom()
        );
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        boolean identified =
                CircuitItemStackHelper.isIdentified(stack);

        if (!identified) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.pomkotsmechs.circuit.unidentified"
                    ).withStyle(ChatFormatting.GRAY)
            );
        }

        CircuitPrefix prefix =
                CircuitItemStackHelper.getPrefixOrDefault(stack);

        CircuitRarity rarity =
                CircuitItemStackHelper.getRarityOrDefault(stack);

        appendCircuitHeader(
                tooltip,
                prefix,
                rarity
        );

        if (!identified) {
            return;
        }

        tooltip.add(Component.empty());

        List<CircuitSkillSnapshot> snapshots =
                CircuitItemStackHelper.getSkillSnapshots(stack);

        for (CircuitSkillSnapshot snapshot : snapshots) {
            CircuitTooltipBuilder.appendSkillSnapshot(
                    tooltip,
                    snapshot
            );
        }
    }

    private static void appendCircuitHeader(
            List<Component> tooltip,
            CircuitPrefix prefix,
            CircuitRarity rarity
    ) {
        tooltip.add(
                Component.translatable(
                        "tooltip.pomkotsmechs.circuit.type",
                        prefix.name()
                ).withStyle(ChatFormatting.AQUA)
        );

        tooltip.add(
                Component.translatable(
                        "tooltip.pomkotsmechs.circuit.rarity",
                        rarity.displayName()
                ).withStyle(rarity.chatFormatting())
        );
    }
}
