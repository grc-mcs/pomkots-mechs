package grcmcs.minecraft.mods.pomkotsmechs.radio;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public record RadioText(Component text, int durationTicks) {
    public static final int DEFAULT_DURATION = 80;
    public static final int MIN_DURATION = 20;
    public static final int MAX_DURATION = 1200;

    public void write(FriendlyByteBuf buf) {
        buf.writeComponent(text);
        buf.writeVarInt(durationTicks);
    }

    public static RadioText read(FriendlyByteBuf buf) {
        return new RadioText(buf.readComponent(), buf.readVarInt());
    }
}
