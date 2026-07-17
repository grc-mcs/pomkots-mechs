package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Locale;

public enum CircuitRarity implements StringRepresentable {

    COMMON(
            "common",
            1,
            1,
            ChatFormatting.WHITE,
            0xFFFFFF
    ),

    RARE(
            "rare",
            2,
            2,
            ChatFormatting.BLUE,
            0x4A8DFF
    ),

    EPIC(
            "epic",
            3,
            3,
            ChatFormatting.LIGHT_PURPLE,
            0xC05CFF
    );

    private final String name;
    private final int maxModifierCount;
    private final int maxSkillTier;
    private final ChatFormatting chatFormatting;
    private final int tintColor;

    CircuitRarity(
            String name,
            int maxModifierCount,
            int maxSkillTier,
            ChatFormatting chatFormatting,
            int tintColor
    ) {
        this.name = name;
        this.maxModifierCount = maxModifierCount;
        this.maxSkillTier = maxSkillTier;
        this.chatFormatting = chatFormatting;
        this.tintColor = tintColor;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String serializedName() {
        return name;
    }

    public int maxModifierCount() {
        return maxModifierCount;
    }

    public int maxSkillTier() {
        return maxSkillTier;
    }

    public ChatFormatting chatFormatting() {
        return chatFormatting;
    }

    public Component displayName() {
        return Component.translatable(
                "tooltip.pomkotsmechs.circuit.rarity." + name().toLowerCase()
        );
    }

    public int rollModifierCount(RandomSource random) {
        if (maxModifierCount <= 1) {
            return 1;
        }

        /*
         * 例:
         * Rareなら 1〜2個
         * Epicなら 1〜3個
         *
         * 必ず最大数にしたいなら return maxModifierCount;
         */
        return 1 + random.nextInt(maxModifierCount);
    }

    public static CircuitRarity byName(
            String name,
            CircuitRarity fallback
    ) {
        String normalized = name.toLowerCase(Locale.ROOT);

        for (CircuitRarity rarity : values()) {
            if (rarity.name.equals(normalized)) {
                return rarity;
            }
        }

        return fallback;
    }

    public static List<CircuitRarity> creativeOrder() {
        return List.of(
                COMMON,
                RARE,
                EPIC
        );
    }

    public int tintColor() {
        return tintColor;
    }

    public CircuitRarity lower() {
        int index = this.ordinal();

        if (index <= 0) {
            return this;
        }

        return values()[index - 1];
    }
}
