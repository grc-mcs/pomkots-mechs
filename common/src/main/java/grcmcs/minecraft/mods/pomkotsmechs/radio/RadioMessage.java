package grcmcs.minecraft.mods.pomkotsmechs.radio;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record RadioMessage(
        ResourceLocation id,
        Component speaker,
        RadioPortrait portrait,
        List<RadioText> texts
) {
    public static final int MAX_TEXTS = 64;

    public RadioMessage {
        texts = List.copyOf(texts);
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeComponent(speaker);
        portrait.write(buf);
        buf.writeCollection(texts, (target, text) -> text.write(target));
    }

    public static RadioMessage read(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        Component speaker = buf.readComponent();
        RadioPortrait portrait = RadioPortrait.read(buf);
        List<RadioText> texts = buf.readCollection(ArrayList::new, RadioText::read);
        if (texts.isEmpty() || texts.size() > MAX_TEXTS) {
            throw new IllegalArgumentException("Invalid radio text count: " + texts.size());
        }
        return new RadioMessage(id, speaker, portrait, texts);
    }
}
