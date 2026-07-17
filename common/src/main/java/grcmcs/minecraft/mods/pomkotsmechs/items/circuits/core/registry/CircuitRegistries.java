package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public final class CircuitRegistries {

    public static final CircuitSkillRegistry SKILLS =
            new CircuitSkillRegistry();

    public static final CircuitPrefixRegistry PREFIXES =
            new CircuitPrefixRegistry();

    private CircuitRegistries() {
    }

    public static void freezeStaticRegistries() {
        PREFIXES.freeze();

        /*
         * SKILLSはデータパックリロードで差し替えるので、
         * ここではfreezeしない。
         */
    }
}